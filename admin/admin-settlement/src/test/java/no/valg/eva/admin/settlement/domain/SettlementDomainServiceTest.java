package no.valg.eva.admin.settlement.domain;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.repository.VoteCategoryRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.service.ContestReportDomainService;
import no.valg.eva.admin.settlement.domain.builder.SettlementBuilder;
import no.valg.eva.admin.settlement.domain.builder.SettlementBuilderFactory;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettlementDomainServiceTest {

	@Test
    public void performSettlement_givenUserDataAndContest_fetchesContestReportsAndCreatesNewSettlement() {
		Contest contest = mock(Contest.class, RETURNS_DEEP_STUBS);
		List<ContestReport> contestReports = new ArrayList<>();
		ContestReportDomainService contestReportDomainService = mock(ContestReportDomainService.class);
		SettlementRepository settlementRepository = mock(SettlementRepository.class);
		VoteCategoryRepository voteCategoryRepository = mock(VoteCategoryRepository.class);
		SettlementBuilderFactory settlementBuilderFactory = mock(SettlementBuilderFactory.class);
		SettlementBuilder settlementBuilder = mock(SettlementBuilder.class);
		Settlement settlement = mock(Settlement.class);

		when(contest.getElection().isRenumber()).thenReturn(true);
		when(contestReportDomainService.findFinalContestReportsByContest(contest)).thenReturn(contestReports);
		when(settlementBuilder.build()).thenReturn(settlement);
		when(settlementBuilderFactory.settlementBuilderForRenumberingAndStrikeOuts(contest, contestReports)).thenReturn(settlementBuilder);

		SettlementDomainService settlementDomainService = new SettlementDomainService(contestReportDomainService, settlementRepository, voteCategoryRepository,
				settlementBuilderFactory);

		UserData userData = mock(UserData.class);
		settlementDomainService.createSettlement(userData, contest);
		ArgumentCaptor<Settlement> settlementArgumentCaptor = ArgumentCaptor.forClass(Settlement.class);
		verify(settlementRepository).create(eq(userData), settlementArgumentCaptor.capture());
		assertThat(settlement).isSameAs(settlement);
	}
}
