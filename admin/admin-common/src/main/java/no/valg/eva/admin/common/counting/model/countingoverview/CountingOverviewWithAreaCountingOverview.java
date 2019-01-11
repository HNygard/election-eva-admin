package no.valg.eva.admin.common.counting.model.countingoverview;

import static com.codepoetics.protonpack.StreamUtils.zip;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class CountingOverviewWithAreaCountingOverview implements CountingOverview {
	private CountCategory category;
	private ElectionPath contestPath;
	private AreaPath areaPath;
	private boolean hasCount;
	private List<Status> statuses;
	private List<AreaCountingOverview> areaCountingOverviews;

	protected CountingOverviewWithAreaCountingOverview(CountCategory category, ElectionPath contestPath, AreaPath areaPath, boolean hasCount,
			List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		this.category = category;
		this.contestPath = contestPath;
		this.areaPath = areaPath;
		this.hasCount = hasCount;
		this.statuses = statuses;
		this.areaCountingOverviews = areaCountingOverviews;
	}

	@Override
	public CountCategory getCategory() {
		return category;
	}

	@Override
	public ElectionPath getContestPath() {
		return contestPath;
	}

	@Override
	public AreaPath getAreaPath() {
		return areaPath;
	}

	@Override
	public boolean hasCount() {
		return hasCount;
	}

	public List<Status> getStatuses() {
		if (areaCountingOverviews.isEmpty()) {
			return statuses;
		}
		return mergedStatuses();
	}

	public List<Status> getAllStatuses() {
		if (areaCountingOverviews.isEmpty()) {
			return statuses;
		}
		return areaCountingOverviews
				.stream()
				.flatMap(categoryCountingOverview -> categoryCountingOverview.getStatuses().stream())
				.collect(Collectors.toList());
	}

	private List<Status> mergedStatuses() {
		return mergedStatuses(statuses.stream(), mergedAreaCountingOverviewStatuses()).collect(toList());
	}

	private Stream<Status> mergedAreaCountingOverviewStatuses() {
		return areaCountingOverviews
				.stream()
				.map(AreaCountingOverview::getStatuses)
				.map(Collection::stream)
				.reduce(this::mergedStatuses)
				.orElse(empty());
	}

	private Stream<Status> mergedStatuses(Stream<Status> statusStream1, Stream<Status> statusStream2) {
		return zip(statusStream1, statusStream2, Status::merge);
	}

	public List<AreaCountingOverview> getAreaCountingOverviews() {
		return areaCountingOverviews;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewWithAreaCountingOverview)) {
			return false;
		}
		CountingOverviewWithAreaCountingOverview that = (CountingOverviewWithAreaCountingOverview) o;
		return new EqualsBuilder()
				.append(category, that.category)
				.append(contestPath, that.contestPath)
				.append(areaPath, that.areaPath)
				.append(hasCount, that.hasCount)
				.append(statuses, that.statuses)
				.append(areaCountingOverviews, that.areaCountingOverviews)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(category)
				.append(contestPath)
				.append(areaPath)
				.append(hasCount)
				.append(statuses)
				.append(areaCountingOverviews)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("areaCountingOverviews", areaCountingOverviews)
				.append("category", category)
				.append("contestPath", contestPath)
				.append("areaPath", areaPath)
				.append("hasCount", hasCount)
				.append("statuses", statuses)
				.toString();
	}
}
