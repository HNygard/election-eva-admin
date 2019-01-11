package no.valg.eva.admin.common.counting.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.apache.commons.lang3.Range;
import org.testng.annotations.Test;

public class BatchIdTest {
	private static final String BALLOT_COUNT_PK = "77777";
	private static final String BATCH_ID = "7777_1_10";

	@Test
	public void testCreateBatchId() throws Exception {
		
		assertEquals(BatchId.createBatchId(BALLOT_COUNT_PK, Range.between(1, 10)), BALLOT_COUNT_PK + "_1_10");
		
	}

	@Test
	public void testGetId() throws Exception {
		assertEquals(new BatchId(BATCH_ID).getId(), BATCH_ID);
	}

	@Test
	public void testToString() throws Exception {
		assertEquals(new BatchId(BATCH_ID).toString(), BATCH_ID);
	}

	@Test
	public void testEquals() throws Exception {
		assertTrue(new BatchId(BATCH_ID).equals(new BatchId(BATCH_ID)));
	}
}
