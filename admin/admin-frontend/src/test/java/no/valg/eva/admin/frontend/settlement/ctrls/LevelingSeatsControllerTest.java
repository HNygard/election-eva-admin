package no.valg.eva.admin.frontend.settlement.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary;
import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;



public class LevelingSeatsControllerTest extends BaseFrontendTest {

	@Test
	public void init_verifyState() throws Exception {
		LevelingSeatsController ctrl = initializeMocks(LevelingSeatsController.class);

		ctrl.init();

		verify(getInjectMock(LevelingSeatSettlementService.class)).levelingSeatSettlementSummary(eq(getUserDataMock()));
	}

	@Test
	public void distributeLevelingSeats_verifyDistributeAndInfoMessage() throws Exception {
		LevelingSeatsController ctrl = initializeMocks(LevelingSeatsController.class);

		ctrl.distributeLevelingSeats();

		verify(getInjectMock(LevelingSeatSettlementService.class)).distributeLevelingSeats(eq(getUserDataMock()));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@leveling_seats.performed");
	}

	@Test(dataProvider = "isStatusDone")
	public void isStatusDone_withDataProvider_verifyExpected(LevelingSeatSettlementSummary.Status status, boolean expected)
			throws Exception {
		LevelingSeatsController ctrl = initializeMocks(LevelingSeatsController.class);
		mockFieldValue("summary", new LevelingSeatSettlementSummary(status));

		assertThat(ctrl.isStatusDone()).isEqualTo(expected);
	}

	@DataProvider(name = "isStatusDone")
	public Object[][] isStatusDone() {
		return new Object[][] {
				{ LevelingSeatSettlementSummary.Status.DONE, true },
				{ LevelingSeatSettlementSummary.Status.NOT_READY, false },
				{ LevelingSeatSettlementSummary.Status.READY, false }
		};
	}

	@Test(dataProvider = "isStatusReady")
	public void isStatusReady_withDataProvider_verifyExpected(LevelingSeatSettlementSummary.Status status, boolean expected)
			throws Exception {
		LevelingSeatsController ctrl = initializeMocks(LevelingSeatsController.class);
		mockFieldValue("summary", new LevelingSeatSettlementSummary(status));

		assertThat(ctrl.isStatusReady()).isEqualTo(expected);
	}

	@DataProvider(name = "isStatusReady")
	public Object[][] isStatusReady() {
		return new Object[][] {
				{ LevelingSeatSettlementSummary.Status.DONE, false },
				{ LevelingSeatSettlementSummary.Status.NOT_READY, false },
				{ LevelingSeatSettlementSummary.Status.READY, true }
		};
	}
}

