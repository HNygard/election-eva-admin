package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.common.MarkupUtils;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MarkOffCountsModelTest {
	private MarkOffCountsModel model;
	private CountController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);
	}

	@Test
	public void isShowProtocolCount_withIncludeProtocolCount_returnsTrue() {
		when(ctrlStub.isIncludeProtocolCount()).thenReturn(true);

		assertThat(model.size()).isEqualTo(1);
		assertThat(model.isShowProtocolCount()).isTrue();

	}

	@Test
	public void isShowProtocolCount_withoutIncludeProtocolCount_returnsFalse() {
		when(ctrlStub.isIncludeMarkOffCount()).thenReturn(false);

		assertThat(model.size()).isEqualTo(1);
		assertThat(model.isShowProtocolCount()).isFalse();
	}

	@Test
	public void getTotalProtocolCount_checkGetTotalBallotCountForProtocolCounts() throws Exception {
		when(ctrlStub.getCounts().getTotalBallotCountForProtocolCounts()).thenReturn(10);

		MarkOffCountsModel.MarkOffCountsRow row = model.get(0);
		assertThat(row.getTotalProtocolCount()).isEqualTo(10);
	}

	@Test
	public void getTotalMarkOffCount_checkGetTotalMarkOffCount() throws Exception {
		when(ctrlStub.getTotalMarkOffCount()).thenReturn(11);

		MarkOffCountsModel.MarkOffCountsRow row = model.get(0);
		assertThat(row.getTotalMarkOffCount()).isEqualTo(11);
	}

	@Test
	public void getTotalBallotCount_checkGetTotalBallotCount() throws Exception {
		when(ctrlStub.getCount().getTotalBallotCount()).thenReturn(12);

		MarkOffCountsModel.MarkOffCountsRow row = model.get(0);
		assertThat(row.getTotalBallotCount()).isEqualTo(12);
	}

	@Test
	public void getTotalBallotCountDifferenceFromPreviousCount_checkGetTotalBallotCountDifferenceFromPreviousCount() throws Exception {
		when(ctrlStub.getTotalBallotCountDifferenceFromPreviousCount()).thenReturn(13);

		MarkOffCountsModel.MarkOffCountsRow row = model.get(0);
		assertThat(row.getTotalBallotCountDifferenceFromPreviousCount()).isEqualTo(13);
		assertThat(row.getDiffStyleClass()).isEqualTo("diff-pos");
	}

	@Test
	public void isShowTotalMarkOffCount_whenNoTotalMarkOffCount_returnsFalse() throws Exception {
		when(ctrlStub.getTotalMarkOffCount()).thenReturn(null);

		boolean showTotalMarkOffCount = model.isShowTotalMarkOffCount();

		assertThat(showTotalMarkOffCount).isFalse();
	}

	@Test
	public void isShowTotalMarkOffCount_whenTotalMarkOffCount_returnsTrue() throws Exception {
		when(ctrlStub.getTotalMarkOffCount()).thenReturn(1);

		boolean showTotalMarkOffCount = model.isShowTotalMarkOffCount();

		assertThat(showTotalMarkOffCount).isTrue();
	}

	@Test
	public void isShowTotalMarkOffCount_whenPreliminaryCountControllerAndNoTotalMarkOffCount_returnsFalse() throws Exception {
		ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getTotalMarkOffCount()).thenReturn(null);

		boolean showTotalMarkOffCount = model.isShowTotalMarkOffCount();

		assertThat(showTotalMarkOffCount).isFalse();
	}

	@Test
	public void isShowTotalMarkOffCount_whenPreliminaryCountControllerTotalMarkOffCountAndNoExpectedBallotCount_returnsTrue() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getTotalMarkOffCount()).thenReturn(1);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(null);

		boolean showTotalMarkOffCount = model.isShowTotalMarkOffCount();

		assertThat(showTotalMarkOffCount).isTrue();
	}

	@Test
	public void isShowTotalMarkOffCount_whenPreliminaryCountControllerTotalMarkOffCountAndExpectedBallotCount_returnsFalse() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getTotalMarkOffCount()).thenReturn(1);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		boolean showTotalMarkOffCount = model.isShowTotalMarkOffCount();

		assertThat(showTotalMarkOffCount).isFalse();
	}

	@Test
	public void getExpectedBallotCount_whenNotPreliminaryCountController_returnsNull() throws Exception {
		Integer expectedBallotCount = model.get(0).getExpectedBallotCount();

		assertThat(expectedBallotCount).isNull();
	}

	@Test
	public void getExpectedBallotCount_whenPreliminaryCountController_returnsExpectedBallotCount() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		Integer expectedBallotCount = model.get(0).getExpectedBallotCount();

		assertThat(expectedBallotCount).isEqualTo(1);
	}

	@Test
	public void setExpectedBallotCount_whenPreliminaryCountController_modifiesExpectedBallotCountOnPreliminaryCount() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		model.get(0).setExpectedBallotCount(1);

		verify(ctrlStub.getPreliminaryCount()).setExpectedBallotCount(1);
	}

	@Test
	public void getExpectedBallotCountDifference_whenPreliminaryCountController_returnsExpectedBallotCountDifference() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getPreliminaryCount().getTotalBallotCount()).thenReturn(2);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		Integer expectedBallotCountDifference = model.get(0).getExpectedBallotCountDifference();

		assertThat(expectedBallotCountDifference).isEqualTo(1);
	}

	@Test
	public void getExpectedBallotCountDifference_whenNotPreliminaryCountController_returnsNull() throws Exception {
		Integer expectedBallotCountDifference = model.get(0).getExpectedBallotCountDifference();

		assertThat(expectedBallotCountDifference).isNull();
	}

	@Test
	public void getDiffStyleClass_whenPositiveDifference_returnsCorrectClass() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getPreliminaryCount().getTotalBallotCount()).thenReturn(2);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(1));
	}

	@Test
	public void getDiffStyleClass_whenNegativeDifference_returnsCorrectClass() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getPreliminaryCount().getTotalBallotCount()).thenReturn(1);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(2);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(-1));
	}

	@Test
	public void getDiffStyleClass_whenNoDifference_returnsCorrectClass() throws Exception {
		PreliminaryCountController ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModel(ctrlStub);

		when(ctrlStub.getPreliminaryCount().getTotalBallotCount()).thenReturn(1);
		when(ctrlStub.getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(0));
	}
}

