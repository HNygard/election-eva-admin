package no.valg.eva.admin.voting.domain.service;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.BoroughElectionDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.voting.LockType.LOCKED;
import static no.valg.eva.admin.common.voting.LockType.NOT_APPLICABLE;
import static no.valg.eva.admin.common.voting.LockType.UNLOCKED;
import static no.valg.eva.admin.common.voting.Tense.FUTURE;
import static no.valg.eva.admin.common.voting.Tense.PAST;
import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VotingCategoryStatusDomainServiceTest extends MockUtilsTestCase {

    private static final boolean XIM = true;
    private static final boolean PAPER = false;
    private static final boolean BOROUGH = true;
    private static final boolean MUNICIPALITY = false;
    private static final boolean PRINTED = true;
    private static final boolean NOT_PRINTED = false;
    private static final boolean CONSIDER_END_OF_ELECTION = true;
    private static final boolean DISREGARD_END_OF_ELECTION = false;

    private VotingCategoryStatusDomainService votingCategoryStatusDomainService;
    private MvArea mvArea;
    private Municipality municipality;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        votingCategoryStatusDomainService = initializeMocks(VotingCategoryStatusDomainService.class);
        mvArea = createMock(MvArea.class);
        MunicipalityRepository municipalityRepository = getInjectMock(MunicipalityRepository.class);
        municipality = mvArea.getMunicipality();
        when(municipalityRepository.getReference(any())).thenReturn(municipality);
    }

    @Test(dataProvider = "minusThirtyMunicipalityWithExpectedMessageProperties")
    public void votingCategoryStatuses_givenMinusThirtyMunicipality_verifiesMessageProperties(List<String> expected, boolean considerEndOfElection) {
        initializeMockedDates(mvArea, null);
        BoroughElectionDomainService boroughElectionDomainService = getInjectMock(BoroughElectionDomainService.class);
        when(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea)).thenReturn(false);
        when(municipality.dateForFirstElectionDay()).thenReturn(null);

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        assertThat(votingCategoryStatuses.size()).isEqualTo(expected.size());
        for (int i = 0; i < expected.size(); i++) {
            assertThat(votingCategoryStatuses.get(i).getMessageProperty()).isEqualTo(expected.get(i));
        }
    }

    @DataProvider
    private Object[][] minusThirtyMunicipalityWithExpectedMessageProperties() {
        return new Object[][]{
                {expectedMessagePropertyForMinusThirtyMunicipality(), CONSIDER_END_OF_ELECTION},
                {expectedMessagePropertyForMinusThirtyMunicipality(), DISREGARD_END_OF_ELECTION}
        };
    }

    private List<String> expectedMessagePropertyForMinusThirtyMunicipality() {
        List<String> messageProperties = new ArrayList<>();
        messageProperties.add("@voting_category_status[EARLY_FI].name");
        messageProperties.add("@voting_category_status[ADVANCE_FI].name");
        messageProperties.add("@voting_category_status[ADVANCE_FU].name");
        messageProperties.add("@voting_category_status[ADVANCE_FB].name");
        messageProperties.add("@voting_category_status[ADVANCE_FE].name");
        return messageProperties;
    }

    @Test(dataProvider = "ximAndBoroughWithExpextedMessageProperty")
    public void votingCategoryStatuses_givenXimAndBorough_returnsMessageProperty(boolean xim, boolean hasAccessToBoroughs, List<String> expected,
                                                                                 boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getMunicipality().isElectronicMarkoffs()).thenReturn(xim);
        BoroughElectionDomainService boroughElectionDomainService = getInjectMock(BoroughElectionDomainService.class);
        when(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea)).thenReturn(hasAccessToBoroughs);
        when(mvArea.getElectionEvent().getElectionEndDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getElectionEndTime()).thenReturn(LocalTime.now());

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        assertThat(votingCategoryStatuses.size()).isEqualTo(expected.size());
        for (int i = 0; i < expected.size(); i++) {
            assertThat(votingCategoryStatuses.get(i).getMessageProperty()).isEqualTo(expected.get(i));
        }
    }

    private void initializeMockedDates(MvArea mvArea) {
        initializeMockedDates(mvArea, LocalDate.now());
    }

    private void initializeMockedDates(MvArea mvArea, LocalDate localDate) {
        when(mvArea.getElectionEvent().getEarlyAdvanceVotingStartDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getAdvanceVotingStartDate()).thenReturn(LocalDate.now());
        when(mvArea.getMunicipality().dateForFirstElectionDay()).thenReturn(localDate);
    }

    @DataProvider
    private Object[][] ximAndBoroughWithExpextedMessageProperty() {
        return new Object[][]{
                {XIM, BOROUGH, expectedMessageProperty(XIM), CONSIDER_END_OF_ELECTION},
                {XIM, MUNICIPALITY, expectedMessageProperty(XIM), CONSIDER_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, expectedMessageProperty(PAPER), CONSIDER_END_OF_ELECTION},

                {XIM, BOROUGH, expectedMessageProperty(XIM), DISREGARD_END_OF_ELECTION},
                {XIM, MUNICIPALITY, expectedMessageProperty(XIM), DISREGARD_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, expectedMessageProperty(PAPER), DISREGARD_END_OF_ELECTION}
        };
    }

    private List<String> expectedMessageProperty(boolean xim) {
        List<String> messageProperties = new ArrayList<>();
        messageProperties.add("@voting_category_status[EARLY_FI].name");
        messageProperties.add("@voting_category_status[ADVANCE_FI].name");
        messageProperties.add("@voting_category_status[ADVANCE_FU].name");
        messageProperties.add("@voting_category_status[ADVANCE_FB].name");
        messageProperties.add("@voting_category_status[ADVANCE_FE].name");
        messageProperties.add("@voting_category_status[ELECTION_DAY_VS].name");
        if (xim) {
            messageProperties.add("@voting_category_status[ELECTION_DAY_VB].name");
        } else {
            messageProperties.add("@voting_category_status[ELECTION_DAY_VF].name");
        }
        messageProperties.add("@voting_category_status[LATE_FI].name");
        return messageProperties;
    }

    @Test(dataProvider = "endOfElection", expectedExceptions = IllegalStateException.class)
    public void votingCategoryStatuses_givenXimAndBorough_throwsException(boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getMunicipality().isElectronicMarkoffs()).thenReturn(PAPER);
        BoroughElectionDomainService boroughElectionDomainService = getInjectMock(BoroughElectionDomainService.class);
        when(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea)).thenReturn(BOROUGH);

        votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);
    }

    @DataProvider
    private Object[][] endOfElection() {
        return new Object[][]{
                {CONSIDER_END_OF_ELECTION},
                {CONSIDER_END_OF_ELECTION}
        };
    }

    @Test(dataProvider = "ximAndBoroughWitPresentTense")
    public void votingCategoryStatuses_givenDemoElection_returnsPresentTense(boolean xim, boolean hasAccessToBoroughs, boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getElectionEvent().isDemoElection()).thenReturn(true);
        when(municipality.isElectronicMarkoffs()).thenReturn(xim);
        BoroughElectionDomainService boroughElectionDomainService = getInjectMock(BoroughElectionDomainService.class);
        when(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea)).thenReturn(hasAccessToBoroughs);

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        votingCategoryStatuses.forEach(votingCategoryStatus -> assertThat(votingCategoryStatus.getTense()).isEqualTo(Tense.PRESENT));
    }

    @DataProvider
    private Object[][] ximAndBoroughWitPresentTense() {
        return new Object[][]{
                {XIM, BOROUGH, CONSIDER_END_OF_ELECTION},
                {XIM, MUNICIPALITY, CONSIDER_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, CONSIDER_END_OF_ELECTION},

                {XIM, BOROUGH, DISREGARD_END_OF_ELECTION},
                {XIM, MUNICIPALITY, DISREGARD_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, DISREGARD_END_OF_ELECTION}
        };
    }

    @Test(dataProvider = "ximAndBoroughWithUnlockedType")
    public void votingCategoryStatuses_givenDemoElection_returnsUnlockedLockTypeOrNotAppicable(boolean xim, boolean hasAccessToBoroughs, boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getElectionEvent().isDemoElection()).thenReturn(true);
        when(municipality.isElectronicMarkoffs()).thenReturn(xim);
        BoroughElectionDomainService boroughElectionDomainService = getInjectMock(BoroughElectionDomainService.class);
        when(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea)).thenReturn(hasAccessToBoroughs);

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        for (VotingCategoryStatus votingCategoryStatus : votingCategoryStatuses) {
            VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
            switch (votingPhase) {
                case ELECTION_DAY:
                case LATE:
                    assertThat(votingCategoryStatus.getLocked()).isEqualTo(NOT_APPLICABLE);
                    break;
                default:
                    assertThat(votingCategoryStatus.getLocked()).isEqualTo(LockType.UNLOCKED);
            }
        }
    }

    @DataProvider
    private Object[][] ximAndBoroughWithUnlockedType() {
        return new Object[][]{
                {XIM, BOROUGH, CONSIDER_END_OF_ELECTION},
                {XIM, MUNICIPALITY, CONSIDER_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, CONSIDER_END_OF_ELECTION},

                {XIM, BOROUGH, DISREGARD_END_OF_ELECTION},
                {XIM, MUNICIPALITY, DISREGARD_END_OF_ELECTION},
                {PAPER, MUNICIPALITY, DISREGARD_END_OF_ELECTION}
        };
    }

    @Test(dataProvider = "datesForPhases")
    public void votingCategoryStatuses_givenPhase_returnsTense(boolean xim, boolean electoralRollPrinted, LocalDate date, ArrayList<Tense> expected,
                                                               boolean considerEndOfElection) {
        when(municipality.isElectronicMarkoffs()).thenReturn(xim);
        when(municipality.isAvkrysningsmanntallKjort()).thenReturn(electoralRollPrinted);
        when(mvArea.getElectionEvent().getEarlyAdvanceVotingStartDate()).thenReturn(date);
        when(mvArea.getElectionEvent().getAdvanceVotingStartDate()).thenReturn(date.plusDays(5));
        when(municipality.dateForFirstElectionDay()).thenReturn(date.plusDays(10));
        when(mvArea.getElectionEvent().getElectionEndDate()).thenReturn(date.plusDays(14));
        when(mvArea.getElectionEvent().getElectionEndTime()).thenReturn(date.plusDays(14).toDateTimeAtStartOfDay().toLocalTime());

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);
        for (VotingCategoryStatus votingCategoryStatus : votingCategoryStatuses) {
            VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
            int ordinal = votingPhase.ordinal();
            Tense tense = expected.get(ordinal);
            assertThat(votingCategoryStatus.getTense()).isEqualTo(tense);
        }
    }

    @DataProvider
    private Object[][] datesForPhases() {
        return new Object[][]{
                {XIM, NOT_PRINTED,   votingRegistrationStartsIn(1),  tenseForPhases(FUTURE, FUTURE, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION},  // Before early advance voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(2), tenseForPhases(PRESENT, FUTURE, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION}, // During early advance voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PRESENT, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION},   // During advance voting
                {PAPER, NOT_PRINTED, votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PRESENT, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION},   // During advance voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PAST, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION},      // During advance voting
                {PAPER, NOT_PRINTED, votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, FUTURE, FUTURE), CONSIDER_END_OF_ELECTION},     // During election day voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, PRESENT, PRESENT), CONSIDER_END_OF_ELECTION},   // During election day voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, PRESENT, PRESENT), CONSIDER_END_OF_ELECTION},   // During election day voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(16), tenseForPhases(PAST, PAST, PAST, PAST), CONSIDER_END_OF_ELECTION},         // After end of election
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(16), tenseForPhases(PAST, PAST, PAST, PAST), CONSIDER_END_OF_ELECTION},         // After end of election
                
                {XIM, NOT_PRINTED,   votingRegistrationStartsIn(1),  tenseForPhases(FUTURE, FUTURE, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION},  // Before early advance voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(2), tenseForPhases(PRESENT, FUTURE, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION}, // During early advance voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PRESENT, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION},   // During advance voting
                {PAPER, NOT_PRINTED, votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PRESENT, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION},   // During advance voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(8), tenseForPhases(PAST, PAST, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION},      // During advance voting
                {PAPER, NOT_PRINTED, votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, FUTURE, FUTURE), DISREGARD_END_OF_ELECTION},     // During election day voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, PRESENT, PRESENT), DISREGARD_END_OF_ELECTION},   // During election day voting
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(12), tenseForPhases(PAST, PAST, PRESENT, PRESENT), DISREGARD_END_OF_ELECTION},   // During election day voting
                {PAPER, PRINTED,     votingRegistrationDidStartDaysAgo(16), tenseForPhases(PAST, PAST, PRESENT, PRESENT), DISREGARD_END_OF_ELECTION},   // After end of election
                {XIM, NOT_PRINTED,   votingRegistrationDidStartDaysAgo(16), tenseForPhases(PAST, PAST, PRESENT, PRESENT), DISREGARD_END_OF_ELECTION}    // After end of election
        };
    }

    private LocalDate votingRegistrationStartsIn(int days) {
        return LocalDate.now().plusDays(days);
    }

    private LocalDate votingRegistrationDidStartDaysAgo(int days) {
        return LocalDate.now().minusDays(days);
    }

    private ArrayList tenseForPhases(Tense early, Tense advance, Tense electionDay, Tense late) {
        return new ArrayList(asList(early, advance, electionDay, late));
    }

    @Test(dataProvider = "endOfElection")
    public void votingCategoryStatuses_givenPhaseWithoutLock_returnsNotApplicable(boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getElectionEvent().getElectionEndDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getElectionEndTime()).thenReturn(LocalTime.now());

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        for (VotingCategoryStatus votingCategoryStatus : votingCategoryStatuses) {
            VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
            switch (votingPhase) {
                case ELECTION_DAY:
                case LATE:
                    assertThat(votingCategoryStatus.getLocked()).isEqualTo(NOT_APPLICABLE);
                    break;
                default:
                    assertThat(votingCategoryStatus.getLocked()).isNotEqualTo(NOT_APPLICABLE);
            }
        }
    }

    @Test(dataProvider = "lockedStatuses")
    public void votingCategoryStatuses_givenPhaseWithLock_returnsLock(LocalDate advanceStartDate, boolean electoralRollPrinted, LockType expectedEarly,
                                                                      LockType expectedAdvance, boolean considerEndOfElection) {
        initializeMockedDates(mvArea);
        when(mvArea.getElectionEvent().getAdvanceVotingStartDate()).thenReturn(advanceStartDate);
        when(municipality.isAvkrysningsmanntallKjort()).thenReturn(electoralRollPrinted);
        when(mvArea.getElectionEvent().getElectionEndDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getElectionEndTime()).thenReturn(LocalTime.now());

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        for (VotingCategoryStatus votingCategoryStatus : votingCategoryStatuses) {
            VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
            switch (votingPhase) {
                case EARLY:
                    assertThat(votingCategoryStatus.getLocked()).isEqualTo(expectedEarly);
                    break;
                case ADVANCE:
                    assertThat(votingCategoryStatus.getLocked()).isEqualTo(expectedAdvance);
                    break;
                default:
                    assertThat(votingCategoryStatus.getLocked()).isNotEqualTo(LOCKED);
                    assertThat(votingCategoryStatus.getLocked()).isNotEqualTo(UNLOCKED);
            }
        }
    }

    @DataProvider
    private Object[][] lockedStatuses() {
        return new Object[][]{
                {LocalDate.now().plusDays(1), false, UNLOCKED, UNLOCKED, CONSIDER_END_OF_ELECTION},
                {LocalDate.now().minusDays(1), false, LOCKED, LOCKED, CONSIDER_END_OF_ELECTION},
                {LocalDate.now().minusDays(1), true, LOCKED, LOCKED, CONSIDER_END_OF_ELECTION},

                {LocalDate.now().plusDays(1), false, UNLOCKED, UNLOCKED, DISREGARD_END_OF_ELECTION},
                {LocalDate.now().minusDays(1), false, LOCKED, LOCKED, DISREGARD_END_OF_ELECTION},
                {LocalDate.now().minusDays(1), true, LOCKED, LOCKED, DISREGARD_END_OF_ELECTION}
        };
    }

    @Test(dataProvider = "endOfElection")
    public void votingCategoryStatuses_givenPhaseWithStartDate_returnsStartAndEndDate(boolean considerEndOfElection) {
        LocalDate earlyAdvanceVortingStartDate = new LocalDate(2010, 1, 1);
        LocalDate advanceVotingStartDate = new LocalDate(2010, 2, 2);
        LocalDate dateForFirstElectionDay = new LocalDate(2010, 3, 3);
        LocalDate firstDateOfYearAfterElectionYear = new LocalDate(2011, 1, 1);
        when(mvArea.getElectionEvent().getEarlyAdvanceVotingStartDate()).thenReturn(earlyAdvanceVortingStartDate);
        when(mvArea.getElectionEvent().getAdvanceVotingStartDate()).thenReturn(advanceVotingStartDate);
        when(municipality.dateForFirstElectionDay()).thenReturn(dateForFirstElectionDay);
        when(mvArea.getElectionEvent().getElectionEndDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getElectionEndTime()).thenReturn(LocalTime.now());

        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);

        for (VotingCategoryStatus votingCategoryStatus : votingCategoryStatuses) {
            VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
            switch (votingPhase) {
                case EARLY:
                    assertThat(votingCategoryStatus.getStartingDate()).isEqualTo(earlyAdvanceVortingStartDate);
                    assertThat(votingCategoryStatus.getEndingDate()).isEqualTo(advanceVotingStartDate);
                    break;
                case ADVANCE:
                    assertThat(votingCategoryStatus.getStartingDate()).isEqualTo(advanceVotingStartDate);
                    assertThat(votingCategoryStatus.getEndingDate()).isEqualTo(dateForFirstElectionDay);
                    break;
                case ELECTION_DAY:
                case LATE:
                    assertThat(votingCategoryStatus.getStartingDate()).isEqualTo(dateForFirstElectionDay);
                    assertThat(votingCategoryStatus.getEndingDate()).isEqualTo(firstDateOfYearAfterElectionYear);
                    break;
            }
        }
    }

    @Test(dataProvider = "endOfElection", expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@config.election_event.missing_value")
    public void votingCategoryStatuses_givenMissingDates_throwEvoteException(boolean considerEndOfElection) {
        votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);
    }

    @Test(dataProvider = "endOfElection", expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@config.local.election_day.missing_value")
    public void votingCategoryStatuses_givenMissingOpeningHours_throwEvoteException(boolean considerEndOfElection) {
        when(mvArea.getElectionEvent().getEarlyAdvanceVotingStartDate()).thenReturn(LocalDate.now());
        when(mvArea.getElectionEvent().getAdvanceVotingStartDate()).thenReturn(LocalDate.now());
        when(mvArea.getMunicipality().dateForFirstElectionDay()).thenReturn(new LocalDate(Long.MAX_VALUE, DateTimeZone.UTC));
        votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, considerEndOfElection);
    }
}
