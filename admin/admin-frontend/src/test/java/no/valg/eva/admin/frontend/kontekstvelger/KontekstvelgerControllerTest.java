package no.valg.eva.admin.frontend.kontekstvelger;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.panel.OpptellingskategoriPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;
import no.valg.eva.admin.frontend.security.PageAccess;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.OPPSETT;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.side;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KontekstvelgerControllerTest extends BaseFrontendTest {
	private KontekstvelgerController controller;
	private ValghierarkiPanel valghierarkiPanel;
	private ValggeografiPanel valggeografiPanel;
	private OpptellingskategoriPanel opptellingskategoriPanel;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(KontekstvelgerController.class);
		valghierarkiPanel = getInjectMock(ValghierarkiPanel.class);
		valggeografiPanel = getInjectMock(ValggeografiPanel.class);
		opptellingskategoriPanel = getInjectMock(OpptellingskategoriPanel.class);
	}

	@Test
	public void doInit_gittController_initPanelerMedOppsettOgKontekst() {
		KontekstvelgerOppsett oppsett = kontekstvelgerOppsett(VALG, STEMMEKRETS, true);
		Kontekst kontekst = kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		getServletContainer().setRequestParameter(OPPSETT.toString(), oppsett.serialize());
		getServletContainer().setRequestParameter(KONTEKST.toString(), kontekst.serialize());
		controller.doInit();
		verify(valghierarkiPanel).initOppsett(oppsett);
		verify(valghierarkiPanel).initTabeller(kontekst);
		verify(valggeografiPanel).initOppsett(oppsett);
		verify(opptellingskategoriPanel).initOppsett(oppsett);
	}

	@Test(dataProvider = "valghierarkiStiOgValggeografiStiOgCountCategoryOgRedirectUrl")
	public void doInit_gittAlleVerdierValgt_redirecterTilUrl(
			ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, CountCategory countCategory, String redirectUrl) throws Exception {
		KontekstvelgerOppsett oppsett = kontekstvelgerOppsett(
				valghierarkiSti != null ? valghierarkiSti.nivaa() : null,
				valggeografiSti != null ? valggeografiSti.nivaa() : null,
				countCategory != null);
		getServletContainer().setRequestParameter(OPPSETT.toString(), oppsett.serialize());
		when(valghierarkiPanel.erVerdiValgt()).thenReturn(valghierarkiSti != null);
		when(valghierarkiPanel.erValgbar()).thenReturn(valghierarkiSti != null);
		when(valghierarkiPanel.valgtVerdi()).thenReturn(valghierarkiSti);
		when(valggeografiPanel.erVerdiValgt()).thenReturn(valggeografiSti != null);
		when(valggeografiPanel.erValgbar()).thenReturn(valggeografiSti != null);
		when(valggeografiPanel.valgtVerdi()).thenReturn(valggeografiSti);
		when(opptellingskategoriPanel.erVerdiValgt()).thenReturn(countCategory != null);
		when(opptellingskategoriPanel.erValgbar()).thenReturn(countCategory != null);
		when(opptellingskategoriPanel.valgtVerdi()).thenReturn(countCategory);
		when(getInjectMock(PageAccess.class).getPage(anyString())).thenReturn("redirectUrl");

		controller.doInit();

		getServletContainer().verifyRedirect(redirectUrl);
	}

	@DataProvider
	public Object[][] valghierarkiStiOgValggeografiStiOgCountCategoryOgRedirectUrl() {
		return new Object[][] {
				{ VALG_STI, KOMMUNE_STI, FO, "redirectUrl?kontekst=hierarki|111111.11.11|geografi|111111.11.11.1111|countCategory|FO&auto=true" },
				{ VALG_STI, KOMMUNE_STI, null, "redirectUrl?kontekst=hierarki|111111.11.11|geografi|111111.11.11.1111&auto=true" },
				{ VALG_STI, null, FO, "redirectUrl?kontekst=hierarki|111111.11.11|countCategory|FO&auto=true" },
				{ null, KOMMUNE_STI, FO, "redirectUrl?kontekst=geografi|111111.11.11.1111|countCategory|FO&auto=true" },
				{ VALG_STI, null, null, "redirectUrl?kontekst=hierarki|111111.11.11&auto=true" },
				{ null, KOMMUNE_STI, null, "redirectUrl?kontekst=geografi|111111.11.11.1111&auto=true" },
				{ null, null, FO, "redirectUrl?kontekst=countCategory|FO&auto=true" },
		};
	}

	@Test
	public void getAktueltPanel_gittForstePanelErValgt_returnererAndrePanel() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(VALG, STEMMEKRETS, false).serialize());
		controller.doInit();
		when(valghierarkiPanel.erVerdiIkkeValgtMenValgbar()).thenReturn(false);
		when(valggeografiPanel.erVerdiIkkeValgtMenValgbar()).thenReturn(true);
		assertThat(controller.getCurrentContextPickerPanel()).isSameAs(valggeografiPanel);
	}

	@Test
	public void redirectTilUrlEllerInitNestePanel_gittAlleVerdierErValgt_redirecterTilUrl() throws Exception {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(VALG, STEMMEKRETS, false).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		when(getInjectMock(PageAccess.class).getPage(anyString())).thenReturn("redirectUrl");
		controller.doInit();
		when(valghierarkiPanel.erVerdiValgt()).thenReturn(true);
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		when(valggeografiPanel.erVerdiValgt()).thenReturn(true);
		when(valggeografiPanel.valgtVerdi()).thenReturn(STEMMEKRETS_STI);

		controller.redirectTilUrlEllerInitNestePanel();

		getServletContainer().verifyRedirect("redirectUrl?kontekst=hierarki|111111.11.11|geografi|111111.11.11.1111.111111.1111&auto=false");
	}

	@Test
	public void redirectTilUrlEllerInitNestePanel_gittIkkeAlleVerdierErValgt_initNesteTabell() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(VALG, STEMMEKRETS, false).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		controller.doInit();
		when(valghierarkiPanel.erVerdiValgt()).thenReturn(true);
		when(valggeografiPanel.erVerdiIkkeValgtMenValgbar()).thenReturn(true);
		controller.redirectTilUrlEllerInitNestePanel();
		verify(valggeografiPanel).initTabeller(null);
	}

	@Test
	public void getLinkNavn1_gittValghierarkiPanelErValgbar_returnerNavn() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, null).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valghierarkiPanel.getNavn()).thenReturn("VALG");
		when(getMessageProviderMock().get("@common.choose")).thenReturn("Velg");
		when(getMessageProviderMock().get("VALG")).thenReturn("valg");
		controller.doInit();
		assertThat(controller.getLinkNavn1()).isEqualTo("Velg valg");
	}

	@Test
	public void getLinkNavn1_gittValggeografiPanelErValgbar_returnerNavn() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, null, STEMMEKRETS).serialize());
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.getNavn()).thenReturn("STEMMEKRETS");
		when(getMessageProviderMock().get("@common.choose")).thenReturn("Velg");
		when(getMessageProviderMock().get("STEMMEKRETS")).thenReturn("stemmekrets");
		controller.doInit();
		assertThat(controller.getLinkNavn1()).isEqualTo("Velg stemmekrets");
	}

	@Test
	public void getLinkNavn1_gittOpptellingskategoriPanelErValgbar_returnerNavn() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(true, null, null).serialize());
		when(opptellingskategoriPanel.erValgbar()).thenReturn(true);
		when(opptellingskategoriPanel.getNavn()).thenReturn("TELLEKATEGORI");
		when(getMessageProviderMock().get("@common.choose")).thenReturn("Velg");
		when(getMessageProviderMock().get("TELLEKATEGORI")).thenReturn("tellekategori");
		controller.doInit();
		assertThat(controller.getLinkNavn1()).isEqualTo("Velg tellekategori");
	}

	@Test
	public void getLinkNavn2_gittValghierarkiPanelOgValggeografiPanelErValgbar_returnerNavn() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, STEMMEKRETS).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.getNavn()).thenReturn("STEMMEKRETS");
		when(getMessageProviderMock().get("@common.choose")).thenReturn("Velg");
		when(getMessageProviderMock().get("STEMMEKRETS")).thenReturn("stemmekrets");
		controller.doInit();
		assertThat(controller.getLinkNavn2()).isEqualTo("Velg stemmekrets");
	}

	@Test
	public void isVisLink1_gittValghierarkiPanelErValgbarOgVerdiValgt_returnerTrue() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, STEMMEKRETS).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valghierarkiPanel.erVerdiValgt()).thenReturn(true);
		controller.doInit();
		assertThat(controller.isVisLink1()).isTrue();
	}

	@Test
	public void isVisLink1_gittValghierarkiPanelErValgbarOgIkkeVerdiValgt_returnerFalse() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, null).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valghierarkiPanel.erVerdiValgt()).thenReturn(false);
		controller.doInit();
		
		assertThat(controller.isVisLink1()).isFalse();
	}

	@Test
	public void isVisLink2_gittBareValghierarkiPanelErValgbar_returnerFalse() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, null).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		controller.doInit();
		assertThat(controller.isVisLink2()).isFalse();
	}

	@Test
	public void isVisLink2_gittValghierarkiPanelOgValggeografiPanelErValgbarOgIkkeVerdiValgt_returnerFalse() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, STEMMEKRETS).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erVerdiValgt()).thenReturn(false);
		controller.doInit();
		assertThat(controller.isVisLink2()).isFalse();
	}

	@Test
	public void isVisLink2_gittValghierarkiPanelOgValggeografiPanelErValgbarOgVerdiValgt_returnerFalse() {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(false, VALG, STEMMEKRETS).serialize());
		when(valghierarkiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erValgbar()).thenReturn(true);
		when(valggeografiPanel.erVerdiValgt()).thenReturn(true);
		controller.doInit();
		assertThat(controller.isVisLink2()).isTrue();
	}

	@Test
	public void visPanel1_gittOppsettMedValg_redirecterTilUrl() throws Exception {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett().serialize());
		controller.doInit();
		controller.visPanel1();
		getServletContainer().verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][side|uri|redirectUrl]");
	}

	@Test
	public void visPanel2_gittOppsettMedValgOgStemmekrets_redirecterTilUrl() throws Exception {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(VALG, STEMMEKRETS, false).serialize());
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		controller.doInit();
		controller.visPanel2();
		getServletContainer().verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][geografi|nivaer|5][side|uri|redirectUrl]"
				+ "&kontekst=hierarki|111111.11.11");
	}

	@Test
	public void visPanel2_gittOppsettMedStemmekretsOgOpptellingskategori_redirecterTilUrl() throws Exception {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(null, STEMMEKRETS, true).serialize());
		when(valggeografiPanel.valgtVerdi()).thenReturn(STEMMEKRETS_STI);
		controller.doInit();
		controller.visPanel2();
		getServletContainer().verifyRedirect(
				"/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|5][opptellingskategori][side|uri|redirectUrl]"
						+ "&kontekst=geografi|111111.11.11.1111.111111.1111");
	}

	@Test
	public void visPanel2_gittOppsettMedOpptellingskategoriOgValg_redirecterTilUrl() throws Exception {
		getServletContainer().setRequestParameter(OPPSETT.toString(), kontekstvelgerOppsett(true, VALG, null).serialize());
		when(opptellingskategoriPanel.valgtVerdi()).thenReturn(FO);
		controller.doInit();
		controller.visPanel2();
		getServletContainer().verifyRedirect(
				"/secure/kontekstvelger.xhtml?oppsett=[opptellingskategori][hierarki|nivaer|2][side|uri|redirectUrl]"
						+ "&kontekst=countCategory|FO");
	}

	private KontekstvelgerOppsett kontekstvelgerOppsett() {
		return kontekstvelgerOppsett(VALG, null, false);
	}

	private KontekstvelgerOppsett kontekstvelgerOppsett(ValghierarkiNivaa valghierarkiNivaa, ValggeografiNivaa valggeografiNivaa, boolean opptellingskategori) {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		if (valghierarkiNivaa != null) {
			oppsett.leggTil(hierarki(valghierarkiNivaa));
		}
		if (valggeografiNivaa != null) {
			oppsett.leggTil(geografi(valggeografiNivaa));
		}
		if (opptellingskategori) {
			oppsett.leggTil(opptellingskategori());
		}
		oppsett.leggTil(side("redirectUrl"));
		return oppsett;
	}

	private KontekstvelgerOppsett kontekstvelgerOppsett(boolean opptellingskategori, ValghierarkiNivaa valghierarkiNivaa, ValggeografiNivaa valggeografiNivaa) {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		if (opptellingskategori) {
			oppsett.leggTil(opptellingskategori());
		}
		if (valghierarkiNivaa != null) {
			oppsett.leggTil(hierarki(valghierarkiNivaa));
		}
		if (valggeografiNivaa != null) {
			oppsett.leggTil(geografi(valggeografiNivaa));
		}
		oppsett.leggTil(side("redirectUrl"));
		return oppsett;
	}

	private Kontekst kontekst() {
		return Kontekst.deserialize("hierarki|111111.11.11");
	}
}
