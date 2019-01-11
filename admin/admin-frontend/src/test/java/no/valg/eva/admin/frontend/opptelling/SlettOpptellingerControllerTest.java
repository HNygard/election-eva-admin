package no.valg.eva.admin.frontend.opptelling;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.FYLKESVALGSTYRET;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.VALGSTYRET;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.opptelling.service.OpptellingService;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SlettOpptellingerControllerTest extends BaseFrontendTest {
	private SlettOpptellingerController controller;
	private OpptellingService service;
	private ValghierarkiService valghierarkiService;
	private ValggeografiService valggeografiService;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(SlettOpptellingerController.class);
		service = getInjectMock(OpptellingService.class);
		valghierarkiService = getInjectMock(ValghierarkiService.class);
		valggeografiService = getInjectMock(ValggeografiService.class);
	}

	@Test(dataProvider = "getKontekstvelgerOppsettTestData")
	public void getKontekstvelgerOppsett_gittTestData_returnererOppsett(ValggeografiNivaa valggeografiNivaa, KontekstvelgerOppsett oppsett) throws Exception {
		when(getUserDataMock().getOperatorAreaLevel()).thenReturn(valggeografiNivaa.tilAreaLevelEnum());
		assertThat(controller.getKontekstVelgerOppsett()).isEqualTo(oppsett);
	}

	@DataProvider
	public Object[][] getKontekstvelgerOppsettTestData() {
		return new Object[][]{
				{KOMMUNE, kontekstvelgerOppsett(KOMMUNE, BYDEL, STEMMEKRETS)},
				{FYLKESKOMMUNE, kontekstvelgerOppsett(FYLKESKOMMUNE, KOMMUNE, BYDEL, STEMMEKRETS)},
				{VALGHENDELSE, kontekstvelgerOppsett(LAND, FYLKESKOMMUNE, KOMMUNE, BYDEL, STEMMEKRETS)}
		};
	}

	private KontekstvelgerOppsett kontekstvelgerOppsett(ValggeografiNivaa... nivaaer) {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE, VALG, VALGDISTRIKT));
		oppsett.leggTil(geografi(nivaaer));
		return oppsett;
	}

	@Test(dataProvider = "slettOpptellingerTestData")
	public void slettOpptellinger_gittTestData_kallerTjenesteOgSetterRiktigTilstand(
			CountCategory[] valgteCountCategories, Styretype[] valgteStyretyper) throws Exception {
		Valghierarki valghierarki = valghierarki();
		Valggeografi valggeografi = valggeografi();
		when(valghierarkiService.valghierarki(VALG_STI)).thenReturn(valghierarki);
		when(valggeografiService.valggeografi(KOMMUNE_STI)).thenReturn(valggeografi);
		controller.initialized(kontekst());
		controller.setValgteCountCategories(valgteCountCategories);
		controller.setValgteStyretyper(valgteStyretyper);

		controller.slettOpptellinger();
		verify(service).slettOpptellinger(getUserDataMock(), VALG_STI, KOMMUNE_STI, valgteCountCategories, valgteStyretyper);
		assertFacesMessage(SEVERITY_INFO, "[@delete.vote_counts.confirmation, VALG_111111_11_11, KOMMUNE_111111_11_11_1111]");
		assertThat(controller.isSlettetOpptellinger()).isTrue();
	}

	@DataProvider
	public Object[][] slettOpptellingerTestData() {
		CountCategory[] valgteCountCategories = {FO, VO};
		Styretype[] valgteStyretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		return new Object[][]{
				{null, null},
				{valgteCountCategories, null},
				{null, valgteStyretyper},
				{valgteCountCategories, valgteStyretyper},
		};
	}

	@Test
	public void getValghierarkiNavn_gittInitialized_returnerValghierarkiNavn() throws Exception {
		Valghierarki valghierarki = valghierarki();
		when(valghierarkiService.valghierarki(VALG_STI)).thenReturn(valghierarki);
		controller.initialized(kontekst());
		assertThat(controller.getValghierakiNavn()).isEqualTo(VALG_NAVN_111111_11_11);
	}

	@Test
	public void getValggeografiNavn_gittInitialized_returnerValggeografiNavn() throws Exception {
		Valggeografi valggeografi = valggeografi();
		when(valggeografiService.valggeografi(KOMMUNE_STI)).thenReturn(valggeografi);
		controller.initialized(kontekst());
		assertThat(controller.getValggeografiNavn()).isEqualTo(KOMMUNE_NAVN_111111_11_11_1111);
	}

	@Test
	public void getCountCategories_gittController_returnererAlleCountCategories() throws Exception {
		assertThat(controller.getCountCategories()).isEqualTo(CountCategory.values());
	}

	@Test
	public void getStyretyper_gittController_returnererAlleStyretyper() throws Exception {
		assertThat(controller.getStyretyper()).isEqualTo(Styretype.values());
	}

	@Test
	public void propertyValgteCountCategories_gittController_fungererSomDenSkal() throws Exception {
		CountCategory[] countCategories = {FO, VO};
		controller.setValgteCountCategories(countCategories);
		assertThat(controller.getValgteCountCategories()).isEqualTo(countCategories);
		controller.setValgteCountCategories(null);
		assertThat(controller.getValgteCountCategories()).isNull();
	}

	@Test
	public void propertyValgteStyretyper_gittController_fungererSomDenSkal() throws Exception {
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		controller.setValgteStyretyper(styretyper);
		assertThat(controller.getValgteStyretyper()).isEqualTo(styretyper);
		controller.setValgteStyretyper(null);
		assertThat(controller.getValgteStyretyper()).isNull();
	}

	private Valghierarki valghierarki() {
		Valghierarki valghierarki = createMock(Valghierarki.class);
		when(valghierarki.sti()).thenReturn(VALG_STI);
		when(valghierarki.navn()).thenReturn(VALG_NAVN_111111_11_11);
		return valghierarki;
	}

	private Valggeografi valggeografi() {
		Valggeografi valggeografi = createMock(Valggeografi.class);
		when(valggeografi.sti()).thenReturn(KOMMUNE_STI);
		when(valggeografi.navn()).thenReturn(KOMMUNE_NAVN_111111_11_11_1111);
		return valggeografi;
	}

	private Kontekst kontekst() {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		kontekst.setValggeografiSti(KOMMUNE_STI);
		return kontekst;
	}
}
