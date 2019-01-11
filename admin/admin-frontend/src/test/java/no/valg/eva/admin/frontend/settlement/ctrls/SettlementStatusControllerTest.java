package no.valg.eva.admin.frontend.settlement.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.settlement.model.CountingArea;
import no.valg.eva.admin.common.settlement.model.SettlementStatus;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SettlementStatusControllerTest extends BaseFrontendTest {

	@DataProvider(name = "renderCountingModeInSettlementStatus")
	public static Object[][] renderCountingModeInSettlementStatus() {
		return new Object[][] {
				{ true, true, true },
				{ true, false, true },
				{ false, true, true },
				{ false, false, false }
		};
	}

	@Test
	public void initView_withTwoElementsInCategoryStatusMap_shouldReturnTwoItemInStatusList() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class), getCategoryMap(true, CountCategory.FO, CountCategory.BF));

		assertThat(ctrl.getSettlementStatusList()).hasSize(2);
		assertThat(ctrl.getSettlementStatusList().get(0).getCountCategory()).isSameAs(CountCategory.FO);
		assertThat(ctrl.getSettlementStatusList().get(1).getCountCategory()).isSameAs(CountCategory.BF);
	}

	@Test(enabled = false)
	public void makeSettlement_withReadySettlement_shouldMakeSettlementAndAddCreatedMessage() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class), getCategoryMap(true, CountCategory.FO));
		mockFieldValue("settlement", null);

		ctrl.makeSettlement();

		verify(getInjectMock(SettlementService.class)).createSettlement(eq(getUserDataMock()), any(ElectionPath.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@settlement.make_settlement_done");
	}

	@Test
	public void isReadyForSettlement_withNonReadyStatus_returnsFalse() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class), getCategoryMap(false, CountCategory.FO));

		assertThat(ctrl.isReadyForSettlement()).isFalse();
	}

	@Test
	public void backToSelectContest_returnsRedirectURL() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class), getCategoryMap(true, CountCategory.FO));

		assertThat(ctrl.backToSelectContest()).isEqualTo("settlementStatus.xhtml?faces-redirect=true");
	}

	@Test(dataProvider = "renderCountingModeInSettlementStatus")
	public void renderCountingModeInSettlementStatus_withDataProvider_verifyExpected(boolean isMunicipalityLevel, boolean isBoroughLevel, boolean expected)
			throws Exception {
		ContestInfo contestInfoStub = createMock(ContestInfo.class);
		when(contestInfoStub.getAreaPath().isMunicipalityLevel()).thenReturn(isMunicipalityLevel);
		when(contestInfoStub.getAreaPath().isBoroughLevel()).thenReturn(isBoroughLevel);
		SettlementStatusController ctrl = initViewWith(contestInfoStub, getCategoryMap(true, CountCategory.FO));

		assertThat(ctrl.renderCountingModeInSettlementStatus()).isEqualTo(expected);
	}

	@Test
	public void selectSettlementStatusForCountingAreasNotReady_withCountCategoryFO_getSelectedCountingAreasShouldReturnThreeItems() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class),
				getCategoryMap(CountStatus.APPROVED, CountStatus.APPROVED, CountStatus.TO_SETTLEMENT, CountStatus.SAVED, CountStatus.SAVED, CountStatus.SAVED));

		ctrl.selectSettlementStatusForCountingAreasNotReady(CountCategory.FO);

		assertThat(ctrl.getSelectedCountingAreas()).hasSize(3);
	}

	@Test
	public void selectSettlementStatusForCountingAreasApproved_withCountCategoryFO_getSelectedCountingAreasShouldReturnTwoItems() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class),
				getCategoryMap(CountStatus.APPROVED, CountStatus.APPROVED, CountStatus.TO_SETTLEMENT, CountStatus.SAVED, CountStatus.SAVED, CountStatus.SAVED));

		ctrl.selectSettlementStatusForCountingAreasApproved(CountCategory.FO);

		assertThat(ctrl.getSelectedCountingAreas()).hasSize(2);
	}

	@Test
	public void selectSettlementStatusForCountingAreasReadyForSettlement_withCountCategoryFO_getSelectedCountingAreasShouldReturnOneItem() throws Exception {
		SettlementStatusController ctrl = initViewWith(createMock(ContestInfo.class),
				getCategoryMap(CountStatus.APPROVED, CountStatus.APPROVED, CountStatus.TO_SETTLEMENT, CountStatus.SAVED, CountStatus.SAVED, CountStatus.SAVED));

		ctrl.selectSettlementStatusForCountingAreasReadyForSettlement(CountCategory.FO);

		assertThat(ctrl.getSelectedCountingAreas()).hasSize(1);
	}

	@Test(dataProvider = "getName")
	public void getName_withDataProvider_verifyExpected(String boroughName, String pollingDistrictName, String expected) {
		SettlementStatusController ctrl = new SettlementStatusController();
		CountingArea countingArea = new CountingArea("Hordaland", boroughName, pollingDistrictName, CountStatus.APPROVED);

		assertThat(ctrl.getName(countingArea)).isEqualTo(expected);
	}

	@DataProvider(name = "getName")
	public Object[][] getName() {
		return new Object[][] {
				{ "Hele kommunen", "Hele kommunen", "Hordaland, Hele kommunen" },
				{ "Hele kommunen", "Etne", "Hordaland, Hele kommunen, Etne" },
		};
	}

	private Map<CountCategory, SettlementStatus> getCategoryMap(boolean ready, CountCategory... categories) {
		Map<CountCategory, SettlementStatus> map = new HashMap<>();
		for (CountCategory category : categories) {
			SettlementStatus settlementStatusStub = createMock(SettlementStatus.class);
			when(settlementStatusStub.isCountingAreasNotReadyForSettlement()).thenReturn(!ready);
			when(settlementStatusStub.getCountCategory()).thenReturn(category);
			map.put(category, settlementStatusStub);
		}
		return map;
	}

	private Map<CountCategory, SettlementStatus> getCategoryMap(CountStatus... statuses) {
		List<CountingArea> countingAreaList = new ArrayList<>();
		for (CountStatus status : statuses) {
			CountingArea areaStub = createMock(CountingArea.class);
			when(areaStub.getCountStatus()).thenReturn(status);
			countingAreaList.add(areaStub);
		}
		SettlementStatus statusStub = createMock(SettlementStatus.class);
		when(statusStub.getCountingAreaList()).thenReturn(countingAreaList);
		Map<CountCategory, SettlementStatus> map = new HashMap<>();
		map.put(CountCategory.FO, statusStub);
		return map;
	}

	private SettlementStatusController initViewWith(ContestInfo contestInfo, Map<CountCategory, SettlementStatus> statuses) throws Exception {
		SettlementStatusController ctrl = initializeMocks(SettlementStatusController.class);
		ElectionPath contestPath = ElectionPath.from("752900.01.02.000303");
		mockFieldValue("contestInfo", contestInfo);
		when(contestInfo.getElectionPath()).thenReturn(contestPath);
		when(getInjectMock(SettlementService.class).settlementStatusMap(eq(getUserDataMock()), eq(contestPath)))
				.thenReturn(statuses);
		ctrl.initView();
		return ctrl;
	}
}

