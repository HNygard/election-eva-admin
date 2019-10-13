package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.counting.model.BatchId.createBatchId;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.exception.ModifiedBallotBatchCreationFailed;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.counting.builder.ModifiedBallotBatchBuilder;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.domain.modifiedballots.ModifiedBallotDomainService;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ModifiedBallotBatchService")
@Default
@Remote(ModifiedBallotBatchService.class)
public class ModifiedBallotBatchApplicationService implements ModifiedBallotBatchService {
	@Inject
	protected ModifiedBallotBatchRepository modifiedBallotBatchRepository;
	@Inject
	protected ModifiedBallotDomainService modifiedBallotDomainService;
	@Inject
	protected ContestReportRepository contestReportRepository;
	@Inject
	protected CandidateRepository candidateRepository;
	@Inject
	protected ContestRepository contestRepository;

	@Override
	@Security(accesses = Opptelling_Rettelser_Rediger, type = WRITE)
	public no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch createModifiedBallotBatch(
			UserData userData, BallotCount ballotCount, int noOfModifiedBallotsInBatch, ModifiedBallotBatchProcess process) {
		Operator operator = userData.getOperator();

		ContestReport contestReport = contestReportRepository.findByBallotCount(ballotCount.getBallotCountRef());
		no.valg.eva.admin.counting.domain.model.BallotCount count = contestReport.getBallotCount(ballotCount.getBallotCountRef());
		ModifiedBallotBatch modifiedBallotBatch = createModifiedBallotBatch(userData, ballotCount, noOfModifiedBallotsInBatch, operator, count, process);
		List<Candidate> personalVoteCandidates = candidateRepository.findByAffiliation(count.getBallot().getAffiliation().getPk());
		List<Candidate> writeInCandidates = candidateRepository.findCandidatesForOtherApprovedBallotsInSameContest(count.getBallot().getPk());

		return new ModifiedBallotBatchBuilder().fromEntity(modifiedBallotBatch, electionConfigurationFor(contestReport.getContest()))
				.withPersonalVoteCandidates(personalVoteCandidates)
				.withWriteInCandidates(writeInCandidates)
				.build();
	}

	private ModifiedBallotConfiguration electionConfigurationFor(Contest contest) {
		Election election = contest.getElection();
		return new ModifiedBallotConfiguration(election.isRenumber(), election.isRenumberLimit(), election.isWritein(), election.isStrikeout(),
				election.isPersonal(), contest.getMaxWriteIn(), contest.getMaxRenumber());
	}

	private ModifiedBallotBatch createModifiedBallotBatch(
			UserData userData, BallotCount ballotCount, int noOfModifiedBallotsInBatch, Operator operator,
			no.valg.eva.admin.counting.domain.model.BallotCount count, ModifiedBallotBatchProcess process) {
		ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatchRepository.createModifiedBallotBatch(userData,
				new ModifiedBallotBatch(operator, count, process));
		ModifiedBallotsStatus modifiedBallotCount = modifiedBallotDomainService.buildModifiedBallotsStatus(count, ballotCount, process);
		verifyRequest(noOfModifiedBallotsInBatch, modifiedBallotCount);
		createModifiedBallotBatchMembers(modifiedBallotBatch, noOfModifiedBallotsInBatch, count);
		modifiedBallotBatch.setId(createBatchId(count.getPk().toString(), modifiedBallotBatch.getSerialNumberRange()));
		return modifiedBallotBatch;
	}

	private void verifyRequest(int noOfModifiedBallotsInBatch, ModifiedBallotsStatus modifiedBallotCount) {
		int numberOfAvailableModifiedBallots = modifiedBallotCount.getRemaining();
		verifyThatNumberOfBallotsIsAPositiveInteger(noOfModifiedBallotsInBatch, numberOfAvailableModifiedBallots);
		verifyThatThereAreEnoughRemainingBallotsForBatch(noOfModifiedBallotsInBatch, numberOfAvailableModifiedBallots);
	}

	private void verifyThatNumberOfBallotsIsAPositiveInteger(int noOfModifiedBallotsInBatch, int numberOfAvailableModifiedBallots) {
		if (noOfModifiedBallotsInBatch < 1) {
			throw new ModifiedBallotBatchCreationFailed(noOfModifiedBallotsInBatch, numberOfAvailableModifiedBallots);
		}
	}

	private void verifyThatThereAreEnoughRemainingBallotsForBatch(int noOfModifiedBallotsInBatch, int numberOfAvailableModifiedBallots) {
		if (numberOfAvailableModifiedBallots < noOfModifiedBallotsInBatch) {
			throw new ModifiedBallotBatchCreationFailed(noOfModifiedBallotsInBatch, numberOfAvailableModifiedBallots);
		}
	}

	private void createModifiedBallotBatchMembers(ModifiedBallotBatch modifiedBallotBatch, int noOfModifiedBallotsInBatch,
			no.valg.eva.admin.counting.domain.model.BallotCount ballotCount) {
		int highestSerialNumberForBallotCount = modifiedBallotBatchRepository.findHighestBatchMemberSerialNumberForBallotCount(new BallotCountRef(ballotCount
				.getPk()));
		for (int i = 0; i < noOfModifiedBallotsInBatch; i++) {
			CastBallot modifiedCastBallot = new CastBallot(ballotCount, MODIFIED);
			ModifiedBallotBatchMember batchMember = new ModifiedBallotBatchMember(modifiedCastBallot, false, highestSerialNumberForBallotCount + i + 1);
			modifiedBallotBatch.addModifiedBallotBatchMember(batchMember);
		}
	}

	@Override
	@Security(accesses = Opptelling_Rettelser_Rediger, type = READ)
	public no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch findActiveBatchByBatchId(
            UserData userData,
			BatchId modifiedBallotBatchId) {
		
        List<ModifiedBallotBatch> modifiedBallotBatches = modifiedBallotBatchRepository.activeBatchesForOperator(userData.getOperator());
		for (ModifiedBallotBatch modifiedBallotBatch : modifiedBallotBatches) {
			if (modifiedBallotBatch.getId().equals(modifiedBallotBatchId.getId())) {
				Contest contest = modifiedBallotBatch.getBallotCount().getVoteCount().getContestReport().getContest();
				ModifiedBallotBatchBuilder modifiedBallotBatchBuilder = new ModifiedBallotBatchBuilder().fromEntity(modifiedBallotBatch,
						electionConfigurationFor(contest));
				if (!modifiedBallotBatch.getBatchMembers().isEmpty()) {
					Long affPk = modifiedBallotBatch.getBatchMembers().iterator().next().getCastBallot().getBallotCount().getBallot().getAffiliation()
							.getPk();
					Long ballotPk = modifiedBallotBatch.getBatchMembers().iterator().next().getCastBallot().getBallotCount().getBallot().getPk();
					List<Candidate> personalVoteCandidates = candidateRepository.findByAffiliation(affPk);
					List<Candidate> writeInCandidates = candidateRepository.findCandidatesForOtherApprovedBallotsInSameContest(ballotPk);
					modifiedBallotBatchBuilder = modifiedBallotBatchBuilder
							.withPersonalVoteCandidates(personalVoteCandidates)
							.withWriteInCandidates(writeInCandidates);
				}
				return modifiedBallotBatchBuilder.build();
			}
		}
		throw new EvoteException("Unknown batchId: " + modifiedBallotBatchId);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public boolean hasModifiedBallotBatchForBallotCountPks(UserData userData, List<BallotCountRef> ballotCountPks) {
		List<Long> pks = new ArrayList<>();
		for (BallotCountRef ref : ballotCountPks) {
			pks.add(ref.getPk());
		}
		return modifiedBallotBatchRepository.countModifiedBallotBatchForBallotCountPks(pks) > 0;
	}

	@Override
	@Security(accesses = {Aggregert_Opptelling_Rediger, Opptelling_Rettelser_Rediger}, type = READ)
	public List<ModifiedBallotsStatus> buildModifiedBallotStatuses(UserData userData, FinalCount finalCount, ModifiedBallotBatchProcess process) {
		return modifiedBallotDomainService.buildModifiedBallotsStatuses(finalCount, userData.getOperator(), process);
	}
}
