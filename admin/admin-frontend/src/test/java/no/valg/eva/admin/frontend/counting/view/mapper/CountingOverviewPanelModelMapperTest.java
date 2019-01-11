package no.valg.eva.admin.frontend.counting.view.mapper;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewNameColumnModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewPanelModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewStatusColumnModel;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.testng.annotations.Test;

public class CountingOverviewPanelModelMapperTest extends MockUtilsTestCase {
	private static final ReportingUnitTypeId REPORTING_UNIT_TYPE_ID = VALGSTYRET;
	private static final AreaLevelEnum PICKER_AREA_LEVEL = MUNICIPALITY;
	private static final StatusType STATUS_TYPE = PROTOCOL_COUNT_STATUS;

	@Test
	public void countingOverviewPanelModel_givenTestData_returnsModel() throws Exception {
		AreaCountingOverview areaCountingOverview = createMock(AreaCountingOverview.class);
		List<AreaCountingOverview> areaCountingOverviews = singletonList(areaCountingOverview);
		CategoryCountingOverview categoryCountingOverview = categoryCountingOverview(areaCountingOverviews);
		List<CategoryCountingOverview> categoryCountingOverviews = singletonList(categoryCountingOverview);
		CountingOverviewRoot countingOverviewRoot = countingOverviewRoot(categoryCountingOverviews);

		CountingOverviewPanelModel panel = new CountingOverviewPanelModelMapper().countingOverviewPanelModel(countingOverviewRoot, REPORTING_UNIT_TYPE_ID,
				PICKER_AREA_LEVEL);

		CountingOverviewPanelModel expectedPanel = panel(countingOverviewRoot);
		assertThat(panel).isEqualTo(expectedPanel);
		List<TreeNode> treeRootChildren = panel.getTreeRoot().getChildren();
		assertThat(treeRootChildren).containsExactly(treeNodes(expectedPanel.getTreeRoot(), categoryCountingOverviews));
		assertThat(treeRootChildren.get(0).getChildren()).containsExactly(treeNodes(expectedPanel.getTreeRoot().getChildren().get(0), areaCountingOverviews));
	}

	private CategoryCountingOverview categoryCountingOverview(List<AreaCountingOverview> areaCountingOverviews) {
		CategoryCountingOverview categoryCountingOverview = createMock(CategoryCountingOverview.class);
		when(categoryCountingOverview.getAreaCountingOverviews()).thenReturn(areaCountingOverviews);
		return categoryCountingOverview;
	}

	private CountingOverviewRoot countingOverviewRoot(List<CategoryCountingOverview> categoryCountingOverviews) {
		CountingOverviewRoot countingOverviewRoot = createMock(CountingOverviewRoot.class);
		when(countingOverviewRoot.getCategoryCountingOverviews()).thenReturn(categoryCountingOverviews);
		when(countingOverviewRoot.getStatusTypes()).thenReturn(singletonList(STATUS_TYPE));
		return countingOverviewRoot;
	}

	private CountingOverviewPanelModel panel(CountingOverviewRoot countingOverviewRoot) {
		return new CountingOverviewPanelModel(countingOverviewRoot, new DefaultTreeNode(countingOverviewRoot), new CountingOverviewNameColumnModel(),
				new CountingOverviewStatusColumnModel(STATUS_TYPE), new CountingOverviewActionsColumnModel(REPORTING_UNIT_TYPE_ID, PICKER_AREA_LEVEL));
	}

	private TreeNode[] treeNodes(TreeNode parent, List<? extends CountingOverview> countingOverviews) {
		return countingOverviews.stream().map(categoryCountingOverview -> new DefaultTreeNode(categoryCountingOverview, parent)).toArray(TreeNode[]::new);
	}
}
