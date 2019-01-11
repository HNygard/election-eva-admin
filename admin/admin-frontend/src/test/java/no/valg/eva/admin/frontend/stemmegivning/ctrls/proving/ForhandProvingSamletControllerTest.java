package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.service.configuration.LegacyPollingPlaceService;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import org.joda.time.LocalDate;
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

public class ForhandProvingSamletControllerTest extends BaseFrontendTest {

	@Test
	public void initialized_medKontekst_verifiserInit() throws Exception {
		ctrl();

		verify(getInjectMock(LegacyPollingPlaceService.class)).findAdvancedPollingPlaceByMunicipality(
				eq(getUserDataMock()),
				anyLong(),
                any());
	}

	@Test
	public void isValgting_returnererFalse() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();

		assertThat(ctrl.isValgting()).isFalse();
	}

	@Test
	public void findVotings_medManglendeDato_returnererFeilmelding() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.getForm().setRegisteredToday(false);

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_ERROR, "@voting.approveVoting.searchApproveNegativeVoting.errorMissingDate");
	}

	@Test
	public void findVotings_medFraDatoEtterTilDato_returnererFeilmelding() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.getForm().setRegisteredToday(false);
		ctrl.getForm().setStartDate(LocalDate.parse("2017-01-01"));
		ctrl.getForm().setEndDate(LocalDate.parse("2016-01-01"));

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_ERROR, "@common.message.evote_application_exception.END_DATE_BEFORE_START_DATE");
	}

	@Test
	public void findVotings_medFeilVoterNumber_returnererFeilmelding() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.getForm().setRegisteredToday(true);
		ctrl.getForm().setStartVotingNumber("1");
		ctrl.getForm().setEndVotingNumber("0");

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_ERROR, "@voting.approveVoting.searchApproveNegativeVoting.errorMissingVotingNumber");
	}

	@Test
	public void findVotings_medIngenStemmerFunnet_returnererMelding() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.getForm().setRegisteredToday(true);
		stub_findVotingStatistics(new ArrayList<>());

		ctrl.findVotings();

		assertFacesMessage(SEVERITY_INFO, "@voting.approveVoting.searchApproveNegativeVoting.noAdvanceVotings");
	}

	@Test
	public void findVotings_medStemmerFunnet_verifiserState() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.getForm().setRegisteredToday(true);
        stub_findVotingStatistics(singletonList(createMock(VotingDto.class)));
        stub_findAdvanceVotingPickList(singletonList(pickListItem()));

		ctrl.findVotings();

		assertThat(ctrl.getVotingStatistics()).isNotEmpty();
		assertThat(ctrl.getPickListItems()).isNotEmpty();
		assertThat(ctrl.getApproveVotingStatistics()).isNotNull();
		assertThat(ctrl.isVisResultat()).isTrue();
	}

	@Test
	public void updateVotingsApproved_withData_updatesApproved() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		ctrl.setPickListItems(new ArrayList<>());
		ctrl.getForm().setRegisteredToday(true);
		ctrl.getForm().setStartVotingNumber("1");
		ctrl.getForm().setEndVotingNumber("2");
        stub_findVotingStatistics(singletonList(createMock(VotingDto.class)));
		stub_updateAdvanceVotingsApproved(2);

		ctrl.updateVotingsApproved();

		assertFacesMessage(SEVERITY_INFO, "[@voting.approveVoting.noVotingsApproved, 2]");
		assertThat(ctrl.isVisResultat()).isFalse();
	}

	@Test
	public void selectVoterInNegativeVotingList_redirecterTilVelger() throws Exception {
		ThisForhandProvingSamletController ctrl = ctrl();
		SelectEvent event = createMock(SelectEvent.class);
		PickListItem pickListItem = pickListItem();
		when(event.getObject()).thenReturn(pickListItem);

		ctrl.selectVoterInNegativeVotingList(event);

		ArgumentCaptor<ProvingSamletRedirectInfo> argumentCaptor = ArgumentCaptor.forClass(ProvingSamletRedirectInfo.class);
		verify(getServletContainer().getRequestMock().getSession()).setAttribute(eq(REDIRECT_INFO_SESSION_KEY), argumentCaptor.capture());
		ProvingSamletRedirectInfo info = argumentCaptor.getValue();
		assertThat(info.getData()).isEqualTo("1");
		assertThat(info.getUrl()).isEqualTo("/secure/forhandProvingSamlet.xhtml?a=b");
		assertThat(info.getTitle()).isEqualTo("@menu.approveVoting.approveVotingNegative");
		assertThat(info.getForm()).isNotNull();
		verify(getFacesContextMock().getExternalContext()).redirect("/secure/forhandProvingVelger.xhtml?a=b");
	}

	private static class ThisForhandProvingSamletController extends ForhandProvingSamletController {

		@Override
		protected RedirectInfo getAndRemoveRedirectInfo() {
			return null;
		}

		@Override
		public String getDenneSidenURL() {
			return "/secure/forhandProvingSamlet.xhtml?a=b";
		}
	}

	private ThisForhandProvingSamletController ctrl() throws Exception {
		ThisForhandProvingSamletController ctrl = initializeMocks(new ThisForhandProvingSamletController());
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
				any(LocalDate.class),
				any(LocalDate.class),
				anyInt(),
				anyInt(),
				anyBoolean(),
				any(String[].class),
				anyBoolean())).thenReturn(liste);
		return liste;
	}

	private List<PickListItem> stub_findAdvanceVotingPickList(List<PickListItem> liste) {
		when(getInjectMock(VotingService.class).findAdvanceVotingPickList(
				eq(getUserDataMock()),
				anyLong(),
				anyLong(),
				anyLong(),
				any(LocalDate.class),
				any(LocalDate.class),
				anyInt(),
				anyInt())).thenReturn(liste);
		return liste;
	}

	private void stub_updateAdvanceVotingsApproved(int result) {
		when(getInjectMock(VotingService.class).updateAdvanceVotingsApproved(
				eq(getUserDataMock()),
				anyLong(),
				anyLong(),
				anyLong(),
				any(LocalDate.class),
				any(LocalDate.class),
				anyInt(),
				anyInt())).thenReturn(result);
	}

	private PickListItem pickListItem() {
		PickListItem result = createMock(PickListItem.class);
		when(result.getStatus()).thenReturn("1");
		when(result.getVoterId()).thenReturn("1");
		return result;
	}
}
