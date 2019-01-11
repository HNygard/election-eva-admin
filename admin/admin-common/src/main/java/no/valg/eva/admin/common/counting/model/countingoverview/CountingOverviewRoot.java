package no.valg.eva.admin.common.counting.model.countingoverview;

import static com.codepoetics.protonpack.StreamUtils.zip;
import static java.util.stream.Stream.empty;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CountingOverviewRoot implements CountingOverview {
	private final AreaPath areaPath;
	private final String areaName;
	private final List<StatusType> statusTypes;
	private final List<CategoryCountingOverview> categoryCountingOverviews;

	public CountingOverviewRoot(AreaPath areaPath, String areaName, List<StatusType> statusTypes, List<CategoryCountingOverview> categoryCountingOverviews) {
		this.areaPath = areaPath;
		this.areaName = areaName;
		this.statusTypes = statusTypes;
		this.categoryCountingOverviews = categoryCountingOverviews;
	}

	@Override
	public String getName() {
		return areaName;
	}

	@Override
	public List<Status> getStatuses() {
		return categoryCountingOverviews
				.stream()
				.map(CategoryCountingOverview::getStatuses)
				.map(Collection::stream)
				.reduce(this::zipStatuses)
				.orElse(empty())
				.collect(Collectors.toList());
	}

	private Stream<Status> zipStatuses(Stream<Status> statusStream1, Stream<Status> statusStream2) {
		return zip(statusStream1, statusStream2, Status::merge);
	}

	@Override
	public AreaPath getAreaPath() {
		return areaPath;
	}

	public List<StatusType> getStatusTypes() {
		return statusTypes;
	}

	public List<CategoryCountingOverview> getCategoryCountingOverviews() {
		return categoryCountingOverviews;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewRoot)) {
			return false;
		}
		CountingOverviewRoot that = (CountingOverviewRoot) o;
		return new EqualsBuilder()
				.append(areaPath, that.areaPath)
				.append(areaName, that.areaName)
				.append(statusTypes, that.statusTypes)
				.append(categoryCountingOverviews, that.categoryCountingOverviews)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(areaPath)
				.append(areaName)
				.append(statusTypes)
				.append(categoryCountingOverviews)
				.toHashCode();
	}

}
