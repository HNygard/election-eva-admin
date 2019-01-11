package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.service.configuration.AffiliationService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.service.PartiService;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Locale;
import org.testng.annotations.Test;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpprettListeforslagControllerTest extends BaseFrontendTest {

	@Test
	public void newListProposal_withNullAffiliation_returnsChoosePartyErrorMessage() throws Exception {
		OpprettListeforslagController ctrl = initializeMocks(OpprettListeforslagController.class);
		when(getInjectMock(AffiliationService.class).createNewAffiliation(
				eq(getUserDataMock()), any(Contest.class), any(Parti.class), any(Locale.class),
				eq(BallotStatus.BallotStatusValue.PENDING.getId()))).thenReturn(null);

		ctrl.newListProposal();

		assertFacesMessage(SEVERITY_ERROR, "[@common.message.required, @listProposal.party.choose]");
	}

	@Test
	public void getPartiesWithoutAffiliations_callsService() throws Exception {
		OpprettListeforslagController ctrl = initializeMocks(OpprettListeforslagController.class);
		mockField("contest", Contest.class);
		ctrl.getPartiesWithoutAffiliations();

		verify(getInjectMock(PartiService.class)).partierUtenListeforslag(eq(getUserDataMock()), any(ElectionPath.class));
	}

}
