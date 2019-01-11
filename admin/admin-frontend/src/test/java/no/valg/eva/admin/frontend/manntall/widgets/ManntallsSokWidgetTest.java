package no.valg.eva.admin.frontend.manntall.widgets;

import no.evote.service.configuration.VoterService;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class ManntallsSokWidgetTest extends BaseFrontendTest {

	private static final String KORREKT_MANNTALLSNUMMER = "123456789080";

	private MyListener listener;

	@BeforeMethod
	public void beforeMethod() {
		listener = new MyListener();
	}

	@Test
	public void addListener_setterOppControllerMedKommuneId() throws Exception {
		ManntallsSokWidget widget = widget();

		assertThat(widget.getAvansertSok().getKommuneId()).isEqualTo("0301");
	}

	@Test
	public void onTabChange_medEvent_resetterControllerOgKallerListener() throws Exception {
		ManntallsSokWidget widget = widget();
		widget.setManntallsnummer("test");

		widget.onTabChange(createMock(TabChangeEvent.class));

		assertThat(widget.getManntallsnummer()).isNull();
		assertThat(listener.getManntallsSokInit()).isEqualTo(1);
	}

	@Test
	public void onRowSelect_medEvent_setterVelgerBeregnerManntallsnummerOgKallerListener() throws Exception {
		ManntallsSokWidget widget = widget();
		widget.setManntallsnummer("test");
		SelectEvent event = selectEvent(velger(1000L));

		widget.onRowSelect(event);

		assertThat(widget.getVelger()).isSameAs(event.getObject());
		assertThat(listener.getVelger()).isSameAs(event.getObject());
		assertThat(widget.getManntallsnummerObject()).isNotNull();
	}

	@Test
	public void sokLopenummer_medUgyldigLopenummer_returnererTomtResultat() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_erValgårssifferGyldig(false);
		widget.setLopenummer("1");
		widget.setStemmekategori(VS.getId());
		stub_findVotingByVotingNumber(null);

		widget.sokLopenummer();

		assertFacesMessage(SEVERITY_ERROR, "LOPENUMMER");
	}

	@Test
	public void sokLopenummer_medGyldigLopenummer_returnererTomtResultat() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_erValgårssifferGyldig(false);
		widget.setLopenummer("2");
		widget.setStemmekategori(FO.getId());
		Voting stemmegivning = createMock(Voting.class);
		stub_findVotingByVotingNumber(stemmegivning);
		when(stemmegivning.getVoter().getNumber()).thenReturn(1L);

		widget.sokLopenummer();

		assertThat(listener.getVelger()).isSameAs(stemmegivning.getVoter());
		assertThat(widget.getManntallsnummerObject()).isNotNull();
	}

	private void stub_findVotingByVotingNumber(Voting stemmegivning) {
		when(getInjectMock(VotingService.class).findVotingByVotingNumber(
                eq(getUserDataMock()), any(), any(KommuneSti.class), anyLong(), anyBoolean())).thenReturn(stemmegivning);
	}

	@Test
	public void sokManntallsnummer_medErValgårssifferGyldig_girFeilmelding() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_erValgårssifferGyldig(false);
		widget.setManntallsnummer(KORREKT_MANNTALLSNUMMER);

		widget.sokManntallsnummer();

		assertFacesMessage(SEVERITY_ERROR, "@voting.validation.electionCardNotValid");
		assertThat(listener.getManntallsSokInit()).isEqualTo(1);
	}

	@Test
	public void sokManntallsnummer_medTomtResultat_girFeilmeldingOgKallerListenerMedTypeForTomt() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_erValgårssifferGyldig(true);
		stub_findByManntallsnummer(new ArrayList<>());
		widget.setManntallsnummer(KORREKT_MANNTALLSNUMMER);

		widget.sokManntallsnummer();

		assertFacesMessage(SEVERITY_ERROR, "MANNTALLSNUMMER");
	}

	@Test
	public void sokManntallsnummer_medTreff_kallerListenerMedVelger() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_erValgårssifferGyldig(true);
        stub_findByManntallsnummer(singletonList(createMock(Voter.class)));
		widget.setManntallsnummer(KORREKT_MANNTALLSNUMMER);

		widget.sokManntallsnummer();

		assertThat(listener.getVelger()).isNotNull();
	}

	@Test
	public void sokFodselsnummer_medTomtResultat_girFeilmeldingOgKallerListenerMedTypeForTomt() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_findByElectionEventAndId(new ArrayList<>());
		widget.setManntallsnummer("12345678901");

		widget.sokFodselsnummer();

		assertFacesMessage(SEVERITY_ERROR, "FODSELSNUMMER");
	}

	@Test
	public void sokFodselsnummer_medTreff_kallerListenerMedVelger() throws Exception {
		ManntallsSokWidget widget = widget();
        stub_findByElectionEventAndId(singletonList(createMock(Voter.class)));
		widget.setManntallsnummer("12345678901");

		widget.sokFodselsnummer();

		assertThat(listener.getVelger()).isNotNull();
	}

	@Test
	public void sokAvansert_medTomtResultat_kallerListenerMedTypeForTomt() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_searchVoter(new ArrayList<>());

		widget.sokAvansert();

		assertFacesMessage(SEVERITY_ERROR, "AVANSERT");
	}

	@Test
	public void sokAvansert_medTreff_kallerListenerMedVelger() throws Exception {
		ManntallsSokWidget widget = widget();
        stub_searchVoter(singletonList(createMock(Voter.class)));

		widget.sokAvansert();

		assertThat(listener.getVelger()).isNotNull();
	}

	@Test
	public void sokAvansert_medMerEnn50Treff_girFeilmelding() throws Exception {
		ManntallsSokWidget widget = widget();
		stub_searchVoter(voters(55));

		widget.sokAvansert();

		assertFacesMessage(SEVERITY_ERROR, "@electoralRoll.specifySearch");
	}

	private ManntallsSokWidget widget() throws Exception {
		ManntallsSokWidget widget = initializeMocks(ManntallsSokWidget.class);
		stubResolveExpression("#{cc.attrs.kommuneId}", "0301");
		widget.addListener(listener);
		return widget;
	}

	private class MyListener implements ManntallsSokListener {
		private int manntallsSokInit;
		private Voter velger;

		@Override
		public void manntallsSokInit() {
			manntallsSokInit++;
		}

		@Override
		public void manntallsSokVelger(Voter velger) {
			this.velger = velger;
		}

		@Override
		public void manntallsSokTomtResultat() {

		}

		@Override
		public String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
			return manntallsSokType.name();
		}

		@Override
		public KommuneSti getKommuneSti() {
			return ValggeografiSti.kommuneSti(AreaPath.from("111111.22.33.0301"));
		}

		@Override
		public ValggruppeSti getValggruppeSti() {
			return null;
		}

		public int getManntallsSokInit() {
			return manntallsSokInit;
		}

		public Voter getVelger() {
			return velger;
		}
	}

	private SelectEvent selectEvent(Voter velger) {
		SelectEvent event = createMock(SelectEvent.class);
		when(event.getObject()).thenReturn(velger);
		return event;
	}

	private Voter velger(Long manntallsnummer) {
		Voter velger = createMock(Voter.class);
		when(velger.getNumber()).thenReturn(manntallsnummer);
		return velger;
	}

	private void stub_erValgårssifferGyldig(boolean resultat) {
		when(getInjectMock(ManntallsnummerService.class).erValgaarssifferGyldig(eq(getUserDataMock()), any(Manntallsnummer.class))).thenReturn(resultat);
	}

	private List<Voter> stub_findByManntallsnummer(List<Voter> liste) {
		when(getInjectMock(VoterService.class).findByManntallsnummer(eq(getUserDataMock()), any(Manntallsnummer.class))).thenReturn(liste);
		return liste;
	}

	private List<Voter> stub_findByElectionEventAndId(List<Voter> liste) {
        when(getInjectMock(VoterService.class).findByElectionEventAndId(eq(getUserDataMock()), any(), anyLong())).thenReturn(liste);
		return liste;
	}

	private List<Voter> stub_searchVoter(List<Voter> liste) {
		when(getInjectMock(VoterService.class).searchVoter(
				eq(getUserDataMock()),
				any(Voter.class),
                any(),
				eq("0301"),
				eq(50),
				anyBoolean(),
				anyLong())).thenReturn(liste);
		return liste;
	}

	private List<Voter> voters(int size) {
		List<Voter> resultat = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			resultat.add(createMock(Voter.class));
		}
		return resultat;
	}

}

