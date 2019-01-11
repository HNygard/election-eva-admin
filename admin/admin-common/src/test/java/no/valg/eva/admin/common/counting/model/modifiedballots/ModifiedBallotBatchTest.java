package no.valg.eva.admin.common.counting.model.modifiedballots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

public class ModifiedBallotBatchTest {
	
	private static final boolean DONE = true;
	private static final boolean NOT_DONE = false;

	@Test
	public void isPotentiallyDone_whenSomeModifiedBallotsAreNotDoneOrDoesNotHavePersonalVotesOrWriteIns_returnsFalse() {
		ModifiedBallotBatch modifiedBallotBatch = buildModifiedBallotBatch(withUnchangedUnmodifiedBallots());
		
		boolean isPotentiallyDone = modifiedBallotBatch.isDone();
		
		assertThat(isPotentiallyDone).isFalse();
	}

	@Test
	public void isPotentiallyDone_whenAllBallotsAreDone_returnsTrue() {
		ModifiedBallotBatch modifiedBallotBatch = buildModifiedBallotBatch(withDoneUnmodifiedBallots());
		
		boolean isPotentiallyDone = modifiedBallotBatch.isDone();
		
		assertThat(isPotentiallyDone).isTrue();
	}

	@Test
	public void isPotentiallyDone_whenAllBallotsHavePersonalVotesOrWriteIns_returnsTrue() {
		ModifiedBallotBatch modifiedBallotBatch = buildModifiedBallotBatch(withModifiedBallotsWithPersonalVotesOrWriteIns());

		boolean isPotentiallyDone = modifiedBallotBatch.isDone();

		assertThat(isPotentiallyDone).isTrue();
	}

	private ModifiedBallotBatch buildModifiedBallotBatch(List<ModifiedBallot> modifiedBallots) {
		return new ModifiedBallotBatch(null, modifiedBallots, null);
	}

	private List<ModifiedBallot> withDoneUnmodifiedBallots() {
		return buildModifiedBallots(withDoneModifiedBallot(), withDoneModifiedBallot(), withDoneModifiedBallot());
	}

	private List<ModifiedBallot> withUnchangedUnmodifiedBallots() {
		return buildModifiedBallots(withUnchangedModifiedBallot(), withUnchangedModifiedBallot(), withUnchangedModifiedBallot());
	}

	private List<ModifiedBallot> withModifiedBallotsWithPersonalVotesOrWriteIns() {
		return buildModifiedBallots(withPersonalVoteModifiedBallot(), withWriteInModifiedBallot(), withPersonalVoteAndWriteInModifiedBallot());
	}

	private List<ModifiedBallot> buildModifiedBallots(ModifiedBallot... modifiedBallotArguments) {
		List<ModifiedBallot> modifiedBallots = new ArrayList<>();
		Collections.addAll(modifiedBallots, modifiedBallotArguments);
		return modifiedBallots;
	}

	private ModifiedBallot withUnchangedModifiedBallot() {
		return new ModifiedBallot(null, 1, "someAffiliation", null, NOT_DONE);
	}

	private ModifiedBallot withDoneModifiedBallot() {
		return new ModifiedBallot(null, 1, "someAffiliation", null, DONE);
	}

	private ModifiedBallot withPersonalVoteAndWriteInModifiedBallot() {
		ModifiedBallot modifiedBallot = withUnchangedModifiedBallot();
		modifiedBallot.getPersonVotes().add(buildCandidate());
		return modifiedBallot;
	}

	private ModifiedBallot withWriteInModifiedBallot() {
		ModifiedBallot modifiedBallot = withUnchangedModifiedBallot();
		modifiedBallot.getWriteIns().add(buildCandidate());
		return modifiedBallot;
	}

	private ModifiedBallot withPersonalVoteModifiedBallot() {
		ModifiedBallot modifiedBallot = withUnchangedModifiedBallot();
		modifiedBallot.getPersonVotes().add(buildCandidate());
		modifiedBallot.getWriteIns().add(buildCandidate());
		return modifiedBallot;
	}

	private Candidate buildCandidate() {
		return new Candidate("someName", null, "somePartyName", 0);
	}
}
