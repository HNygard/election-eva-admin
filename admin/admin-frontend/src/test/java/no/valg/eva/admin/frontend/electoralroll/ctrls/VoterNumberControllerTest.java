package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.service.BatchService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class VoterNumberControllerTest extends BaseFrontendTest {

	@Test(dataProvider = "init")
	public void init_withDataProvider_verifyExpected(ManntallsnummergenereringStatus status, String expectedMessage) throws Exception {
		VoterNumberController ctrl = ctrl(status);

		ctrl.init();

		assertThat(ctrl.getBatches()).isNotNull();
		assertFacesMessage(FacesMessage.SEVERITY_WARN, expectedMessage);
		verify_loadData();
	}

	@DataProvider(name = "init")
	public Object[][] init() {
		return new Object[][] {
				{ ManntallsnummergenereringStatus.ALLEREDE_GENERERT, "@electoralRoll.generateVoterNumber.wasNotGenerated" },
				{ ManntallsnummergenereringStatus.SKJARINGSDATO_I_FREMTIDEN, "@electoralRoll.generateVoterNumber.cutOffAfterToday" },
				{ ManntallsnummergenereringStatus.VALGHENDELSE_LAAST, "@electoralRoll.generateVoterNumber.eventDisabled" },
				{ ManntallsnummergenereringStatus.INGEN_VELGERE, "@electoralRoll.generateVoterNumber.noVoters" },
		};
	}

	@Test
	public void isStatusOk_withStatusOk_returnsTrue() throws Exception {
		VoterNumberController ctrl = ctrl(ManntallsnummergenereringStatus.OK);
		ctrl.init();

		assertThat(ctrl.isStatusOk()).isTrue();
	}

	@Test
	public void generateVoterNumbers_withElectionEvent_verifyGenerateAndMessage() throws Exception {
		VoterNumberController ctrl = ctrl(ManntallsnummergenereringStatus.OK);

		ctrl.generateVoterNumbers();

		verify(getInjectMock(ManntallsnummerService.class)).genererManntallsnumre(eq(getUserDataMock()), anyLong());
		verify_loadData();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@electoralRoll.generateVoterNumber.wasGenerated");
	}

	private VoterNumberController ctrl(ManntallsnummergenereringStatus status) throws Exception {
		VoterNumberController ctrl = initializeMocks(VoterNumberController.class);
		stub_getAssignVoterNumberStatus(status);
		return ctrl;
	}

	private void stub_getAssignVoterNumberStatus(ManntallsnummergenereringStatus result) {
		when(getInjectMock(ManntallsnummerService.class).hentManntallsnummergenereringStatus(eq(getUserDataMock()), any(ElectionEvent.class))).thenReturn(result);
	}

	private void verify_loadData() {
		verify(getInjectMock(ManntallsnummerService.class)).hentManntallsnummergenereringStatus(eq(getUserDataMock()), any(ElectionEvent.class));
        verify(getInjectMock(BatchService.class)).listBatchesByEventAndCategory(eq(getUserDataMock()), any(Jobbkategori.class), any());

	}
}

