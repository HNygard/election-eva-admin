package no.valg.eva.admin.counting.domain.service;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.VoteCountBuilder;
import no.valg.eva.admin.counting.domain.auditevents.ThreadLocalVoteCountAuditDetailsMap;
import no.valg.eva.admin.counting.domain.auditevents.VoteCountAuditDetails;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;

public class FinalCountService extends BaseCountService {

	private final Set<CountCategory> validCountCategoriesForBoroughElection;
	private CountingCodeValueRepository countingCodeValueRepository;
	private BallotRejectionRepository ballotRejectionRepository;
	private VoteCountService voteCountService;
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	public FinalCountService(ReportCountCategoryRepository reportCountCategoryRepository,
			CountingCodeValueRepository countingCodeValueRepository,
			ReportingUnitRepository reportingUnitRepository,
			BallotRejectionRepository ballotRejectionRepository, VoteCountService voteCountService,
			VoteCountStatusendringTrigger voteCountStatusendringTrigger) {

		super(reportingUnitRepository, reportCountCategoryRepository, null, null);

		this.countingCodeValueRepository = countingCodeValueRepository;
		this.ballotRejectionRepository = ballotRejectionRepository;
		this.voteCountService = voteCountService;
		this.voteCountStatusendringTrigger = voteCountStatusendringTrigger;

		validCountCategoriesForBoroughElection = new HashSet<>();
		validCountCategoriesForBoroughElection.add(CountCategory.VO);
		validCountCategoriesForBoroughElection.add(CountCategory.FO);
		validCountCategoriesForBoroughElection.add(CountCategory.FS);
		validCountCategoriesForBoroughElection.add(CountCategory.BF);
		validCountCategoriesForBoroughElection.add(CountCategory.VS);
		validCountCategoriesForBoroughElection.add(CountCategory.VB);
	}

	/**
	 * Validates that count category for count is a valid category for this municipality and contest
	 * 
	 * @param countCategory count category chosen in gui
	 * @param municipality for this count
	 * @param electionGroup which election group this count belongs to
	 */
	public void validateCountCategory(final CountCategory countCategory, final Municipality municipality, final ElectionGroup electionGroup) {
		ReportCountCategory reportCountCategory = getReportCountCategory(municipality, electionGroup, countCategory);

		if (reportCountCategory == null) {
			throw new IllegalArgumentException("Illegal argument, vote count category is not allowed for this municipality and contest");
		}
	}

	/**
	 * Validates that count category for count is a valid category for this borough and contest
	 * @param countCategory count category chosen in gui
	 */
	public void validateCountCategoryForBoroughElection(final CountCategory countCategory) {
		if (!validCountCategoriesForBoroughElection.contains(countCategory)) {
			throw new IllegalArgumentException(
					String.format("Illegal argument, vote count category '%s' is not allowed for this borough and contest", countCategory.getId()));
		}
	}

	/**
	 * Saves a new final count, that is creates a contest report and then creates a new vote count containing ballot counts
	 */
	public FinalCount saveNewFinalCount(
			ReportingUnit reportingUnit,
			CountContext context,
			FinalCount finalCount,
			MvArea countArea,
			Map<String, Ballot> ballotMap,
			MvElection mvElectionContest) {

		ContestReport contestReport = voteCountService.findContestReport(reportingUnit, mvElectionContest);
		if (contestReport == null) {
			Contest contest = mvElectionContest.getContest();
			contestReport = voteCountService.createContestReport(initContestReport(contest, reportingUnit));
		}

		CountCategory category = context.getCategory();
		no.valg.eva.admin.counting.domain.model.CountQualifier finalCountQualifier = countingCodeValueRepository
				.findCountQualifierById(CountQualifier.FINAL.getId());
		VoteCountCategory voteCountCategory = countingCodeValueRepository.findVoteCountCategoryById(category.getId());
		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusById(finalCount.getStatus().getId());

		VoteCount finalVoteCount = new VoteCountBuilder()
				.applyArea(countArea)
				.applyVoteCountCategory(voteCountCategory)
				.applyVoteCountStatus(voteCountStatus)
				.applyCountQualifier(finalCountQualifier)
				.applyFinalCount(finalCount)
				.build();
		contestReport.add(finalVoteCount);

		finalVoteCount.addNewBallotCount(ballotMap.get(EvoteConstants.BALLOT_BLANK), finalCount.getBlankBallotCount(), BLANK_MODIFIED_COUNT_ZERO);
		for (final BallotCount ballotCount : finalCount.getBallotCounts()) {
			finalVoteCount.addNewBallotCount(
					ballotMap.get(ballotCount.getId()),
					ballotCount.getUnmodifiedCount(),
					ballotCount.getModifiedCount());
		}

		Map<String, BallotRejection> ballotRejectionMap = initBallotRejectionMap(context);

		for (final RejectedBallotCount rejectedBallotCount : finalCount.getRejectedBallotCounts()) {
			finalVoteCount.addNewRejectedBallotCount(
					ballotRejectionMap.get(rejectedBallotCount.getId()),
					rejectedBallotCount.getCount());
		}

		finalCount.setId(finalVoteCount.getId());
		finalCount.setVoteCountPk(finalVoteCount.getPk());

		for (BallotCount ballotCount : finalCount.getBallotCounts()) {
			String ballotCountId = ballotCount.getId();
			if (isNotBlank(ballotCountId)) {
				Long ballotCountPk = finalVoteCount.getBallotCountMap().get(ballotCountId).getPk();
				if (ballotCountPk != null) {
					ballotCount.setBallotCountRef(new BallotCountRef(ballotCountPk));
				}
			}
		}

		auditLogFinalVoteCount(finalVoteCount);
		voteCountStatusendringTrigger.fireEventForStatusendring(finalCount, countArea, mvElectionContest, CountStatus.NEW, reportingUnit.reportingUnitTypeId());
		return finalCount;
	}

	private void auditLogFinalVoteCount(VoteCount finalVoteCount) {
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(FINAL, new VoteCountAuditDetails(finalVoteCount, true, true));
	}

	private Map<String, BallotRejection> initBallotRejectionMap(CountContext context) {
		List<BallotRejection> ballotRejections = ballotRejectionRepository.findBallotRejectionsByEarlyVoting(context.isEarlyVoting());
		Map<String, BallotRejection> ballotRejectionMap = new HashMap<>();
		for (BallotRejection ballotRejection : ballotRejections) {
			ballotRejectionMap.put(ballotRejection.getId(), ballotRejection);
		}
		return ballotRejectionMap;
	}
}
