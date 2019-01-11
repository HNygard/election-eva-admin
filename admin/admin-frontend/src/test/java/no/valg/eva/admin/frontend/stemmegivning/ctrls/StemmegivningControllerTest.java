package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import no.evote.service.configuration.MvAreaService;
import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.NIVAER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.INGEN_VALGKRETS_FOR_VELGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StemmegivningControllerTest extends BaseFrontendTest {

	@Test
	public void getKontekstVelgerOppsett_medOmrådeNivåKommune_sjekkSetup() throws Exception {
		StemmegivningController ctrl = ctrl();

		KontekstvelgerOppsett setup = ctrl.getKontekstVelgerOppsett();

		assertThat(setup.getElementer()).hasSize(2);
		assertThat(setup.getElementer().get(0).getType()).isSameAs(HIERARKI);
		assertThat(setup.getElementer().get(0).get(NIVAER)).isEqualTo("1");
		assertThat(setup.getElementer().get(1).getType()).isSameAs(GEOGRAFI);
		assertThat(setup.getElementer().get(1).get(NIVAER)).isEqualTo("6");
	}

	@Test
	public void initialized_medContextPickerData_sjekkTilstand() throws Exception {
		MyController ctrl = ctrl();

		ctrl.initialized(kontekst());

		assertThat(ctrl.getValgGruppe()).isNotNull();
		assertThat(ctrl.getStemmested()).isNotNull();
		assertThat(ctrl.isKontekstKlarKalt()).isTrue();
		verify(getInjectMock(ManntallsSokWidget.class)).addListener(ctrl);
	}

	@Test
	public void isVelgerEgenKommune_medVelgerOgOmradeLike_returnererTrue() throws Exception {
		StemmegivningController ctrl = ctrl();
		stub_mvAreaService_findSingleByPath(AREA_PATH_MUNICIPALITY);
		Voter velger = medVelger(ctrl, createMock(Voter.class));
		when(velger.getMunicipalityId()).thenReturn(AREA_PATH_MUNICIPALITY.getLeafId());

		assertThat(ctrl.isVelgerEgenKommune()).isTrue();
	}

	@Test
	public void getManntallsnummer_medManntallsnummer_returnererFormatertManntallsnummer() throws Exception {
		StemmegivningController ctrl = ctrl();
		Manntallsnummer manntallsnummer = createMock(Manntallsnummer.class);
		when(manntallsnummer.getKortManntallsnummerMedZeroPadding()).thenReturn("1234");
		when(manntallsnummer.getSluttsifre()).thenReturn("567");
		when(getInjectMock(ManntallsSokWidget.class).getManntallsnummerObject()).thenReturn(manntallsnummer);

		String resultat = ctrl.getManntallsnummer();

		assertThat(resultat).isEqualTo("1234 567");
	}

	private Kontekst kontekst() {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
		kontekst.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_POLLING_PLACE));
		return kontekst;
	}

	private void stub_mvAreaService_findSingleByPath(AreaPath areaPath) {
		MvArea mvArea = new MvAreaBuilder(areaPath).getValue();
		when(getInjectMock(MvAreaService.class).findSingleByPath(any(ValggeografiSti.class))).thenReturn(mvArea);
	}

	private Voter medVelger(StemmegivningController ctrl, Voter velger) {
		ctrl.initialized(kontekst());
		stub_hentStemmegivningsForberedelser(singletonList(new VelgerMelding(INGEN_VALGKRETS_FOR_VELGER)));
		ctrl.manntallsSokVelger(velger);
		return velger;
	}

	private MyController ctrl() throws Exception {
		return initializeMocks(new MyController());
	}

	private void stub_hentStemmegivningsForberedelser(List<VelgerMelding> meldinger) {
		VelgerSomSkalStemme result = new VelgerSomSkalStemme(new ArrayList<>());
		result.setKanRegistrereStemmegivning(true);

		result.getStemmetypeListe().add(createMock(VotingCategory.class));
		result.getVelgerMeldinger().addAll(meldinger);
		when(getInjectMock(VotingService.class).hentVelgerSomSkalStemme(eq(getUserDataMock()), any(StemmegivningsType.class), any(ElectionPath.class),
				any(AreaPath.class), any(Voter.class))).thenReturn(result);
	}

	private static class MyController extends StemmegivningController {
		private ValggeografiNivaa valggeografiNivaa;
		private boolean kontekstKlarKalt;

		MyController() {
			this(STEMMESTED);
		}

		MyController(ValggeografiNivaa valggeografiNivaa) {
			this.valggeografiNivaa = valggeografiNivaa;
		}

		@Override
		public ValggeografiNivaa getStemmestedNiva() {
			return valggeografiNivaa;
		}

		@Override
		public void kontekstKlar() {
			kontekstKlarKalt = true;
		}

		boolean isKontekstKlarKalt() {
			return kontekstKlarKalt;
		}

		@Override
		public String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
			return null;
		}
	}

}
