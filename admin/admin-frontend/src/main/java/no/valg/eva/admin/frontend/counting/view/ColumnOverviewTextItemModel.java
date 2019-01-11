package no.valg.eva.admin.frontend.counting.view;

public class ColumnOverviewTextItemModel extends ColumnOverviewItemModel {
	public ColumnOverviewTextItemModel(String value) {
		super(value);
	}

	public ColumnOverviewTextItemModel(Integer value) {
		super(value != null ? String.valueOf(value) : null);
	}
}
