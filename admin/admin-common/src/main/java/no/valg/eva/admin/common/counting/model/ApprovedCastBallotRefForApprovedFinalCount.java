package no.valg.eva.admin.common.counting.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class ApprovedCastBallotRefForApprovedFinalCount extends CastBallotRefForApprovedFinalCount {
	private final BallotId ballotId;

	public ApprovedCastBallotRefForApprovedFinalCount(
			ReportingUnitTypeId reportingUnitTypeId, CountContext countContext, AreaPath countingAreaPath, CastBallotId castBallotId, BallotId ballotId) {
		super(reportingUnitTypeId, countContext, countingAreaPath, castBallotId);
		this.ballotId = ballotId;
	}

	public BallotId ballotId() {
		return ballotId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ApprovedCastBallotRefForApprovedFinalCount)) {
			return false;
		}
		ApprovedCastBallotRefForApprovedFinalCount that = (ApprovedCastBallotRefForApprovedFinalCount) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(ballotId, that.ballotId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(ballotId)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("ballotId", ballotId)
				.toString();
	}
}
