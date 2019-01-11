package no.valg.eva.admin.counting.domain.service.votecount;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.FinalCountBuilder;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;

public class FindCountService {
	private static final boolean MANUAL_COUNT_TRUE = true;

	private VoteCountService voteCountService;
	private ReportingUnitRepository reportingUnitRepository;
	private AffiliationRepository affiliationRepository;
	private BallotRejectionRepository ballotRejectionRepository;
	private ReportingUnitDomainService reportingUnitDomainService;
	private MvElectionRepository mvElectionRepository;
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;

	@Inject
	public FindCountService(VoteCountService voteCountService, ReportingUnitRepository reportingUnitRepository,
			AffiliationRepository affiliationRepository, BallotRejectionRepository ballotRejectionRepository,
			ReportingUnitDomainService reportingUnitDomainService, MvElectionRepository mvElectionRepository,
			AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService) {
		this.voteCountService = voteCountService;
		this.reportingUnitRepository = reportingUnitRepository;
		this.affiliationRepository = affiliationRepository;
		this.ballotRejectionRepository = ballotRejectionRepository;
		this.reportingUnitDomainService = reportingUnitDomainService;
		this.mvElectionRepository = mvElectionRepository;
		this.antallStemmesedlerLagtTilSideDomainService = antallStemmesedlerLagtTilSideDomainService;
	}

	/**
	 * Finds county final counts.
	 */
	public List<FinalCount> findCountyFinalCounts(AreaPath operatorAreaPath, MvElection contestMvElection, MvArea countingMvArea, CountCategory category) {
		boolean samiElection = isSamiElection(contestMvElection);
		if (contestMvElection.getContest().isOnCountyLevel() || samiElection) {
			AreaPath areaPathForReportingUnit = reportingUnitDomainService.areaPathForFindingReportingUnit(
					samiElection ? OPPTELLINGSVALGSTYRET : FYLKESVALGSTYRET,
					operatorAreaPath, countingMvArea);
			ReportingUnit reportingUnit = samiElection ? reportingUnitRepository.byAreaPathElectionPathAndType(areaPathForReportingUnit,
					ElectionPath.from(contestMvElection.getElectionPath()), OPPTELLINGSVALGSTYRET)
					: reportingUnitRepository.findByAreaPathAndType(areaPathForReportingUnit, FYLKESVALGSTYRET);
			return findFinalCounts(reportingUnit, contestMvElection, countingMvArea, category);
		}
		return emptyList();
	}

	private boolean isSamiElection(MvElection contestMvElection) {
		return !contestMvElection.getContest().isSingleArea();
	}

	/**
	 * Finds municipality final counts.
	 */
	public List<FinalCount> findMunicipalityFinalCounts(AreaPath operatorAreaPath, MvElection contestMvElection, MvArea countingMvArea,
			CountCategory category) {
		AreaPath areaPathForReportingUnit = reportingUnitDomainService.areaPathForFindingReportingUnit(VALGSTYRET, operatorAreaPath, countingMvArea);
		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(areaPathForReportingUnit, VALGSTYRET);
		return findFinalCounts(reportingUnit, contestMvElection, countingMvArea, category);
	}

	private List<FinalCount> findFinalCounts(ReportingUnit reportingUnit, MvElection contestMvElection, MvArea countingMvArea, CountCategory category) {
		Contest contest = contestMvElection.getContest();
		String areaName = countingMvArea.getAreaName();

		AreaPath countingAreaPath = new AreaPath(countingMvArea.getPath());
		List<VoteCount> finalVoteCounts = voteCountService.findFinalVoteCounts(reportingUnit, contestMvElection, countingMvArea, category);
		List<FinalCount> finalCounts = buildFinalCounts(reportingUnit, finalVoteCounts);

		if (finalCounts.isEmpty()) {
			List<Affiliation> affiliations = affiliationRepository.findApprovedByContest(contest.getPk());
			FinalCount finalCount = new FinalCountBuilder(category, countingAreaPath, areaName, MANUAL_COUNT_TRUE, reportingUnit).build();
			finalCount.setBallotCounts(ballotCounts(affiliations));
			finalCount.setRejectedBallotCounts(rejectedBallotCounts(category.isEarlyVoting()));
			finalCounts.add(finalCount);
		}

		return finalCounts;
	}

	private List<BallotCount> ballotCounts(List<Affiliation> affiliations) {
		List<BallotCount> ballotCounts = new ArrayList<>();
		for (Affiliation affiliation : affiliations) {
			if (affiliation.getBallot().isBlank()) {
				continue;
			}
			String id = affiliation.getBallot().getId();
			String name = affiliation.getParty().getName();
			BallotCount ballotCount = new BallotCount();
			ballotCount.setId(id);
			ballotCount.setName(name);
			ballotCounts.add(ballotCount);
		}
		return ballotCounts;
	}

	public List<? extends Count> findMunicipalityCountsByStatus(AreaPath valgstyretAreaPath, ElectionPath contestPath, CountStatus countStatus) {
		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(valgstyretAreaPath, VALGSTYRET);

		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		boolean penultimateRecount = contestMvElection.getContest().isContestOrElectionPenultimateRecount();
		if (penultimateRecount) {
			List<VoteCount> finalVoteCounts = voteCountService.findVoteCountsFor(reportingUnit, contestPath, countStatus, FINAL);
			return buildFinalCounts(reportingUnit, finalVoteCounts);
		}
		List<VoteCount> preliminaryVoteCounts = voteCountService.findVoteCountsFor(reportingUnit, contestPath, countStatus, PRELIMINARY);

		return buildPreliminaryCounts(reportingUnit, preliminaryVoteCounts, contestMvElection);
	}

	public List<FinalCount> findCountyFinalCountsByStatus(AreaPath operatorAreaPath, ElectionPath contestPath, CountStatus countStatus,
			MvElection contestMvElection) {
		ReportingUnit reportingUnit = reportingUnitDomainService.reportingUnitForCountyFinalCount(operatorAreaPath, operatorAreaPath, contestMvElection);
		List<VoteCount> finalVoteCounts = voteCountService.findVoteCountsFor(reportingUnit, contestPath, countStatus, FINAL);
		return buildFinalCounts(reportingUnit, finalVoteCounts);
	}

	private List<PreliminaryCount> buildPreliminaryCounts(ReportingUnit reportingUnit, List<VoteCount> voteCounts, MvElection mvElectionContest) {
		return voteCounts.stream()
				.map(voteCount -> buildPreliminaryCount(reportingUnit, voteCount, mvElectionContest))
				.collect(Collectors.toList());
	}

	private PreliminaryCount buildPreliminaryCount(ReportingUnit reportingUnit, VoteCount voteCount, MvElection contestMvElection) {
		String voteCountCategoryId = voteCount.getVoteCountCategoryId();
		MvArea countingMvArea = voteCount.getMvArea();
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());
		String areaName = countingMvArea.getAreaName();
		CountCategory countCategory = CountCategory.fromId(voteCountCategoryId);

		CountContext context = new CountContext(contestMvElection.electionPath(), countCategory);

		PreliminaryCountDataProvider preliminaryCountDataProvider = new PreliminaryCountDataProvider(affiliationRepository, voteCountService, reportingUnit,
				voteCount, context, contestMvElection, countingMvArea, antallStemmesedlerLagtTilSideDomainService);

		PreliminaryCount preliminaryCount = new PreliminaryCount(voteCount.getId(), countingAreaPath, countCategory, areaName, "", voteCount.isManualCount(),
				preliminaryCountDataProvider.blankBallotCount());
		preliminaryCount.setBallotCounts(preliminaryCountDataProvider.ballotCounts());

		return preliminaryCount;
	}

	private List<FinalCount> buildFinalCounts(ReportingUnit reportingUnit, List<VoteCount> finalVoteCounts) {
		List<FinalCount> finalCounts = new ArrayList<>();
		for (VoteCount finalVoteCount : finalVoteCounts) {
			FinalCount finalCount = buildFinalCount(reportingUnit, finalVoteCount);
			finalCounts.add(finalCount);
		}
		// Order final counts by index (which is calculated based on id)
		finalCounts.sort(comparingInt(FinalCount::getIndex));
		return finalCounts;
	}

	private FinalCount buildFinalCount(ReportingUnit reportingUnit, VoteCount finalVoteCount) {
		String voteCountCategoryId = finalVoteCount.getVoteCountCategoryId();
		MvArea countingMvArea = finalVoteCount.getMvArea();
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());
		String areaName = countingMvArea.getAreaName();
		boolean earlyVoting = finalVoteCount.getVoteCountCategory().isEarlyVoting();
		FinalCount finalCount = new FinalCountBuilder(CountCategory.fromId(voteCountCategoryId), countingAreaPath, areaName,
				finalVoteCount.isManualCount(), reportingUnit)
						.applyBallotRejections(ballotRejectionRepository.findBallotRejectionsByEarlyVoting(earlyVoting))
						.applyFinalVoteCount(finalVoteCount)
						.build();
		finalCount.setVoteCountPk(finalVoteCount.getPk());
		return finalCount;
	}

	private List<RejectedBallotCount> rejectedBallotCounts(boolean earlyVoting) {
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		List<BallotRejection> ballotRejections = ballotRejectionRepository.findBallotRejectionsByEarlyVoting(earlyVoting);
		for (BallotRejection ballotRejection : ballotRejections) {
			String id = ballotRejection.getId();
			String name = ballotRejection.getName();
			RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();
			rejectedBallotCount.setId(id);
			rejectedBallotCount.setName(name);
			rejectedBallotCounts.add(rejectedBallotCount);
		}
		return rejectedBallotCounts;
	}

	public FinalCount findApprovedCountyFinalCount(AreaPath operatorAreaPath, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		ReportingUnit reportingUnit = reportingUnitDomainService.reportingUnitForCountyFinalCount(operatorAreaPath,
				AreaPath.from(countingMvArea.getAreaPath()), contestMvElection);
		return findApprovedFinalCount(reportingUnit, context, countingMvArea, contestMvElection);
	}

	public FinalCount findApprovedMunicipalityFinalCount(AreaPath operatorAreaPath, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		AreaPath areaPathForReportingUnit = reportingUnitDomainService.areaPathForFindingReportingUnit(VALGSTYRET, operatorAreaPath, countingMvArea);
		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(areaPathForReportingUnit, VALGSTYRET);
		return findApprovedFinalCount(reportingUnit, context, countingMvArea, contestMvElection);
	}

	private FinalCount findApprovedFinalCount(ReportingUnit reportingUnit, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		VoteCount approvedFinalVoteCount = voteCountService.findApprovedFinalVoteCount(reportingUnit, context, countingMvArea, contestMvElection);
		if (approvedFinalVoteCount == null) {
			return null;
		}
		return buildFinalCount(reportingUnit, approvedFinalVoteCount);
	}

}
