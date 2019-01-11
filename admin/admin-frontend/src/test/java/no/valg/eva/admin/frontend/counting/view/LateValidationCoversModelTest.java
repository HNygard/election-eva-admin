package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class LateValidationCoversModelTest {

	private LateValidationCoversModel model;
	private PreliminaryCountController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new LateValidationCoversModel(ctrlStub);
	}

	@Test
	public void getTotalMarkOffCount_shouldReturnCtrlGetTotalMarkOffCount() throws Exception {
		when(ctrlStub.getTotalMarkOffCount()).thenReturn(10);

		assertThat(model.size()).isEqualTo(2);
		assertThat(model.getTotalMarkOffCount()).isEqualTo(10);
	}

	@Test
	public void getTitle_shouldReturnHardcodedTitle() {
		ModelRow row = model.get(0);
		assertThat(row.getTitle()).isEqualTo("@count.ballot.electoral_roll");
		row = model.get(1);
		assertThat(row.getTitle()).isEqualTo("@count.ballot.late_validation_covers");
	}

	@Test
	public void getCount_shouldReturnCountsMarkoffCountAndCountLateValidationCovers() {
		when(ctrlStub.getCounts().getMarkOffCount()).thenReturn(11);
		when(ctrlStub.getCount().getLateValidationCovers()).thenReturn(12);

		ModelRow row = model.get(0);
		assertThat(row.getCount()).isEqualTo(11);
		assertThat(row.getRowStyleClass()).isEqualTo("row_markoff_count");
		row = model.get(1);
		assertThat(row.getCount()).isEqualTo(12);
		assertThat(row.getRowStyleClass()).isEqualTo("row_late_validation_covers");
	}

	@Test
	public void setCount_withValue_shouldCallCountSetLateValidationCoversWithSameValue() {
		ModelRow row = model.get(0);
		row.setCount(10);
		row = model.get(1);
		row.setCount(10);

		verify(ctrlStub.getPreliminaryCount()).setLateValidationCovers(10);
	}

	@Test
	public void isCountInput_shouldReturnFalseAndTrue() {
		ModelRow row = model.get(0);
		assertThat(row.isCountInput()).isFalse();
		row = model.get(1);
		assertThat(row.isCountInput()).isFalse();
	}
}

