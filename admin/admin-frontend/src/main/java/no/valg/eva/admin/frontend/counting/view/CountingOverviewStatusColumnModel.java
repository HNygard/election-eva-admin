package no.valg.eva.admin.frontend.counting.view;

import static java.util.Arrays.asList;

import java.util.List;

import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;

public class CountingOverviewStatusColumnModel extends CountingOverviewColumnModel {
	private final StatusType statusType;

	public CountingOverviewStatusColumnModel(StatusType statusType) {
		super(statusType.getHeader());
		this.statusType = statusType;
	}

	@Override
	public List<ColumnOverviewItemModel> itemsFor(CountingOverview countingOverview) {
		Status status = countingOverview
				.getStatuses()
				.stream()
				.filter(s -> s.getStatusType() == statusType)
				.findFirst()
				.orElse(statusType.defaultStatus());
		if (status instanceof CountingStatus) {
			return asList(
					new ColumnOverviewIconItemModel(status.getPrimaryIconStyle()),
					new ColumnOverviewIconItemModel(status.getSecondaryIconStyle()),
					new ColumnOverviewTextItemModel(status.getRejectedBallotCount()));
		}
		return asList(
				new ColumnOverviewIconItemModel(status.getPrimaryIconStyle()),
				new ColumnOverviewIconItemModel(status.getSecondaryIconStyle()),
				new ColumnOverviewTextItemModel(status.getRejectedBallotCount()));
	}
}
