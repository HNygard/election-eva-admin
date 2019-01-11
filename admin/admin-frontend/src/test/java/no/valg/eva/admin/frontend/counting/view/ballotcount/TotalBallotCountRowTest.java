package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TotalBallotCountRowTest {

	private TotalBallotCountRow row;

	@BeforeMethod
	public void setUp() throws Exception {
		CountController ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		when(ctrlStub.getCounts().getOrdinaryBallotCountForProtocolCounts()).thenReturn(10);
		when(ctrlStub.getCount().getOrdinaryBallotCount()).thenReturn(11);
		when(ctrlStub.getOrdinaryBallotCountDifferenceFromPreviousCount()).thenReturn(12);
		when(ctrlStub.getCount().getModifiedBallotCount()).thenReturn(13);
		when(ctrlStub.getCount().getUnmodifiedBallotCount()).thenReturn(14);
		row = new TotalBallotCountRow(ctrlStub);
	}

	@Test
	public void getTitle_shouldReturnTotalBallotCounts() throws Exception {
		assertThat(row.getTitle()).isEqualTo("@count.label.totalBallotCounts");
	}

	@Test
	public void getProtocolCount_shouldReturn10() throws Exception {
		assertThat(row.getProtocolCount()).isEqualTo(10);
	}

	@Test
	public void getCount_shouldReturn11() throws Exception {
		assertThat(row.getCount()).isEqualTo(11);
	}

	@Test
	public void getDiff_shouldReturn12() throws Exception {
		assertThat(row.getDiff()).isEqualTo(12);
	}

	@Test
	public void getModifiedCount_shouldReturn13() throws Exception {
		assertThat(row.getModifiedCount()).isEqualTo(13);
	}

	@Test
	public void getUnmodifiedBallotCount_shouldReturn14() throws Exception {
		assertThat(row.getUnmodifiedCount()).isEqualTo(14);
	}

	@Test
	public void getRowStyleClass_shouldReturnRowStyleClass() throws Exception {
		assertThat(row.getRowStyleClass()).isEqualTo("row_total_ballot_count");
	}

}

