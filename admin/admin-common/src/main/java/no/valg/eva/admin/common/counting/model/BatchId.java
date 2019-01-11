package no.valg.eva.admin.common.counting.model;

import static java.lang.String.format;

import java.io.Serializable;

import org.apache.commons.lang3.Range;

/**
 */
public class BatchId implements Serializable {
	private final String batchId;

	public BatchId(String batchId) {
		this.batchId = batchId;
	}

	public static String createBatchId(String ballotCountId, Range<Integer> serialNumberRange) {
		return format("%1$s_%2$d_%3$d",
				ballotCountId,
				serialNumberRange.getMinimum(),
				serialNumberRange.getMaximum());
	}

	public String getId() {
		return batchId;
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BatchId batchId1 = (BatchId) o;

		if (!batchId.equals(batchId1.batchId)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return batchId.hashCode();
	}
}
