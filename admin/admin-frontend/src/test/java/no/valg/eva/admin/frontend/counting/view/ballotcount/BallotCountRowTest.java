package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.BallotCount;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class BallotCountRowTest {

	private BallotCountRow row;
	private BallotCount ballotCountStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ballotCountStub = mock(BallotCount.class, RETURNS_DEEP_STUBS);

		when(ballotCountStub.getName()).thenReturn("@some.name");
		when(ballotCountStub.getId()).thenReturn("id");

		row = new BallotCountRow(ballotCountStub);
	}

	@Test
	public void getTitle_shouldReturnBallotCountName() throws Exception {
		assertThat(row.getTitle()).isEqualTo("@some.name");
		assertThat(row.getBallotCount()).isSameAs(ballotCountStub);
	}

	@Test
	public void getId_shouldReturnBallotCountId() throws Exception {
		assertThat(row.getId()).isEqualTo("id");
	}

	@Test
	public void getProtocolCount_shouldReturnNull() throws Exception {
		assertThat(row.getProtocolCount()).isNull();
	}

	@Test
	public void isCountInput_shouldReturnTrue() throws Exception {
		assertThat(row.isCountInput()).isTrue();
	}

	@Test
	public void getCount_shouldReturn10() throws Exception {
		when(ballotCountStub.getCount()).thenReturn(10);

		assertThat(row.getCount()).isEqualTo(10);
	}

	@Test
	public void setCount_withValue_verifySetUnmodifiedCountCalled() throws Exception {
		row.setCount(12);

		verify(ballotCountStub).setUnmodifiedCount(12);
	}

	@Test
	public void getDiff_shouldReturnNull() throws Exception {
		assertThat(row.getDiff()).isNull();
	}

	@Test
	public void getStyleClass_shouldReturnEmptyString() throws Exception {
		assertThat(row.getStyleClass()).isEmpty();
	}

	@Test
	public void getRowStyleClass_shouldReturnRowStyleClass() throws Exception {
		assertThat(row.getRowStyleClass()).isEqualTo("row_ballot row_ballot_id");
	}
}

