package no.valg.eva.admin.common.counting.model.countingoverview;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

import java.util.List;

import static java.util.Collections.emptyList;

public class CategoryCountingOverview extends CountingOverviewWithAreaCountingOverview {
	public CategoryCountingOverview(CountCategory category, ElectionPath contestPath, AreaPath areaPath, boolean hasCount,
			List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		super(category, contestPath, areaPath, hasCount, statuses, areaCountingOverviews);
	}

	public CategoryCountingOverview(CountCategory category, ElectionPath contestPath, AreaPath areaPath, List<Status> statuses) {
		super(category, contestPath, areaPath, true, statuses, emptyList());
	}

	@Override
	public String getName() {
		return getCategory().messageProperty();
	}
}
