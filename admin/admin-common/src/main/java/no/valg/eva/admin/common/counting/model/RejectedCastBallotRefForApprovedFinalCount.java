package no.valg.eva.admin.common.counting.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class RejectedCastBallotRefForApprovedFinalCount extends CastBallotRefForApprovedFinalCount {
	private final BallotRejectionId ballotRejectionId;

	public RejectedCastBallotRefForApprovedFinalCount(
			ReportingUnitTypeId reportingUnitTypeId, CountContext countContext, AreaPath countingAreaPath,
			CastBallotId castBallotId, BallotRejectionId ballotRejectionId) {
		super(reportingUnitTypeId, countContext, countingAreaPath, castBallotId);
		this.ballotRejectionId = ballotRejectionId;
	}

	public BallotRejectionId ballotRejectionId() {
		return ballotRejectionId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RejectedCastBallotRefForApprovedFinalCount)) {
			return false;
		}
		RejectedCastBallotRefForApprovedFinalCount that = (RejectedCastBallotRefForApprovedFinalCount) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(ballotRejectionId, that.ballotRejectionId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(ballotRejectionId)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("ballotRejectionId", ballotRejectionId)
				.toString();
	}
}
