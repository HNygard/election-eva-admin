package no.valg.eva.admin.common.counting.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class CastBallotRefForApprovedFinalCount implements Serializable {
	private final ReportingUnitTypeId reportingUnitTypeId;
	private final CountContext countContext;
	private final AreaPath countingAreaPath;
	private final CastBallotId castBallotId;

	public CastBallotRefForApprovedFinalCount(
			ReportingUnitTypeId reportingUnitTypeId, CountContext countContext, AreaPath countingAreaPath, CastBallotId castBallotId) {
		this.reportingUnitTypeId = reportingUnitTypeId;
		this.countContext = countContext;
		this.countingAreaPath = countingAreaPath;
		this.castBallotId = castBallotId;
	}

	public ReportingUnitTypeId reportingUnitTypeId() {
		return reportingUnitTypeId;
	}

	public CountContext countContext() {
		return countContext;
	}

	public AreaPath countingAreaPath() {
		return countingAreaPath;
	}

	public CastBallotId castBallotId() {
		return castBallotId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CastBallotRefForApprovedFinalCount)) {
			return false;
		}
		CastBallotRefForApprovedFinalCount that = (CastBallotRefForApprovedFinalCount) o;
		return new EqualsBuilder()
				.append(reportingUnitTypeId, that.reportingUnitTypeId)
				.append(countContext, that.countContext)
				.append(countingAreaPath, that.countingAreaPath)
				.append(castBallotId, that.castBallotId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(reportingUnitTypeId)
				.append(countContext)
				.append(countingAreaPath)
				.append(castBallotId)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("reportingUnitTypeId", reportingUnitTypeId)
				.append("countContext", countContext)
				.append("countingAreaPath", countingAreaPath)
				.append("castBallotId", castBallotId)
				.toString();
	}
}
