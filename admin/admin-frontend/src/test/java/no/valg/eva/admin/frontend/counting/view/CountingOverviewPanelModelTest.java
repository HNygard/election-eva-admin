package no.valg.eva.admin.frontend.counting.view;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewPanelModelTest {
	private static final String TITLE = "title";
	private static final String TITLE_STYLE = "title_style";
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111");
	private static final AreaPath PD_AREA_PATH_1 = AreaPath.from("111111.11.11.1111.111111");
	private static final AreaPath PD_AREA_PATH_2 = AreaPath.from("222222.22.22.2222.222222");

	@DataProvider
	public static Object[][] includesAreaPathTestData() {
		return new Object[][] {
				new Object[] { PD_AREA_PATH_1, true },
				new Object[] { PD_AREA_PATH_2, false }
		};
	}

	@DataProvider
	public static Object[][] expandTreeIfMatchedTestData() {
		return new Object[][] {
				new Object[] { VO, PD_AREA_PATH_1, true },
				new Object[] { VO, PD_AREA_PATH_2, false },
				new Object[] { FO, PD_AREA_PATH_1, false },
				new Object[] { FO, PD_AREA_PATH_2, false },
		};
	}

	@Test
	public void constructor_givenParameters_createsObject() throws Exception {
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class, RETURNS_DEEP_STUBS);
		TreeNode treeRoot = mock(TreeNode.class);
		CountingOverviewColumnModel column = mock(CountingOverviewColumnModel.class);

		when(countingOverviewRoot.getName()).thenReturn(TITLE);
		when(countingOverviewRoot.getStatus().getPanelStyle()).thenReturn(TITLE_STYLE);
		when(countingOverviewRoot.getAreaPath()).thenReturn(AREA_PATH);

		CountingOverviewPanelModel panel = new CountingOverviewPanelModel(countingOverviewRoot, treeRoot, column);

		assertThat(panel.getCountingOverviewRoot()).isEqualTo(countingOverviewRoot);
		assertThat(panel.getTitle()).isEqualTo(TITLE);
		assertThat(panel.getTitleStyle()).isEqualTo(TITLE_STYLE);
		assertThat(panel.getTreeRoot()).isEqualTo(treeRoot);
		assertThat(panel.getColumns()).containsExactly(column);
		assertThat(panel.getAreaPath()).isEqualTo(AREA_PATH);
	}

	@Test(dataProvider = "includesAreaPathTestData")
	public void includesAreaPath_givenTestData_returnsTrueOrFalse(AreaPath areaPath, boolean expected) throws Exception {
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class, RETURNS_DEEP_STUBS);
		TreeNode treeRoot = mock(TreeNode.class);
		CountingOverviewColumnModel column = mock(CountingOverviewColumnModel.class);
		CountingOverviewPanelModel panel = new CountingOverviewPanelModel(countingOverviewRoot, treeRoot, column);

		when(countingOverviewRoot.getAreaPath()).thenReturn(AREA_PATH);

		assertThat(panel.includesAreaPath(areaPath)).isEqualTo(expected);
	}

	@Test(dataProvider = "expandTreeIfMatchedTestData")
	public void expandTreeIfMatched(CountCategory category, AreaPath areaPath, boolean expected) throws Exception {
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);
		TreeNode treeRoot = new DefaultTreeNode();
		TreeNode secondLevel = new DefaultTreeNode(null, treeRoot);
		CountingOverview countingOverview = mock(CountingOverview.class);
		TreeNode thirdLevel = new DefaultTreeNode(countingOverview, secondLevel);
		CountingOverviewColumnModel column = mock(CountingOverviewColumnModel.class);
		CountingOverviewPanelModel panel = new CountingOverviewPanelModel(countingOverviewRoot, treeRoot, column);

		when(countingOverview.getCategory()).thenReturn(VO);
		when(countingOverview.getAreaPath()).thenReturn(PD_AREA_PATH_1);

		panel.expandTreeIfMatched(category, areaPath);

		assertThat(treeRoot.isExpanded()).isEqualTo(expected);
		assertThat(secondLevel.isExpanded()).isEqualTo(expected);
		assertThat(thirdLevel.isExpanded()).isEqualTo(false);
	}
}
