package no.valg.eva.admin.frontend.delete.ctrls;

import no.evote.security.UserData;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoterService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


public class DeleteVotersControllerTest extends BaseFrontendTest {

	@Test
	public void deleteVoters_withEvoteException_shouldAddErrorMessage() throws Exception {
		DeleteVotersController ctrl = initializeMocks(DeleteVotersController.class);
		evoteExceptionWhen(MvElectionService.class, "@evote").findRoot(any(UserData.class), anyLong());

		ctrl.deleteVoters();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote");
	}

	@Test
	public void deleteVoters_shouldDeleteVoters() throws Exception {
		DeleteVotersController ctrl = initializeMocks(DeleteVotersController.class);
		mockFieldValue("mvArea", new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue());

		ctrl.deleteVoters();

		verify(getInjectMock(VoterService.class)).deleteVoters(eq(getUserDataMock()), any(MvElection.class), any(MvArea.class));
		assertThat(ctrl.isDeleted()).isTrue();
	}
}

