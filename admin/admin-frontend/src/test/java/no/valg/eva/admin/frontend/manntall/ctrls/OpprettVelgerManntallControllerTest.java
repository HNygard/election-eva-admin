package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.model.SpesRegType;
import no.evote.model.Statuskode;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.VoterService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpprettVelgerManntallControllerTest extends BaseFrontendTest {

	@Test
	public void initialized_medKommune_verifiserInit() throws Exception {
		OpprettVelgerManntallController ctrl = ctrl();

		assertThat(ctrl.getVelger()).isNotNull();
		assertThat(ctrl.getKommuneListe()).hasSize(1);
		assertThat(ctrl.getStemmekretsListe()).isNotNull();
	}

	@Test
	public void opprettVelger_medVelger_verifiserLagring() throws Exception {
		OpprettVelgerManntallController ctrl = ctrl();
		ctrl.getVelger().setFirstName("Test");
		ctrl.getVelger().setLastName("Testesen");
		ctrl.setStemmekretsSti(AREA_PATH_POLLING_DISTRICT.path());
		stub_create(ctrl.getVelger());

		ctrl.opprettVelger();

		verifiserPopuler(ctrl.getVelger());
		assertFacesMessage(SEVERITY_INFO, "[@electoralRoll.newVoterCreated, Testesen Test]");
		verify(getFacesContextMock().getExternalContext()).redirect("/secure/manntall/sok.xhtml?a=b");
	}

	private Voter stub_create(Voter velger) {
		when(getInjectMock(VoterService.class).create(getUserDataMock(), velger)).thenReturn(velger);
		return velger;
	}

	private OpprettVelgerManntallController ctrl() throws Exception {
		OpprettVelgerManntallController ctrl = initializeMocks(OpprettVelgerManntallController.class);
		Kontekst data = new Kontekst();
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
        mockFieldValue("pageURL", "/secure/manntall/opprett.xhtml?a=b");

		MvArea kommune = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();
		when(getInjectMock(MvAreaService.class).findSingleByPath(any(ValggeografiSti.class))).thenReturn(kommune);

		ctrl.initialized(data);

		when(getInjectMock(VoterService.class).findAllSpesRegTypes(getUserDataMock())).thenReturn(
                singletonList(spesRegType()));
		when(getInjectMock(VoterService.class).findAllStatuskoder(getUserDataMock())).thenReturn(
                singletonList(statuskode()));
		when(getInjectMock(VoterService.class).findAllAarsakskoder()).thenReturn(
                singletonList(aarsakskode()));

		return ctrl;
	}

	private void verifiserPopuler(Voter velger) {
		assertThat(velger.getImportBatchNumber()).isNull();
		assertThat(velger.getEndringstype()).isEqualTo('T');
		assertThat(velger.getDateTimeSubmitted()).isNotNull();
		assertThat(velger.getRegDato()).isNotNull();
		assertThat(velger.getNameLine()).isEqualTo("Testesen Test");
		assertThat(velger.getSpesRegType()).isNotNull();
		assertThat(velger.getStatuskode()).isNotNull();
		assertThat(velger.getAarsakskode()).isNotNull();
		assertThat(velger.getMailingCountryCode()).isEqualTo("000");
		assertThat(velger.isEligible()).isTrue();
		assertThat(velger.isApproved()).isFalse();
		assertThat(velger.getElectionEvent()).isNotNull();
	}

	private SpesRegType spesRegType() {
		SpesRegType result = new SpesRegType();
		result.setId('1');
		return result;
	}

	private Statuskode statuskode() {
		Statuskode result = new Statuskode();
		result.setId('2');
		return result;
	}

	private Aarsakskode aarsakskode() {
		Aarsakskode result = new Aarsakskode();
		result.setId("3");
		return result;
	}

}
