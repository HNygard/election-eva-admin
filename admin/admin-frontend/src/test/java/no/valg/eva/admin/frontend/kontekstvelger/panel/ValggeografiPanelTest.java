package no.valg.eva.admin.frontend.kontekstvelger.panel;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNER_111111_11_1X;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.valg;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste.LAG_NYTT_VALGKORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiPanelTest extends MockUtilsTestCase {
	private ValggeografiPanel panel;
	private KontekstvelgerController controller;
	private ValggeografiService service;
	private ValggeografiService lagNyttValgkortValggeografiService;
	private UserData userData;
	private ValghierarkiPanel valghierarkiPanel;
	private OpptellingskategoriPanel opptellingskategoriPanel;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = createMock(KontekstvelgerController.class);
		service = createMock(ValggeografiService.class);
		lagNyttValgkortValggeografiService = createMock(ValggeografiService.class);
		userData = createMock(UserData.class);
		valghierarkiPanel = createMock(ValghierarkiPanel.class);
		opptellingskategoriPanel = createMock(OpptellingskategoriPanel.class);
		panel = new ValggeografiPanel(controller, userData, service, lagNyttValgkortValggeografiService, valghierarkiPanel, opptellingskategoriPanel);
	}

	@Test(dataProvider = "valggeografiNivaaOgNavn")
	public void navn(ValggeografiNivaa valggeografiNivaa, String navn) throws Exception {
		assertThat(ValggeografiPanel.navn(valggeografiNivaa)).isEqualTo(navn);
	}

	@DataProvider
	public Object[][] valggeografiNivaaOgNavn() {
		return new Object[][]{
				{VALGHENDELSE, "@area_level[0].name"},
				{LAND, "@area_level[1].name"},
				{FYLKESKOMMUNE, "@area_level[2].name"},
				{KOMMUNE, "@area_level[3].name"},
				{BYDEL, "@area_level[4].name"},
				{STEMMEKRETS, "@area_level[5].name"},
				{STEMMESTED, "@area_level[6].name"},
				{RODE, "@area_level[7].name"},
		};
	}

	@Test
	public void init_gittOppsettUtenValggeografiOgIngenKontekst_verdiErIkkeValgtOgIkkeValgbar() throws Exception {
		panel.initOppsett(new KontekstvelgerOppsett());
		assertThat(panel.valgtVerdi()).isNull();
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@Test(dataProvider = "valghierarkiStierOgCountCategoriesOgValggeografiFilter")
	public void init_gittOppsettMedValggeografiOgIngenKontekst_verdiErIkkeValgtMenValgbar(
			ValghierarkiSti valghierarkiSti, CountCategory countCategory) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		when(service.fylkeskommuner(userData, valghierarkiSti, countCategory)).thenReturn(FYLKESKOMMUNER_111111_11_1X);
		when(valghierarkiPanel.valgtVerdi()).thenReturn(valghierarkiSti);
		when(opptellingskategoriPanel.valgtVerdi()).thenReturn(countCategory);
		panel.initOppsett(oppsett);
		assertThat(panel.valgtVerdi()).isNull();
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isFalse();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isTrue();
	}

	@DataProvider
	public Object[][] valghierarkiStierOgCountCategoriesOgValggeografiFilter() {
		return new Object[][]{
				{null, null},
				{VALG_STI, null},
				{VALG_STI, null},
				{null, FO},
				{VALG_STI, FO}
		};
	}

	@Test
	public void init_gittOppsettMedValggeografiOgKontekstMedValggeografiSti_verdiErValgt() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		Kontekst kontekst = Kontekst.deserialize("geografi|111111.11.11.1111");
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.valgtVerdi()).isEqualTo(KOMMUNE_STI);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@Test(dataProvider = "nivaaOgNivaaForValgtValghierarkiOgValgtSti")
	public void init_gittOppsettMedValggeografiOgValgtValgOgFilterHarValgDistriktForValgtValghierarki_verdiErValgt(
			ValggeografiNivaa nivaa, ValggeografiNivaa nivaaForValgtValghierarki, ValggeografiSti valgtSti) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(nivaa).medFilter(HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI));
		ValghierarkiSti valgtValghierarkiSti = valghierarkiSti();
		Valghierarki valgtValghierarki = valg(nivaaForValgtValghierarki);
		when(service.fylkeskommuner(userData, valgtValghierarkiSti, null)).thenReturn(singletonList(FYLKESKOMMUNE_111111_11_11));
		when(service.kommuner(userData, FYLKESKOMMUNE_STI, valgtValghierarkiSti, null)).thenReturn(singletonList(KOMMUNE_111111_11_11_1111));
		when(service.bydeler(userData, KOMMUNE_STI, valgtValghierarkiSti, null)).thenReturn(singletonList(BYDEL_111111_11_11_1111_111111));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(valgtValghierarkiSti);
		when(valghierarkiPanel.valgtValghierarki()).thenReturn(valgtValghierarki);
		when(opptellingskategoriPanel.valgtVerdi()).thenReturn(null);
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		assertThat(panel.valgtVerdi()).isEqualTo(valgtSti);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@DataProvider
	public Object[][] nivaaOgNivaaForValgtValghierarkiOgValgtSti() {
		return new Object[][]{
				{FYLKESKOMMUNE, FYLKESKOMMUNE, FYLKESKOMMUNE_STI},
				{FYLKESKOMMUNE, KOMMUNE, FYLKESKOMMUNE_STI},
				{FYLKESKOMMUNE, BYDEL, FYLKESKOMMUNE_STI},
				{KOMMUNE, KOMMUNE, KOMMUNE_STI},
				{KOMMUNE, BYDEL, KOMMUNE_STI},
				{BYDEL, BYDEL, BYDEL_STI}
		};
	}

	@Test(dataProvider = "nivaaOgValgtSti")
	public void init_gittOppsettMedValggeografiOgValgtValg_verdiErValgt(ValggeografiNivaa nivaa, ValggeografiSti valgtSti) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(nivaa));
		ValghierarkiSti valgtValghierarkiSti = valghierarkiSti();
		Valghierarki valgtValghierarki = valg(FYLKESKOMMUNE);
		when(service.fylkeskommuner(userData, valgtValghierarkiSti, null)).thenReturn(singletonList(FYLKESKOMMUNE_111111_11_11));
		when(service.kommuner(userData, FYLKESKOMMUNE_STI, valgtValghierarkiSti, null)).thenReturn(singletonList(KOMMUNE_111111_11_11_1111));
		when(service.bydeler(userData, KOMMUNE_STI, valgtValghierarkiSti, null)).thenReturn(singletonList(BYDEL_111111_11_11_1111_111111));
		when(service.stemmekretser(userData, BYDEL_STI, valgtValghierarkiSti, null)).thenReturn(singletonList(STEMMEKRETS_111111_11_11_1111_111111_1111));
		when(service.stemmesteder(userData, STEMMEKRETS_STI)).thenReturn(singletonList(STEMMESTED_111111_11_11_1111_111111_1111_1111));
		when(service.roder(STEMMESTED_STI)).thenReturn(singletonList(RODE_111111_11_11_1111_111111_1111_1111_11));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(valgtValghierarkiSti);
		when(valghierarkiPanel.valgtValghierarki()).thenReturn(valgtValghierarki);
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		assertThat(panel.valgtVerdi()).isEqualTo(valgtSti);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@DataProvider
	public Object[][] nivaaOgValgtSti() {
		return new Object[][]{
				{FYLKESKOMMUNE, FYLKESKOMMUNE_STI},
				{KOMMUNE, KOMMUNE_STI},
				{BYDEL, BYDEL_STI},
				{STEMMEKRETS, STEMMEKRETS_STI},
				{STEMMESTED, STEMMESTED_STI},
				{RODE, RODE_STI}
		};
	}

	@Test
	public void getValggeografiService_gittStandardTjeneste_returnererStandardTjeneste() throws Exception {
		assertThat(panel.getValggeografiService()).isSameAs(service);
	}

	@Test
	public void getValggeografiService_gittTjenesteForLagNyttValgkort_returnererStandardTjeneste() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(VALGHENDELSE).medTjeneste(LAG_NYTT_VALGKORT));
		panel.initOppsett(oppsett);
		assertThat(panel.getValggeografiService()).isSameAs(lagNyttValgkortValggeografiService);
	}

	@Test
	public void getId_gittPanel_returnererId() throws Exception {
		assertThat(panel.getId()).isEqualTo("valggeografiPanel");
	}

	@Test
	public void getNavn_gittEttValgbartValggeografiNivaa_returnerNavnForNivaa() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(VALGHENDELSE));
		panel.initOppsett(oppsett);
		assertThat(panel.getNavn()).isEqualTo("@area_level[" + VALGHENDELSE.nivaa() + "].name");
	}

	@Test
	public void getNavn_gittFlereValgbareValggeografiNivaa_returnerGenereltNavn() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(VALGHENDELSE, LAND));
		panel.initOppsett(oppsett);
		assertThat(panel.getNavn()).isEqualTo("@area.common.area_level");
	}

	@Test
	public void velg_gittNivaa_setterValgtStiOgKallerRedirectTilUrlEllerInitNestePanelPaaController() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE));
		when(service.fylkeskommuner(userData, valghierarkiPanel.valgtVerdi(), null)).thenReturn(singletonList(FYLKESKOMMUNE_111111_11_11));
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		panel.velg(FYLKESKOMMUNE);
		assertThat(panel.valgtVerdi()).isEqualTo(panel.getFylkeskommunerTabell().valgtSti());
		verify(controller).redirectTilUrlEllerInitNestePanel();
	}

	@Test(dataProvider = "nivaaOgResultat")
	public void visKnapp_gittNivaa_returnerForventetVerdi(ValggeografiNivaa nivaa, boolean resultat) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE));
		when(service.fylkeskommuner(userData, valghierarkiPanel.valgtVerdi(), null)).thenReturn(singletonList(FYLKESKOMMUNE_111111_11_11));
		panel.initOppsett(oppsett);
		assertThat(panel.visKnapp(nivaa)).isEqualTo(resultat);
	}

	@DataProvider
	public Object[][] nivaaOgResultat() {
		return new Object[][]{
				{VALGHENDELSE, false},
				{LAND, false},
				{FYLKESKOMMUNE, true},
				{KOMMUNE, false},
				{BYDEL, false},
				{STEMMEKRETS, false},
				{STEMMESTED, false},
				{RODE, false}
		};
	}
}
