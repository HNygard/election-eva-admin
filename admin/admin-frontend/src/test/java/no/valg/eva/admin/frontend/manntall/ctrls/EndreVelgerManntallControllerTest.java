package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.service.configuration.VoterService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.Test;

import javax.faces.event.ValueChangeEvent;
import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.stemmekretsSti;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EndreVelgerManntallControllerTest extends BaseFrontendTest {

	@Test
	public void endreStemmeberettiget_setterCtrlIRedigerModus() throws Exception {
		EndreVelgerManntallController ctrl = initializeMocks(EndreVelgerManntallController.class);

		ctrl.endreStemmeberettiget();

		assertThat(ctrl.isRediger()).isTrue();
		assertThat(ctrl.isRedigereStemmeberettiget()).isTrue();
	}

	@Test
	public void lagreEndreStemmeberettiget_medVelger_lagrerVelger() throws Exception {
		EndreVelgerManntallController ctrl = initializeMocks(EndreVelgerManntallController.class);
		Voter velger = setVelger();
		ctrl.endreStemmeberettiget();

		ctrl.lagreEndreStemmeberettiget();

		assertThat(velger.isEligible()).isTrue();
		assertThat(velger.getAarsakskode()).isEqualTo("100");
		assertFacesMessage(SEVERITY_INFO, "[@electoralRoll.updatedApprovedInformation, Test Testesen]");
		assertThat(ctrl.isRedigereStemmeberettiget()).isFalse();
		verify(getInjectMock(SokManntallController.class)).setVelger(any(Voter.class));
	}

	@Test
	public void endreVelger_medVelger_verifiserInit() throws Exception {
		EndreVelgerManntallController ctrl = initializeMocks(EndreVelgerManntallController.class);
		Voter velger = setVelger();
		stub_findByPathAndLevel();

		ctrl.endreVelger();

		assertThat(ctrl.getVelgerStemmekretsListe()).hasSize(1);
		assertThat(ctrl.getVelgerKommuneId()).isEqualTo(velger.getMvArea().getMunicipalityId());
		assertThat(ctrl.getStemmekretsSti()).isEqualTo(AREA_PATH_POLLING_DISTRICT.path());
	}

	@Test
	public void oppdaterStemmekretsListe_medEvent_oppdatererStemmekretser() throws Exception {
		EndreVelgerManntallController ctrl = initializeMocks(EndreVelgerManntallController.class);
		setVelger();
		stub_findByPathAndLevel();
		ValueChangeEvent event = createMock(ValueChangeEvent.class);
		when(event.getNewValue().toString()).thenReturn(AREA_PATH_MUNICIPALITY.getMunicipalityId());

		ctrl.oppdaterStemmekretsListe(event);

		assertThat(ctrl.getVelgerStemmekretsListe()).isNotEmpty();
		assertThat(ctrl.getStemmekretsSti()).isNotNull();
	}

	@Test
	public void lagreEndreVelger_medVelger_lagrerVelger() throws Exception {
		EndreVelgerManntallController ctrl = initializeMocks(EndreVelgerManntallController.class);
		setVelger();
		stub_findByPathAndLevel();
		ctrl.setStemmekretsSti(AREA_PATH_POLLING_DISTRICT.path());

		ctrl.lagreEndreVelger();

		assertFacesMessage(SEVERITY_INFO, "[@electoralRoll.updatedPersonalInformation, Testesen Test]");
		assertThat(ctrl.isRedigereVelger()).isFalse();
	}

	private Voter setVelger() {
		when(getInjectMock(UserDataController.class).getElectionEvent().getId()).thenReturn("111111");
		stub_findAllAarsakskoder();
		MvArea stemmekrets = new MvAreaBuilder(AREA_PATH_POLLING_DISTRICT).getValue();
		Voter velger = new Voter();
		velger.setMvArea(stemmekrets);
		velger.setFirstName("Test");
		velger.setLastName("Testesen");
		velger.updateNameLine();
		velger.setApproved(true);
		when(getInjectMock(SokManntallController.class).getVelger()).thenReturn(velger);
		when(getInjectMock(SokManntallController.class).isHarVelger()).thenReturn(true);
		when(getInjectMock(SokManntallController.class).getKommune()).thenReturn(stemmekrets);
		when(getInjectMock(SokManntallController.class).byggNavnelinje(any(Voter.class))).thenReturn("Testesen Test");
		return velger;
	}

	private void stub_findAllAarsakskoder() {
		List<Aarsakskode> koder = singletonList(aarsakskode("100"));
		when(getInjectMock(VoterService.class).findAllAarsakskoder()).thenReturn(koder);
	}

	private void stub_findByPathAndLevel() {
		Stemmekrets stemmekrets =
				new Stemmekrets(stemmekretsSti(AREA_PATH_POLLING_DISTRICT), "Stemmekrets 01", false, "Fylkeskommune 01", "Kommune 01", "Bydel 01");
		List<Stemmekrets> list = singletonList(stemmekrets);
		when(getInjectMock(ValggeografiService.class).stemmekretser(
                any(KommuneSti.class), ArgumentMatchers.<PollingDistrictType[]>any())).thenReturn(list);
	}

	private Aarsakskode aarsakskode(String id) {
		Aarsakskode result = createMock(Aarsakskode.class);
		when(result.getId()).thenReturn(id);
		return result;
	}

}
