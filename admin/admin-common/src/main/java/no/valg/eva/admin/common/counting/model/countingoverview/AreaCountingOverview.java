package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AreaCountingOverview extends CountingOverviewWithAreaCountingOverview {
	private String areaName;

	public AreaCountingOverview(String areaName, CountCategory category, ElectionPath contestPath, AreaPath areaPath,
			boolean hasCount, List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		super(category, contestPath, areaPath, hasCount, statuses, areaCountingOverviews);
		this.areaName = areaName;
	}

	public AreaCountingOverview(String areaName, CountCategory category, ElectionPath contestPath, AreaPath areaPath, boolean hasCount, List<Status> statuses) {
		super(category, contestPath, areaPath, hasCount, statuses, emptyList());
		this.areaName = areaName;
	}

	public AreaCountingOverview(String areaName, CountCategory category, ElectionPath contestPath, AreaPath areaPath, List<Status> statuses) {
		super(category, contestPath, areaPath, false, statuses, emptyList());
		this.areaName = areaName;
	}

	@Override
	public String getName() {
		return getAreaPath().getPollingDistrictId() + " " + areaName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AreaCountingOverview)) {
			return false;
		}
		AreaCountingOverview that = (AreaCountingOverview) o;
		return new EqualsBuilder()
				.append(areaName, that.areaName)
				.appendSuper(super.equals(o))
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(areaName)
				.appendSuper(super.hashCode())
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("areaName", areaName)
				.appendSuper(super.toString())
				.toString();
	}
}
