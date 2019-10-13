package no.valg.eva.admin.counting.domain.builder;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;

@Default
@ApplicationScoped
public class CountingOverviewRootBuilder {
	@Inject
	private CategoryCountingOverviewBuilder categoryCountingOverviewBuilder;

	public CountingOverviewRootBuilder() {

	}
	public CountingOverviewRootBuilder(CategoryCountingOverviewBuilder categoryCountingOverviewBuilder) {
		this.categoryCountingOverviewBuilder = categoryCountingOverviewBuilder;
	}

	public CountingOverviewRoot countingOverviewRoot(
			Contest contest, MvArea mvArea, List<StatusType> statusTypes, List<CountCategory> countCategories, List<VoteCountDigest> voteCounts,
			Function<CountCategory, CountingMode> countingModeMapper) {
		List<CategoryCountingOverview> categoryCountingOverviews = categoryCountingOverviewBuilder.categoryCountingOverviews(contest, mvArea, statusTypes,
				countCategories, voteCounts, countingModeMapper);
		AreaPath areaPath = AreaPath.from(mvArea.getAreaPath());
		return new CountingOverviewRoot(areaPath, mvArea.getAreaName(), statusTypes, categoryCountingOverviews);
	}
}
