package no.valg.eva.admin.frontend.delete.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


public class DeleteLevelingSeatSettlementControllerTest extends BaseFrontendTest {

	@Test
	public void deleteLevelingSeatSettlement_verifyDeleted() throws Exception {
		DeleteLevelingSeatSettlementController ctrl = initializeMocks(DeleteLevelingSeatSettlementController.class);

		ctrl.deleteLevelingSeatSettlement();

		verify(getInjectMock(LevelingSeatSettlementService.class)).deleteLevelingSeatSettlement(eq(getUserDataMock()));
		assertThat(ctrl.isDeleted()).isTrue();
	}
}

