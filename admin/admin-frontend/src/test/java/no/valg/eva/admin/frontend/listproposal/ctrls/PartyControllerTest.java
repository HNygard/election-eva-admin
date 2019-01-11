package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.service.configuration.PartyService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartyControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_withParties_verifyState() throws Exception {
		PartyController ctrl = initializeMocks(PartyController.class);
		stub_findAllPartiesButNotBlank();

		ctrl.doInit();

		assertThat(ctrl.getSelectedParty()).isNotNull();
		assertThat(ctrl.getParties()).hasSize(2);
		assertThat(ctrl.getParties().get(0).getOversattNavn()).isEqualTo("@party[H].name");
		assertThat(ctrl.getParties().get(1).getOversattNavn()).isEqualTo("Test");
	}

	@Test
	public void createOrUpdateParty_withInvalidParty_returnsErrorMessage() throws Exception {
		PartyController ctrl = initializeMocks(PartyController.class);
		mockFieldValue("selectedParty", parti(Partikategori.LANDSDEKKENDE, "H", "Høyre"));
		List<String> errors = stub_validateParty(Arrays.asList("@error1", "@error2"));

		ctrl.createOrUpdateParty();

        List<FacesMessage> expectedMessages = new ArrayList<>();
		for (String error : errors) {
            expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
        }
        assertFacesMessages(expectedMessages);
	}

	@Test
	public void createOrUpdateParty_withValidNewParty_createsNewParty() throws Exception {
		PartyController ctrl = initializeMocks(PartyController.class);
		mockFieldValue("selectedParty", parti(Partikategori.LANDSDEKKENDE, "H", "Høyre"));
		stub_validateParty(new ArrayList<>());
		stub_findAllPartiesButNotBlank();
		ctrl.doInit();

		ctrl.createOrUpdateParty();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@party.created");
		verify(getInjectMock(PartyService.class)).create(eq(getUserDataMock()), any(Parti.class));
		verify(getRequestContextMock()).execute("PF('newPartyWidget').hide()");
		verify(getRequestContextMock()).update("editPartyForm");
	}

	@Test
	public void deleteParty_withInvalidParty_returnsErrorMessage() throws Exception {
		PartyController ctrl = initializeMocks(PartyController.class);
		Parti parti = parti(Partikategori.LANDSDEKKENDE, "H", "Høyre");
		List<String> errors = stub_validatePartyForDelete(Arrays.asList("@error1", "@error2"));

		ctrl.deleteParty(parti);

        List<FacesMessage> expectedMessages = new ArrayList<>();
		for (String error : errors) {
            expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
        }
        assertFacesMessages(expectedMessages);
	}

	@Test
	public void deleteParty_withValidParty_deletesParty() throws Exception {
		PartyController ctrl = initializeMocks(PartyController.class);
		Parti parti = parti(Partikategori.LANDSDEKKENDE, "H", "Høyre");
		stub_findAllPartiesButNotBlank();
		ctrl.doInit();
		stub_validatePartyForDelete(new ArrayList<>());

		ctrl.deleteParty(parti);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@party.deleted, Høyre]");
		verify(getInjectMock(PartyService.class)).delete(getUserDataMock(), parti);
	}

	private List<Parti> stub_findAllPartiesButNotBlank() {
		List<Parti> result = new ArrayList<>();
		result.add(parti(Partikategori.LOKALT, "TEST", "Test"));
		result.add(parti(Partikategori.LANDSDEKKENDE, "H", "@party[H].name"));
		when(getInjectMock(PartyService.class).findAllPartiesButNotBlank(eq(getUserDataMock()), anyLong())).thenReturn(result);
		return result;
	}

	private Parti parti(Partikategori kategori, String id, String navn) {
		Parti result = new Parti(kategori, id);
		result.setForenkletBehandling(true);
		result.setGodkjent(true);
		result.setOversattNavn(navn);
		return result;
	}

	private List<String> stub_validateParty(List<String> errors) {
		when(getInjectMock(PartyService.class).validateParty(eq(getUserDataMock()), any(Parti.class))).thenReturn(errors);
		return errors;
	}

	private List<String> stub_validatePartyForDelete(List<String> errors) {
		when(getInjectMock(PartyService.class).validatePartyForDelete(eq(getUserDataMock()), any(Parti.class))).thenReturn(errors);
		return errors;
	}

}
