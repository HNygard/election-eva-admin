package no.valg.eva.admin.frontend.counting.view.mapper;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewColumnModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewNameColumnModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewPanelModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewStatusColumnModel;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class CountingOverviewPanelModelMapper implements Serializable {

	private static final int NAME_AND_ACTION_COLUMNS = 2;

	public CountingOverviewPanelModel countingOverviewPanelModel(CountingOverviewRoot countingOverviewRoot, ReportingUnitTypeId reportingUnitTypeId,
			AreaLevelEnum pickerAreaLevel) {
		return new CountingOverviewPanelModel(countingOverviewRoot, treeNode(countingOverviewRoot),
				columns(countingOverviewRoot, reportingUnitTypeId, pickerAreaLevel));
	}

	private TreeNode treeNode(CountingOverviewRoot countingOverviewRoot) {
		DefaultTreeNode treeNode = new DefaultTreeNode(countingOverviewRoot);
		countingOverviewRoot.getCategoryCountingOverviews().forEach(categoryCountingOverview -> treeNode(categoryCountingOverview, treeNode));
		return treeNode;
	}

	private void treeNode(CategoryCountingOverview categoryCountingOverview, DefaultTreeNode parentTreeNode) {
		DefaultTreeNode treeNode = new DefaultTreeNode(categoryCountingOverview, parentTreeNode);
		categoryCountingOverview.getAreaCountingOverviews().forEach(areaCountingOverview -> treeNode(areaCountingOverview, treeNode));
	}

	private void treeNode(AreaCountingOverview areaCountingOverview, DefaultTreeNode parentTreeNode) {
		DefaultTreeNode treeNode = new DefaultTreeNode(areaCountingOverview, parentTreeNode);
		areaCountingOverview
				.getAreaCountingOverviews()
				.forEach(childAreaCountingOverview -> treeNode(childAreaCountingOverview, treeNode));
	}

	private CountingOverviewColumnModel[] columns(CountingOverviewRoot countingOverviewRoot, ReportingUnitTypeId reportingUnitTypeId,
			AreaLevelEnum pickerAreaLevel) {
		List<StatusType> statusTypes = countingOverviewRoot.getStatusTypes();
		CountingOverviewColumnModel[] columns = new CountingOverviewColumnModel[statusTypes.size() + NAME_AND_ACTION_COLUMNS];
		columns[0] = new CountingOverviewNameColumnModel();
		for (int i = 0; i < statusTypes.size(); i++) {
			columns[i + 1] = new CountingOverviewStatusColumnModel(statusTypes.get(i));
		}
		columns[columns.length - 1] = new CountingOverviewActionsColumnModel(reportingUnitTypeId, pickerAreaLevel);
		return columns;
	}
}
