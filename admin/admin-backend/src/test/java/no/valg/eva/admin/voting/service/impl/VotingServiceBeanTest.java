package no.valg.eva.admin.voting.service.impl;

import no.evote.constants.AreaLevelEnum;
import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.exception.EvoteException;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;
import org.assertj.core.api.Condition;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.valueOf;
import static no.valg.eva.admin.common.AreaPath.OSLO_COUNTY_ID;
import static no.valg.eva.admin.common.AreaPath.from;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.common.voting.VotingPhase.ADVANCE;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = TestGroups.REPOSITORY)
public class VotingServiceBeanTest extends AbstractJpaTestBase {
    private static final String HALDEN = "0101";
    private static final String OSLO = AreaPath.OSLO_MUNICIPALITY_ID;
    private static final String BOROUGH_ID_STHANSHAUGEN = "030104";
    private static final String POLLING_DISTRICTD_ID_ILA_SKOLE = "0404";
    private static final String MV_AREA_OSLO = "200701.47.03.0301";
    private static final String MV_AREA_POLLING_PLACE_ILA_SKOLE = "200701.47.03.0301.030104.0404.0404";
    private static final String POLLING_DISTRICT_PATH_RIS_SKOLE = "200701.47.03.0301.030107.0702";
    private static final String POLLING_DISTRICT_PATH_SLEMDAL_SKOLE = "200701.47.03.0301.030107.0703";
    private static final String MV_AREA_POLLING_PLACE_MUNICIPALITY = "200701.47.03.0301.030100.0000.0001";
    private static final ValgdistriktSti VALGDISTRIKT_STI_STHANSHAUGEN = new ValgdistriktSti("200701", "01", "03", "030104");
    private static final String[] VOTING_CATEGORIES = new String[]{FI.getId(), FU.getId(), FB.getId(), VS.getId()};
    private static final boolean VOTER_NOT_LOGGED_IN_IN_MUNICIPALITY = false;
    private static final boolean EXPECT_VOTING_WAS_APPROVED = true;
    private static final boolean EXPECT_VOTING_WAS_NOT_APPROVED = false;
    private static final Object NO_RECEIVED_TIMESTAMP = null;
    private static final Object NO_VALIDATION_TIMESTAMP = null;
    private static final boolean VOTER_LOGGED_IN_IN_MUNICIPALITY = true;

    private VotingServiceBean votingService;
    private VotingRepository votingRepository;
    private VoterRepository voterRepository = null;
    private MvElectionRepository mvElectionRepository = null;
    private MvAreaRepository mvAreaRepository = null;
    private Voter voter = null;
    private Voting voting = null;
    private ElectionEvent electionEvent;
    private BaseTestFixture testFixture;
    private Long haldenMunicipalityPk;
    private Long bkfElectionGroupPk;
    private ElectionGroup electionGroup;
    private PollingPlace pollingPlace;
    private MvArea ilaSkolePollingDistrictMvArea;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        BackendContainer backend = new BackendContainer(getEntityManager());
        backend.initServices();

        votingService = backend.getVotingService();
        votingRepository = backend.getVotingRepository();
        voterRepository = backend.getVoterRepository();
        ElectionGroupRepository electionGroupRepository = backend.getElectionGroupRepository();
        mvElectionRepository = backend.getMvElectionRepository();
        mvAreaRepository = backend.getMvAreaRepository();

        ElectionEventRepository electionEventRepository = backend.getElectionEventRepository();
        electionEvent = electionEventRepository.findById("200701");
        electionGroup = electionGroupRepository.findElectionGroupById(electionEvent.getPk(), "01");
        haldenMunicipalityPk = getMunicipalityFromPath("200701.47.01.0101.010100");
        bkfElectionGroupPk = electionGroup.getPk();

        testFixture = new BaseTestFixture(backend.getUserDataService(), backend.getAccessRepository());
        testFixture.init();
        pollingPlace = mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace();
        ilaSkolePollingDistrictMvArea = mvAreaRepository.findSingleByPath("200701.47.03.0301.030104.0404");
    }

    @Test
    public void testMarkOffVoterVO() {
        voter = buildVoter();
        voterRepository.update(testFixture.getUserData(), voter);
        voting = buildVoting();
        voting = votingService.markOffVoter(testFixture.getUserData(), voting.getPollingPlace(), voting.getElectionGroup(), voter, false, ELECTION_DAY);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertTrue(voting.getMvArea().getPk().equals(voter.getMvArea().getPk()));
    }

    @Test
    public void testFindVotingStatisticsFI() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertFalse(vsdList.isEmpty());
    }

    @Test
    public void testFindVotingStatisticsFU() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FU.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertFalse(vsdList.isEmpty());
    }

    @Test
    public void testFindVotingStatisticsFB() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertFalse(vsdList.isEmpty());
    }

    @Test
    public void testFindVotingStatisticsFA() {
        buildVoter();

        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertTrue(vsdList.isEmpty());
    }

    @Test
    public void testFindVotingStatisticsLateValidation() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(true);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertEquals(vsdList.size(), 1);

        vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(),
                LocalDate.now(), 1, 999999, false, VOTING_CATEGORIES, false);
        assertEquals(vsdList.size(), 0);
    }

    @Test
    public void testFindVotingStatisticsFIFUFBFA() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);
        long fiVoting = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FU.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);
        long fuVoting = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);
        long fbVoting = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);
        long faVoting = voting.getPk();

        List<VotingDto> vsdList = votingService.findVotingStatistics(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 1, 999999, true, VOTING_CATEGORIES, false);
        assertTrue(vsdList.size() == 3);

        votingRepository.delete(testFixture.getUserData(), fiVoting);
        votingRepository.delete(testFixture.getUserData(), fuVoting);
        votingRepository.delete(testFixture.getUserData(), fbVoting);
        votingRepository.delete(testFixture.getUserData(), faVoting);
    }

    @Test
    public void testFindAdvanceVotingPickListNotApproved() {
        buildVoter();
        voter.setApproved(false);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertTrue(pickListDtoList.isEmpty());

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertFalse(pickListDtoList.isEmpty());
    }

    @Test
    public void testFindAdvanceVotingPickListNotApprovedFB() {
        buildVoter();
        voter.setApproved(false);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertTrue(pickListDtoList.isEmpty());

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertFalse(pickListDtoList.isEmpty());
    }

    @Test
    public void testFindAdvanceVotingPickListDuplicate() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertTrue(pickListDtoList.isEmpty());

        long firstVotingDuplicate = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(false);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertFalse(pickListDtoList.isEmpty());

        votingRepository.delete(testFixture.getUserData(), firstVotingDuplicate);
    }

    @Test
    public void testFindAdvanceVotingPickListFEIncluded() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertTrue(pickListDtoList.isEmpty());

        long firstVotingDuplicate = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FE.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(false);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertFalse(pickListDtoList.isEmpty());

        votingRepository.delete(testFixture.getUserData(), firstVotingDuplicate);
    }

    @Test
    public void testFindAdvancePickListFENotInMunicipality() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId(OSLO_COUNTY_ID);
        voter.setMunicipalityId(OSLO);
        voter.setBoroughId(BOROUGH_ID_STHANSHAUGEN);
        voter.setPollingDistrictId(POLLING_DISTRICTD_ID_ILA_SKOLE);
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.03.0301.030104.0404"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FE.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setPhase(ADVANCE);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertEquals(pickListDtoList.size(), 1);
        assertEquals(pickListDtoList.get(0).getRejectionReason(), "@voting.approveVoting.suggestedRejection.notInElectoralRoll");
    }

    @Test
    public void testFindAdvanceVotingPickListNotReturnLateValidation() {
        buildVoter();
        voter.setApproved(false);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(true);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<PickListItem> pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertEquals(pickListDtoList.size(), 0);

        voting.setLateValidation(false);
        voting = votingRepository.update(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findAdvanceVotingPickList(0L, haldenMunicipalityPk, bkfElectionGroupPk,
                LocalDate.now(), LocalDate.now(), 0, 0);
        assertEquals(pickListDtoList.size(), 1);
    }

    @Test
    public void testFindElectionDatVotingPickListNotInMunicipality() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId(OSLO_COUNTY_ID);
        voter.setMunicipalityId(OSLO);
        voter.setBoroughId(BOROUGH_ID_STHANSHAUGEN);
        voter.setPollingDistrictId(POLLING_DISTRICTD_ID_ILA_SKOLE);
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.03.0301.030104.0404"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        List<PickListItem> pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk,
                0, 0,
                VS.getId(), VB.getId());
        assertEquals(pickListDtoList.size(), 0);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VS.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk, 0, 0,
                VS.getId(),
                VB.getId());
        assertEquals(pickListDtoList.size(), 1);
    }

    @Test
    public void testFindElectionDayVotingPickListFENotInMunicipality() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId(OSLO_COUNTY_ID);
        voter.setMunicipalityId(OSLO);
        voter.setBoroughId(BOROUGH_ID_STHANSHAUGEN);
        voter.setPollingDistrictId(POLLING_DISTRICTD_ID_ILA_SKOLE);
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.03.0301.030104.0404"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        List<PickListItem> pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk,
                0, 0,
                VS.getId(), VB.getId());
        assertEquals(pickListDtoList.size(), 0);

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FE.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk, 0, 0,
                VS.getId(),
                VB.getId());
        assertEquals(pickListDtoList.size(), 0);
    }

    @Test
    public void testFindElectionDayVotingPickListFEIncluded() {
        buildVoter();
        voter.setApproved(true);
        voter.setCountyId("01");
        voter.setMunicipalityId(HALDEN);
        voter.setBoroughId("010100");
        voter.setPollingDistrictId("0101");
        voter.setMvArea(mvAreaRepository.findSingleByPath("200701.47.01.0101.010100.0001"));
        voter = voterRepository.update(testFixture.getUserData(), voter);

        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<PickListItem> pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk,
                0, 0,
                VS.getId(), VB.getId());
        assertEquals(pickListDtoList.size(), 0);
        assertTrue(pickListDtoList.isEmpty());

        long firstVotingDuplicate = voting.getPk();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.01.0101.010100.0000.0001", AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FE.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(false);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        pickListDtoList = votingService.findElectionDayVotingPickList(haldenMunicipalityPk, bkfElectionGroupPk, 0, 0,
                VS.getId(),
                VB.getId());
        assertTrue(pickListDtoList.isEmpty());

        votingRepository.delete(testFixture.getUserData(), firstVotingDuplicate);
    }

    @DataProvider(name = "testMarkOffVoterAdvanceVoteInBallotBoxData")
    private static Object[][] testMarkOffVoterAdvanceVoteInBallotBoxData() {
        return new Object[][]{
                {VOTER_NOT_LOGGED_IN_IN_MUNICIPALITY, EXPECT_VOTING_WAS_NOT_APPROVED, NO_RECEIVED_TIMESTAMP, NO_VALIDATION_TIMESTAMP, FA},
                {VOTER_LOGGED_IN_IN_MUNICIPALITY, EXPECT_VOTING_WAS_APPROVED, now(), now(), FI}
        };
    }

    @Test(dataProvider = "testMarkOffVoterAdvanceVoteInBallotBoxData")
    public void testMarkOffVoterAdvanceVoteInBallotBox(
            boolean voterIsLoggedInMunicipality,
            boolean expectApproved,
            final DateTime expectedReceivedTimestamp,
            DateTime expectedValidationTimestamp,
            VotingCategory expectedVotingCategory) {
        Voter voter = buildVoter();
        Voting voting = votingService.markOffVoterAdvanceVoteInBallotBox(
                testFixture.getUserData(),
                pollingPlace,
                electionGroup, voter, voterIsLoggedInMunicipality, ADVANCE);

        assertThat(voting.isApproved()).isEqualTo(expectApproved);
        assertThat(voting.getCastTimestamp()).is(aboutSameTimeAs(now()));
        assertThat(voting.getReceivedTimestamp()).is(aboutSameTimeAs(expectedReceivedTimestamp));
        assertThat(voting.getValidationTimestamp()).is(aboutSameTimeAs(expectedValidationTimestamp));
        assertThat(voting.getVotingCategory().getId()).isEqualTo(expectedVotingCategory.getId());
        assertThat(voting.getElectionGroup()).isEqualTo(electionGroup);
        assertThat(voting.getPollingPlace()).isEqualTo(pollingPlace);
        assertThat(voting.getMvArea()).isEqualTo(voter.getMvArea());
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Missing MvArea when creating Voting")
    public void markOffVoterAdvanceVoteInBallotBox_withNoMvArea_throwsEvoteException() {
        Voter voter = buildVoter();
        voter.setMvArea(null);
        votingService.markOffVoterAdvanceVoteInBallotBox(
                testFixture.getUserData(),
                pollingPlace,
                electionGroup, voter, false, ADVANCE);
    }

    private Condition<DateTime> aboutSameTimeAs(final DateTime dateTime2) {
        return new Condition<DateTime>() {
            @Override
            public boolean matches(DateTime dateTime1) {
                return notJustOneDateIsNull(dateTime1, dateTime2)  && 
                        (dateTime2 == null || dateDiffInMillisLessThan(dateTime1, dateTime2));
            }

            private boolean notJustOneDateIsNull(DateTime dateTime1, DateTime dateTime2) {
                return !((dateTime1 == null && dateTime2 != null) || (dateTime1 != null && dateTime2 == null));
            }

            private boolean dateDiffInMillisLessThan(DateTime dateTime1, DateTime dateTime2) {
                return dateTime2.getMillis() - dateTime1.getMillis() < 5000;
            }
        };
    }

    @Test
    public void twoVotersCastAdvancedVotesThenMovesToDifferentPollingDistrictsBeforeVotingsAreApproved_votingsReflectNewPollingDistrictsAfterValidation() {
        class VotingTestData {
            private String voterId;
            private String polingDistrictPathWhenAdvanceVoteWasCast;
            private Long votingPk;
            private String movedToPollingDistrictPathAfterVoteWasCast;

            public VotingTestData(String voterId, String polingDistrictPathWhenAdvanceVoteWasCast, String movedToPollingDistrictPathAfterVoteWasCast) {
                this.voterId = voterId;
                this.polingDistrictPathWhenAdvanceVoteWasCast = polingDistrictPathWhenAdvanceVoteWasCast;
                this.movedToPollingDistrictPathAfterVoteWasCast = movedToPollingDistrictPathAfterVoteWasCast;
            }
        }
        List<VotingTestData> votingTestDatas = newArrayList(
                new VotingTestData("1234567890", POLLING_DISTRICTD_ID_ILA_SKOLE, POLLING_DISTRICT_PATH_RIS_SKOLE),
                new VotingTestData("08038554957", POLLING_DISTRICTD_ID_ILA_SKOLE, POLLING_DISTRICT_PATH_SLEMDAL_SKOLE));

        pollingPlace = mvAreaRepository.findSingleByPath(from(MV_AREA_POLLING_PLACE_MUNICIPALITY)).getPollingPlace();
        for (VotingTestData testData : votingTestDatas) {
            Voter voter = buildVoter(testData.voterId, testData.polingDistrictPathWhenAdvanceVoteWasCast, BOROUGH_ID_STHANSHAUGEN,
                    ilaSkolePollingDistrictMvArea);
            Voting voting = votingService.markOffVoterAdvance(
                    testFixture.getUserData(),
                    pollingPlace,
                    electionGroup,
                    voter,
                    true,
                    "FI",
                    null,
                    ADVANCE);
            testData.votingPk = voting.getPk();
            assertThat(voting.isApproved()).isFalse();
            MvArea votersMvAreaAtVotingTime = voter.getMvArea();
            assertThat(voting.getMvArea()).isEqualTo(votersMvAreaAtVotingTime);
            Long votingPk = voting.getPk();
            testData.votingPk = votingPk;
            assertThat(approvedVoting(votingPk)).isEqualTo(false);
            moveVoterToPollingDistrict(voter, mvAreaRepository.findSingleByPath(from(testData.movedToPollingDistrictPathAfterVoteWasCast)));
        }

        int numberOfVotingsApproved = votingService.updateAdvanceVotingsApproved(
                0L,
                getMunicipalityFromPath(MV_AREA_OSLO),
                electionGroup.getPk(), new LocalDate(), new LocalDate(), 0, 0);

        assertThat(numberOfVotingsApproved).isEqualTo(votingTestDatas.size());

        for (VotingTestData votingTestData : votingTestDatas) {
            assertThat(approvedVoting(votingTestData.votingPk)).isEqualTo(true);

            DateTime dateForVoting = validationDateForVoting(votingTestData.votingPk);
            DateTime expectedTimestamp = now();
            
            assertThat(dateForVoting).is(aboutSameTimeAs(expectedTimestamp));

            MvArea mvArea = mvAreaRepository.findSingleByPath(from(votingTestData.movedToPollingDistrictPathAfterVoteWasCast));
            assertThat(valueOf(mvAreaPkForVoting(votingTestData.votingPk))).isEqualTo(mvArea.getPk());
        }

    }

    @Test
    public void updateAdvanceVotingApproved_setsApprovedOfVoting() {
        Voting voting = new Voting();
        voting.setCastTimestamp(now());
        voting.setVoter(voterRepository.create(testFixture.getUserData(), buildVoter()));
        voting.setVotingCategory(votingRepository.findVotingCategoryById("FI"));
        voting.setElectionGroup(electionGroup);
        voting.setOperator(testFixture.getUserData().getOperator());
        voting.setPhase(ADVANCE);
        voting.setMvArea(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0).getMvArea());
        votingService.updateAdvanceVotingApproved(testFixture.getUserData(), voting);
        assertThat(voting.getValidationTimestamp()).is(aboutSameTimeAs(now()));
        assertThat(voting.isApproved()).isTrue();
    }

    @Test
    public void registerVoteCentrally_withValidInput_returnsVoting() {
        Voting voting = votingService.registerVoteCentrally(testFixture.getUserData(),
                electionGroup, voterRepository.create(testFixture.getUserData(), buildVoter()),
                votingRepository.findVotingCategoryById(VF.getId()).getId(), ilaSkolePollingDistrictMvArea, ELECTION_DAY);

        assertThat(voting).isNotNull();
        assertThat(voting.getCastTimestamp()).is(aboutSameTimeAs(now()));
        assertThat(voting.getReceivedTimestamp()).is(aboutSameTimeAs(now()));
        assertThat(voting.getVotingCategory().getId()).isEqualTo(VF.getId());
        assertThat(voting.isApproved()).isFalse();
        assertThat(voting.getPollingPlace().getId()).isEqualTo("0000");
    }

    private DateTime validationDateForVoting(Long votingPk) {
        return new DateTime(getEntityManager().createNativeQuery("select validation_timestamp from voting where voting_pk = " + votingPk).getSingleResult());
    }

    private int mvAreaPkForVoting(Long votingPk) {
        return (int) getEntityManager().createNativeQuery("select mv_area_pk from voting where voting_pk = " + votingPk).getSingleResult();
    }

    private boolean approvedVoting(Long votingPk) {
        return (Boolean) getEntityManager().createNativeQuery("select approved from voting where voting_pk = " + votingPk).getSingleResult();
    }

    private void moveVoterToPollingDistrict(Voter voter, MvArea pollingDistrictSlemdalSkole) {
        voter.setMvArea(pollingDistrictSlemdalSkole);
        voterRepository.update(testFixture.getUserData(), voter);
    }

    private Voter buildVoter() {
        return buildVoter("08038554957", POLLING_DISTRICTD_ID_ILA_SKOLE, BOROUGH_ID_STHANSHAUGEN, ilaSkolePollingDistrictMvArea);
    }

    private Voter buildVoter(String personId, String pollingDistrictId, String boroughId, MvArea mvArea) {
        voter = new Voter();
        voter.setElectionEvent(electionEvent);
        voter.setId(personId);
        voter.setFirstName("Tor");
        voter.setLastName("Torsen");
        voter.setNameLine("Tor Torsen");
        voter.setCountryId("47");
        voter.setCountyId(OSLO_COUNTY_ID);
        voter.setMunicipalityId(OSLO);
        voter.setBoroughId(boroughId);
        voter.setPollingDistrictId(pollingDistrictId);
        voter.setEligible(true);
        voter.setDateTimeSubmitted(now().toDate());
        voter.setAarsakskode("02");
        voter.setRegDato(LocalDate.now());
        voter.setSpesRegType('0');
        voter.setAddressLine1("Trettebakken");
        voter.setPostalCode("0755");
        voter.setApproved(true);
        voter.setMvArea(mvArea);
        voter = voterRepository.create(testFixture.getUserData(), voter);
        return voter;
    }

    private Voting buildVoting() {
        Voting voting = new Voting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(MV_AREA_POLLING_PLACE_ILA_SKOLE, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        voting.setElectionGroup(mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup());
        voting.setCastTimestamp(now());
        voting.setReceivedTimestamp(now());
        voting.setValidationTimestamp(now());
        voting.setVoter(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0));
        voting.setMvArea(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0).getMvArea());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VO.getId()));
        voting.setApproved(true);
        voting.setPhase(ELECTION_DAY);
        return voting;
    }

    private Long getMunicipalityFromPath(final String path) {
        return mvAreaRepository.findSingleByPath(path).getMunicipality().getPk();
    }
}

