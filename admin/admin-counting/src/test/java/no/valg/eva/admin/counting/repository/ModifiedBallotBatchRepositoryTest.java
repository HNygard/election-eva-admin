package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ModifiedBallotBatchRepositoryTest extends ModifiedBallotBatchTestSupport {

	private static final boolean DONE = true;
	private static final boolean IN_PROGRESS = false;
	private static final String BATCH_ID = "batch_id";

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		// make sure there are no-arg constructors
		new ModifiedBallotBatchRepository();
		setupTransactionSynchronizationRegistry();

		setUpRepositories();
		setupTestData();
	}

	@Test
	public void testActiveBatchesForOperator() throws Exception {
		List<ModifiedBallotBatch> modifiedBallotBatches = modifiedBallotBatchRepository.activeBatchesForOperator(operator);
		assertThat(modifiedBallotBatches).isEmpty();
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch(operator, ballotCount, MODIFIED_BALLOTS_PROCESS);
		ModifiedBallotBatchMember modifiedBallotBatchMember = new ModifiedBallotBatchMember();
		modifiedBallotBatchMember.setDone(false);
		modifiedBallotBatchMember.setCastBallot(new CastBallot(ballotCount, MODIFIED));
		modifiedBallotBatch.addModifiedBallotBatchMember(modifiedBallotBatchMember);
		modifiedBallotBatchRepository.createModifiedBallotBatch(userData, modifiedBallotBatch);
		modifiedBallotBatches = modifiedBallotBatchRepository.activeBatchesForOperator(operator);
		assertThat(modifiedBallotBatches).hasSize(1);
	}

	@Test
	public void testCreateBatchForOperator() throws Exception {
		assertThat(modifiedBallotBatchRepository
				.createModifiedBallotBatch(userData, new ModifiedBallotBatch(operator, ballotCount, MODIFIED_BALLOTS_PROCESS))).isNotNull();
	}

	@Test
	public void testAddModifiedBallotToBatch() throws Exception {
		ModifiedBallotBatch batch = createModifiedBallotBatch(false);
		Assert.assertEquals(batch.getId(), BATCH_ID);
	}

	private ModifiedBallotBatch createModifiedBallotBatch(boolean done) {
		ModifiedBallotBatch batch = new ModifiedBallotBatch(operator, ballotCount, MODIFIED_BALLOTS_PROCESS);
		batch = modifiedBallotBatchRepository.createModifiedBallotBatch(userData, batch);

		CastBallot castBallot = createModifiedCastBallot("0000", createBallotCount("00000", contestPk));
		ModifiedBallotBatchMember modifiedBallotBatchMember = createModifiedBallotBatchMember(done, castBallot, batch);
		batch.setId(BATCH_ID);
		batch.addModifiedBallotBatchMember(modifiedBallotBatchMember);
		return batch;
	}

	private ModifiedBallotBatchMember createModifiedBallotBatchMember(boolean done, CastBallot castBallot, ModifiedBallotBatch batch) {
		ModifiedBallotBatchMember modifiedBallotBatchMember = new ModifiedBallotBatchMember(castBallot);
		modifiedBallotBatchMember.setDone(done);
		batch.addModifiedBallotBatchMember(modifiedBallotBatchMember);
		return modifiedBallotBatchMember;
	}

	@Test
	public void findByBatchId_batchWithBatchIdExists_returnsBatch() {
		createModifiedBallotBatch(DONE);

		ModifiedBallotBatch result = modifiedBallotBatchRepository.findByBatchId(new BatchId(BATCH_ID));

		assertThat(result.getId()).isEqualTo(BATCH_ID);
	}

	@Test
	public void countModifiedBallotBatchForBallotCountPks_withInvalidPks_returnsZero() throws Exception {
		List<Long> pks = new ArrayList<>();
		pks.add(-1L);
		assertThat(modifiedBallotBatchRepository.countModifiedBallotBatchForBallotCountPks(pks)).isEqualTo(0);
	}

	@Test
	public void countModifiedBallotBatchForBallotCountPks_withValidPks_returnsCorrectCount() throws Exception {
		ModifiedBallotBatch batch1 = createModifiedBallotBatch(IN_PROGRESS);
		List<Long> pks = new ArrayList<>();
		pks.add(batch1.getBallotCount().getPk());
		assertThat(modifiedBallotBatchRepository.countModifiedBallotBatchForBallotCountPks(pks)).isEqualTo(1);
	}

}
