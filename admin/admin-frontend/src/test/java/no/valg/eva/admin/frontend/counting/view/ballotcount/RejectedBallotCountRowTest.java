package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.frontend.counting.ctrls.BaseCountControllerTest;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RejectedBallotCountRowTest extends BaseCountControllerTest {

	private RejectedBallotCountRow row;
	private RejectedBallotCount countStub;

	@BeforeMethod
	public void setUp() throws Exception {

		CountController ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		countStub = mock(RejectedBallotCount.class, RETURNS_DEEP_STUBS);

		when(countStub.getName()).thenReturn("name");
		when(countStub.getId()).thenReturn("id");
		MessageProvider mock = mock(MessageProvider.class, RETURNS_DEEP_STUBS);
		when(mock.get(anyString())).thenReturn("MyName");
		when(ctrlStub.getMessageProvider()).thenReturn(mock);

		row = new RejectedBallotCountRow(ctrlStub, countStub);
	}

	@Test
    public void getTitle_shouldReturnNameAndId() {
		assertThat(row.getTitle()).isEqualTo("MyName (id)");
	}

	@Test
    public void isCountInput_returnsTrue() {
		assertThat(row.isCountInput()).isTrue();
	}

	@Test
    public void getCount_shouldReturn10() {
		when(countStub.getCount()).thenReturn(10);

		assertThat(row.getCount()).isEqualTo(10);
	}

	@Test
    public void setCount_verifyRejectedBallotCountSetCount() {
		row.setCount(11);

		verify(countStub).setCount(11);
	}

	@Test
    public void getStyleClass_shouldReturnEmptyString() {
		assertThat(row.getStyleClass()).isEmpty();
	}

	@Test
    public void getRowStyleClass_shouldReturnRowStyleClass() {
		assertThat(row.getRowStyleClass()).isEqualTo("row_rejected row_rejected_id");
	}

}

