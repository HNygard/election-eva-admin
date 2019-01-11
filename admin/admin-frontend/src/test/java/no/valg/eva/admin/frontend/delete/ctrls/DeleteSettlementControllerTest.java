package no.valg.eva.admin.frontend.delete.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DeleteSettlementControllerTest extends BaseFrontendTest {

	@Test
	public void deleteSettlement_withEvoteException_shouldAddErrorMessage() throws Exception {
		DeleteSettlementController ctrl = initializeMocks(DeleteSettlementController.class);
		MvElection mvElection = mockField("mvElection", MvElection.class);
		mockField("mvArea", MvArea.class);
		evoteException("@evote").when(mvElection).electionPath();

		ctrl.deleteSettlement();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote");
	}

	@Test
	public void deleteSettlement_shouldDeleteSettlement() throws Exception {
		DeleteSettlementController ctrl = initializeMocks(DeleteSettlementController.class);
		MvElection mvElection = mockField("mvElection", MvElection.class);
		MvArea mvArea = mockField("mvArea", MvArea.class);
		when(mvElection.toString()).thenReturn("Valg");
		when(mvArea.toString()).thenReturn("Oslo");

		ctrl.deleteSettlement();

		verify(getInjectMock(SettlementService.class)).deleteSettlements(eq(getUserDataMock()), any(ElectionPath.class), any(AreaPath.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@delete.settlement.confirmation, Valg, Oslo]");
	}
}

