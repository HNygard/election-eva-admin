package no.valg.eva.admin.frontend.counting.view;

import static java.util.Collections.singletonList;

import java.util.List;

import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;

public class CountingOverviewNameColumnModel extends CountingOverviewColumnModel {
	public CountingOverviewNameColumnModel() {
		super("@count.overview.category");
	}

	@Override
	public String getStyle() {
		return "width: 400px;";
	}

	@Override
	public List<ColumnOverviewItemModel> itemsFor(CountingOverview countingOverview) {
		return singletonList(new ColumnOverviewTextItemModel(countingOverview.getName()));
	}
}
