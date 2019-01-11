package no.valg.eva.admin.counting.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Ballot;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;

public class ModifiedBallotBatchBuilder {
	private ModifiedBallotBatch modifiedBallotBatch;

	public static Candidate mapCandidateEntityToViewModel(no.valg.eva.admin.configuration.domain.model.Candidate candidate) {
		String partyName = candidate.getAffiliation().getParty().getName();
		return new Candidate(candidate.getNameLine(), new CandidateRef(candidate.getPk()), partyName, candidate.getDisplayOrder());
	}

	public ModifiedBallotBatchBuilder fromEntity(
            no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch entity,
			ModifiedBallotConfiguration modifiedBallotConfiguration) {

		Ballot ballot = new Ballot(new BallotId(entity.getBallotCount().getBallotId()), modifiedBallotConfiguration);
		List<ModifiedBallot> modifiedBallots = new ArrayList<>();
		for (ModifiedBallotBatchMember batchMember : entity.getBatchMembers()) {
			boolean done = batchMember.isDone();
			ModifiedBallot modifiedBallot = new ModifiedBallot(new BatchId(entity.getId()), batchMember.getSerialNumber(), nameOfMember(batchMember),
					ballot.getBallotId(), done);
			modifiedBallots.add(modifiedBallot);
		}
		modifiedBallotBatch = new ModifiedBallotBatch(new BatchId(entity.getId()), modifiedBallots, ballot);
		return this;
	}

	public ModifiedBallotBatchBuilder withPersonalVoteCandidates(Collection<no.valg.eva.admin.configuration.domain.model.Candidate> candidates) {
		for (no.valg.eva.admin.configuration.domain.model.Candidate candidate : candidates) {
			modifiedBallotBatch.addPersonalVoteCandidateFor(mapCandidateEntityToViewModel(candidate));
		}
		return this;
	}

	public ModifiedBallotBatchBuilder withWriteInCandidates(Collection<no.valg.eva.admin.configuration.domain.model.Candidate> candidates) {
		for (no.valg.eva.admin.configuration.domain.model.Candidate candidate : candidates) {
			modifiedBallotBatch.addWriteInCandidateFor(mapCandidateEntityToViewModel(candidate));
		}
		return this;
	}

	private String nameOfMember(ModifiedBallotBatchMember member) {
		return member.partyName();
	}

	public ModifiedBallotBatch build() {
		return modifiedBallotBatch;
	}
}
