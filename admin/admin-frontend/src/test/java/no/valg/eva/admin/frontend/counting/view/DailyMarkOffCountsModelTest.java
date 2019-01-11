package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;
import no.valg.eva.admin.frontend.counting.ctrls.ProtocolAndPreliminaryCountController;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



public class DailyMarkOffCountsModelTest extends BaseFrontendTest {

	private DailyMarkOffCountsModel model;
	private CountController ctrlStub;
	private ProtocolAndPreliminaryCountController preliminaryCountControllerStub;
	private List<DailyMarkOffCount> counts;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		preliminaryCountControllerStub = mock(ProtocolAndPreliminaryCountController.class, RETURNS_DEEP_STUBS);

		LocalDate currentTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate("2014-01-01");
		counts = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			counts.add(new DailyMarkOffCount(currentTime, i + 100));
			currentTime = currentTime.plusDays(1);
		}
		when(ctrlStub.getDailyMarkOffCounts()).thenReturn(new DailyMarkOffCounts(counts));
		when(preliminaryCountControllerStub.getDailyMarkOffCounts()).thenReturn(new DailyMarkOffCounts(counts));

		model = new MyDailyMarkOffCountsModel(ctrlStub);
	}

	@Test
	public void getSumMarkOffCount_shouldReturnSumOfGetMarkOffCount() throws Exception {
		assertThat(model.size()).isEqualTo(5);
		assertThat(model.getSumMarkOffCount()).isEqualTo(510);
	}

	@Test
	public void getPreviousTabTitle_withProtocolAndPreliminaryCountController_shouldReturnPrelimText() throws Exception {
		model = new DailyMarkOffCountsModel(preliminaryCountControllerStub);

		assertThat(model.getPreviousTabTitle()).isEqualTo("@count.tab.type[P].approved");
	}

	@Test
	public void getPreviousTabTitle_withPreliminaryCountController_shouldReturnPrelimText() throws Exception {
		model = new DailyMarkOffCountsModel(createMock(PreliminaryCountController.class));

		assertThat(model.getPreviousTabTitle()).isEqualTo("@count.tab.type[P].approved");
	}

	@Test
	public void getPreviousTabTitle_shouldReturnPreviousTabTitle() throws Exception {
		when(ctrlStub.getPreviousTab().getTitle()).thenReturn("@some.title");

		assertThat(model.getPreviousTabTitle()).isEqualTo("@some.title");
	}

	@Test
	public void getTitle_shouldReturnFormattedDate() throws Exception {
		assertThat(model.get(0).getTitle()).isEqualTo("onsdag 1 jan. 2014");
		assertThat(model.get(1).getTitle()).isEqualTo("torsdag 2 jan. 2014");
		assertThat(model.get(2).getTitle()).isEqualTo("fredag 3 jan. 2014");
		assertThat(model.get(3).getTitle()).isEqualTo("lørdag 4 jan. 2014");
		assertThat(model.get(4).getTitle()).isEqualTo("søndag 5 jan. 2014");
	}

	@Test
	public void getCount_shouldReturnMarkOffCount() throws Exception {
		assertThat(model.get(0).getCount()).isEqualTo(100);
		assertThat(model.get(0).getRowStyleClass()).isEqualTo("row_daily_markoff_count");
		assertThat(model.get(1).getCount()).isEqualTo(101);
		assertThat(model.get(1).getRowStyleClass()).isEqualTo("row_daily_markoff_count");
		assertThat(model.get(2).getCount()).isEqualTo(102);
		assertThat(model.get(2).getRowStyleClass()).isEqualTo("row_daily_markoff_count");
		assertThat(model.get(3).getCount()).isEqualTo(103);
		assertThat(model.get(3).getRowStyleClass()).isEqualTo("row_daily_markoff_count");
		assertThat(model.get(4).getCount()).isEqualTo(104);
		assertThat(model.get(4).getRowStyleClass()).isEqualTo("row_daily_markoff_count");
	}

	@Test
	public void setCount_shouldCallDailyMarkOffCountSetMarkOffCount() throws Exception {
		model.get(2).setCount(100);

		assertThat(counts.get(2).getMarkOffCount()).isEqualTo(100);
	}

	class MyDailyMarkOffCountsModel extends DailyMarkOffCountsModel {
		MyDailyMarkOffCountsModel(CountController ctrl) {
			super(ctrl);
		}

		@Override
		Locale getLocale() {
			return new Locale("no");
		}
	}

}

