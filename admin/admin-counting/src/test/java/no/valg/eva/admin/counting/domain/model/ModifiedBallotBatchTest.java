package no.valg.eva.admin.counting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ModifiedBallotBatchTest {

	@Test
	public void memberWithSerialnumber_memberExists_memberWithGivenSerialnumberIsReturned() {
		ModifiedBallotBatch batch = new ModifiedBallotBatch();
		ModifiedBallotBatchMember member = new ModifiedBallotBatchMember();
		member.setSerialNumber(1);
		batch.addModifiedBallotBatchMember(member);

		assertThat(batch.memberWithSerialnumber(1)).isEqualTo(member);
	}

	@Test
	public void memberWithSerialnumber_memberDoesNotExist_nullIsReturned() {
		ModifiedBallotBatch batch = new ModifiedBallotBatch();
		ModifiedBallotBatchMember member = new ModifiedBallotBatchMember();
		member.setSerialNumber(1);
		batch.addModifiedBallotBatchMember(member);

		assertThat(batch.memberWithSerialnumber(2)).isNull();
	}

	@Test
	public void inProgressCount_whenBatchesInProgress_returnNonZeroInProgressCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		modifiedBallotBatch.getBatchMembers().add(new ModifiedBallotBatchMember());

		int inProgressCount = modifiedBallotBatch.inProgressCount();

		assertThat(inProgressCount).isEqualTo(1);
	}

	@Test
	public void inProgressCount_whenNoBatches_returnZeroInProgressCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();

		int inProgressCount = modifiedBallotBatch.inProgressCount();

		assertThat(inProgressCount).isEqualTo(0);
	}

	@Test
	public void inProgressCount_whenNoBatchesInProgress_returnZeroInProgressCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		ModifiedBallotBatchMember ballotBatchMember = new ModifiedBallotBatchMember();
		ballotBatchMember.setDone(true);
		modifiedBallotBatch.getBatchMembers().add(ballotBatchMember);

		int inProgressCount = modifiedBallotBatch.inProgressCount();

		assertThat(inProgressCount).isEqualTo(0);
	}

	@Test
	public void completedCount_whenBatchesCompleted_returnNonZeroCompletedCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		ModifiedBallotBatchMember ballotBatchMember = new ModifiedBallotBatchMember();
		ballotBatchMember.setDone(true);
		modifiedBallotBatch.getBatchMembers().add(ballotBatchMember);

		int completedCount = modifiedBallotBatch.completedCount();

		assertThat(completedCount).isEqualTo(1);
	}

	@Test
	public void completedCount_whenNoBatches_returnZeroCompletedCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();

		int completedCount = modifiedBallotBatch.completedCount();

		assertThat(completedCount).isEqualTo(0);
	}

	@Test
	public void completedCount_whenNoCompletedBatches_returnZeroCompletedCount() throws Exception {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		ModifiedBallotBatchMember ballotBatchMember = new ModifiedBallotBatchMember();
		modifiedBallotBatch.getBatchMembers().add(ballotBatchMember);

		int completedCount = modifiedBallotBatch.completedCount();

		assertThat(completedCount).isEqualTo(0);
	}
}
