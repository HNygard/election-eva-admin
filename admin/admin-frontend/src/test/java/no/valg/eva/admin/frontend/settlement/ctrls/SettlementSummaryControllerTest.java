package no.valg.eva.admin.frontend.settlement.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.settlement.model.BallotCount;
import no.valg.eva.admin.common.settlement.model.BallotCountSummary;
import no.valg.eva.admin.common.settlement.model.BallotInfo;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;
import no.valg.eva.admin.common.settlement.model.SplitBallotCount;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.settlement.SummaryRow;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class SettlementSummaryControllerTest extends BaseFrontendTest {

	@DataProvider(name = "prevSettlementSummaryPage")
	public static Object[][] prevSettlementSummaryPage() {
		return new Object[][] {
				{ 0, 0 },
				{ 1, 0 },
				{ 3, 1 }
		};
	}

	@DataProvider(name = "nextSettlementSummaryPage")
	public static Object[][] nextSettlementSummaryPage() {
		return new Object[][] {
				{ 6, 0, 2 },
				{ 4, 0, 1 },
				{ 4, 1, 1 },
				{ 3, 0, 0 }
		};
	}

	@DataProvider(name = "button")
	public static Object[][] button() {
		return new Object[][] {
				{ 0, ButtonType.PREV, true, true },
				{ 1, ButtonType.PREV, false, true },
				{ 0, ButtonType.NEXT, false, true },
				{ 10, ButtonType.NEXT, true, true }
		};
	}

	@Test
	public void initView_withStubs_checkThatSettlementSummaryIsSet() throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		mockField("contestInfo", ContestInfo.class);
		SettlementSummary summaryStub = createMock(SettlementSummary.class);
		when(getInjectMock(SettlementService.class).settlementSummary(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(summaryStub);
		ctrl.setSettlementDone(true);

		ctrl.initView();

		assertThat(ctrl.getSettlementSummary()).isSameAs(summaryStub);
	}

	@Test
	public void backToSelectContest_shouldReturnRedirectURL() throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);

		assertThat(ctrl.backToSelectContest()).isEqualTo("settlementSummary.xhtml?faces-redirect=true");
	}

	@Test(dataProvider = "prevSettlementSummaryPage")
	public void prevSettlementSummaryPage_withDataProvider_verifyExpected(int initial, int expected) throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		setIndex(initial);

		ctrl.prevSettlementSummaryPage();

		assertThat(getPrivateField("countCategoryIndexForSettlementSummary", int.class)).isEqualTo(expected);
	}

	@Test(dataProvider = "nextSettlementSummaryPage")
	public void nextSettlementSummaryPage_with_should(int summarySize, int initial, int expected) throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		SettlementSummary summaryStub = mockField("settlementSummary", SettlementSummary.class);
		setIndex(initial);
		when(summaryStub.getCountCategories().size()).thenReturn(summarySize);

		ctrl.nextSettlementSummaryPage();

		assertThat(getPrivateField("countCategoryIndexForSettlementSummary", int.class)).isEqualTo(expected);
	}

	@Test
	public void getSummaryRow_withBallotCountSummary_checkReturnValues() throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		SettlementSummary summaryStub = mockField("settlementSummary", SettlementSummary.class);
		BallotInfo info = new BallotInfo("H", "Høyre");
		List<BallotCount> list = new ArrayList<>();
		list.add(new SplitBallotCount(CountCategory.FO, 10, 15));
		BallotCountSummary<BallotCount> summary = new BallotCountSummary<>(info, list);
		when(summaryStub.getCountCategories().size()).thenReturn(3);
		when(summaryStub.getCountCategories().get(anyInt())).thenReturn(CountCategory.FO);

		SummaryRow row = ctrl.getSummaryRow(summary);

		assertThat(row.getStyleClass()).isEqualTo("data");
		assertThat(row.getId()).isEqualTo("H");
		assertThat(row.getName()).isEqualTo("Høyre");
		assertThat(row.getCategoryName(0)).isEqualTo("@vote_count_category[FO].name");
		assertThat(row.getModifiedCount(0)).isEqualTo("10");
		assertThat(row.getUnmodifiedCount(0)).isEqualTo("15");
		assertThat(row.getCount(0)).isEqualTo("25");
		assertThat(row.getTotalModifiedCount()).isEqualTo("10");
		assertThat(row.getTotalUnmodifiedCount()).isEqualTo("15");
		assertThat(row.getTotalCount()).isEqualTo("25");

	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(int initialIndex, ButtonType type, boolean expectedDisabled, boolean expectedRendered) throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		setIndex(initialIndex);
		SettlementSummary summaryStub = mockField("settlementSummary", SettlementSummary.class);
		when(summaryStub.getCountCategories().size()).thenReturn(10);

		Button button = ctrl.button(type);

		assertThat(button.isDisabled()).isEqualTo(expectedDisabled);
		assertThat(button.isRendered()).isEqualTo(expectedRendered);
	}

	@Test
	public void button_withPrevAndIndex1_disabledFalseAndRenderedTrue() throws Exception {
		SettlementSummaryController ctrl = initializeMocks(SettlementSummaryController.class);
		setIndex(1);

		Button button = ctrl.button(ButtonType.PREV);

		assertThat(button.isDisabled()).isFalse();
		assertThat(button.isRendered()).isTrue();
	}

	@Test
    public void getSettlementSummary_with_should() {

	}

	private void setIndex(int index) throws Exception {
		mockFieldValue("countCategoryIndexForSettlementSummary", index);
	}
}

