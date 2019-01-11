package no.valg.eva.admin.counting.domain.service.votecount;

import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.counting.builder.BallotCountBuilder;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;

public class PreliminaryCountDataProvider {
	private AffiliationRepository affiliationRepository;
	private VoteCountService voteCountService;
	private ReportingUnit reportingUnit;
	private VoteCount preliminaryVoteCount;
	private CountContext context;
	private CountCategory category;
	private MvElection contestMvElection;
	private MvArea countingMvArea;
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;

	public PreliminaryCountDataProvider(
			AffiliationRepository affiliationRepository, VoteCountService voteCountService, ReportingUnit reportingUnit,
			VoteCount preliminaryVoteCount, CountContext context, MvElection contestMvElection, MvArea countingMvArea,
			AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService) {
		this.affiliationRepository = affiliationRepository;
		this.voteCountService = voteCountService;
		this.reportingUnit = reportingUnit;
		this.preliminaryVoteCount = preliminaryVoteCount;
		this.context = context;
		this.category = context.getCategory();
		this.contestMvElection = contestMvElection;
		this.countingMvArea = countingMvArea;
		this.antallStemmesedlerLagtTilSideDomainService = antallStemmesedlerLagtTilSideDomainService;
	}

	public String id() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getId() : null;
	}

	public int version() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getAuditOplock() : 0;
	}

	public CountStatus status() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getCountStatus() : NEW;
	}

	public String comment() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getInfoText() : null;
	}

	public int blankBallotCount() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getBlankBallotCount() : 0;
	}

	public int questionableBallotCount() {
		return preliminaryVoteCount != null ? preliminaryVoteCount.getRejectedBallots() : 0;
	}

	public List<BallotCount> ballotCounts() {
		return preliminaryVoteCount != null ? ballotCountsFromPreliminaryVoteCount() : ballotCountsFromAffiliations();
	}

	private List<BallotCount> ballotCountsFromPreliminaryVoteCount() {
		List<BallotCount> ballotCounts = new ArrayList<>();
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap = preliminaryVoteCount.getBallotCountMap();
		ContestReport contestReport = preliminaryVoteCount.getContestReport();
		Contest contest = contestReport.getContest();
		Set<Ballot> sortedApprovedBallots = contest.getSortedApprovedBallots();
		for (Ballot ballot : sortedApprovedBallots) {
			if (ballot.isBlank()) {
				continue;
			}
			ballotCounts.add(ballotCount(ballotCountMap, ballot));
		}
		return ballotCounts;
	}

	private boolean isBlankBallot(Affiliation affiliation) {
		return affiliation.getBallot().isBlank();
	}

	private BallotCount ballotCount(Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap, Ballot ballot) {
		String ballotId = ballot.getId();
		if (ballotCountMap.containsKey(ballotId)) {
			return new BallotCountBuilder().applyEntity(ballotCountMap.get(ballotId)).build();
		} else {
			return new BallotCountBuilder().applyBallot(ballot).build();
		}
	}

	private List<BallotCount> ballotCountsFromAffiliations() {
		Long contestPk = contestMvElection.getContest().getPk();
		List<Affiliation> affiliations = affiliationRepository.findApprovedByContest(contestPk);
		List<BallotCount> ballotCounts = new ArrayList<>();
		for (Affiliation affiliation : affiliations) {
			if (isBlankBallot(affiliation)) {
				continue;
			}
			BallotCount ballotCount = new BallotCountBuilder().applyAffiliation(affiliation).build();
			ballotCounts.add(ballotCount);
		}
		return ballotCounts;
	}

	public Integer lateValidationCovers() {
		if (category == FO || category == CountCategory.FS) {
			Municipality municipality = countingMvArea.getMunicipality();
			AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = antallStemmesedlerLagtTilSideDomainService.hentAntallStemmesedlerLagtTilSide(municipality);
			Contest contest = contestMvElection.getContest();
			if (contest.isOnBoroughLevel()) {
				return antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgdistrikt(contest);
			}
			return antallStemmesedlerLagtTilSide.getTotaltAntallStemmesedlerLagtTilSideForValg();
		}
		return null;
	}

	public boolean isAntallStemmesedlerLagtTilSideLagret() {
		return antallStemmesedlerLagtTilSideDomainService.isAntallStemmesedlerLagtTilSideLagret(contestMvElection, countingMvArea.getMunicipality());
	}

	public Integer expectedBallotCount() {
		if (category != FO || !isOnTechnicalPollingDistrict()) {
			return null;
		}
		if (preliminaryVoteCount != null && preliminaryVoteCount.getTechnicalVotings() != null) {
			return preliminaryVoteCount.getTechnicalVotings();
		}
		return 0;
	}

	private boolean isOnTechnicalPollingDistrict() {
		return voteCountService.countingMode(context, countingMvArea.getMunicipality(), contestMvElection) == BY_TECHNICAL_POLLING_DISTRICT;
	}

	public Integer markOffCount() {
		if (category != CountCategory.VO) {
			Long markOffCount = voteCountService.markOffCountForPreliminaryCount(context, contestMvElection, countingMvArea, category);
			return markOffCount != null ? markOffCount.intValue() : null;
		}
		return null;
	}

	public Integer totalBallotCountForOtherPollingDistricts() {
		if (category == FO && isOnTechnicalPollingDistrict() && markOffCount() != null) {
			ElectionPath contestPath = ElectionPath.from(contestMvElection.getPath());
			List<VoteCount> voteCounts = voteCountService
					.findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(reportingUnit, contestPath, FO);
			return totalBallotCountForVoteCountsOtherThanCurrentMvArea(voteCounts, countingMvArea);
		}
		return null;
	}

	private int totalBallotCountForVoteCountsOtherThanCurrentMvArea(List<VoteCount> voteCounts, MvArea currentMvArea) {
		int totalBallotCountForOtherPollingDistricts = 0;
		for (VoteCount voteCount : voteCounts) {
			if (voteCount.getMvArea().equals(currentMvArea)) {
				continue;
			}
			int rejectedBallots = voteCount.getRejectedBallots();
			int approvedBallots = voteCount.getApprovedBallots();
			totalBallotCountForOtherPollingDistricts += rejectedBallots + approvedBallots;
		}
		return totalBallotCountForOtherPollingDistricts;
	}
}
