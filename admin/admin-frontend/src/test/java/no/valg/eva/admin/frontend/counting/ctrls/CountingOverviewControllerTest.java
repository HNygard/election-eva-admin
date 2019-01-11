package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.RejectedBallotsStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.service.CountingOverviewService;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewPanelModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewTabModel;
import no.valg.eva.admin.frontend.counting.view.mapper.CountingOverviewPanelModelMapper;
import no.valg.eva.admin.frontend.counting.view.mapper.CountingOverviewTabModelMapper;
import no.valg.eva.admin.frontend.security.PageAccess;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.FINAL_COUNT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CountingOverviewControllerTest extends BaseFrontendTest {

    private static final AreaPath A_MUNICIPALITY_PATH = new AreaPath("150001.47.01.0101");

	@DataProvider
	public static Object[][] tabsTestData() {
		CountCategory category = VO;
		AreaPath areaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		ElectionPath pickerElectionPath = ElectionPath.from("111111.11.22");
		AreaPath pickerAreaPath = AreaPath.from("111111.11.11.1111");
		return new Object[][] {
				new Object[] { null, null, null, null, 0 },
				new Object[] { category, areaPath, pickerElectionPath, pickerAreaPath, 1 }
		};
	}

	@DataProvider
	public static Object[][] panelsTestData() {
		CountCategory category = VO;
		AreaPath areaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		ElectionPath pickerElectionPath = ElectionPath.from("111111.11.22");
		AreaPath pickerAreaPath = AreaPath.from("111111.11.11.1111");
		return new Object[][] {
				new Object[] { null, null, null, null, 1, "0" },
				new Object[] { null, null, null, null, 2, "" },
				new Object[] { category, areaPath, pickerElectionPath, pickerAreaPath, 2, "1" }
		};
	}

	@DataProvider
	public static Object[][] statusSummaryTestData() {
		return new Object[][] {
				new Object[] { true, null, false },
				new Object[] { false, new RejectedBallotsStatus(false), true },
				new Object[] { false, new CountingStatus(), true },
				new Object[] { false, new CountingStatus(FINAL_COUNT_STATUS, APPROVED), false },
		};
	}

	@Test(dataProvider = "tabsTestData")
	public void init_givenNonElectionEventAdminUser_initsTabs(
			CountCategory category, AreaPath areaPath, ElectionPath pickerElectionPath, AreaPath pickerAreaPath, int activeTabIndex) throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);
		CountingOverviewService countingOverviewService = getInjectMock(CountingOverviewService.class);
		CountingOverviewTabModelMapper countingOverviewTabModelMapper = getInjectMock(CountingOverviewTabModelMapper.class);
		ContestInfo contestInfo1 = mock(ContestInfo.class);
		ContestInfo contestInfo2 = mock(ContestInfo.class);
		CountingOverviewTabModel tab1 = mock(CountingOverviewTabModel.class);
        when(tab1.getElectionPath()).thenReturn(ElectionPath.from("111111.11.22"));
		CountingOverviewTabModel tab2 = mock(CountingOverviewTabModel.class);
        when(tab2.getElectionPath()).thenReturn(ElectionPath.from("111111.11.22"));

		initRequestParameters(category, areaPath, pickerElectionPath, pickerAreaPath);
		when(countingOverviewService.electionsFor(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(asList(contestInfo1, contestInfo2));
		when(countingOverviewService.countingOverviewsFor(any(UserData.class), any(ElectionPath.class), any(AreaPath.class))).thenReturn(emptyList());
		when(countingOverviewTabModelMapper.countingOverviewTabModel(contestInfo1)).thenReturn(tab1);
		when(countingOverviewTabModelMapper.countingOverviewTabModel(contestInfo2)).thenReturn(tab2);
		if (activeTabIndex == 0) {
			when(tab1.matchesElectionPath(pickerElectionPath)).thenReturn(true);
		} else {
			when(tab2.matchesElectionPath(pickerElectionPath)).thenReturn(true);
		}
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);

		controller.init();

		assertThat(controller.getTabs()).containsExactly(tab1, tab2);
		assertThat(controller.getActiveTabIndex()).isEqualTo(activeTabIndex);
	}

	@Test(dataProvider = "panelsTestData")
	public void init_givenNonElectionEventAdminUser_initsPanels(
			CountCategory category, AreaPath areaPath, ElectionPath pickerElectionPath, AreaPath pickerAreaPath,
			int panelCount, String activePanelIndeces) throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);
		CountingOverviewService countingOverviewService = getInjectMock(CountingOverviewService.class);
		CountingOverviewTabModelMapper countingOverviewTabModelMapper = getInjectMock(CountingOverviewTabModelMapper.class);
		CountingOverviewPanelModelMapper countingOverviewPanelModelMapper = getInjectMock(CountingOverviewPanelModelMapper.class);
		ContestInfo contestInfo = mock(ContestInfo.class);
		ElectionPath electionPath = mock(ElectionPath.class);
		CountingOverviewTabModel tab = mock(CountingOverviewTabModel.class);
		CountingOverviewRoot countingOverviewRoot1 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot2 = mock(CountingOverviewRoot.class);
		CountingOverviewPanelModel panel1 = mock(CountingOverviewPanelModel.class);
		CountingOverviewPanelModel panel2 = mock(CountingOverviewPanelModel.class);
		List<CountingOverviewRoot> countingOverviewRoots;
		if (panelCount == 1) {
			countingOverviewRoots = singletonList(countingOverviewRoot1);
		} else {
			countingOverviewRoots = asList(countingOverviewRoot1, countingOverviewRoot2);
		}

		initRequestParameters(category, areaPath, pickerElectionPath, pickerAreaPath);
		when(countingOverviewService.electionsFor(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(singletonList(contestInfo));
		when(countingOverviewService.countingOverviewsFor(eq(getUserDataMock()), eq(electionPath), any(AreaPath.class))).thenReturn(countingOverviewRoots);
		when(countingOverviewTabModelMapper.countingOverviewTabModel(contestInfo)).thenReturn(tab);
		when(countingOverviewPanelModelMapper.countingOverviewPanelModel(countingOverviewRoot1, null, MUNICIPALITY)).thenReturn(panel1);
		when(countingOverviewPanelModelMapper.countingOverviewPanelModel(countingOverviewRoot2, null, MUNICIPALITY)).thenReturn(panel2);
		when(tab.getElectionPath()).thenReturn(electionPath);
		if (pickerElectionPath != null) {
			when(tab.matchesElectionPath(pickerElectionPath)).thenReturn(true);
		}
		if ("0".equals(activePanelIndeces)) {
			when(panel1.includesAreaPath(areaPath)).thenReturn(true);
		} else if ("1".equals(activePanelIndeces)) {
			when(panel2.includesAreaPath(areaPath)).thenReturn(true);
		}
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);

		controller.init();

		if (panelCount == 1) {
			assertThat(controller.getPanels()).containsExactly(panel1);
		} else {
			assertThat(controller.getPanels()).containsExactly(panel1, panel2);
		}
		assertThat(controller.getActivePanelIndeces()).isEqualTo(activePanelIndeces);
		if (category != null && areaPath != null) {
			verify(panel1).expandTreeIfMatched(category, areaPath);
			verify(panel2).expandTreeIfMatched(category, areaPath);
		}
	}

	@Test
	public void init_givenElectionEventAdminUser_redirectToPicker() throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);
		getServletContainer().setRequestURI("/my/uri");

		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
		when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

		controller.init();

		verify(getFacesContextMock().getExternalContext())
				.redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|/my/uri]");
	}

	@Test
    public void setActiveTabIndex_givenTabIndex_setsActiveTabIndex() {
		CountingOverviewController controller = new CountingOverviewController();
		controller.setActiveTabIndex(1);
		assertThat(controller.getActiveTabIndex()).isEqualTo(1);
	}

	@Test
    public void setActivePanelIndeces_givenPanelTabIndeces_setsActivePanelIndeces() {
		CountingOverviewController controller = new CountingOverviewController();
		controller.setActivePanelIndeces("1");
		assertThat(controller.getActivePanelIndeces()).isEqualTo("1");
	}

	@Test(dataProvider = "statusSummaryTestData")
	public void isStatusSummaryRenderedFor_givenCountingOverviewRoot_returnTrueOrFalse(boolean panelSizeOne, Status status, boolean expected) throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);
		CountingOverviewService countingOverviewService = getInjectMock(CountingOverviewService.class);
		CountingOverviewTabModelMapper countingOverviewTabModelMapper = getInjectMock(CountingOverviewTabModelMapper.class);
		CountingOverviewPanelModelMapper countingOverviewPanelModelMapper = getInjectMock(CountingOverviewPanelModelMapper.class);
		ElectionPath electionPath = mock(ElectionPath.class);
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);
		List<CountingOverviewRoot> countingOverviewRoots;
		if (panelSizeOne) {
			countingOverviewRoots = singletonList(countingOverviewRoot);
		} else {
			countingOverviewRoots = asList(countingOverviewRoot, mock(CountingOverviewRoot.class));
		}
		CountingOverviewTabModel tab = mock(CountingOverviewTabModel.class);

		initRequestParameters();
		when(tab.getElectionPath()).thenReturn(electionPath);
		when(countingOverviewService.electionsFor(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(singletonList(mock(ContestInfo.class)));
		when(countingOverviewService.countingOverviewsFor(eq(getUserDataMock()), eq(electionPath), any(AreaPath.class))).thenReturn(countingOverviewRoots);
		when(countingOverviewTabModelMapper.countingOverviewTabModel(any(ContestInfo.class))).thenReturn(tab);
		when(countingOverviewPanelModelMapper
				.countingOverviewPanelModel(any(CountingOverviewRoot.class), any(ReportingUnitTypeId.class), any(AreaLevelEnum.class)))
						.thenReturn(mock(CountingOverviewPanelModel.class));
		when(countingOverviewRoot.getStatus()).thenReturn(status);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);

		controller.init();

		assertThat(controller.isStatusSummaryRenderedFor(countingOverviewRoot)).isEqualTo(expected);
	}

	@Test
	public void onTabChange_givenEvent_setsActiveTabIndexAndInitsPanels() throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);
		CountingOverviewService countingOverviewService = getInjectMock(CountingOverviewService.class);
		CountingOverviewTabModelMapper countingOverviewTabModelMapper = getInjectMock(CountingOverviewTabModelMapper.class);
		CountingOverviewPanelModelMapper countingOverviewPanelModelMapper = getInjectMock(CountingOverviewPanelModelMapper.class);
		CountingOverviewTabModel tab1 = mock(CountingOverviewTabModel.class);
        when(tab1.getElectionPath()).thenReturn(ElectionPath.from("111111.11.22"));
		CountingOverviewTabModel tab2 = mock(CountingOverviewTabModel.class);
        when(tab2.getElectionPath()).thenReturn(ElectionPath.from("111111.11.22"));
		ElectionPath electionPath = mock(ElectionPath.class);
		TabChangeEvent event = mock(TabChangeEvent.class);

		initRequestParameters();
		when(countingOverviewService.electionsFor(eq(getUserDataMock()), any(AreaPath.class)))
				.thenReturn(asList(mock(ContestInfo.class), mock(ContestInfo.class)));
		when(countingOverviewService.countingOverviewsFor(eq(getUserDataMock()), any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(singletonList(mock(CountingOverviewRoot.class)));
		when(countingOverviewTabModelMapper.countingOverviewTabModel(any(ContestInfo.class))).thenReturn(tab1, tab2);
		when(countingOverviewPanelModelMapper
				.countingOverviewPanelModel(any(CountingOverviewRoot.class), any(ReportingUnitTypeId.class), any(AreaLevelEnum.class)))
						.thenReturn(mock(CountingOverviewPanelModel.class));
		when(tab2.getElectionPath()).thenReturn(electionPath);
		when(event.getData()).thenReturn(tab2);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);

		controller.init();
		controller.onTabChange(event);

		assertThat(controller.getActiveTabIndex()).isEqualTo(1);
        verify(countingOverviewService).countingOverviewsFor(getUserDataMock(), ElectionPath.from("111111.11.22"), AreaPath.from("111111.11.11.1111"));
	}

	private void initRequestParameters() {
		initRequestParameters(null, null, null, null);
	}

	private void initRequestParameters(CountCategory category, AreaPath areaPath, ElectionPath pickerElectionPath, AreaPath pickerAreaPath) {
		getServletContainer().setRequestParameter("category", category);
		getServletContainer().setRequestParameter("areaPath", areaPath);
		getServletContainer().setRequestParameter("pickerElectionPath", pickerElectionPath);
		getServletContainer().setRequestParameter("pickerAreaPath", pickerAreaPath);
	}

	@Test
	public void aktiverRapporteringsknapp_enValgnattrapporteringKanRapporteres_true() throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);

		List<CountingOverviewTabModel> tabs = new ArrayList<>();
		CountingOverviewTabModel tab = mock(CountingOverviewTabModel.class);
		when(tab.getElectionPath()).thenReturn(mock(ElectionPath.class));
		tabs.add(tab);
		mockFieldValue("tabs", tabs);
		mockFieldValue("pickerAreaPath", A_MUNICIPALITY_PATH);
		when(getInjectMock(ValgnattReportService.class).kanFylketRapportere(any(UserData.class), any(ElectionPath.class)))
				.thenReturn(false);

		when(getInjectMock(ValgnattReportService.class).antallRapporterbare(any(UserData.class), any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(1L);

		assertThat(controller.aktiverRapporteringsknapp()).isTrue();
	}

	@Test
	public void aktiverRapporteringsknapp_fylketKanRapportere_true() throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);

		List<CountingOverviewTabModel> tabs = new ArrayList<>();
		CountingOverviewTabModel tab = mock(CountingOverviewTabModel.class);
		when(tab.getElectionPath()).thenReturn(mock(ElectionPath.class));
		tabs.add(tab);
		mockFieldValue("tabs", tabs);

		when(getUserDataMock().isFylkesvalgstyret()).thenReturn(true);

		when(getInjectMock(ValgnattReportService.class).kanFylketRapportere(any(UserData.class), any(ElectionPath.class)))
				.thenReturn(true);

		assertThat(controller.aktiverRapporteringsknapp()).isTrue();
	}

	@Test
	public void aktiverRapporteringsknapp_bydelsvalg_false() throws Exception {
		CountingOverviewController controller = initializeMocks(CountingOverviewController.class);

		List<CountingOverviewTabModel> tabs = new ArrayList<>();
		CountingOverviewTabModel tab = mock(CountingOverviewTabModel.class);
		ElectionPath fakeElectionPath = mock(ElectionPath.class);
		when(tab.getElectionPath()).thenReturn(fakeElectionPath);
		tabs.add(tab);
		mockFieldValue("tabs", tabs);

		when(fakeElectionPath.getLevel()).thenReturn(ElectionLevelEnum.ELECTION);

		assertThat(controller.aktiverRapporteringsknapp()).isFalse();
	}

}
