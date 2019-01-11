package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

public final class BallotRejectionId implements Serializable {
	private final String id;

	public BallotRejectionId(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BallotRejectionId)) {
			return false;
		}
		BallotRejectionId that = (BallotRejectionId) o;
		return new EqualsBuilder()
				.append(id, that.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}
}
