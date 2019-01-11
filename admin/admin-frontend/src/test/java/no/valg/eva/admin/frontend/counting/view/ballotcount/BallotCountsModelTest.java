package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.frontend.counting.ctrls.BaseFinalCountController;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.counting.view.Tab;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BallotCountsModelTest {

	private BallotCountsModel model;
	private CountController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = mock(CountController.class, RETURNS_DEEP_STUBS);
		List<BallotCount> ballotCountStubs = getBallotCountStubs();
		List<RejectedBallotCount> rejectedBallotCountStubs = getRejectedBallotCountStubs();

		when(ctrlStub.getCount().getBallotCounts()).thenReturn(ballotCountStubs);
		when(ctrlStub.getCount().getRejectedBallotCounts()).thenReturn(rejectedBallotCountStubs);
		when(ctrlStub.getTab()).thenReturn(new Tab("id", "@title", "template", ctrlStub, true));

		MessageProvider messageProviderStub = mock(MessageProvider.class, RETURNS_DEEP_STUBS);
		when(messageProviderStub.get(anyString())).thenReturn("MyTitle");
		when(ctrlStub.getMessageProvider()).thenReturn(messageProviderStub);

		model = new BallotCountsModel(ctrlStub);
	}

	@Test(dataProvider = "construct")
    public void construct_parameterized_checkResult(boolean isSplitBallotCounts, boolean hasRejectedBallotCounts, int result) {
		when(ctrlStub.isSplitBallotCounts()).thenReturn(isSplitBallotCounts);
		when(ctrlStub.getCount().hasRejectedBallotCounts()).thenReturn(hasRejectedBallotCounts);
		model = new BallotCountsModel(ctrlStub);

		assertThat(model.size()).isEqualTo(result);
	}

	@Test
    public void isShowProtocolCount_shouldReturnFalse() {
		assertThat(model.isShowProtocolCount()).isFalse();
	}

	@Test
    public void getTotalBallotCountForProtocolCounts_shouldReturn10() {
		when(ctrlStub.getCounts().getTotalBallotCountForProtocolCounts()).thenReturn(10);

		assertThat(model.getTotalBallotCountForProtocolCounts()).isEqualTo(10);
	}

	@Test
    public void getTotalBallotCount_shouldReturn11() {
		when(ctrlStub.getCount().getTotalBallotCount()).thenReturn(11);

		assertThat(model.getTotalBallotCount()).isEqualTo(11);
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_shouldReturn12() {
		when(ctrlStub.getTotalBallotCountDifferenceFromPreviousCount()).thenReturn(12);

		assertThat(model.getTotalBallotCountDifferenceFromPreviousCount()).isEqualTo(12);
	}

	@Test
    public void getTabTitle_withFinalCountController_shouldReturnTabTitleAndFinalCountNr() {
		MessageProvider messageProviderStub = mock(MessageProvider.class, RETURNS_DEEP_STUBS);
		BaseFinalCountController ctrl = mock(BaseFinalCountController.class, RETURNS_DEEP_STUBS);
        when(messageProviderStub.get(any())).thenReturn("MyTitle");
		when(ctrl.getMessageProvider()).thenReturn(messageProviderStub);
		model = new BallotCountsModel(ctrl);

		assertThat(model.getTabTitle()).isEqualTo("MyTitle #0");
	}

	@Test
    public void getTabTitle_shouldReturnTabTitle() {
		assertThat(model.getTabTitle()).isEqualTo("@title");
	}

	private List<BallotCount> getBallotCountStubs() {
		List<BallotCount> counts = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			counts.add(new BallotCount("id" + i, "name" + i, 100 + i, 100 + i));
		}
		return counts;
	}

	private List<RejectedBallotCount> getRejectedBallotCountStubs() {
		List<RejectedBallotCount> counts = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			counts.add(new RejectedBallotCount("id" + i, "name" + i, 200 + i));
		}
		return counts;
	}

	@DataProvider(name = "construct")
	public static Object[][] construct() {
		return new Object[][] {
				{ false, false, 8 },
				{ false, true, 14 },
				{ true, false, 8 },
				{ true, true, 14 }
		};
	}

}

