package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TotalRejectedBallotCountRowTest {

	private TotalRejectedBallotCountRow row;

	@BeforeMethod
	public void setUp() throws Exception {
		CountController ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		when(ctrlStub.getCount().getTotalRejectedBallotCount()).thenReturn(10);
		row = new TotalRejectedBallotCountRow(ctrlStub);
	}

	@Test
	public void getTitle_shouldReturnTotalRejected() throws Exception {
		assertThat(row.getTitle()).isEqualTo("@count.ballot.totalRejected");
	}

	@Test
	public void getTitle_shouldReturn10() throws Exception {
		assertThat(row.getCount()).isEqualTo(10);
	}

	@Test
	public void getRowStyleClass_shouldReturnRowStyleClass() throws Exception {
		assertThat(row.getRowStyleClass()).isEqualTo("row_total_rejected_ballot_count");
	}
}

