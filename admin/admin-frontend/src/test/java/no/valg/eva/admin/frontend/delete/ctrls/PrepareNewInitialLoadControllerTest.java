package no.valg.eva.admin.frontend.delete.ctrls;

import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoterService;
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
import static org.mockito.Mockito.when;


public class PrepareNewInitialLoadControllerTest extends BaseFrontendTest {

	@Test
	public void prepareForNewInitialLoad_withEvoteException_shouldAddErrorMessage() throws Exception {
		PrepareNewInitialLoadController ctrl = initializeMocks(PrepareNewInitialLoadController.class);
		evoteExceptionWhen(MvElectionService.class, "@evote").findRoot(any(UserData.class), anyLong());

		ctrl.prepareForNewInitialLoad();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote");
	}

	@Test
	public void prepareForNewInitialLoad_shouldPrepare() throws Exception {
		PrepareNewInitialLoadController ctrl = initializeMocks(PrepareNewInitialLoadController.class);
		when(getInjectMock(MvAreaService.class).findRoot(anyLong()).toString()).thenReturn("Oslo");

		ctrl.prepareForNewInitialLoad();

		verify(getInjectMock(VoterService.class)).prepareNewInitialLoad(eq(getUserDataMock()), any(MvElection.class), any(MvArea.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@delete.prepareInitialLoad.confirmation, Oslo]");
		assertThat(ctrl.isDeleted()).isTrue();
	}
}

