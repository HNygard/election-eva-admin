package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.common.MarkupUtils;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MarkOffCountsModelForAllPollingDistrictsTest {
	private MarkOffCountsModelForAllPollingDistricts model;
	private PreliminaryCountController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(PreliminaryCountController.class, RETURNS_DEEP_STUBS);
		model = new MarkOffCountsModelForAllPollingDistricts(ctrlStub);
	}

	@Test
	public void getTotalMarkOffCount_checkGetTotalMarkOffCount() throws Exception {
		when(ctrlStub.getTotalMarkOffCount()).thenReturn(11);

		int totalMarkOffCount = model.get(0).getTotalMarkOffCount();

		assertThat(totalMarkOffCount).isEqualTo(11);
	}

	@Test
	public void getTotalBallotCountForAllPollingDistricts_checkGetTotalBallotCountForAllPollingDistricts() throws Exception {
		when(ctrlStub.getPreliminaryCount().getTotalBallotCountForAllPollingDistricts()).thenReturn(11);

		Integer totalBallotCountForAllPollingDistricts = model.get(0).getTotalBallotCountForAllPollingDistricts();

		assertThat(totalBallotCountForAllPollingDistricts).isEqualTo(11);
	}

	@Test
	public void getTotalBallotCountDifferenceForAllPollingDistricts_checkGetTotalBallotCountDifferenceForAllPollingDistricts() throws Exception {
		when(ctrlStub.getPreliminaryCount().getTotalBallotCountForAllPollingDistricts()).thenReturn(11);
		when(ctrlStub.getPreliminaryCount().getMarkOffCount()).thenReturn(10);
		when(ctrlStub.getPreliminaryCount().getLateValidationCovers()).thenReturn(3);

		Integer totalBallotCountDifferenceForAllPollingDistricts = model.get(0).getTotalBallotCountDifferenceForAllPollingDistricts();

		assertThat(totalBallotCountDifferenceForAllPollingDistricts).isEqualTo(4);
	}

	@Test
	public void getDiffStyleClass_whenPositiveDifference_returnsCorrectClass() throws Exception {
		when(ctrlStub.getPreliminaryCount().getTotalBallotCountForAllPollingDistricts()).thenReturn(11);
		when(ctrlStub.getPreliminaryCount().getMarkOffCount()).thenReturn(10);
		when(ctrlStub.getPreliminaryCount().getLateValidationCovers()).thenReturn(3);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(1));
	}

	@Test
	public void getDiffStyleClass_whenNegativeDifference_returnsCorrectClass() throws Exception {
		when(ctrlStub.getPreliminaryCount().getTotalBallotCountForAllPollingDistricts()).thenReturn(11);
		when(ctrlStub.getPreliminaryCount().getMarkOffCount()).thenReturn(15);
		when(ctrlStub.getPreliminaryCount().getLateValidationCovers()).thenReturn(3);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(-1));
	}

	@Test
	public void getDiffStyleClass_whenNoDifference_returnsCorrectClass() throws Exception {
		when(ctrlStub.getPreliminaryCount().getTotalBallotCountForAllPollingDistricts()).thenReturn(11);
		when(ctrlStub.getPreliminaryCount().getMarkOffCount()).thenReturn(14);
		when(ctrlStub.getPreliminaryCount().getLateValidationCovers()).thenReturn(3);

		String diffStyleClass = model.get(0).getDiffStyleClass();

		assertThat(diffStyleClass).isEqualTo(MarkupUtils.getClass(0));
	}
}

