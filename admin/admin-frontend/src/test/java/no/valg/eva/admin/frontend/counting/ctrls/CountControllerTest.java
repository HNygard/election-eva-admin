package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.frontend.counting.view.Tab;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CountControllerTest extends BaseCountControllerTest {

	@DataProvider(name = "getPreviousController")
	public static Object[][] getPreviousController() {
		return new Object[][] {
				{ -1, -1, false },
				{ 0, -1, false },
				{ 5, 0, false },
				{ 5, 2, true }
		};
	}

	@DataProvider(name = "isPreviousApproved")
	public static Object[][] isPreviousApproved() {
		return new Object[][] {
				{ -1, -1, false, true },
				{ 0, -1, false, true },
				{ 5, 0, false, true },
				{ 5, 2, false, false },
				{ 5, 2, true, true }
		};
	}

	@DataProvider(name = "getNextController")
	public static Object[][] getNextController() {
		return new Object[][] {
				{ -1, -1, false },
				{ 0, -1, false },
				{ 5, 4, false },
				{ 5, 2, true }
		};
	}

	@DataProvider(name = "isNextApproved")
	public static Object[][] isNextApproved() {
		return new Object[][] {
				{ -1, -1, false, false },
				{ 0, -1, false, false },
				{ 5, 0, false, false },
				{ 5, 2, false, false },
				{ 5, 2, true, true }
		};
	}

	@Test
	public void doInit_withData_checkState() throws Exception {
		CountController countController = makeNonAbstractCountController();
		countController.doInit();
		assertThat(countController.getCountContext()).isNotNull();
		assertThat(countController.getCountContext().getCategory()).isEqualTo(CountCategory.VO);
		assertThat(countController.getCountContext().getCategoryMessageProperty()).isEqualTo("@vote_count_category[VO].name");
		assertThat(countController.getCountContext().getContestPath()).isEqualTo(new ElectionPath("730001.01.01.000001"));
	}

	@Test
	public void hasWriteAccess_forUserWithEditAccess_isTrue() throws Exception {
		CountController countController = makeNonAbstractCountController();
		hasAccess(Opptelling_Forhånd_Rediger, false);
		hasAccess(Opptelling_Valgting_Rediger);

		assertThat(countController.hasWriteAccess()).isTrue();
	}

	@Test
	public void hasWriteAccess_forUserWithOutEditAccess_isFalse() throws Exception {
		CountController countController = makeNonAbstractCountController();
		hasAccess(Opptelling_Forhånd_Rediger, false);
		hasAccess(Opptelling_Valgting_Rediger, false);

		assertThat(countController.hasWriteAccess()).isFalse();
	}

	@Test
	public void hasRevokeAccess_forUserWithRevokeAccess_isTrue() throws Exception {
		CountController countController = makeNonAbstractCountController();
		hasAccess(Opptelling_Opphev_Foreløpig_Telling);

		assertThat(countController.hasOpptellingOpphevForeløpigTelling()).isTrue();
	}

	@Test
	public void hasRevokeAccess_forUserWithOutRevokeAccess_isFalse() throws Exception {
		CountController countController = makeNonAbstractCountController();
		hasAccess(Opptelling_Opphev_Foreløpig_Telling, false);

		assertThat(countController.hasOpptellingOpphevForeløpigTelling()).isFalse();
	}

	@Test
	public void getDisplayAreaName_whenThePollingDistrictInTheCountRepresentsTheMunicipality_returnsMunicipality() throws Exception {
		Count count = new ProtocolCount("SomeId", new AreaPath("123456.78.90.1234.567890.0000"), "SomeAreaName", "SomeReportingUnitAreaName", true);
		CountController countController = makeNonAbstractCountController(count);
		when(getMessageProviderMock().get("@area.polling_district.municipality")).thenReturn("Municipality");

		String displayAreaName = countController.getDisplayAreaName();

		assertThat(displayAreaName).isEqualTo("Municipality");
	}

	@Test
	public void getDisplayAreaName_whenThereIsANormalPollingDistrict_returnsPollingDistrictSummary() throws Exception {
		String pollingDistrict = "1234";
		Count count = new ProtocolCount("SomeId", new AreaPath("123456.78.90.1234.567890." + pollingDistrict), "SomeAreaName", "SomeReportingUnitAreaName", true);
		when(getMessageProviderMock().get("@area_level[5].name")).thenReturn("Polling district");
		CountController countController = makeNonAbstractCountController(count);

		String displayAreaName = countController.getDisplayAreaName();

		assertThat(displayAreaName).isEqualTo(pollingDistrict + " SomeAreaName");
	}

	@Test
	public void getDisplayAreaName_whenBydelsvalg_returnsBoroughSummary() throws Exception {
		String borough = "567890";
		Count count = new ProtocolCount("SomeId", new AreaPath("123456.78.90.1234." + borough), "SomeAreaName", "SomeReportingUnitAreaName", true);
		when(getMessageProviderMock().get("@area_level[4].name")).thenReturn("Borough");
		CountController countController = makeNonAbstractCountController(count);

		String displayAreaName = countController.getDisplayAreaName();

		assertThat(displayAreaName).isEqualTo(borough + " SomeAreaName");
	}

	@Test
	public void isCountEditable_withPreviousNotApproved_returnsFalse() throws Exception {
		CountController countController = makeNonAbstractCountController(mock(Count.class));
		// when(getStartCountingControllerMock().isPreviousApproved(countController)).thenReturn(false);

		assertThat(countController.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withApproved_returnsFalse() throws Exception {
		Count count = mock(Count.class);
		when(count.isApproved()).thenReturn(true);
		CountController countController = makeNonAbstractCountController(count);

		assertThat(countController.isCountEditable()).isFalse();
	}

	@Test(enabled = false)
	public void isCountEditable_withNoCount_returnsFalse() throws Exception {
		CountController countController = makeNonAbstractCountController();
		assertThat(countController.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withNoNewOrSavedCount_returnsFalse() throws Exception {
		Count stub = mock(Count.class);
		CountController countController = makeNonAbstractCountController(stub);
		// when(getStartCountingControllerMock().isPreviousApproved(countController)).thenReturn(true);
		when(stub.isNew()).thenReturn(false);
		when(stub.isSaved()).thenReturn(false);

		assertThat(countController.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withNewCount_returnsTrue() throws Exception {
		Count stub = mock(Count.class, RETURNS_DEEP_STUBS);
		CountController countController = makeNonAbstractCountController(stub);
		when(stub.isEditable()).thenReturn(true);

		assertThat(countController.isCountEditable()).isTrue();
	}

	@Test(dataProvider = "getPreviousController")
	public void getPreviousController_parameterized_checkNotNull(int numTabs, int tabIndex, boolean isNotNull) throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		if (numTabs >= 0) {
			setupTabs(numTabs, false);
			ctrl.setTabIndex(tabIndex);
		}

		assertThat(ctrl.getPreviousController() != null).isEqualTo(isNotNull);
	}

	@Test(dataProvider = "isPreviousApproved")
	public void isPreviousApproved_parameterized_checkResult(int numTabs, int tabIndex, boolean isApproved, boolean result) throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		if (numTabs >= 0) {
			setupTabs(numTabs, isApproved);
			ctrl.setTabIndex(tabIndex);
		}

		assertThat(ctrl.isPreviousApproved()).isEqualTo(result);
	}

	@Test(dataProvider = "getNextController")
	public void getNextController_parameterized_checkNotNull(int numTabs, int tabIndex, boolean isNotNull) throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		if (numTabs >= 0) {
			setupTabs(numTabs, false);
			ctrl.setTabIndex(tabIndex);
		}

		assertThat(ctrl.getNextController() != null).isEqualTo(isNotNull);
	}

	@Test(dataProvider = "isNextApproved")
	public void isNextApproved_parameterized_checkResult(int numTabs, int tabIndex, boolean isApproved, boolean result) throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		if (numTabs >= 0) {
			setupTabs(numTabs, isApproved);
			ctrl.setTabIndex(tabIndex);
		}

		assertThat(ctrl.isNextApproved()).isEqualTo(result);
	}

	@Test
	public void isUserOnCountyLevel_givenStartCountingControllerIsUserOnCountyLevelIsTrue_returnsTrue() throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(true);
		assertThat(ctrl.isUserOnCountyLevel()).isTrue();
	}

	@Test
	public void isUserOnCountyLevel_givenStartCountingControllerIsUserOnCountyLevelIsFalse_returnsFalse() throws Exception {
		CountController ctrl = makeNonAbstractCountController();
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(false);
		assertThat(ctrl.isUserOnCountyLevel()).isFalse();
	}

	@Test
	public void getApproveDialog_returnsNull() throws Exception {
		CountController ctrl = makeNonAbstractCountController();

		assertThat(ctrl.getApproveDialog()).isNull();
	}

	private void setupTabs(int count, boolean approved) {
		List<Tab> tabs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Tab tab = mock(Tab.class, RETURNS_DEEP_STUBS);
			CountController ctrl = mock(CountController.class, RETURNS_DEEP_STUBS);
			when(ctrl.isApproved()).thenReturn(approved);
			when(tab.getController()).thenReturn(ctrl);

			tabs.add(tab);
		}
		when(getStartCountingControllerMock().getTabs()).thenReturn(tabs);
	}

	private CountController makeNonAbstractCountController() throws Exception {
		return makeNonAbstractCountController(null);
	}

	private CountController makeNonAbstractCountController(final Count count) throws Exception {
		CountController ctrl = new CountController() {
			@Override
			public void initCountController() {

			}

			@Override
			public void saveCount() {
			}

			@Override
			public void revokeApprovedCount() {

			}

			@Override
			public boolean isApproved() {
				return count.isApproved();
			}

			@Override
			public boolean isSplitBallotCounts() {
				return false;
			}

			@Override
			public Count getCount() {
				return count;
			}

			@Override
			public void setCount(Count count) {
				// do nothing
			}

			@Override
			public int getTotalBallotCountDifferenceFromPreviousCount() {
				return 0;
			}

			@Override
			public boolean isCommentRequired() {
				return false;
			}
		};
		ctrl = initializeMocks(ctrl);
		ElectionPath contestPath = new ElectionPath("730001.01.01.000001");
		when(getStartCountingControllerMock().getCountContext()).thenReturn(new CountContext(contestPath, CountCategory.VO));
		when(getStartCountingControllerMock().getCountCategory()).thenReturn(CountCategory.VO);
		when(getStartCountingControllerMock().getContestPath()).thenReturn(contestPath);
		when(getStartCountingControllerMock().getAreaPath()).thenReturn(new AreaPath("123456.78.90.1234.567890.0000"));
		return ctrl;
	}
}

