package no.valg.eva.admin.frontend.counting.view;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.common.ElectionPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CountingOverviewTabModel {
	private final ElectionPath electionPath;
	private final String title;

	public CountingOverviewTabModel(ElectionPath electionPath, String title) {
		this.electionPath = electionPath;
		this.title = title;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public String getId() {
		return electionPath.path();
	}

	public String getTitle() {
		return title;
	}

	public boolean matchesElectionPath(ElectionPath electionPath) {
		electionPath.assertElectionLevel();
		return this.electionPath.toElectionPath().equals(electionPath);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewTabModel)) {
			return false;
		}
		CountingOverviewTabModel that = (CountingOverviewTabModel) o;
		return new EqualsBuilder()
				.append(electionPath, that.electionPath)
				.append(title, that.title)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(electionPath)
				.append(title)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("electionPath", electionPath)
				.append("title", title)
				.toString();
	}
}
