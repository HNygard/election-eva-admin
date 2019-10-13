package no.valg.eva.admin.counting.application.modifiedballots;

import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.personal;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.renumber;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.strikeout;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.writein;
import static no.valg.eva.admin.counting.builder.ModifiedBallotBatchBuilder.mapCandidateEntityToViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ModifiedBallotAuditEvent;
import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Ballot;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CastBallotRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallots;
import no.valg.eva.admin.common.counting.service.ModifiedBallotService;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ModifiedBallotService")


@Default
@Remote(ModifiedBallotService.class)
public class ModifiedBallotApplicationService implements ModifiedBallotService {
	@Inject
	private ModifiedBallotBatchRepository modifiedBallotBatchRepository;
	@Inject
	private CountingCodeValueRepository countingCodeValueRepository;
	@Inject
	private CandidateRepository candidateRepository;
	@Inject
	private ContestReportRepository contestReportRepository;

	public ModifiedBallotApplicationService() {
	}

	public ModifiedBallotApplicationService(ModifiedBallotBatchRepository modifiedBallotBatchRepository,
			CountingCodeValueRepository countingCodeValueRepository,
			CandidateRepository candidateRepository,
			ContestReportRepository contestReportRepository) {
		this.modifiedBallotBatchRepository = modifiedBallotBatchRepository;
		this.countingCodeValueRepository = countingCodeValueRepository;
		this.candidateRepository = candidateRepository;
		this.contestReportRepository = contestReportRepository;
	}

	@Override
	@Security(accesses = Accesses.Opptelling_Rettelser_Rediger, type = READ)
	public ModifiedBallots modifiedBallotsFor(UserData userData, BallotCountRef ballotCountRef, ModifiedBallotBatchProcess processFilter) {
		List<ModifiedBallot> modifiedBallots = new ArrayList<>();
		ContestReport contestReport = contestReportRepository.findByBallotCount(ballotCountRef);

		BallotCount ballotCount = contestReport.getBallotCount(ballotCountRef);
		for (CastBallot castBallot : ballotCount.getModifiedCastBallots()) {
			ModifiedBallotBatchProcess process = castBallot.getModifiedBallotBatchMember().getModifiedBallotBatch().getProcess();
			if (process == processFilter) {
				modifiedBallots.add(mapToModifiedBallot(castBallot, ballotCount.getBallot()));
			}
		}

		ModifiedBallotConfiguration modifiedBallotConfiguration = electionConfigurationFor(contestReport.getContest());

		List<no.valg.eva.admin.configuration.domain.model.Candidate> candidatesOnBallot = candidateRepository
				.findByAffiliation(ballotCount.getBallot().getAffiliation().getPk());
		List<no.valg.eva.admin.configuration.domain.model.Candidate> writeInCandidates = candidateRepository
				.findCandidatesForOtherApprovedBallotsInSameContest(ballotCount.getBallot()
						.getPk());

		return new ModifiedBallots(modifiedBallots, createBallot(new BallotId(ballotCount.getBallotId()), candidatesOnBallot,
				writeInCandidates, modifiedBallotConfiguration));
	}

	private ModifiedBallotConfiguration electionConfigurationFor(Contest contest) {
		Election election = contest.getElection();
		return new ModifiedBallotConfiguration(election.isRenumber(), election.isRenumberLimit(), election.isWritein(), election.isStrikeout(),
				election.isPersonal(), contest.getMaxWriteIn(), contest.getMaxRenumber());
	}

	private Ballot createBallot(
			BallotId ballotId,
			List<no.valg.eva.admin.configuration.domain.model.Candidate> candidatesOnBallot,
			List<no.valg.eva.admin.configuration.domain.model.Candidate> writeInCandidates,
			ModifiedBallotConfiguration modifiedBallotConfiguration) {

		Ballot ballot = new Ballot(ballotId, modifiedBallotConfiguration);
		for (no.valg.eva.admin.configuration.domain.model.Candidate candidate : candidatesOnBallot) {
			ballot.addForPersonalVote(mapCandidateEntityToViewModel(candidate));
		}
		for (no.valg.eva.admin.configuration.domain.model.Candidate candidate : writeInCandidates) {
			ballot.getCandidatesForWriteIn().add(mapCandidateEntityToViewModel(candidate));
		}
		return ballot;
	}

	private ModifiedBallot mapToModifiedBallot(CastBallot castBallot, no.valg.eva.admin.configuration.domain.model.Ballot ballot) {
		ModifiedBallotBatchMember member = castBallot.getModifiedBallotBatchMember();
		ModifiedBallot result = new ModifiedBallot(
				new BatchId(member.getModifiedBallotBatch().getId()), member.getSerialNumber(),
				ballot.getAffiliation().partyName(), new BallotId(ballot.getId()), true);
		addCandidatesOn(result, castBallot.getCandidateVotes());
		return result;
	}

	@Override
	@Security(accesses = Accesses.Opptelling_Rettelser_Rediger, type = READ)
	public ModifiedBallot load(UserData userData, ModifiedBallot modifiedBallotRef) {

		ModifiedBallot modifiedBallot = new ModifiedBallot(modifiedBallotRef.getBatchId(), modifiedBallotRef.getSerialNumber(),
				modifiedBallotRef.getAffiliation(),
				modifiedBallotRef.getBallotId(), modifiedBallotRef.isDone());

		CastBallot castBallot = updateBatchMemberAndFindModifiedBallotEnityFor(modifiedBallotRef);
		addCandidatesOn(modifiedBallot, castBallot.getCandidateVotes());
		if (castBallot.getBinaryData() != null) {
			modifiedBallot.setCastBallotRef(new CastBallotRef(castBallot.getBinaryData().getPk(), castBallot.getId()));
		}
		return modifiedBallot;
	}

	private void addCandidatesOn(ModifiedBallot result, Set<CandidateVote> candidateVotes) {
		for (CandidateVote candidateVote : candidateVotes) {
			Candidate candidate = mapCandidateEntityToViewModel(candidateVote.getCandidate());
			if (candidateVote.isWriteIn()) {
				result.addWriteInFor(candidate);
			} else {
				if (candidateVote.isPersonalVote()) {
					candidate.setPersonalVote(true);
				}
				if (candidateVote.isRenumbering()) {
					candidate.setRenumberPosition(candidateVote.getRenumberPosition());
				}
				if (candidateVote.isStrikeOut()) {
					candidate.setStrikedOut(true);
				}
				result.addPersonVotesFor(candidate);
			}
		}
	}

	@Override
	@Security(accesses = Accesses.Opptelling_Rettelser_Rediger, type = WRITE)
	@AuditLog(eventClass = ModifiedBallotAuditEvent.class, eventType = AuditEventTypes.Update)
	public void update(UserData userData, ModifiedBallot modifiedBallot) {
		CastBallot entity = updateBatchMemberAndFindModifiedBallotEnityFor(modifiedBallot);

		Set<CandidateVote> candidateVotes = candidateVotesFor(modifiedBallot.personalVotes(), personal);
		candidateVotes.addAll(candidateVotesFor(modifiedBallot.getWriteIns(), writein));
		candidateVotes.addAll(candidateVotesFor(modifiedBallot.renumberings(), renumber));
		candidateVotes.addAll(candidateVotesFor(modifiedBallot.strikeOuts(), strikeout));

		entity.updateCandidateVotes(candidateVotes);
	}

	private CastBallot updateBatchMemberAndFindModifiedBallotEnityFor(ModifiedBallot modifiedBallot) {
		ModifiedBallotBatch batch = modifiedBallotBatchRepository.findByBatchId(modifiedBallot.getBatchId());
		ModifiedBallotBatchMember batchMember = batch.memberWithSerialnumber(modifiedBallot.getSerialNumber());
		batchMember.setDone(true);
		return batchMember.getCastBallot();
	}

	private Set<CandidateVote> candidateVotesFor(Set<Candidate> candidates, VoteCategory.VoteCategoryValues voteCategoryId) {
		Set<CandidateVote> candidateVotes = new HashSet<>();
		for (Candidate candidate : candidates) {
			CandidateVote candidateVote;
			if (voteCategoryId.equals(renumber)) {
				candidateVote = new CandidateVote(candidateEntityFor(candidate), voteCategoryFor(voteCategoryId), null, candidate.getRenumberPosition());
			} else {
				candidateVote = new CandidateVote(candidateEntityFor(candidate), voteCategoryFor(voteCategoryId), null, null);
			}
			candidateVotes.add(candidateVote);
		}
		return candidateVotes;
	}

	private VoteCategory voteCategoryFor(VoteCategory.VoteCategoryValues voteCategoryId) {
		return countingCodeValueRepository.findVoteCategoryById(voteCategoryId);
	}

	private no.valg.eva.admin.configuration.domain.model.Candidate candidateEntityFor(Candidate candidate) {
		return candidateRepository.findCandidateByPk(candidate.getCandidateRef().getPk());
	}
}
