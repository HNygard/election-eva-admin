package no.valg.eva.admin.frontend.counting.view;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.List;

import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class CountingOverviewColumnModel {
	private String header;

	public CountingOverviewColumnModel(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public String getStyle() {
		return "";
	}

	public abstract List<ColumnOverviewItemModel> itemsFor(CountingOverview countingOverviewWithNavigation);

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewColumnModel)) {
			return false;
		}
		CountingOverviewColumnModel that = (CountingOverviewColumnModel) o;
		return new EqualsBuilder()
				.append(header, that.header)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(header)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("header", header)
				.toString();
	}
}
