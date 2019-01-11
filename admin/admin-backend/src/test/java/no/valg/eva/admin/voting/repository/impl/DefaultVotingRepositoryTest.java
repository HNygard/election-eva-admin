package no.valg.eva.admin.voting.repository.impl;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.application.VotingMapper;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.PersistenceException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static no.valg.eva.admin.common.AreaPath.OSLO_COUNTY_ID;
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
import static no.valg.eva.admin.common.voting.VotingPhase.LATE;
import static no.valg.eva.admin.voting.repository.impl.DefaultVotingRepository.idForVotingCategory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = TestGroups.REPOSITORY)
public class DefaultVotingRepositoryTest extends AbstractJpaTestBase {
    private static final String OSLO = AreaPath.OSLO_MUNICIPALITY_ID;
    private static final String STHANSHAUGEN = "030104";
    private static final String ILA_SKOLE = "0404";
    private static final String MV_AREA_POLLING_PLACE_ILA_SKOLE = "200701.47.03.0301.030104.0404.0404";
    private static final ValgdistriktSti VALGDISTRIKT_STI_STHANSHAUGEN = new ValgdistriktSti("200701", "01", "03", "030104");
    private static final ValggruppeSti VALGGRUPPE_STI = new ValggruppeSti("200701", "01");
    private final String POLLING_PLACE_AREA_PATH_HALDEN = "200701.47.01.0101.010100.0000.0001";
    private VotingRepository votingRepository;
    private VoterRepository voterRepository = null;
    private MvElectionRepository mvElectionRepository = null;
    private MvAreaRepository mvAreaRepository = null;
    private VotingRejectionRepository votingRejectionRepository = null;
    private Voter voter = null;
    private Voting voting = null;
    private ElectionEvent electionEvent;
    private BaseTestFixture testFixture;
    private Long osloMunicipalityPk;
    private Long haldenMunicipalityPk;
    private Long hvalerMunicipalityPk;
    private Long fredrikstadMunicipalityPk;
    private Long bkfElectionGroupPk;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        BackendContainer backend = new BackendContainer(getEntityManager());
        backend.initServices();

        votingRepository = backend.getVotingRepository();
        voterRepository = backend.getVoterRepository();
        mvElectionRepository = backend.getMvElectionRepository();
        mvAreaRepository = backend.getMvAreaRepository();
        votingRejectionRepository = backend.getVotingRejectionRepository();

        ElectionEventRepository electionEventRepository = backend.getElectionEventRepository();
        electionEvent = electionEventRepository.findById("200701");

        haldenMunicipalityPk = getMunicipalityFromPath("200701.47.01.0101.010100");
        hvalerMunicipalityPk = getMunicipalityFromPath("200701.47.01.0111.011100");
        fredrikstadMunicipalityPk = getMunicipalityFromPath("200701.47.01.0106.010600");
        osloMunicipalityPk = getMunicipalityFromPath("200701.47.03.0301");
        bkfElectionGroupPk = getElectionGroupFromPath();

        testFixture = new BaseTestFixture(backend.getUserDataService(), backend.getAccessRepository());
        testFixture.init();
    }

    @Test
    public void testCountUnapprovedAdvanceVotings() {
        buildAndPersistVoter();

        buildAndPersistVotingToConfirm();
        buildAndPersistVotingToConfirm();

        long approvedVotings = votingRepository.countUnapprovedAdvanceVotings(AreaPath.from(MV_AREA_POLLING_PLACE_ILA_SKOLE));

        assertThat(approvedVotings).isEqualTo(2);
    }

    private void buildAndPersistVoter() {
        voter = buildVoter();
        voterRepository.update(testFixture.getUserData(), voter);
    }

    private Voting buildAndPersistVotingToConfirm() {
        Voting voting = buildVoting(MV_AREA_POLLING_PLACE_ILA_SKOLE, VALGDISTRIKT_STI_STHANSHAUGEN, false, null);
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertNull(voting.getValidationTimestamp());

        return voting;
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@voting.markOff.noElectronicVotes")
    public void testCreateVoting_givenNotElectronicMarkOffs_verifiesException() {
        buildAndPersistVoter();
        Voting voting = buildVoting();
        voting.getVotingCategory().setId(VO.getId());
        voting.setMvArea(mvAreaRepository.findSingleByPath(POLLING_PLACE_AREA_PATH_HALDEN));
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        voting.getMvArea().getMunicipality().setElectronicMarkoffs(false);
        
        votingRepository.create(testFixture.getUserData(), voting);
    }

    @Test
    public void testCreateVoting_givenNoCastTimeStamp_verifiesAddedCastTimeStamp() {
        buildAndPersistVoter();
        Voting voting = new Voting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(MV_AREA_POLLING_PLACE_ILA_SKOLE, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        voting.setElectionGroup(mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup());
        voting.setVoter(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0));
        voting.setMvArea(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0).getMvArea());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VO.getId()));
        voting.setPhase(ELECTION_DAY);

        assertNull(voting.getCastTimestamp());

        LocalDateTime now = LocalDateTime.now();
        Voting votingResult = votingRepository.create(testFixture.getUserData(), voting);
        
        assertNotNull(votingResult.getCastTimestamp());
        
        Duration castTimeDiffFromNow = Duration.between(votingResult.getCastTimeStampAsJavaTime(), now);
        assertTrue(castTimeDiffFromNow.getSeconds() < 5, "Casttime diff from now should have been less than 5 seconds - actual: " + castTimeDiffFromNow.getSeconds() + " seconds. ");
    }

    @Test
    public void testUpdateVoting() {
        buildAndPersistVoter();
        Voting voting = buildAndPersistVotingToConfirm();
        
        assertFalse(voting.isApproved());
        
        voting.setApproved(true);

        Voting updatedVoting = votingRepository.update(testFixture.getUserData(), voting);
        assertTrue(updatedVoting.isApproved());
    }

    @Test
    public void testMarkOffVoterFI() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterFU() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FU.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterFB() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterFA() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterVS() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VS.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterVB() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0)
                .getPollingPlace());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VB.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffVoterVF() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VF.getId()));
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertNotNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()));
        assertNull(votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber());
        assertEquals(voting.getMvArea().getPk(), voter.getMvArea().getPk());
    }

    @Test
    public void testMarkOffDuplicateApproved() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        Voting votingApproved = buildVoting();
        try {
            votingRepository.create(testFixture.getUserData(), votingApproved);
            fail();
        } catch (PersistenceException e) {
        }
    }

    @Test
    public void testGetVotingsByElectionGroupAndVoter() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertEquals(1, votingRepository.getVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size());
        assertEquals(0, votingRepository.getVotingsByElectionGroupAndVoter(1111111111L, voting.getElectionGroup().getPk()).size());
    }

    @Test
    public void testGetVotingsByElectionGroupVoterAndMunicipality() {
        buildAndPersistVoter();

        Voting votingHalden = buildVoting();
        votingHalden.setPollingPlace(mvAreaRepository.findByPathAndLevel(POLLING_PLACE_AREA_PATH_HALDEN,
                AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        votingHalden.setVotingCategory(votingRepository.findVotingCategoryById(VS.getId()));
        votingHalden.setApproved(false);
        votingHalden.setValidationTimestamp(null);
        votingHalden = votingRepository.create(testFixture.getUserData(), votingHalden);

        Voting votingOslo = buildVoting();
        votingOslo.setPollingPlace(mvAreaRepository.findByPathAndLevel("200701.47.03.0301.030104.0404.0404",
                AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        votingOslo.setVotingCategory(votingRepository.findVotingCategoryById(VS.getId()));
        votingOslo.setApproved(false);
        votingOslo.setValidationTimestamp(null);
        votingOslo = votingRepository.create(testFixture.getUserData(), votingOslo);

        assertEquals(
                votingRepository.getVotingsByElectionGroupVoterAndMunicipality(voter.getPk(), votingHalden.getElectionGroup().getPk(), haldenMunicipalityPk)
                        .size(),
                1);
        assertEquals(
                votingRepository.getVotingsByElectionGroupVoterAndMunicipality(voter.getPk(), votingHalden.getElectionGroup().getPk(), haldenMunicipalityPk)
                        .get(0).getPk(),
                votingHalden.getPk());

        votingRepository.delete(testFixture.getUserData(), votingOslo.getPk());
        votingRepository.delete(testFixture.getUserData(), votingHalden.getPk());
    }

    @Test
    public void testGetReceivedVotingsByElectionGroupAndVoter1() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertEquals(1, votingRepository.getReceivedVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size());
        assertEquals(0, votingRepository.getReceivedVotingsByElectionGroupAndVoter(1111111111L, voting.getElectionGroup().getPk()).size());
    }

    @Test
    public void testGetReceivedVotingsByElectionGroupAndVoter2() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
        voting.setApproved(false);
        voting.setValidationTimestamp(null);
        voting.setReceivedTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertEquals(0, votingRepository.getReceivedVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size());
    }

    @Test
    public void testGetReceivedVotingsByElectionGroupAndVoter3() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting.setReceivedTimestamp(null);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        assertEquals(0, votingRepository.getReceivedVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size());
    }

    @Test
    public void testGetApprovedVotingsForVoterByElectionGroup() {
        buildAndPersistVoter();
        voting = buildVoting();
        voting = votingRepository.create(testFixture.getUserData(), voting);

        List<Voting> approvedVotings = votingRepository.getApprovedVotingsForVoterByElectionGroup(testFixture.getUserData(), voting.getVoter().getPk(), mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup().getPk());
        assertEquals(1, approvedVotings.size());

        votingRepository.delete(testFixture.getUserData(), voting.getPk());
        voting = buildVoting();
        voting.setApproved(false);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        approvedVotings = votingRepository.getApprovedVotingsForVoterByElectionGroup(testFixture.getUserData(), voting.getVoter().getPk(), mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup().getPk());
        assertEquals(0, approvedVotings.size());
    }

    @Test(dataProvider = "getElectionDayVotingsWithMarkoffsTestData")
    public void testGetElectionDayVotingsWithMarkoffs(boolean isManualContest, int expectedNumberOfVotings) {
        PollingDistrict pollingDistrict = pollingDistrict(POLLING_PLACE_AREA_PATH_HALDEN);
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = votingCategoryById(FI);

        List<Object[]> electionDayVotingsWithMarkoffs = votingRepository.getElectionDaysWithMarkoffs(false, pollingDistrict, votingCategory);
        assertEquals(electionDayVotingsWithMarkoffs.size(), expectedNumberOfVotings);
    }

    @DataProvider
    public Object[][] getElectionDayVotingsWithMarkoffsTestData() {
        return new Object[][]{
                {true, 2},
                {false, 2}
        };
    }

    private PollingDistrict pollingDistrict(String path) {
        return mvAreaRepository.findByPathAndLevel(path, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingDistrict();
    }

    private no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategoryById(VotingCategory votingCategory) {
        return votingRepository.findVotingCategoryById(votingCategory.getId());
    }

    @Test
    public void testGetRejectedVotingsByElectionGroupAndMunicipality() {
        int i = votingRepository.getRejectedVotingsByElectionGroupAndMunicipality(OSLO, mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup().getPk()).size();

        buildAndPersistVoter();
        voting = buildVoting();
        voting.setVotingRejection(votingRejectionRepository.findByPk(1L));
        voting = votingRepository.create(testFixture.getUserData(), voting);

        int j = votingRepository.getRejectedVotingsByElectionGroupAndMunicipality(OSLO,
                mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup().getPk()).size();

        assertEquals(++i, j);
    }

    @Test
    public void testFindVotingByVotingNumber() {
        buildAndPersistVoter();

        voting = buildVoting();
        voting.setValidationTimestamp(null);
        voting.setApproved(false);
        voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
        voting.setPhase(ADVANCE);
        voting = votingRepository.create(testFixture.getUserData(), voting);

        Voting v = votingRepository.findVotingByVotingNumber(voter.getMvArea().getMunicipality().getPk(),
                mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI_STHANSHAUGEN).getElectionGroup().getPk(),
                votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber(),
                votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingCategory().isEarlyVoting());

        assertNotNull(v);
        assertEquals(v.getVotingNumber().intValue(), votingRepository.findByPk(testFixture.getUserData(), voting.getPk()).getVotingNumber().intValue());

        v = votingRepository.findVotingByVotingNumber(99L, 99L, 99, true);
        assertNull(v);
    }

    @Test
    public void testGetVoterFromVoting() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, ADVANCE);

        Voter voterFromBase = voting.getVoter();

        assertTrue(voterFromBase != null && voterFromBase.getPk().equals(voter.getPk()));
    }

    @Test
    public void updateApproveAdvanceFEVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FE.getId(), false, ADVANCE);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int updates = votingRepository.updateAdvanceVotingsApproved(0L, osloMunicipalityPk, bkfElectionGroupPk, startDate, endDate, 0, 0);

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
    }

    @Test
    public void updateApproveAdvanceAdvanceVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, ADVANCE);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int updates = votingRepository.updateAdvanceVotingsApproved(0L, osloMunicipalityPk, bkfElectionGroupPk, startDate, endDate, 0, 0);

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
    }

    @Test
    public void updateApproveAdvanceElectionDayVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(VS.getId(), false, ELECTION_DAY);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int updates = votingRepository.updateAdvanceVotingsApproved(0L, osloMunicipalityPk, bkfElectionGroupPk, startDate, endDate, 0, 0);

        assertEquals(updates, 0);
    }

    @Test
    public void updateApproveAdvanceLateValidationVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, true, LATE);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int updates = votingRepository.updateAdvanceVotingsApproved(0L, osloMunicipalityPk, bkfElectionGroupPk, startDate, endDate, 0, 0);

        assertEquals(updates, 0);
    }

    @Test
    public void updateApproveElectionDayFEVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FE.getId(), false, ADVANCE);

        int updates = votingRepository.updateElectionDayVotingsApproved(osloMunicipalityPk, bkfElectionGroupPk, 0, 0, VS.getId(), VB.getId());

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
        assertThat(voting.getMvArea()).isEqualTo(voter.getMvArea());
    }

    @Test
    public void updateApproveElectionDayVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(VS.getId(), false, ELECTION_DAY);

        int updates = votingRepository.updateElectionDayVotingsApproved(osloMunicipalityPk, bkfElectionGroupPk, 0, 0, VS.getId(), VB.getId());

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
    }

    @Test
    public void updateApproveElectionDayAdvanceVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, LATE);

        int updates = votingRepository.updateElectionDayVotingsApproved(osloMunicipalityPk, bkfElectionGroupPk, 0, 0, VS.getId(), VB.getId());

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
    }

    @Test
    public void updateApproveElectionDayAdvanceLateValidationVote() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, true, LATE);

        int updates = votingRepository.updateElectionDayVotingsApproved(osloMunicipalityPk, bkfElectionGroupPk, 0, 0, VS.getId(), VB.getId());

        assertEquals(updates, 1);

        getEntityManager().refresh(voting);
        assertTrue(voting.isApproved());
    }

    @Test
    public void testFindVotingByVotingNumber_givenMunicipality_verifiesVoting() {
        buildAndUpdateVoter(true);
        buildAndCreateVoting(FI.getId(), false, false, ADVANCE);

        Voting votingByVotingNumber = votingRepository.findVotingByVotingNumber(no.valg.eva.admin.common.configuration.model.Municipality.builder().pk(osloMunicipalityPk).build(), VotingMapper.toDto(voting));
        assertEquals(votingByVotingNumber, voting);
        assertEquals(votingByVotingNumber.getVotingCategory().getId(), voting.getVotingCategory().getId());
        assertEquals(votingByVotingNumber.getPhase(), voting.getPhase());
    }

    @Test
    public void testDeleteVotingsForFictitiousVoter() {
        buildAndUpdateVoter(true, true);
        buildAndCreateVoting(FI.getId(), false, ADVANCE);

        assertEquals(votingRepository.getVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size(), 1,
                "There should be one voting.");

        assertEquals(voterRepository.findByElectionEventAndId(voter.getElectionEvent().getPk(), voter.getId()).size(), 1, "There should be one voter.");

        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(VALGGRUPPE_STI);
        votingRepository.deleteVotings(mvElection, voting.getMvArea(), null);

        assertEquals(votingRepository.getVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size(), 0,
                "Voting should have been deleted.");

        assertEquals(voterRepository.findByElectionEventAndId(voter.getElectionEvent().getPk(), voter.getId()).size(), 0,
                "Fictitious voter should have been deleted.");

        voter = voterRepository.findByPk(voter.getPk());
    }

    @Test
    public void testDeleteVotingsNonFictiveVoter() {
        buildAndUpdateVoter(true, false);
        buildAndCreateVoting(FI.getId(), false, ADVANCE);

        assertEquals(votingRepository.getVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size(), 1,
                "There should be one voting.");

        assertEquals(voterRepository.findByElectionEventAndId(voter.getElectionEvent().getPk(), voter.getId()).size(), 1, "There should be one voter.");

        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(VALGGRUPPE_STI);
        votingRepository.deleteVotings(mvElection, voting.getMvArea(), null);

        assertEquals(votingRepository.getVotingsByElectionGroupAndVoter(voting.getVoter().getPk(), voting.getElectionGroup().getPk()).size(), 0,
                "Voting should have been deleted.");

        assertEquals(voterRepository.findByElectionEventAndId(voter.getElectionEvent().getPk(), voter.getId()).size(), 1,
                "Non fictive voter should still exist.");
    }

    @Test
    public void hasApprovedVoting_whenAVoterHasApprovedVoting_returnsTrue() {
        buildAndUpdateVoter(true, false);
        buildAndCreateVoting(FI.getId(), true, ADVANCE);

        boolean hasVoted = votingRepository.hasApprovedVoting(voter);

        assertThat(hasVoted).isTrue();
    }

    @Test
    public void hasApprovedVoting_whenAVoterHasVotedButVotingIsNotApproved_returnsFalse() {
        buildAndUpdateVoter(true, false);
        buildAndCreateVoting(FI.getId(), false, ADVANCE);

        boolean hasVoted = votingRepository.hasApprovedVoting(voter);

        assertThat(hasVoted).isFalse();
    }

    @Test
    public void hasApprovedVoting_whenAVoterHasNotVoted_returnsFalse() {
        buildAndUpdateVoter(true, false);

        boolean hasVoted = votingRepository.hasApprovedVoting(voter);

        assertThat(hasVoted).isFalse();
    }

    private Voter buildAndUpdateVoter(boolean approved) {
        return buildAndUpdateVoter(approved, false);
    }

    private Voter buildAndUpdateVoter(boolean approved, boolean ficticious) {
        Voter voter = buildVoter(approved, ficticious);
        voter = voterRepository.update(testFixture.getUserData(), voter);
        return voter;
    }

    private Voter buildVoter(boolean approved, boolean ficticious) {
        Voter voter = buildVoter();
        voter.setApproved(approved);
        voter.setFictitious(ficticious);
        return voter;
    }
    
    private Voter buildVoter(){
        return buildVoter("200701.47.03.0301.030104.0404", OSLO_COUNTY_ID, OSLO);
    }

    private Voter buildVoter(String areaPath, String countyId, String municipalityId) {
        voter = new Voter();
        voter.setElectionEvent(electionEvent);
        voter.setId("08038554957");
        voter.setFirstName("Tor");
        voter.setLastName("Torsen");
        voter.setNameLine("Tor Torsen");
        voter.setCountryId("47");
        voter.setCountyId(countyId);
        voter.setMunicipalityId(municipalityId);
        voter.setBoroughId(STHANSHAUGEN);
        voter.setPollingDistrictId(ILA_SKOLE);
        voter.setEligible(true);
        voter.setDateTimeSubmitted(DateTime.now().toDate());
        voter.setAarsakskode("02");
        voter.setRegDato(LocalDate.now());
        voter.setSpesRegType('0');
        voter.setAddressLine1("Trettebakken");
        voter.setPostalCode("0755");
        voter.setApproved(true);
        voter.setMvArea(mvAreaRepository.findSingleByPath(areaPath));
        voter = voterRepository.create(testFixture.getUserData(), voter);
        return voter;
    }

    private void buildAndCreateVoting(String votingCategory, boolean approved, VotingPhase votingPhase) {
        buildAndCreateVoting(votingCategory, approved, false, votingPhase);
    }

    private void buildAndCreateVoting(String votingCategory, boolean approved, boolean lateValidation, VotingPhase votingPhase) {
        voting = buildVoting();
        voting.setVotingCategory(votingRepository.findVotingCategoryById(votingCategory));
        voting.setApproved(approved);
        voting.setValidationTimestamp(null);
        voting.setLateValidation(lateValidation);
        voting.setPhase(votingPhase);

        voting = votingRepository.create(testFixture.getUserData(), voting);
    }

    private Voting buildVoting() {
        return buildVoting(MV_AREA_POLLING_PLACE_ILA_SKOLE, VALGDISTRIKT_STI_STHANSHAUGEN, true, DateTime.now());
    }

    private Voting buildVoting(String pollingPlaceAreaPath, ValgdistriktSti electionPath, boolean isApproved, DateTime validatedTimeStamp) {
        Voting voting = new Voting();
        voting.setPollingPlace(mvAreaRepository.findByPathAndLevel(pollingPlaceAreaPath, AreaLevelEnum.POLLING_PLACE.getLevel()).get(0).getPollingPlace());
        voting.setElectionGroup(mvElectionRepository.finnEnkeltMedSti(electionPath).getElectionGroup());
        voting.setCastTimestamp(DateTime.now());
        voting.setReceivedTimestamp(DateTime.now());
        voting.setValidationTimestamp(validatedTimeStamp);
        voting.setVoter(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0));
        voting.setMvArea(voterRepository.findByElectionEventAndId(electionEvent.getPk(), voter.getId()).get(0).getMvArea());
        voting.setVotingCategory(votingRepository.findVotingCategoryById(VO.getId()));
        voting.setApproved(isApproved);
        voting.setPhase(ELECTION_DAY);
        return voting;
    }

    private Long getElectionGroupFromPath() {
        return mvElectionRepository.finnEnkeltMedSti(VALGGRUPPE_STI).getElectionGroup().getPk();
    }

    private Long getMunicipalityFromPath(final String path) {
        return mvAreaRepository.findSingleByPath(path).getMunicipality().getPk();
    }

    @Test
    public void slettStemmegivningerGittI_forEnValggeografiSti_fjernerStemmegivningtest() {
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(AreaPath.from("200701.47.01.0101.010100.0000"));
        assertThat(votingRepository.slettStemmegivningerFraVelgereTilhoerendeI(valggeografiSti)).isEqualTo(495);
    }

    @Test
    public void findVotingsToConfirm_givenNonValidatedVotingList_returnVotingList() {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(fredrikstadMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> votingsToConfirm = votingRepository.findVotingsToConfirm(municipality, electionGroup);

        int expected = 11; //Dev node: Summen av kategoriene i votingsToConfirmForCategories
        assertThat(votingsToConfirm.size()).isEqualTo(expected);
    }

    @Test(dataProvider = "approvedVotingsForMunicipalityForCategories")
    public void findApprovedVotingsForMunicipality_givenMunicipalityAndElectionGroupAndVotingCategory_returnsVotigList(VotingCategory votingCategory,
                                                                                                                       VotingPhase votingPhase, int expected) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(fredrikstadMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> approvedVotingsForMunicipality = votingRepository.findApprovedVotingsForMunicipality(municipality, electionGroup, votingCategory,
                isLateValidation(votingPhase));

        assertThat(approvedVotingsForMunicipality.size()).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] approvedVotingsForMunicipalityForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 0},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.LATE, 0}
        };
    }

    @Test
    public void findApprovedVotingsForMunicipality_givenMunicipalityAndElectionGroup_returnsVotigList() {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(fredrikstadMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> approvedVotingsForMunicipality = votingRepository.findApprovedVotingsForMunicipality(municipality, electionGroup);

        int expected = 0;
        assertThat(approvedVotingsForMunicipality.size()).isEqualTo(expected);
    }

    @Test(dataProvider = "votingsToConfirmForCategories")
    public void findVotingsToConfirm_givenNonValidatedVotingList_returnVotingList(VotingCategory votingCategory, VotingPhase votingPhase, int expected) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(fredrikstadMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> votingsToConfirm = votingRepository.findVotingsToConfirm(municipality, electionGroup, votingCategory, isLateValidation(votingPhase));

        assertThat(votingsToConfirm.size()).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] votingsToConfirmForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 11},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.LATE, 0}
        };
    }

    @Test
    public void testIdForVotingCategoryIsNullSafe() {
        VotingCategory vc = VotingCategory.fromId("FI");
        assertEquals(idForVotingCategory(vc), "FI");
        assertNull(idForVotingCategory(null));
    }

    @DataProvider
    public Object[][] votingsToConfirmWithDeadForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 0},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.LATE, 0}
        };
    }

    @DataProvider
    public Object[][] votingsToConfirmWithMultipleVotingsForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 11},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.LATE, 0}
        };
    }

    @DataProvider
    public Object[][] votingsToConfirmWithNotInElectoralRollForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 0},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.LATE, 0}
        };
    }

    @Test(dataProvider = "approvedVotingsForCategories")
    public void countApprovedVotings_whenApprovedVotings_returnNumberOfVotings(VotingCategory votingCategory, VotingPhase votingPhase, int expected) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(haldenMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        long approvedVotings = votingRepository.countApprovedEnvelopeVotings(municipality, electionGroup, votingCategory, isLateValidation(votingPhase), null, null);

        assertThat(approvedVotings).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] approvedVotingsForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 1429},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 5},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 0},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.ELECTION_DAY, 100},
                {VotingCategory.FI, VotingPhase.LATE, 0}
        };
    }

    private boolean isLateValidation(VotingPhase votingPhase) {
        return VotingPhase.LATE == votingPhase;
    }

    @Test(dataProvider = "rejectedVotingsForCategories")
    public void countRejectedVotings_whenUnapporovedVotings_returnNumberOfVotings(VotingCategory votingCategory, VotingPhase votingPhase, int expected) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(hvalerMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        long rejectedVotings = votingRepository.countRejectedEnvelopeVotings(municipality, electionGroup, votingCategory, isLateValidation(votingPhase), null, null);

        assertThat(rejectedVotings).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] rejectedVotingsForCategories() {
        return new Object[][]{
                {VotingCategory.FI, ADVANCE, 0},
                {VotingCategory.FB, ADVANCE, 0},
                {VotingCategory.FU, ADVANCE, 0},
                {VotingCategory.FE, ADVANCE, 0},
                {VotingCategory.FA, ADVANCE, 0},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.VF, VotingPhase.ELECTION_DAY, 0},
                {VotingCategory.FI, VotingPhase.LATE, 0}
        };
    }

    @Test(dataProvider = "rejectedVotingsForCategories")
    public void findRejectedVotingsForMunicipality_givenMunicipalityAndElectionGroupAndVotingCategory_returnVotingList(VotingCategory votingCategory,
                                                                                                                       VotingPhase votingPhase, int expected) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(hvalerMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> rejectedVotingsForMunicipality = votingRepository.findRejectedVotingsForMunicipality(municipality, electionGroup, votingCategory,
                isLateValidation(votingPhase));

        assertThat(rejectedVotingsForMunicipality.size()).isEqualTo(expected);
    }

    @Test
    public void findRejectedVotingsForMunicipality_givenMunicipalityAndElectionGroup_returnVotingList() {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(hvalerMunicipalityPk);
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);

        List<Voting> rejectedVotingsForMunicipality = votingRepository.findRejectedVotingsForMunicipality(municipality, electionGroup);

        int expected = 0;
        assertThat(rejectedVotingsForMunicipality.size()).isEqualTo(expected);
    }


}

