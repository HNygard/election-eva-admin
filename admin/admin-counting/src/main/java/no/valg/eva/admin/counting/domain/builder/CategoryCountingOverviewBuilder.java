package no.valg.eva.admin.counting.domain.builder;

import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;

@Default
@ApplicationScoped
public class CategoryCountingOverviewBuilder {
	@Inject
	private CountingOverviewStatusBuilder countingOverviewStatusBuilder;
	@Inject
	private AreaCountingOverviewBuilder areaCountingOverviewBuilder;

	public CategoryCountingOverviewBuilder() {

	}
	public CategoryCountingOverviewBuilder(CountingOverviewStatusBuilder countingOverviewStatusBuilder,
			AreaCountingOverviewBuilder areaCountingOverviewBuilder) {
		this.countingOverviewStatusBuilder = countingOverviewStatusBuilder;
		this.areaCountingOverviewBuilder = areaCountingOverviewBuilder;
	}

	public List<CategoryCountingOverview> categoryCountingOverviews(Contest contest, MvArea mvArea, List<StatusType> statusTypes,
			List<CountCategory> categories, List<VoteCountDigest> voteCounts,
			Function<CountCategory, CountingMode> countingModeMapper) {
		return categories
				.stream()
				.map(category -> categoryCountingOverview(contest, mvArea, statusTypes, category, voteCounts, countingModeMapper))
				.sorted((o1, o2) -> o1.getCategory().compareTo(o2.getCategory()))
				.collect(toList());
	}

	private CategoryCountingOverview categoryCountingOverview(
			Contest contest, MvArea mvArea, List<StatusType> statusTypes, CountCategory category, List<VoteCountDigest> voteCounts,
			Function<CountCategory, CountingMode> countingModeMapper) {
		ElectionPath contestPath = contest.electionPath();
		AreaPath countingAreaPath = countingAreaPath(contest, mvArea, category);
		CountingMode countingMode = countingModeMapper.apply(category);
		List<Status> countingOverviewStatuses = countingOverviewStatusBuilder.countingOverviewStatuses(category, countingAreaPath, MUNICIPALITY, statusTypes,
				voteCounts, countingMode);
		if (isAreaCountingOverviewsIncluded(category, countingMode, mvArea.getMunicipality(), statusTypes)) {
			List<AreaCountingOverview> areaCountingOverviews = areaCountingOverviewBuilder.areaCountingOverviews(category, contest, mvArea, statusTypes,
					voteCounts, countingMode);
			return new CategoryCountingOverview(
					category, contestPath, countingAreaPath, hasCount(countingMode), countingOverviewStatuses, areaCountingOverviews);
		}
		return new CategoryCountingOverview(category, contestPath, countingAreaPath, countingOverviewStatuses);
	}

	private boolean hasCount(CountingMode countingMode) {
		return !countingMode.isPollingDistrictOrTechnicalPollingDistrictCount();
	}

	private AreaPath countingAreaPath(Contest contest, MvArea mvArea, CountCategory category) {
		AreaPath areaPath = AreaPath.from(mvArea.getAreaPath());
		if (contest.isOnBoroughLevel() && category != VO) {
			return areaPath.toBoroughPath();
		}
		return areaPath.toMunicipalityPollingDistrictPath();
	}

	private boolean isAreaCountingOverviewsIncluded(CountCategory category, CountingMode countingMode, Municipality municipality,
			List<StatusType> statusTypes) {
		return countingMode.isPollingDistrictOrTechnicalPollingDistrictCount()
				|| isVoAndRequiredProtocolCountAndProtocolCountIncluded(category, municipality, statusTypes);
	}

	private boolean isVoAndRequiredProtocolCountAndProtocolCountIncluded(CountCategory category, Municipality municipality, List<StatusType> statusTypes) {
		return category == VO && municipality.isRequiredProtocolCount() && statusTypes.contains(StatusType.PROTOCOL_COUNT_STATUS);
	}
}
