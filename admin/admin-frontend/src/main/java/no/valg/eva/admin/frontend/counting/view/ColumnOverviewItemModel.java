package no.valg.eva.admin.frontend.counting.view;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class ColumnOverviewItemModel {
	private final String value;
	private final String description;

	protected ColumnOverviewItemModel(String value, String description) {
		this.value = value;
		this.description = description;
	}

	protected ColumnOverviewItemModel(String value) {
		this.value = value;
		this.description = null;
	}

	public String getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public boolean isText() {
		return this instanceof ColumnOverviewTextItemModel;
	}

	public boolean isIcon() {
		return this instanceof ColumnOverviewIconItemModel;
	}

	public boolean isLink() {
		return this instanceof ColumnOverviewLinkItemModel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ColumnOverviewItemModel that = (ColumnOverviewItemModel) o;
		return new EqualsBuilder()
				.append(value, that.value)
				.append(description, that.description)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(value)
				.append(description)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("value", value)
				.append("description", description)
				.toString();
	}
}
