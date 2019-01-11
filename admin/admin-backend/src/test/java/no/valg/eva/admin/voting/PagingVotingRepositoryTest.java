package no.valg.eva.admin.voting;

import no.evote.exception.EvoteException;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingRejection;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.PagingVotingRepository;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_ALREADY_VOTED_FF;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_ALREADY_VOTED_FI;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_DID_NOT_HAVE_OPPORTUNITY_TO_VOTE_VC;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_ELECTION_DAY_VA;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FH;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTING_ENVELOPE_OPENED_OR_ATTEMPTED_OPENED_FE;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTING_HAS_INCOMPLETE_VOTER_INFO_FB;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTING_NOT_DELIVERED_AT_CORRECT_TIME_FC;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTING_NOT_DELIVERED_TO_CORRECT_RECEIVER_FD;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTING_RECEIVED_TOO_LATE_FG;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test(groups = TestGroups.REPOSITORY)
public class PagingVotingRepositoryTest extends AbstractJpaTestBase {

    private static final ValggruppeSti VALGGRUPPE_STI = new ValggruppeSti("200701", "01");
    private MvElectionRepository mvElectionRepository = null;
    private MvAreaRepository mvAreaRepository = null;
    private Long osloMunicipalityPk;
    private Long haldenMunicipalityPk;
    private Long bkfElectionGroupPk;

    private PagingVotingRepository pagingVotingRepository;
    private VotingRepository votingRepository;
    private VotingRejectionRepository votingRejectionRepository;
    private BaseTestFixture testFixture;
    private static Map<VotingRejection, String> votingRejectionMap = new HashMap<>();
    private static final Map<VotingCategory, String> votingCategoryMap = new HashMap<>();

    @BeforeMethod
    public void setUp() {
        BackendContainer backend = new BackendContainer(getEntityManager());
        backend.initServices();

        mvAreaRepository = backend.getMvAreaRepository();
        mvElectionRepository = backend.getMvElectionRepository();

        haldenMunicipalityPk = getMunicipalityFromPath("200701.47.01.0101.010100");
        osloMunicipalityPk = getMunicipalityFromPath("200701.47.03.0301");
        bkfElectionGroupPk = getElectionGroupFromPath();

        testFixture = new BaseTestFixture(backend.getUserDataService(), backend.getAccessRepository());
        testFixture.init();

        votingRejectionRepository = backend.getVotingRejectionRepository();
        pagingVotingRepository = backend.getPagingVotingRepository();
        votingRepository = backend.getVotingRepository();

        loadVotingRejectionMap();
        loadVotingCategoryMap();
    }

    private void loadVotingRejectionMap() {
        votingRejectionMap.put(VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_ELECTION_DAY_VA, VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_ELECTION_DAY_VA.getId());
        votingRejectionMap.put(VOTING_ENVELOPE_OPENED_OR_ATTEMPTED_OPENED_FE, VOTING_ENVELOPE_OPENED_OR_ATTEMPTED_OPENED_FE.getId());
        votingRejectionMap.put(VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA, VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA.getId());
        votingRejectionMap.put(VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FH, VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FH.getId());
        votingRejectionMap.put(VOTING_NOT_DELIVERED_TO_CORRECT_RECEIVER_FD, VOTING_NOT_DELIVERED_TO_CORRECT_RECEIVER_FD.getId());
        votingRejectionMap.put(VOTER_DID_NOT_HAVE_OPPORTUNITY_TO_VOTE_VC, VOTER_DID_NOT_HAVE_OPPORTUNITY_TO_VOTE_VC.getId());
        votingRejectionMap.put(VotingRejection.VOTING_NOT_DELIVERED_AT_CORRECT_TIME_FC, VOTING_NOT_DELIVERED_AT_CORRECT_TIME_FC.getId());
        votingRejectionMap.put(VOTING_HAS_INCOMPLETE_VOTER_INFO_FB, VOTING_HAS_INCOMPLETE_VOTER_INFO_FB.getId());
        votingRejectionMap.put(VOTING_RECEIVED_TOO_LATE_FG, VOTING_RECEIVED_TOO_LATE_FG.getId());
        votingRejectionMap.put(VOTER_ALREADY_VOTED_FF, VOTER_ALREADY_VOTED_FF.getId());
        votingRejectionMap.put(VOTER_ALREADY_VOTED_FI, VOTER_ALREADY_VOTED_FI.getId());
    }

    private void loadVotingCategoryMap() {
        votingCategoryMap.put(FI, FI.getId());
        votingCategoryMap.put(VotingCategory.FB, FB.getId());
        votingCategoryMap.put(VotingCategory.FU, FU.getId());
        votingCategoryMap.put(VotingCategory.FE, FE.getId());
        votingCategoryMap.put(VotingCategory.FA, FA.getId());
        votingCategoryMap.put(VotingCategory.VS, VS.getId());
        votingCategoryMap.put(VotingCategory.VB, VB.getId());
        votingCategoryMap.put(VotingCategory.VO, VO.getId());
        votingCategoryMap.put(VotingCategory.VF, VF.getId());
    }

    private Long getMunicipalityFromPath(final String path) {
        return mvAreaRepository.findSingleByPath(path).getMunicipality().getPk();
    }

    private Long getElectionGroupFromPath() {
        return mvElectionRepository.finnEnkeltMedSti(VALGGRUPPE_STI).getElectionGroup().getPk();
    }

    @Test(dataProvider = "pagedVotings")
    public void testPagedVotings_givenVotingCategory_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotings() {
        return new Object[][]{
                {votingFilters(), votingSorting(), 0, 10, 10},
                {votingFilters(), votingSorting(), 10, 10, 10},
                {votingFilters(), votingSorting(), 20, 10, 10},
                {votingFilters(), votingSorting(), 30, 10, 10},
                {votingFilters(), votingSorting(), 40, 10, 10},
                {votingFilters(), votingSorting(), 50, 10, 10},
                {votingFilters(), votingSorting(), 60, 10, 0},
        };
    }

    private VotingSorting votingSorting() {
        return VotingSorting.builder().build();
    }

    @Test(dataProvider = "pagedVotingsNegativeOffset")
    public void testPagedVotings_givenNegativeLimit_verifiesException(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        try {
            pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
            fail();
        } catch (EvoteException e) {
            assertEquals(e.getMessage(), "Offset cannot be a negative number: [-1]");
        }
    }

    @DataProvider
    public Object[][] pagedVotingsNegativeOffset() {
        return new Object[][]{
                {votingFilters(), votingSorting(), -1, 10},
        };
    }

    private VotingFilters votingFilters() {
        return VotingFilters.builder().build();
    }

    @Test(dataProvider = "pagedVotingsExceededLimit")
    public void testPagedVotings_givenExceededLimit_verifiesException(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        try {
            pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
            fail();
        } catch (EvoteException e) {
            assertEquals(e.getMessage(), "Limit should not be bigger than max limit: 500, provided limit: 600");
        }
    }

    @DataProvider
    public Object[][] pagedVotingsExceededLimit() {
        return new Object[][]{
                {VotingFilters.builder().build(), votingSorting(), 0, 600},
        };
    }

    @Test(dataProvider = "pagedVotingsValidatedQuery")
    public void testPagedVotings_givenValidatedFlag_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotingsValidatedQuery() {
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(true)
                        .build(), votingSorting(), 0, 10, 1},
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(), votingSorting(), 0, 10, 10},
        };
    }

    @Test(dataProvider = "pagedVotingsStatusApprovedQuery")
    public void testPagedVotings_givenApprovedStatus_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                     int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);

        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotingsStatusApprovedQuery() {
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(true)
                        .votingConfirmationStatus(VotingConfirmationStatus.APPROVED)
                        .build(), votingSorting(), 0, 10, 1}
        };
    }

    @Test(dataProvider = "pagedVotingsStatusRejectedQuery")
    public void testPagedVotings_givenRejectedStatus_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                     int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(haldenMunicipalityPk);

        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotingsStatusRejectedQuery() {
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(true)
                        .votingConfirmationStatus(VotingConfirmationStatus.REJECTED)
                        .build(), votingSorting(), 0, 10, 3}
        };
    }

    @Test(dataProvider = "pagedVotingsStatusToBeConfirmedQuery")
    public void testPagedVotings_givenToBeConfirmedStatus_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                          int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);

        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotingsStatusToBeConfirmedQuery() {
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .votingConfirmationStatus(VotingConfirmationStatus.TO_BE_CONFIRMED)
                        .build(), votingSorting(), 0, 10, 10},
        };
    }

    @Test(dataProvider = "pagedVotingsStatusDateSpanQuery")
    public void testPagedVotings_givenDateSpan_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                               int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);

        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    private Municipality mockMunicipality(Long osloMunicipalityPk) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.getPk()).thenReturn(osloMunicipalityPk);
        return municipality;
    }

    @DataProvider
    public Object[][] pagedVotingsStatusDateSpanQuery() {
        final int year = 2007;
        final int month = 9;
        final int day = 5;
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .fromDate(localDateTime(year, month - 1, day))
                        .toDateIncluding(localDateTime(year, month, day))
                        .build(), votingSorting(), 0, 10, 10},
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .fromDate(null)
                        .toDateIncluding(null)
                        .build(), votingSorting(), 0, 10, 10},
        };
    }

    private java.time.LocalDateTime localDateTime(int year, int month, int day) {
        return java.time.LocalDateTime.now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day);
    }

    @Test(dataProvider = "pagedVotingsSearchQuery")
    public void testPagedVotings_givenSearchQueryString_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                        int offset, int limit, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedCount);
    }

    @DataProvider
    public Object[][] pagedVotingsSearchQuery() {
        return new Object[][]{
                {VotingFilters.builder()
                        .searchQuery("Erik")
                        .build(), votingSorting(), 0, 10, 2},
                {VotingFilters.builder()
                        .searchQuery("Torild")
                        .build(), votingSorting(), 0, 10, 1},
                {VotingFilters.builder()
                        .searchQuery("Yvonne")
                        .build(), votingSorting(), 0, 10, 0},
                {VotingFilters.builder()
                        .searchQuery("803836")
                        .build(), votingSorting(), 0, 10, 1},
        };
    }

    @Test(dataProvider = "pagedVotingsOrderByVoterNameLineQuery")
    public void testPagedVotings_givenOrderByNameLine_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                      int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVoter().getNameLine(), expectedVotings.get(i).getVoter().getNameLine());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsOrderByVoterNameLineQuery() {
        List<Voting> expectedVotings = new ArrayList<>();

        expectedVotings.add(voting(null, voter(null, "Andersen Jan Terje")));
        expectedVotings.add(voting(null, voter(null, "Andreassen Tone Irene")));
        expectedVotings.add(voting(null, voter(null, "Arneberg Fredrik")));

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(),
                        VotingSorting.builder()
                                .sortOrder("ASC")
                                .sortField("nameLine")
                                .build(), 0, 3, expectedVotings}
        };
    }

    @Test(dataProvider = "pagedVotingsOrderByVoterPersonIdQuery")
    public void testPagedVotings_givenOrderByPersonId_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                      int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVoter().getId(), expectedVotings.get(i).getVoter().getId());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsOrderByVoterPersonIdQuery() {
        List<Voting> expectedVotings = new ArrayList<>();

        expectedVotings.add(voting(null, voter("01045034565", null)));
        expectedVotings.add(voting(null, voter("01067242404", null)));
        expectedVotings.add(voting(null, voter("02057425183", null)));

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(),
                        VotingSorting.builder()
                                .sortOrder("ASC")
                                .sortField("personId")
                                .build(), 0, 3, expectedVotings},
        };
    }

    @Test(dataProvider = "pagedVotingsOrderByVotingNumberQuery")
    public void testPagedVotings_givenOrderByVotingNumber_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                          int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVotingNumber(), expectedVotings.get(i).getVotingNumber());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsOrderByVotingNumberQuery() {
        List<Voting> expectedVotings = new ArrayList<>();

        expectedVotings.add(voting(201378, voter("01045034565", null)));
        expectedVotings.add(voting(353932, voter("01067242404", null)));
        expectedVotings.add(voting(358147, voter("02057425183", null)));

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(),
                        VotingSorting.builder()
                                .sortOrder("ASC")
                                .sortField("personId")
                                .build(), 0, 3, expectedVotings},
        };
    }

    @Test(dataProvider = "pagedVotingsOrderBySortDirectionQuery")
    public void testPagedVotings_givenOrderBySortDirection_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting,
                                                                           int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVotingNumber(), expectedVotings.get(i).getVotingNumber());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsOrderBySortDirectionQuery() {
        List<Voting> expectedVotingsSortedAsc = new ArrayList<>();

        expectedVotingsSortedAsc.add(voting(200228, null));
        expectedVotingsSortedAsc.add(voting(200382, null));
        expectedVotingsSortedAsc.add(voting(200791, null));

        List<Voting> expectedVotingsSortedDesc = new ArrayList<>();
        expectedVotingsSortedDesc.add(voting(803836, null));
        expectedVotingsSortedDesc.add(voting(801649, null));
        expectedVotingsSortedDesc.add(voting(801648, null));

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(),
                        VotingSorting.builder()
                                .sortOrder("ASC")
                                .sortField("votingNumber")
                                .build(), 0, 3, expectedVotingsSortedAsc},
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .build(),
                        VotingSorting.builder()
                                .sortOrder("DESC")
                                .sortField("votingNumber")
                                .build(), 0, 3, expectedVotingsSortedDesc},
        };
    }

    private Voting voting(Integer votingNumber, Voter voter) {
        Voting voting = new Voting();
        voting.setVotingNumber(votingNumber);
        voting.setVoter(voter);

        return voting;
    }

    private ElectionGroup mockElectionGroup() {
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        when(electionGroup.getPk()).thenReturn(bkfElectionGroupPk);
        return electionGroup;
    }

    private Voter voter(String personId, String nameLine) {
        Voter voter = new Voter();
        voter.setId(personId);
        voter.setNameLine(nameLine);

        return voter;
    }

    @Test(dataProvider = "pagedVotingsFilteredBySuggestedRejectedTestData")
    public void testPagedVotings_givenSuggestedProcessingFilter_verifiesSuggestedRejectedVotings(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(haldenMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        expectedVotings.forEach(voting -> {
            no.valg.eva.admin.configuration.domain.model.VotingRejection votingRejection = votingRejectionRepository.findById(VOTER_ALREADY_VOTED_FF.getId());
            Voting votingByVotingNumber = votingRepository.findVotingByVotingNumber(municipality.getPk(), electionGroup.getPk(), voting.getVotingNumber(), false);
            votingByVotingNumber.setSuggestedVotingRejection(votingRejection);
            votingByVotingNumber.setValidationTimestamp(null);
            votingRepository.update(testFixture.getUserData(), votingByVotingNumber);
        });

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVotingNumber(), expectedVotings.get(i).getVotingNumber());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsFilteredBySuggestedRejectedTestData() {
        List<Voting> expectedApprovedVotings = new ArrayList<>();
        expectedApprovedVotings.add(voting(3, null));
        expectedApprovedVotings.add(voting(2, null));
        expectedApprovedVotings.add(voting(1, null));

        List<SuggestedProcessingDto> suggestedProcessingList = new ArrayList<>();
        SuggestedProcessingDto suggestedProcessingDto = SuggestedProcessingDto.builder()
                .id(VOTER_ALREADY_VOTED_FF.getId())
                .textProperty(VOTER_ALREADY_VOTED_FF.getId())
                .processingType(ProcessingType.SUGGESTED_REJECTED)
                .build();

        suggestedProcessingList.add(suggestedProcessingDto);
        
        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .suggestedProcessingList(suggestedProcessingList)
                        .build(),
                        VotingSorting.builder()
                                .build(), 0, 3, expectedApprovedVotings},
        };
    }

    @Test(dataProvider = "pagedVotingsFilteredBySuggestedRejectedAndApprovedTestData")
    public void testPagedVotings_givenSuggestedProcessingFilter_verifiesSuggestedRejectedAndSuggestedApprovedVotings(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit, List<Voting> rejectedVotings,
                                                                                                                     List<Voting> approvedVotings, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(haldenMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        rejectedVotings.forEach(voting -> {
            no.valg.eva.admin.configuration.domain.model.VotingRejection votingRejection = votingRejectionRepository.findById(VOTER_ALREADY_VOTED_FF.getId());
            Voting votingByVotingNumber = votingRepository.findVotingByVotingNumber(municipality.getPk(), electionGroup.getPk(), voting.getVotingNumber(), false);
            votingByVotingNumber.setSuggestedVotingRejection(votingRejection);
            votingByVotingNumber.setValidationTimestamp(null);
            votingRepository.update(testFixture.getUserData(), votingByVotingNumber);
        });

        approvedVotings.forEach(voting -> {
            Voting votingByVotingNumber = votingRepository.findVotingByVotingNumber(municipality.getPk(), electionGroup.getPk(), voting.getVotingNumber(), false);
            votingByVotingNumber.setValidationTimestamp(null);
            votingRepository.update(testFixture.getUserData(), votingByVotingNumber);
        });

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            Voting voting = votings.get(i);
            Voting expectedVoting = expectedVotings.get(i);
            assertEquals(voting.getVotingNumber(), expectedVoting.getVotingNumber());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsFilteredBySuggestedRejectedAndApprovedTestData() {
        List<Voting> rejectedVotings = new ArrayList<>();
        rejectedVotings.add(voting(3, null));
        rejectedVotings.add(voting(2, null));
        rejectedVotings.add(voting(1, null));

        List<Voting> approvedVotings = new ArrayList<>();
        approvedVotings.add(voting(4, null));

        List<Voting> expectedVotings = new ArrayList<>(rejectedVotings);
        expectedVotings.addAll(approvedVotings);
        expectedVotings.sort((o1, o2) -> o2.getVotingNumber().compareTo(o1.getVotingNumber()));

        List<SuggestedProcessingDto> suggestedProcessingList = new ArrayList<>();
        SuggestedProcessingDto suggestedRejectedDto = SuggestedProcessingDto.builder()
                .id(VOTER_ALREADY_VOTED_FF.getId())
                .textProperty(VOTER_ALREADY_VOTED_FF.getId())
                .processingType(ProcessingType.SUGGESTED_REJECTED)
                .build();

        SuggestedProcessingDto suggestedApprovedDto = SuggestedProcessingDto.builder()
                .id("TG")
                .textProperty("Til godkjenning")
                .processingType(ProcessingType.SUGGESTED_APPROVED)
                .build();

        suggestedProcessingList.add(suggestedRejectedDto);
        suggestedProcessingList.add(suggestedApprovedDto);

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .suggestedProcessingList(suggestedProcessingList)
                        .build(),
                        VotingSorting.builder()
                                .build(), 0, 4, rejectedVotings, approvedVotings, expectedVotings},
        };
    }

    @Test(dataProvider = "pagedVotingsFilteredBySuggestedProcessingTestData")
    public void testPagedVotings_givenSuggestedProcessingFilter_verifiesVotings(VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit, List<Voting> expectedVotings) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryMap, votingRejectionMap, offset, limit);
        assertEquals(votings.size(), expectedVotings.size());

        for (int i = 0; i < votings.size(); i++) {
            assertEquals(votings.get(i).getVotingNumber(), expectedVotings.get(i).getVotingNumber());
        }
    }

    @DataProvider
    public Object[][] pagedVotingsFilteredBySuggestedProcessingTestData() {
        List<Voting> expectedApprovedVotings = new ArrayList<>();
        expectedApprovedVotings.add(voting(803836, null));
        expectedApprovedVotings.add(voting(801649, null));
        expectedApprovedVotings.add(voting(801648, null));

        return new Object[][]{
                {VotingFilters.builder()
                        .validatedVotings(false)
                        .suggestedProcessing(SuggestedProcessingDto.builder()
                                .id("TG")
                                .textProperty("Til godkjenning")
                                .processingType(ProcessingType.SUGGESTED_APPROVED)
                                .build())
                        .build(),
                        VotingSorting.builder()
                                .build(), 0, 3, expectedApprovedVotings},
        };
    }


    @Test(dataProvider = "countVotingsTestData")
    public void testCountVotings_givenVotingCategory_verifiesVotings(VotingFilters votingFilters, int expectedCount) {
        Municipality municipality = mockMunicipality(osloMunicipalityPk);
        ElectionGroup electionGroup = mockElectionGroup();

        int count = pagingVotingRepository.countVotings(municipality, electionGroup, votingFilters);
        assertEquals(count, expectedCount);
    }

    @DataProvider
    public Object[][] countVotingsTestData() {
        return new Object[][]{
                {VotingFilters.builder().build(), 60},
        };
    }
}