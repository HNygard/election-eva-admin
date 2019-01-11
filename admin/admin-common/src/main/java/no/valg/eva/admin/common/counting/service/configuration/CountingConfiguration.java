package no.valg.eva.admin.common.counting.service.configuration;

import static no.evote.constants.AreaLevelEnum.COUNTY;

import java.io.Serializable;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.constants.CountingMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CountingConfiguration implements Serializable {
	private AreaLevelEnum contestAreaLevel;
	private CountingMode countingMode;
	private boolean requiredProtocolCount;
	private boolean penultimateRecount;

	public AreaLevelEnum getContestAreaLevel() {
		return contestAreaLevel;
	}

	public void setContestAreaLevel(final AreaLevelEnum contestAreaLevel) {
		this.contestAreaLevel = contestAreaLevel;
	}

	public CountingMode getCountingMode() {
		return countingMode;
	}

	public void setCountingMode(final CountingMode countingMode) {
		this.countingMode = countingMode;
	}

	public boolean isRequiredProtocolCount() {
		return requiredProtocolCount;
	}

	public void setRequiredProtocolCount(final boolean requiredProtocolCount) {
		this.requiredProtocolCount = requiredProtocolCount;
	}

	public boolean isPenultimateRecount() {
		return penultimateRecount;
	}

	public void setPenultimateRecount(final boolean penultimateRecount) {
		this.penultimateRecount = penultimateRecount;
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
		CountingConfiguration rhs = (CountingConfiguration) obj;
		return new EqualsBuilder()
				.append(this.contestAreaLevel, rhs.contestAreaLevel)
				.append(this.countingMode, rhs.countingMode)
				.append(this.requiredProtocolCount, rhs.requiredProtocolCount)
				.append(this.penultimateRecount, rhs.penultimateRecount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(contestAreaLevel)
				.append(countingMode)
				.append(requiredProtocolCount)
				.append(penultimateRecount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("contestAreaLevel", contestAreaLevel)
				.append("countingMode", countingMode)
				.append("requiredProtocolCount", requiredProtocolCount)
				.append("penultimateRecount", penultimateRecount)
				.toString();
	}

	public boolean isContestOnCountyLevel() {
		return contestAreaLevel == COUNTY;
	}
}
