package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BallotInfo implements Serializable {
	private final String ballotId;
	private final String ballotName;

	public BallotInfo(String ballotId, String ballotName) {
		this.ballotId = ballotId;
		this.ballotName = ballotName;
	}

	public String getBallotId() {
		return ballotId;
	}

	public String getBallotName() {
		return ballotName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BallotInfo rhs = (BallotInfo) obj;
		return new EqualsBuilder()
				.append(this.ballotId, rhs.ballotId)
				.append(this.ballotName, rhs.ballotName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(ballotId)
				.append(ballotName)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("ballotId", ballotId)
				.append("ballotName", ballotName)
				.toString();
	}
}
