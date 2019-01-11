package no.valg.eva.admin.counting.domain;

import java.util.Map;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.VoteCountBuilder;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.BaseCountService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;

/**
 * Domain service for handling VoteCount instances.
 */
public class VoteCountFactory extends BaseCountService {

	private final CountingCodeValueRepository countingCodeValueRepository;
	private final VoteCountService voteCountService;
	private final AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	public VoteCountFactory(ReportingUnitRepository reportingUnitRepository, ReportCountCategoryRepository reportCountCategoryRepository,
			VotingRepository votingRepository, ManualContestVotingRepository manualContestVotingRepository,
			CountingCodeValueRepository countingCodeValueRepository, VoteCountService voteCountService,
			AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService,
			VoteCountStatusendringTrigger voteCountStatusendringTrigger) {
		super(reportingUnitRepository, reportCountCategoryRepository, votingRepository, manualContestVotingRepository);

		this.countingCodeValueRepository = countingCodeValueRepository;
		this.voteCountService = voteCountService;
		this.antallStemmesedlerLagtTilSideDomainService = antallStemmesedlerLagtTilSideDomainService;
		this.voteCountStatusendringTrigger = voteCountStatusendringTrigger;
	}

	/**
	 * Creates new vote count for protocol count
	 */
	public VoteCount createProtocolVoteCount(ReportingUnit reportingUnit,
			ProtocolCount protocolCount, MvArea protocolCountArea,
			MvElection mvElectionContest, Affiliation blankAffiliation) {

		ContestReport contestReport = voteCountService.findContestReport(reportingUnit, mvElectionContest);
		if (contestReport == null) {
			contestReport = voteCountService.createContestReport(
					initContestReport(mvElectionContest.getContest(), reportingUnit));
		}

		no.valg.eva.admin.counting.domain.model.CountQualifier protocolCountQualifier = countingCodeValueRepository
				.findCountQualifierById(CountQualifier.PROTOCOL.getId());
		VoteCountCategory voteCountCategory = countingCodeValueRepository.findVoteCountCategoryById(CountCategory.VO.getId());
		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusById(protocolCount.getStatus().getId());

		VoteCount protocolVoteCount = new VoteCountBuilder()
				.applyArea(protocolCountArea)
				.applyVoteCountCategory(voteCountCategory)
				.applyVoteCountStatus(voteCountStatus)
				.applyCountQualifier(protocolCountQualifier)
				.applyProtocolCount(protocolCount)
				.build();
		contestReport.add(protocolVoteCount);

		// add ballot counts to vote counts
		if (protocolCount.getBlankBallotCount() != null) {
			protocolVoteCount.addNewBallotCount(blankAffiliation.getBallot(), protocolCount.getBlankBallotCount(), BLANK_MODIFIED_COUNT_ZERO);
		}

		return protocolVoteCount;
	}

	/**
	 * Creates preliminary count
	 */
	public VoteCount createPreliminaryVoteCount(ReportingUnit reportingUnit, CountContext context, PreliminaryCount aPreliminaryCount, MvArea countArea,
			MvElection mvElectionContest, Map<String, Ballot> ballotsForContest) {

		if (context.getCategory() == CountCategory.FO
				&& !antallStemmesedlerLagtTilSideDomainService.isAntallStemmesedlerLagtTilSideLagret(mvElectionContest, countArea.getMunicipality())) {
			throw new EvoteException("Antall stemmesedler lagt til side må settes først.");
		}

		ContestReport contestReport = voteCountService.findContestReport(reportingUnit, mvElectionContest);
		if (contestReport == null) {
			contestReport = voteCountService.createContestReport(
					initContestReport(mvElectionContest.getContest(), reportingUnit));
		}

		no.valg.eva.admin.counting.domain.model.CountQualifier countQualifier = countingCodeValueRepository.findCountQualifierById(CountQualifier.PRELIMINARY
				.getId());
		VoteCountCategory voteCountCategory = countingCodeValueRepository.findVoteCountCategoryById(context.getCategory().getId());
		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusById(aPreliminaryCount.getStatus().getId());

		VoteCount aVoteCount = new VoteCountBuilder()
				.applyArea(countArea)
				.applyVoteCountCategory(voteCountCategory)
				.applyVoteCountStatus(voteCountStatus)
				.applyCountQualifier(countQualifier)
				.applyPreliminaryCount(aPreliminaryCount)
				.build();

		aVoteCount.addNewBallotCount(ballotsForContest.get(EvoteConstants.BALLOT_BLANK), aPreliminaryCount.getBlankBallotCount(), BLANK_MODIFIED_COUNT_ZERO);
		for (final BallotCount ballotCount : aPreliminaryCount.getBallotCounts()) {
			aVoteCount.addNewBallotCount(
					ballotsForContest.get(ballotCount.getId()),
					ballotCount.getUnmodifiedCount(),
					ballotCount.getModifiedCount());
		}

		contestReport.add(aVoteCount);
		voteCountStatusendringTrigger.fireEventForStatusendring(
				aPreliminaryCount, countArea, mvElectionContest, CountStatus.NEW, reportingUnit.reportingUnitTypeId());
		return aVoteCount;
	}

}
