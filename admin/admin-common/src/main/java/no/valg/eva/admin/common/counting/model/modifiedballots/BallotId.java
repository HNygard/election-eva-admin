package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;

public class BallotId implements Serializable {
	private final String id;

	public BallotId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BallotId)) {
			return false;
		}

		BallotId ballotId = (BallotId) o;

		if (id != null ? !id.equals(ballotId.id) : ballotId.id != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
