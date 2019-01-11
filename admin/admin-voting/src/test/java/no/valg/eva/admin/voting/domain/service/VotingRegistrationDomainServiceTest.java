package no.valg.eva.admin.voting.domain.service;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaServiceBean;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_ALREADY_VOTED_FF;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA;
import static no.valg.eva.admin.configuration.domain.model.VoterStatus.DECEASED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class VotingRegistrationDomainServiceTest extends MockUtilsTestCase {

    private VotingRejectionRepository votingRejectionRepository;
    private VotingRegistrationDomainService service;
    private PollingPlace pollingPlaceMock;
    private ElectionGroup electionGroup;
    private UserData userDataMock;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        service = initializeMocks(VotingRegistrationDomainService.class);

        votingRejectionRepository = getInjectMock(VotingRejectionRepository.class);
        pollingPlaceMock = createMock(PollingPlace.class);
        electionGroup = createMock(ElectionGroup.class);
        userDataMock = createMock(UserData.class);
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Missing MvArea when creating Voting")
    public void testRegisterVoting_givenVoterMvAreaNull_verifiesException() {
        Voter voterMock = createMock(Voter.class);
        when(voterMock.getMvArea()).thenReturn(null);
        when(getInjectMock(VotingRepository.class).getVotingsByElectionGroupVoterAndMunicipality(anyLong(), anyLong(), anyLong())).thenReturn(emptyList());

        Municipality municipality = Municipality.builder().build();
        service.registerAdvanceVotingInEnvelope(userDataMock, pollingPlaceMock, electionGroup, municipality, voterMock, FI, false, VotingPhase.ADVANCE);
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeRejectionTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenExistingUnConfirmedVotings_verifiesRejection(boolean isVoterInElectoralRoll, List<Voting> existingVotings, no.valg.eva.admin.common.voting.VotingRejection expectedVotingRejection) {
        MvArea mvArea = mvArea("1234");
        Municipality municipality = Municipality.builder()
                .id(mvArea.getMunicipalityId())
                .build();
        Voter voterMock = voterMock(false, mvArea, isVoterInElectoralRoll);

        if (!existingVotings.isEmpty()) {
            VotingRejection votingRejection = votingRejection(VOTER_ALREADY_VOTED_FF.getId());
            when(votingRejectionRepository.findById(votingRejection.getId())).thenReturn(votingRejection);
        }

        if (!isVoterInElectoralRoll) {
            VotingRejection votingRejection = votingRejection(VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA.getId());
            when(votingRejectionRepository.findById(votingRejection.getId())).thenReturn(votingRejection);
        }
        
        Voting voting = testRegisterAdvanceVotingInEnvelope(voterMock, FI, municipality, false, existingVotings);

        if (expectedVotingRejection != null) {
            assertEquals(voting.getSuggestedVotingRejection().getId(), expectedVotingRejection.getId());
        } else {
            assertNull(voting.getSuggestedVotingRejection());
        }
    }

    @DataProvider
    public Object[][] registerAdvanceVotingInEnvelopeRejectionTestData() {
        return new Object[][]{
                {false, emptyList(), VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA},
                {true, singletonList(voting()), VOTER_ALREADY_VOTED_FF},
                {true, emptyList(), null},
        };
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterInMunicipality_verifiesBallotBoxIdNull(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting, boolean isFictitious) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(FI, municipality, false, isFictitious);

        assertNull(voting.getBallotBoxId());
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterInMunicipality_verifiesVotingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting, boolean isFictitious) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, isFictitious);

        assertEquals(voting.getVotingCategory().getId(), votingCategory.getId());
    }

    @DataProvider
    public Object[][] registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData() {
        VotingCategory domainVotingCategory = votingCategory(FI);

        Voting voting = new Voting();
        voting.setReceivedTimestamp(org.joda.time.LocalDateTime.now().toDateTime());
        voting.setVotingCategory(domainVotingCategory);
        return new Object[][]{
                {voting.getVotingCategory().votingCategoryById(), municipalityMock("1234"), voting, false},
                {voting.getVotingCategory().votingCategoryById(), municipalityMock("999"), voting, true},
        };
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterInMunicipality_verifiesReceivedTimeStampNotNull(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting, boolean isFictitious) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, isFictitious);

        assertTimeDiffLessThan(voting.getReceivedTimeStampAsJavaTime(), expectedVoting.getReceivedTimeStampAsJavaTime(), 5);
        assertFalse(voting.isLateValidation());
        assertFalse(voting.isApproved());
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterInMunicipality_verifiesIsLate(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting, boolean isFictitious) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, isFictitious);

        assertFalse(voting.isLateValidation());
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterInMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterInMunicipality_verifiesIsNotApproved(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting, boolean isFictitious) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, isFictitious);

        assertFalse(voting.isApproved());
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterFromOtherMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterNotInMunicipality_verifiesVotingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, false);

        assertEquals(voting.getVotingCategory().getId(), votingCategory.getId());
        assertNull(voting.getReceivedTimestamp());
        assertFalse(voting.isLateValidation());
        assertFalse(voting.isApproved());
    }

    @DataProvider
    public Object[][] registerAdvanceVotingInEnvelopeVoterFromOtherMunicipalityTestData() {
        VotingCategory domainVotingCategory = votingCategory(FA);

        Voting voting = new Voting();
        voting.setReceivedTimestamp(org.joda.time.LocalDateTime.now().toDateTime());
        voting.setVotingCategory(domainVotingCategory);
        return new Object[][]{
                {voting.getVotingCategory().votingCategoryById(), municipalityMock("999"), voting},
        };
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterFromOtherMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterNotInMunicipality_verifiesReceivedTimestampNull(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, false);
        assertNull(voting.getReceivedTimestamp());
    }

    @Test
    public void testRegisterAdvanceVotingInEnvelope_givenIsLate_verifiesIsLateTrue() {
        Voting voting = testRegisterAdvanceVotingInEnvelope(FI, Municipality.builder().build(), true, false);
        assertTrue(voting.isLateValidation());
    }

    @Test(dataProvider = "registerAdvanceVotingInEnvelopeVoterFromOtherMunicipalityTestData")
    public void testRegisterAdvanceVotingInEnvelope_givenVoterNotInMunicipality_verifiesBallotBoxIdNull(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, Voting expectedVoting) {
        Voting voting = testRegisterAdvanceVotingInEnvelope(votingCategory, municipality, false, false);

        assertNull(voting.getBallotBoxId());
    }

    private Voting testRegisterAdvanceVotingInEnvelope(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, boolean isLate, boolean voterIsFictitious) {
        Voter voterMock = voterMock(voterIsFictitious, mvArea("1234"), false);
        return testRegisterAdvanceVotingInEnvelope(voterMock, votingCategory, municipality, isLate, emptyList());
    }

    private Voting testRegisterAdvanceVotingInEnvelope(Voter voterMock, no.valg.eva.admin.common.voting.VotingCategory votingCategory, Municipality municipality, boolean isLate, List<Voting> existingVotings) {
        VotingRepository votingRepository = getInjectMock(VotingRepository.class);
        when(votingRepository.getVotingsByElectionGroupVoterAndMunicipality(voterMock.getPk(), electionGroup.getPk(), municipality.getPk())).thenReturn(existingVotings);
        when(votingRepository.findVotingCategoryById(votingCategory.getId())).thenReturn(votingCategory(votingCategory));

        Answer<Object> objectAnswer = invocation -> {
            Object[] args = invocation.getArguments();
            return args[1];
        };

        when(votingRepository.create(any(UserData.class), any(Voting.class)))
                .thenAnswer(objectAnswer);

        Voting voting = service.registerAdvanceVotingInEnvelope(userDataMock, pollingPlaceMock, electionGroup, municipality, voterMock, votingCategory, isLate, VotingPhase.ADVANCE);
        verify(votingRepository, times(1)).create(userDataMock, voting);
        return voting;
    }

    private VotingRejection votingRejection(String rejectionId) {
        VotingRejection votingRejection = new VotingRejection();
        votingRejection.setId(rejectionId);
        return votingRejection;
    }

    private VotingCategory votingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategory) {
        VotingCategory domainVotingCategory = new VotingCategory();
        domainVotingCategory.setId(votingCategory.getId());
        return domainVotingCategory;
    }

    private Municipality municipalityMock(String municipalityId) {
        return Municipality.builder()
                .id(municipalityId)
                .build();
    }

    private Voter voterMock(boolean isFictitious, MvArea mvArea, boolean isApproved) {
        Voter voterMock = createMock(Voter.class);
        when(voterMock.getMunicipalityId()).thenReturn(mvArea.getMunicipalityId());
        when(voterMock.getMvArea()).thenReturn(mvArea);
        when(voterMock.isFictitious()).thenReturn(isFictitious);
        when(voterMock.isApproved()).thenReturn(isApproved);
        return voterMock;
    }

    @Test
    public void testRegisterVotingInEnvelopeCentrally_givenVotingCategoryVF_verifiesMvaArea() {
        MvArea municipalityMvArea = mvArea("1234");
        Voter voterMock = voterMock(false, municipalityMvArea, false);

        Voting voting = testRegisterVotingInEnvelopeCentrally(VF, voterMock, pollingPlaceMock, municipalityMvArea);
        assertEquals(voting.getVoter().getMvArea(), municipalityMvArea);
        assertVoting(voting, voterMock, votingCategory(VF), false, pollingPlaceMock, voterMock.getMvArea());
    }

    private void assertVoting(Voting voting, Voter voter, VotingCategory votingCategory, boolean expectingApproved, PollingPlace pollingPlace, MvArea mvArea) {
        assertEquals(voting.getElectionGroup(), electionGroup);
        assertTimeDiffLessThan(voting.getCastTimeStampAsJavaTime(), LocalDateTime.now(), 5);
        assertTimeDiffLessThan(voting.getReceivedTimeStampAsJavaTime(), LocalDateTime.now(), 5);
        assertEquals(voting.getVoter(), voter);
        assertEquals(voting.getVotingCategory(), votingCategory);
        assertEquals(voting.isApproved(), expectingApproved, "Voting should have been approved: " + expectingApproved + ", ");
        assertEquals(voting.getPollingPlace(), pollingPlace, "Expected pollingPlace: " + pollingPlace + ", ");
        assertEquals(voting.getMvArea(), mvArea);
    }

    private MvArea mvArea(String municipalityId) {
        return MvArea.builder()
                .municipalityId(municipalityId)
                .build();
    }

    private Voting testRegisterVotingInEnvelopeCentrally(no.valg.eva.admin.common.voting.VotingCategory votingCategory, Voter voter, PollingPlace pollingPlace, MvArea municipalityMvArea) {
        VotingRepository votingRepository = getInjectMock(VotingRepository.class);

        Municipality municipality = Municipality.builder().build();

        PollingDistrict pollingDistrictMock = createMock(PollingDistrict.class);
        when(pollingDistrictMock.getPk()).thenReturn(1L);

        when(getInjectMock(PollingDistrictRepository.class).findMunicipalityProxy(anyLong())).thenReturn(pollingDistrictMock);
        when(getInjectMock(MvAreaServiceBean.class).findByPollingDistrict(anyLong())).thenReturn(municipalityMvArea);
        when(getInjectMock(PollingPlaceRepository.class).findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox(anyLong(), anyBoolean())).thenReturn(pollingPlace);
        when(votingRepository.findVotingCategoryById(anyString())).thenReturn(votingCategory(votingCategory));

        Answer<Object> objectAnswer = invocation -> {
            Object[] args = invocation.getArguments();
            return args[1];
        };

        when(votingRepository.create(any(UserData.class), any(Voting.class)))
                .thenAnswer(objectAnswer);

        Voting voting = service.registerElectionDayVotingInEnvelopeCentrally(userDataMock, electionGroup, municipality, voter, votingCategory, VotingPhase.ELECTION_DAY);
        verify(votingRepository, times(1)).create(userDataMock, voting);
        return voting;
    }

    @Test(dataProvider = "voterUpdatedTestData")
    public void testVoterUpdated_givenDeceasedVoter_verifiesVotingUpdates(Voter voter, List<Voting> votings) {
        when(getInjectMock(VotingRepository.class).findVotingsToConfirmForVoter(any(Voter.class))).thenReturn(votings);
        service.voterUpdated(userDataMock, voter);
        verify(getInjectMock(VotingRepository.class), times(votings.size())).update(userDataMock, votings.get(0));
    }

    @DataProvider
    public Object[][] voterUpdatedTestData() {
        return new Object[][]{
                {voter(DECEASED.getStatusCode()), singletonList(voting())}
        };
    }

    private Voting voting() {
        return Voting.builder().build();
    }

    private Voter voter(int statusCode) {
        return Voter.builder()
                .statuskode((char) statusCode)
                .build();
    }
}