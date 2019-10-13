package no.valg.eva.admin.settlement.domain;

import static java.lang.String.format;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.repository.VoteCategoryRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.service.ContestReportDomainService;
import no.valg.eva.admin.settlement.domain.builder.SettlementBuilder;
import no.valg.eva.admin.settlement.domain.builder.SettlementBuilderFactory;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

@Default
@ApplicationScoped
public class SettlementDomainService {
	@Inject
	private ContestReportDomainService contestReportDomainService;
	@Inject
	private SettlementRepository settlementRepository;
	@Inject
	private VoteCategoryRepository voteCategoryRepository;
	@Inject
	private SettlementBuilderFactory settlementBuilderFactory;

	public SettlementDomainService(){

	}
	public SettlementDomainService(ContestReportDomainService contestReportDomainService, SettlementRepository settlementRepository,
			VoteCategoryRepository voteCategoryRepository, SettlementBuilderFactory settlementBuilderFactory) {
		this.contestReportDomainService = contestReportDomainService;
		this.settlementRepository = settlementRepository;
		this.voteCategoryRepository = voteCategoryRepository;
		this.settlementBuilderFactory = settlementBuilderFactory;
	}

	public void createSettlement(UserData userData, Contest contest) {
		if (settlementRepository.findSettlementByContest(contest.getPk()) != null) {
			throw new IllegalStateException(format("Settlement for contest <%s> already exists", contest.getId()));
		}
		SettlementBuilder settlementBuilder = settlementBuilder(contest);
		Settlement settlement = settlementBuilder.build();
		settlementRepository.create(userData, settlement);
	}

	private SettlementBuilder settlementBuilder(Contest contest) {
		List<ContestReport> contestReports = contestReportDomainService.findFinalContestReportsByContest(contest);
		SettlementConfig settlementConfig = SettlementConfig.from(contest.getElection());
		switch (settlementConfig) {
		case RENUMBER:
		case RENUMBER_AND_STRIKEOUT:
			return settlementBuilderFactory.settlementBuilderForRenumberingAndStrikeOuts(contest, contestReports);
		case PERSONAL:
		case PERSONAL_AND_WRITE_IN:
			VoteCategory baselineVoteCategory = voteCategoryRepository.findVoteCategoryById("baseline");
			return settlementBuilderFactory.settlementBuilderForPersonalVotesAndWriteIns(contest, contestReports, baselineVoteCategory);
		default:
			throw new IllegalStateException(format("Unknown settlement config: %s", settlementConfig));
		}
	}
}
