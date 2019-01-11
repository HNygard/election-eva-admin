package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.BallotCount;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class BallotCountWithSplitRowTest {

	private BallotCountWithSplitRow row;
	private BallotCount ballotCountStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ballotCountStub = mock(BallotCount.class, RETURNS_DEEP_STUBS);

		row = new BallotCountWithSplitRow(ballotCountStub);
	}

	@Test
	public void isCountInput_shouldReturnFalse() throws Exception {
		assertThat(row.isCountInput()).isFalse();
	}

	@Test
	public void isModifiedCountInput_shouldReturnTrue() throws Exception {
		assertThat(row.isModifiedCountInput()).isTrue();
	}

	@Test
	public void isUnmodifiedCountInput_shouldReturnTrue() throws Exception {
		assertThat(row.isUnmodifiedCountInput()).isTrue();
	}
	
	@Test
	public void getModifiedCount_shouldReturn10() throws Exception {
		when(ballotCountStub.getModifiedCount()).thenReturn(10);
		
		assertThat(row.getModifiedCount()).isEqualTo(10);
	}

	@Test
	public void getUnmodifiedCount_shouldReturn11() throws Exception {
		when(ballotCountStub.getUnmodifiedCount()).thenReturn(11);

		assertThat(row.getUnmodifiedCount()).isEqualTo(11);
	}

	@Test
	public void setModifiedCount_withValue_verifySetModifiedCountCalled() throws Exception {
		row.setModifiedCount(12);

		verify(ballotCountStub).setModifiedCount(12);
	}

	@Test
	public void setUnmodifiedCount_withValue_verifySetUnmodifiedCountCalled() throws Exception {
		row.setUnmodifiedCount(13);

		verify(ballotCountStub).setUnmodifiedCount(13);
	}

	
	
}

