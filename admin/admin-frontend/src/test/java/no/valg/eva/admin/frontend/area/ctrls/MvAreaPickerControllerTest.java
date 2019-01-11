package no.valg.eva.admin.frontend.area.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.presentation.components.Action;
import no.evote.presentation.filter.MvAreaFilter;
import no.evote.security.UserDataBuilder;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.area.MvAreaPickerTable;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.el.ELContext;
import javax.el.MethodExpression;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class MvAreaPickerControllerTest extends BaseFrontendTest {

	private static final int LEVEL_1 = 1;
	private static final int LEVEL_2 = 2;
	private static final int LEVEL_3 = 3;

	private MvAreaPickerController ctrl;

	@BeforeMethod
	public void setup() throws Exception {
		ctrl = initializeMocks(MvAreaPickerController.class);
		stubResolveExpression("includeAreasAboveMyLevelUpTo", "1");
		stubResolveExpression("mvAreaFilters", mockList(1, MvAreaFilter.class));
		stubResolveExpression("actionController", createMock(Action.class));
		stubResolveExpression("mvAreaSelectionLevel", 1);
		when(getServletContainer().getHttpSessionMock().getAttribute("selectedMvArea")).thenReturn(null);
	}

	@Test
	public void initializeOrRedirect_redirects_whenAllowRedirectIsSetToTrue() {
		ctrl.setUserData(UserDataBuilder.create().withPollingDistrictAsSelectedArea().build());
		Action actionMock = mock(Action.class);

		ctrl.initializeOrRedirect(null, null, actionMock, AreaLevelEnum.POLLING_DISTRICT.getLevel());

		verify(actionMock, times(1)).action();
	}

	@Test
	public void isPollingDistrictLevel_whenLevelIsPollingDistrict_returnsTrue() {
		int areaLevel = AreaLevelEnum.POLLING_DISTRICT.getLevel();

		boolean result = ctrl.isPollingDistrictLevel(areaLevel);

		assertThat(result).isTrue();
	}

	@Test
	public void isPollingDistrictLevel_whenLevelIsNotPollingDistrict_returnsFalse() {
		int areaLevel = AreaLevelEnum.MUNICIPALITY.getLevel();

		boolean result = ctrl.isPollingDistrictLevel(areaLevel);

		assertThat(result).isFalse();
	}

	@Test(dataProvider = "isRenderPicker")
	public void isRenderPicker_withDataProvider_verifyExpected(boolean error, String mvAreaSelectionLevel, Integer mvElectionSelectionLevel,
			MvElection selectedMvElection, MvArea selectedMvArea, Boolean keepLastPickerAfterSelection, boolean expected)
			throws Exception {
		MockUtils.setPrivateField(ctrl, "error", error);
		stubResolveExpression("mvAreaSelectionLevel", mvAreaSelectionLevel);
		stubResolveExpression("keepLastPickerAfterSelection", keepLastPickerAfterSelection);
		when(getInjectMock(MvElectionPickerController.class).getSelectionLevel()).thenReturn(mvElectionSelectionLevel);
		when(getInjectMock(MvElectionPickerController.class).getSelectedMvElection()).thenReturn(selectedMvElection);
		ctrl.setSelectedMvArea(selectedMvArea);

		assertThat(ctrl.isRenderPicker()).isEqualTo(expected);
	}

	@DataProvider(name = "isRenderPicker")
	public Object[][] isRenderPicker() {
		return new Object[][] {
				{ true, null, null, null, null, null, false },
				{ false, "2", null, null, null, null, true },
				{ false, "2", 1, null, null, null, false },
				{ false, "2", 1, createMock(MvElection.class), null, null, true },
				{ false, "2", 1, createMock(MvElection.class), createMock(MvArea.class), null, false },
				{ false, "2", 1, createMock(MvElection.class), createMock(MvArea.class), true, true },
				{ false, "2", 1, createMock(MvElection.class), createMock(MvArea.class), false, false }
		};
	}

	@Test(dataProvider = "getHeaderKey")
	public void getHeaderKey_withDataProvider_verifyExpected(String mvAreaAllSelectable, String expected) {
		stubResolveExpression("mvAreaAllSelectable", mvAreaAllSelectable);
		stubResolveExpression("mvAreaSelectionLevel", "3");

		assertThat(ctrl.getHeaderKey()).isEqualTo(expected);
	}

	@DataProvider(name = "getHeaderKey")
	public Object[][] getHeaderKey() {
		return new Object[][] {
				{ "true", "@area.common.area_level" },
				{ "false", "@area_level[3].name" }
		};
	}

	@Test(dataProvider = "isHideTable")
	public void isHideTable_withDataProvider_verifyExpected(int mvAreasSize, int areaTypeLevel, String mvAreaSelectionLevel, String mvAreaAllSelectable,
															Boolean hideBelowWantedAreaLevel, boolean expected) {
		stubResolveExpression("mvAreaSelectionLevel", mvAreaSelectionLevel);
		stubResolveExpression("hideBelowWantedAreaLevel", hideBelowWantedAreaLevel);
		stubResolveExpression("mvAreaAllSelectable", mvAreaAllSelectable);
		stubResolveExpression("mvAreaHideLeadingSingles", null);
		MvAreaPickerTable pickerTable = getMvAreaPickerTable(areaTypeLevel, mvAreasSize);

		assertThat(ctrl.isHideTable(pickerTable)).isEqualTo(expected);
	}

	@DataProvider(name = "isHideTable")
	public Object[][] isHideTable() {
		return new Object[][] {
				{ 2, 1, "1", null, null, false },
				{ 1, 1, "1", null, null, false },
				{ 1, 1, "2", null, null, true },
				{ 1, 1, "2", "true", null, false },
				{ 2, 1, "1", null, true, false },
				{ 2, 1, "2", null, true, false },
				{ 2, 2, "1", null, true, true }
		};
	}

	
	@Test
	public void isHideTable_withHideSinglesAndSinglesOnly_hidesAll() throws Exception {
		stubResolveExpression("mvAreaHideLeadingSingles", true);
		List<MvAreaPickerTable> mvAreaPickerTables = Arrays.asList(
				getMvAreaPickerTable(LEVEL_1, 1),
				getMvAreaPickerTable(LEVEL_2, 1),
				getMvAreaPickerTable(LEVEL_3, 1));
		MockUtils.setPrivateField(ctrl, "mvAreaPickerTables", mvAreaPickerTables);

		for (MvAreaPickerTable table : mvAreaPickerTables) {
			assertThat(ctrl.isHideTable(table)).isEqualTo(true);
		}
	}

	

	@Test(dataProvider = "isRenderButton")
	public void isRenderButton_withDataProvider_verifyExpected(String mvAreaAllSelectable, String mvAreaSelectionLevel, boolean expected) {
		stubResolveExpression("mvAreaAllSelectable", mvAreaAllSelectable);
		stubResolveExpression("mvAreaSelectionLevel", mvAreaSelectionLevel);
		stubResolveExpression("mvAreaHideLeadingSingles", null);
		MvAreaPickerTable table = getMvAreaPickerTable(2, 1);

		assertThat(ctrl.isRenderButton(table)).isEqualTo(expected);
	}

	@DataProvider(name = "isRenderButton")
	public Object[][] isRenderButton() {
		return new Object[][] {
				{ "true", "1", true },
				{ "false", "1", false },
				{ "false", "2", true }
		};
	}

	
	@Test
	public void isRenderButton_withHideSinglesAndSinglesOnlyAtLevel1_hidesLevel1() throws Exception {
		stubResolveExpression("mvAreaHideLeadingSingles", true);
		stubResolveExpression("mvAreaAllSelectable", "true");
		List<MvAreaPickerTable> mvAreaPickerTables = Arrays.asList(
				getMvAreaPickerTable(LEVEL_1, 1),
				getMvAreaPickerTable(LEVEL_2, 3),
				getMvAreaPickerTable(LEVEL_3, 5));
		MockUtils.setPrivateField(ctrl, "mvAreaPickerTables", mvAreaPickerTables);

		assertThat(ctrl.isRenderButton(mvAreaPickerTables.get(0))).isEqualTo(false);
		assertThat(ctrl.isRenderButton(mvAreaPickerTables.get(1))).isEqualTo(true);
		assertThat(ctrl.isRenderButton(mvAreaPickerTables.get(2))).isEqualTo(true);
	}

	

	@Test(dataProvider = "isDisabledButton")
	public void isDisabledButton_withDataProvider_verifyExpected(MvArea selectedMvArea, Boolean disableSelect, Boolean disableSelectWhenNotOk,
																 MethodExpression mvAreaIndicatorEvaluator, boolean methodExpressionResult, boolean expected) {
		MvAreaPickerTable table = createMock(MvAreaPickerTable.class);
		when(table.getSelectedMvArea()).thenReturn(selectedMvArea);
		stubResolveExpression("disableSelect", disableSelect);
		stubResolveExpression("disableSelectWhenNotOk", disableSelectWhenNotOk);
		stubResolveExpression("mvAreaIndicatorEvaluator", mvAreaIndicatorEvaluator);
		if (mvAreaIndicatorEvaluator != null) {
			when(mvAreaIndicatorEvaluator.invoke(any(ELContext.class), any(Object[].class))).thenReturn(methodExpressionResult);
		}
		assertThat(ctrl.isDisabledButton(table)).isEqualTo(expected);
	}

	@DataProvider(name = "isDisabledButton")
	public Object[][] isDisabledButton() {
		return new Object[][] {
				{ null, null, null, null, false, true },
				{ createMock(MvArea.class), null, null, null, false, false },
				{ createMock(MvArea.class), true, null, null, false, true },
				{ createMock(MvArea.class), null, true, null, false, false },
				{ createMock(MvArea.class), null, true, createMock(MethodExpression.class), false, true },
				{ createMock(MvArea.class), null, true, createMock(MethodExpression.class), true, false }
		};
	}

	@Test
	public void select_withRedirectWithoutCid_redirects() throws Exception {
		stubResolveExpression("redirectWithoutCid", true);
		Action actionToRunAfterCompletion = createMock(Action.class);
		ctrl.setContextPickerRoot(null);
		ctrl.setMvAreaPickerTables(Collections.singletonList(createMock(MvAreaPickerTable.class)));
		ctrl.setActionToRunAfterCompletion(actionToRunAfterCompletion);
		when(getUserDataMock().getOperatorRole().getMvArea().getAreaLevel()).thenReturn(2);
		when(actionToRunAfterCompletion.action()).thenReturn("/myurl");

		ctrl.select(3);

		verify(actionToRunAfterCompletion).action();
		verify(getServletContainer().getResponseMock()).sendRedirect("/myurl");
	}

	@Override
	protected void stubResolveExpression(String name, Object value) {
		super.stubResolveExpression("#{cc.attrs." + name + "}", value);
	}

	private MvAreaPickerTable getMvAreaPickerTable(int level, int size) {
		MvAreaPickerTable table = createMock(MvAreaPickerTable.class);
		when(table.getLevel()).thenReturn(level);
		when(table.getMvAreas().size()).thenReturn(size);
		return table;
	}
}

