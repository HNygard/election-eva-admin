package no.valg.eva.admin.frontend.kontekstvelger.panel;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_111111;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.LISTEFORSLAG;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.SLETT_VALGOPPGJOER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiPanelTest extends MockUtilsTestCase {
	private ValghierarkiPanel panel;
	private KontekstvelgerController controller;
	private UserData userData;
	private ValghierarkiService service;
	private ValghierarkiService slettValgoppgjoerValghierarkiService;
	private ValghierarkiService listeforslagValghierarkiService;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = createMock(KontekstvelgerController.class);
		OpptellingskategoriPanel opptellingskategoriPanel = createMock(OpptellingskategoriPanel.class);
		userData = createMock(UserData.class);
		service = createMock(ValghierarkiService.class);
		slettValgoppgjoerValghierarkiService = createMock(ValghierarkiService.class);
		listeforslagValghierarkiService = createMock(ValghierarkiService.class);
		panel = new ValghierarkiPanel(controller, opptellingskategoriPanel, userData, service, slettValgoppgjoerValghierarkiService, listeforslagValghierarkiService);
	}

	@Test(dataProvider = "nivaaer")
	public void navn_gittNivaa_returnerForventetNavn(ValghierarkiNivaa nivaa) throws Exception {
		assertThat(ValghierarkiPanel.navn(nivaa)).isEqualTo("@election_level[" + nivaa.nivaa() + "].name");
	}

	@DataProvider
	public Object[][] nivaaer() {
		return new Object[][]{
				{VALGHENDELSE},
				{VALGGRUPPE},
				{VALG},
				{VALGDISTRIKT}
		};
	}

	@Test
	public void init_gittOppsettUtenValghierarki_panelErIkkeValgbar() throws Exception {
		panel.initOppsett(new KontekstvelgerOppsett());
		assertThat(panel.erValgbar()).isFalse();
	}

	@Test(dataProvider = "nivaaerOgStier")
	public void intt_gittOppsettMedValghierarkiOgTjenesteReturnererEnVerdi_verdiErValgt(ValghierarkiNivaa nivaa, ValghierarkiSti sti) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(nivaa));
		when(service.valghendelse(userData)).thenReturn(VALGHENDELSE_111111);
		when(service.valggrupper(userData)).thenReturn(singletonList(VALGGRUPPE_111111_11));
		when(service.valg(userData, VALGGRUPPE_STI, null)).thenReturn(singletonList(VALG_111111_11_11));
		when(service.valgdistrikter(userData, VALG_STI)).thenReturn(singletonList(VALGDISTRIKT_111111_11_11_111111));
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		assertThat(panel.erValgbar()).isTrue();
		assertThat(panel.valgtVerdi()).isEqualTo(sti);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@Test(dataProvider = "nivaaerOgStier")
	public void init_gittOppsettMedValghierarkiOgKontekstMedValghierarkiSti(ValghierarkiNivaa nivaa, ValghierarkiSti sti) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(nivaa));
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(sti);
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.erValgbar()).isTrue();
		assertThat(panel.valgtVerdi()).isEqualTo(sti);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@DataProvider
	public Object[][] nivaaerOgStier() {
		return new Object[][]{
				{VALGHENDELSE, VALGHENDELSE_STI},
				{VALGGRUPPE, VALGGRUPPE_STI},
				{VALG, VALG_STI},
				{VALGDISTRIKT, VALGDISTRIKT_STI}
		};
	}

	@Test
	public void valgtValghierarki_gittPanelUtenValgtSti_returnerNull() throws Exception {
		assertThat(panel.valgtValghierarki()).isNull();
	}

	@Test
	public void valgtValghierarki_gittPanelMedValgtStiOgStandardTjeneste_returnerValgtValghierarki() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		when(service.valghierarki(VALG_STI)).thenReturn(VALG_111111_11_11);
		oppsett.leggTil(hierarki(VALG));
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.valgtValghierarki()).isEqualTo(VALG_111111_11_11);
	}

	@Test
	public void valgtValghierarki_gittPanelMedValgtStiOgSlettValgoppgjoerTjeneste_returnerValgtValghierarki() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		when(slettValgoppgjoerValghierarkiService.valghierarki(VALG_STI)).thenReturn(VALG_111111_11_11);
		oppsett.leggTil(hierarki(VALG).medTjeneste(SLETT_VALGOPPGJOER));
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.valgtValghierarki()).isEqualTo(VALG_111111_11_11);
	}

	@Test
	public void valgtValghierarki_gittPanelMedValgtStiOgListeforslagTjeneste_returnerValgtValghierarki() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		when(listeforslagValghierarkiService.valghierarki(VALG_STI)).thenReturn(VALG_111111_11_11);
		oppsett.leggTil(hierarki(VALG).medTjeneste(LISTEFORSLAG));
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.valgtValghierarki()).isEqualTo(VALG_111111_11_11);
	}

	@Test
	public void getId_gittPanel_returnererId() throws Exception {
		assertThat(panel.getId()).isEqualTo("valghierarkiPanel");
	}

	@Test
	public void getNavn_gittEttValgbartNivaa_returnerSpesifiktNavn() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGHENDELSE));
		panel.initOppsett(oppsett);
		assertThat(panel.getNavn()).isEqualTo("@election_level[" + VALGHENDELSE.nivaa() + "].name");
	}

	@Test
	public void getNavn_gittFlereValgbareNivaaer_returnerGenereltNavn() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGHENDELSE, VALGGRUPPE));
		when(service.valggrupper(userData)).thenReturn(singletonList(VALGGRUPPE_111111_11));
		panel.initOppsett(oppsett);
		assertThat(panel.getNavn()).isEqualTo("@election.common.election_level");
	}

	@Test
	public void velg_gittNivaa_setterValgtStiOgKallerRedirectTilUrlEllerInitNestePanelPaaController() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGHENDELSE, VALGGRUPPE));
		when(service.valggrupper(userData)).thenReturn(singletonList(VALGGRUPPE_111111_11));
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		panel.velg(VALGGRUPPE);
		assertThat(panel.valgtVerdi()).isEqualTo(VALGGRUPPE_STI);
		verify(controller).redirectTilUrlEllerInitNestePanel();
	}

	@Test(dataProvider = "nivaaOgResultat")
	public void visKnapp_gittNivaa_returnerForventetVerdi(ValghierarkiNivaa nivaa, boolean resultat) throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE));
		when(service.valggrupper(userData)).thenReturn(singletonList(VALGGRUPPE_111111_11));
		panel.initOppsett(oppsett);
		assertThat(panel.visKnapp(nivaa)).isEqualTo(resultat);
	}

	@DataProvider
	public Object[][] nivaaOgResultat() {
		return new Object[][]{
				{VALGHENDELSE, false},
				{VALGGRUPPE, true},
				{VALG, false},
				{VALGDISTRIKT, false}
		};
	}
}
