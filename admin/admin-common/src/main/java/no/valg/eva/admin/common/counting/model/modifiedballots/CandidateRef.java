package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;

public class CandidateRef implements Serializable {
	private long pk;

	public CandidateRef(long pk) {
		this.pk = pk;
	}

	public long getPk() {
		return pk;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		
		CandidateRef that = (CandidateRef) o;

		if (pk != that.pk) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (pk ^ (pk >>> 32));
	}
}
