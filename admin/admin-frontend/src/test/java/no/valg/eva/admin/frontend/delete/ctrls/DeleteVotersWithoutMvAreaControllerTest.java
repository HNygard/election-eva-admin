package no.valg.eva.admin.frontend.delete.ctrls;

import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


public class DeleteVotersWithoutMvAreaControllerTest extends BaseFrontendTest {

	@Test
	public void deleteVotersWithoutMvArea_withEvoteException_shouldAddErrorMessage() throws Exception {
		DeleteVotersWithoutMvAreaController ctrl = initializeMocks(DeleteVotersWithoutMvAreaController.class);
		evoteExceptionWhen(VoterService.class, "@evote").deleteVotersWithoutMvArea(eq(getUserDataMock()), anyLong());

		ctrl.deleteVotersWithoutMvArea();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote");
	}

	@Test
	public void deleteVotersWithoutMvArea_shouldDeleteVoters() throws Exception {
		DeleteVotersWithoutMvAreaController ctrl = initializeMocks(DeleteVotersWithoutMvAreaController.class);

		ctrl.deleteVotersWithoutMvArea();

		verify(getInjectMock(VoterService.class)).deleteVotersWithoutMvArea(eq(getUserDataMock()), anyLong());
		assertThat(ctrl.isDeleted()).isTrue();
	}
}

