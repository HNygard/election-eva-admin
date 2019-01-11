package no.valg.eva.admin.voting.domain.service;

import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.APPROVE;
import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.DEAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

public class ConfirmationCategoryStatusDomainServiceTest extends MockUtilsTestCase {

    private ConfirmationCategoryStatusDomainService confirmationCategorystatusDomainService;
    private MvArea mvArea;
    private VotingRepository votingRepository;
    private ElectionGroup electionGroup;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        confirmationCategorystatusDomainService = initializeMocks(ConfirmationCategoryStatusDomainService.class);
        votingRepository = getInjectMock(VotingRepository.class);
        mvArea = createMock(MvArea.class);
        electionGroup = createMock(ElectionGroup.class);
    }

    @Test(dataProvider = "needsVerification")
    public void confirmationCategoryStatuses_givenUnconfirmedVotings_returnsNeesVerification(List<Voting> votings, VotingPhase votingPhase, boolean needsVerification) {
        VotingCategoryStatusDomainService votingCategoryStatusDomainService = getInjectMock(VotingCategoryStatusDomainService.class);
        when(votingCategoryStatusDomainService.votingCategoryStatuses(any(), anyBoolean()))
                .thenReturn(singletonList(new VotingCategoryStatus("", VotingCategory.FI, votingPhase, Tense.PRESENT, LockType.UNLOCKED, LocalDate.now())));

        when(votingRepository.findVotingsToConfirm(any(), any(), any(), any())).thenReturn(votings);

        List<ConfirmationCategoryStatus> confirmationCategoryStatuses = confirmationCategorystatusDomainService.confirmationCategoryStatuses(mvArea, electionGroup);

        assertThat(confirmationCategoryStatuses.size()).isGreaterThan(0);
        for (ConfirmationCategoryStatus confirmationCategoryStatus : confirmationCategoryStatuses) {
            if (needsVerification) {
                assertThat(confirmationCategoryStatus.isNeedsVerification()).isTrue();
            } else
                assertThat(confirmationCategoryStatus.isNeedsVerification()).isFalse();

        }
    }

    @DataProvider
    public Object[][] needsVerification() {
        List<Voting> votings = new ArrayList<>();
        votings.add(getVotingMock());
        return new Object[][]{
                {emptyList(), VotingPhase.ADVANCE, false},
                {votings, VotingPhase.ADVANCE, true},
                {votings, VotingPhase.EARLY, false}
        };
    }

    private Voting getVotingMock() {
        Voting voting = createMock(Voting.class);
        when(voting.getElectionGroup().getElectionEvent().getAdvanceVotingStartDate()).thenReturn(LocalDate.now());
        when(voting.getCastTimestamp()).thenReturn(DateTime.now());
        return voting;
    }

    @Test(dataProvider = "categoriesAndPhases")
    public void votingConfirmationReport_withVotings_returnConfirmatonReport(VotingCategory votingCategory, VotingPhase votingPhase) {
        List<Voting> votings = Arrays.asList(voting(1L, votingPhase), voting(2L, votingPhase));
        mockFindVotingsToConfirm(votings);

        VotingConfirmationReportDto votingConfirmationReportDto = confirmationCategorystatusDomainService.votingConfirmationReport(
                mvArea, electionGroup, votingCategory, votingPhase, null, null);

        assertThat(votingConfirmationReportDto.getNumberOfApprovedVotings()).isEqualTo(999);
        assertThat(votingConfirmationReportDto.getNumberOfRejectedVotings()).isEqualTo(99);
        assertThat(votingConfirmationReportDto.getNumberOfVotingsToConfirm()).isEqualTo(votings.size());
        assertThat(votingConfirmationReportDto.getVotingDtoListToConfirm().size()).isEqualTo(votings.size());
    }

    @DataProvider
    public Object[][] categoriesAndPhases() {
        return new Object[][]{
                {VotingCategory.FI, VotingPhase.ADVANCE},
                {VotingCategory.FI, VotingPhase.EARLY},
                {VotingCategory.FU, VotingPhase.EARLY},
                {VotingCategory.FB, VotingPhase.EARLY},
                {VotingCategory.FE, VotingPhase.EARLY},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY},
                {VotingCategory.VF, VotingPhase.ELECTION_DAY},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY},
                {VotingCategory.FI, VotingPhase.LATE}
        };
    }

    private Voting voting(Long pk, VotingPhase votingPhase) {
        Voting voting = new Voting();
        voting.setPk(pk);
        voting.setLateValidation(confirmationCategorystatusDomainService.isLateValidation(votingPhase));
        voting.setVoter(voter());
        return voting;
    }

    private Voter voter() {
        return Voter.builder()
                .build();
    }

    private void mockFindVotingsToConfirm(List<Voting> votings) {
        when(votingRepository.countApprovedEnvelopeVotings(any(), any(), any(), anyBoolean(), any(), any())).thenReturn(999L);
        when(votingRepository.countRejectedEnvelopeVotings(any(), any(), any(), anyBoolean(), any(), any())).thenReturn(99L);
        when(votingRepository.findVotingsToConfirm(any(), any(), any(), anyBoolean(), any(), any(), any())).thenReturn(votings);
    }

    @Test(dataProvider = "categoriesAndPhases")
    public void votingConfirmationReport_withSuggestionApprove_returnConfirmationReport(VotingCategory votingCategory, VotingPhase votingPhase) {
        List<Voting> votings = Arrays.asList(voting(1L, votingPhase), voting(2L, votingPhase));
        mockFindVotingsToConfirm(votings);

        VotingConfirmationReportDto votingConfirmationReportDto = confirmationCategorystatusDomainService.votingConfirmationReport(
                mvArea, electionGroup, votingCategory, votingPhase, null, null);

        assertThat(votingConfirmationReportDto.suggestedApprovedVotings().size()).isEqualTo(2);
        assertThat(votingConfirmationReportDto.suggestedRejectedVotings()).isEmpty();
        for (VotingDto suggestedApprovedVoting : votingConfirmationReportDto.suggestedApprovedVotings()) {
            assertThat(suggestedApprovedVoting.getSuggestedProcessing()).isEqualTo(APPROVE.getName());
        }
    }

    @Test(dataProvider = "categoriesAndPhases")
    public void votingConfirmationReport_withVotingsWithSuggestionDead_returnConfirmationReport(VotingCategory votingCategory, VotingPhase votingPhase) {
        List<Voting> votings = Arrays.asList(voting(1L, votingPhase), voting(2L, votingPhase));
        mockFindVotingsToConfirm(votings);

        VotingRejection suggestedVotingRejection = new VotingRejection();
        suggestedVotingRejection.setSuggestedRejectionName(DEAD.getName());
        votings.get(1).setSuggestedVotingRejection(suggestedVotingRejection);

        VotingConfirmationReportDto votingConfirmationReportDto = confirmationCategorystatusDomainService.votingConfirmationReport(mvArea, electionGroup, votingCategory, votingPhase, null, null); 
        assertThat(votingConfirmationReportDto.suggestedApprovedVotings().size()).isEqualTo(1);
        assertThat(votingConfirmationReportDto.suggestedRejectedVotings().size()).isEqualTo(1);
        for (VotingDto suggestedRejectedVoting : votingConfirmationReportDto.suggestedRejectedVotings()) {
            assertThat(suggestedRejectedVoting.getSuggestedProcessing()).isEqualTo(DEAD.getName());
        }
    }

    @Test(dataProvider = "categoriesAndPhases")
    public void votingConfirmationReport_withVotingsWithNotInElectoralRoll_returnConfirmationReport(VotingCategory votingCategory, VotingPhase votingPhase) {
        List<Voting> votings = Arrays.asList(voting(1L, votingPhase), voting(2L, votingPhase));
        mockFindVotingsToConfirm(votings);

        votings.get(1).setSuggestedVotingRejection(new VotingRejection());
        
        VotingConfirmationReportDto votingConfirmationReportDto = confirmationCategorystatusDomainService.votingConfirmationReport(
                mvArea, electionGroup, votingCategory, votingPhase, null, null);

        assertThat(votingConfirmationReportDto.suggestedApprovedVotings().size()).isEqualTo(1);
        assertThat(votingConfirmationReportDto.suggestedRejectedVotings().size()).isEqualTo(1);
    }

    @Test(dataProvider = "categoriesAndPhases")
    public void votingConfirmationReport_withVotingsWithMultipleVotings_returnConfirmationReport(VotingCategory votingCategory, VotingPhase votingPhase) {
        List<Voting> votings = Arrays.asList(voting(1L, votingPhase), voting(2L, votingPhase));
        mockFindVotingsToConfirm(votings);
        
        votings.get(1).setSuggestedVotingRejection(new VotingRejection());
        
        VotingConfirmationReportDto votingConfirmationReportDto = confirmationCategorystatusDomainService.votingConfirmationReport(mvArea, electionGroup, votingCategory, votingPhase, null, null);

        assertThat(votingConfirmationReportDto.suggestedApprovedVotings().size()).isEqualTo(1);
        assertThat(votingConfirmationReportDto.suggestedRejectedVotings().size()).isEqualTo(1);
    }

    @Test(dataProvider = "votingPhases")
    public void isLateValidation(VotingPhase votingPhase, boolean expected) {
        boolean lateValidation = confirmationCategorystatusDomainService.isLateValidation(votingPhase);
        assertThat(lateValidation).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] votingPhases() {
        return new Object[][]{
                {VotingPhase.ADVANCE, false},
                {VotingPhase.EARLY, false},
                {VotingPhase.ELECTION_DAY, false},
                {VotingPhase.LATE, true}
        };
    }
}
