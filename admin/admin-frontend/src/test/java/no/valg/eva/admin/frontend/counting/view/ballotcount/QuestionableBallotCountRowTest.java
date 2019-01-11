package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class QuestionableBallotCountRowTest {

	private QuestionableBallotCountRow row;
	private CountController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);

		row = new QuestionableBallotCountRow(ctrlStub);
	}

	@Test
	public void getTitle_shouldReturnQuestionable() throws Exception {
		assertThat(row.getTitle()).isEqualTo("@count.label.questionable");
	}

	@Test
	public void getProtocolCount_shouldReturn10() throws Exception {
		when(ctrlStub.getCounts().getQuestionableBallotCountForProtocolCounts()).thenReturn(10);

		assertThat(row.getProtocolCount()).isEqualTo(10);
	}

	@Test
	public void isCountInput_shouldReturnTrue() throws Exception {
		assertThat(row.isCountInput()).isTrue();
	}

	@Test
	public void getCount_shouldReturn11() throws Exception {
		when(ctrlStub.getCount().getQuestionableBallotCount()).thenReturn(11);

		assertThat(row.getCount()).isEqualTo(11);
	}

	@Test
	public void setCount_withValue_verifySetQuestionableBallotCountCalled() throws Exception {
		row.setCount(12);

		verify(ctrlStub.getCount()).setQuestionableBallotCount(12);
	}

	@Test
	public void getDiff_shouldReturn13() throws Exception {
		when(ctrlStub.getQuestionableBallotCountDifferenceFromPreviousCount()).thenReturn(13);

		assertThat(row.getDiff()).isEqualTo(13);
	}

	@Test
	public void getStyleClass_shouldReturnEmptyString() throws Exception {
		assertThat(row.getStyleClass()).isEmpty();
	}

	@Test
	public void getRowStyleClass_shouldReturnRowStyleClass() throws Exception {
		assertThat(row.getRowStyleClass()).isEqualTo("row_questionable");
	}
}

