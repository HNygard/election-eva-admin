package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import org.mockito.ArgumentCaptor;
import org.primefaces.event.SelectEvent;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.frontend.common.ctrls.RedirectInfo.REDIRECT_INFO_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValgtingProvingSamletControllerTest extends BaseFrontendTest {

	@Test
	public void isValgting_returnererTrue() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();

		assertThat(ctrl.isValgting()).isTrue();
	}

	@Test
	public void findVotings_medFeilVoterNumber_returnererFeilmelding() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();
		ctrl.getForm().setStartVotingNumber("1");
		ctrl.getForm().setEndVotingNumber("0");

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_ERROR, "@voting.approveVoting.searchApproveNegativeVoting.errorMissingVotingNumber");
	}

	@Test
	public void findVotings_medIngenResultater_returnererMelding() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();
		ctrl.getForm().setStartVotingNumber("1");
		ctrl.getForm().setEndVotingNumber("2");
		ctrl.getForm().setSelectedVotingCategoryId((String) ctrl.votingCategoryChoiceList().get(1).getValue());
		stub_findVotingStatistics(new ArrayList<>());
		stub_findElectionDayVotingPickList(new ArrayList<>());

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_INFO, "@voting.approveVoting.searchApproveElectionDayNegativeVoting.noVotings");
	}

	@Test
	public void findVotings_medResultater_verifiserState() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();
		ctrl.getForm().setStartVotingNumber("1");
		ctrl.getForm().setEndVotingNumber("2");
		ctrl.getForm().setSelectedVotingCategoryId((String) ctrl.votingCategoryChoiceList().get(1).getValue());
		stub_findVotingStatistics(singletonList(createMock(VotingDto.class)));
		stub_findElectionDayVotingPickList(singletonList(pickListItem()));

		ctrl.findVotings();

		assertThat(ctrl.getApproveVotingStatistics()).isNotNull();
		assertThat(ctrl.isVisResultat()).isTrue();
	}

	@Test
	public void updateVotingsApproved_withData_updatesApproved() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();
		ctrl.setPickListItems(new ArrayList<>());
		ctrl.getForm().setSelectedVotingCategoryId((String) ctrl.votingCategoryChoiceList().get(0).getValue());

		ctrl.updateVotingsApproved();

		assertFacesMessage(SEVERITY_INFO, "[@voting.approveVoting.noVotingsApproved, 0]");
		assertThat(ctrl.getApproveVotingStatistics()).isNotNull();
		assertThat(ctrl.isVisResultat()).isFalse();
	}

	@Test
	public void selectVoterInNegativeVotingList_redirecterTilVelger() throws Exception {
		ValgtingProvingSamletController ctrl = ctrl();
		SelectEvent event = createMock(SelectEvent.class);
		PickListItem pickListItem = pickListItem();
		when(event.getObject()).thenReturn(pickListItem);

		ctrl.selectVoterInNegativeVotingList(event);

		ArgumentCaptor<ProvingSamletRedirectInfo> argumentCaptor = ArgumentCaptor.forClass(ProvingSamletRedirectInfo.class);
		verify(getServletContainer().getRequestMock().getSession()).setAttribute(eq(REDIRECT_INFO_SESSION_KEY), argumentCaptor.capture());
		ProvingSamletRedirectInfo info = argumentCaptor.getValue();
		assertThat(info.getData()).isEqualTo("1");
		assertThat(info.getUrl()).isEqualTo("/secure/valgtingProvingSamlet.xhtml?a=b");
		assertThat(info.getTitle()).isEqualTo("@menu.approveVoting.approveVotingNegativeElectionDay");
		assertThat(info.getForm()).isNotNull();
		verify(getFacesContextMock().getExternalContext()).redirect("/secure/valgtingProvingVelger.xhtml?a=b");
	}

	private ThisValgtingProvingSamletController ctrl() throws Exception {
		ThisValgtingProvingSamletController ctrl = initializeMocks(new ThisValgtingProvingSamletController());
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
		kontekst.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));

		ctrl.initialized(kontekst);

		return ctrl;
	}

	private List<VotingDto> stub_findVotingStatistics(List<VotingDto> liste) {
		when(getInjectMock(VotingService.class).findVotingStatistics(
				eq(getUserDataMock()),
				anyLong(),
				anyLong(),
				anyLong(),
				any(),
				any(),
				anyInt(),
				anyInt(),
				anyBoolean(),
				any(String[].class),
				anyBoolean())).thenReturn(liste);
		return liste;
	}

	private List<PickListItem> stub_findElectionDayVotingPickList(List<PickListItem> liste) {
		when(getInjectMock(VotingService.class).findElectionDayVotingPickList(
				eq(getUserDataMock()),
				anyLong(),
				anyLong(),
				anyInt(),
				anyInt(),
				any()))
						.thenReturn(liste);
		return liste;
	}

	private PickListItem pickListItem() {
		PickListItem result = createMock(PickListItem.class);
		when(result.getStatus()).thenReturn("1");
		when(result.getVoterId()).thenReturn("1");
		return result;
	}

	private static class ThisValgtingProvingSamletController extends ValgtingProvingSamletController {

		@Override
		protected RedirectInfo getAndRemoveRedirectInfo() {
			return null;
		}

		@Override
		public String getDenneSidenURL() {
			return "/secure/valgtingProvingSamlet.xhtml?a=b";
		}

	}

}
