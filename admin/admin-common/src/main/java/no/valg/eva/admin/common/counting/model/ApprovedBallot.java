package no.valg.eva.admin.common.counting.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ApprovedBallot implements Serializable {
	private final String id;
	private final String ballotId;
	private final boolean modified;

	public ApprovedBallot(String id, String ballotId, boolean modified) {
		this.id = id;
		this.ballotId = ballotId;
		this.modified = modified;
	}

	public String getId() {
		return id;
	}

	public String getBallotId() {
		return ballotId;
	}

	public boolean isModified() {
		return modified;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ApprovedBallot that = (ApprovedBallot) o;
		return new EqualsBuilder()
				.append(modified, that.modified)
				.append(id, that.id)
				.append(ballotId, that.ballotId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.append(ballotId)
				.append(modified)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("ballotId", ballotId)
				.append("modified", modified)
				.toString();
	}
}
