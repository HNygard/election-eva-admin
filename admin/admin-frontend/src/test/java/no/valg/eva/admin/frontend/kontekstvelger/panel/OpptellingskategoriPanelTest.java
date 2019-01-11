package no.valg.eva.admin.frontend.kontekstvelger.panel;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.OpptellingskategoriService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori.OpptellingskategoriRad;
import no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori.OpptellingskategoriTabell;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpptellingskategoriPanelTest extends MockUtilsTestCase {
	private OpptellingskategoriPanel panel;
	private KontekstvelgerController controller;
	private UserData userData;
	private OpptellingskategoriService service;
	private ValghierarkiPanel valghierarkiPanel;

	@BeforeMethod
	public void setUp() throws Exception {
		panel = initializeMocks(OpptellingskategoriPanel.class);
		controller = getInjectMock(KontekstvelgerController.class);
		userData = getInjectMock(UserData.class);
		service = getInjectMock(OpptellingskategoriService.class);
		valghierarkiPanel = getInjectMock(ValghierarkiPanel.class);
	}

	@Test
	public void init_gittOppsettUtenOpptellingskategoriOgIngenKontekst_verdiErIkkeValgtOgIkkeValgbar() throws Exception {
		panel.initOppsett(new KontekstvelgerOppsett());
		assertThat(panel.valgtVerdi()).isNull();
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@Test
	public void init_gittOppsettMedOpptellingskategoriOgIngenKontekst_verdiErIkkeValgtMenValgbar() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		panel.initOppsett(oppsett);
		assertThat(panel.valgtVerdi()).isNull();
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isFalse();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isTrue();
	}

	@Test
	public void init_gittOppsettMedOpptellingskategoriOgKontekstUtenOpptellingskategori_verdiErIkkeValgtMenValgbar() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		panel.initOppsett(oppsett);
		panel.initTabeller(new Kontekst());
		assertThat(panel.valgtVerdi()).isNull();
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isFalse();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isTrue();
	}

	@Test
	public void init_gittOppsettMedOpptellingskategoriOgKontekstMedOpptellingskategori_verdiErValgtOgValgbar() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		Kontekst kontekst = Kontekst.deserialize("countCategory|FO");
		panel.initOppsett(oppsett);
		panel.initTabeller(kontekst);
		assertThat(panel.valgtVerdi()).isEqualTo(FO);
		assertThat(panel.erVerdiValgtEllerIkkeValgbar()).isTrue();
		assertThat(panel.erVerdiIkkeValgtMenValgbar()).isFalse();
	}

	@Test
	public void getId_gittOpptellingskategoriPanel_returnererId() throws Exception {
		assertThat(panel.getId()).isEqualTo("opptellingskategoriPanel");
	}

	@Test
	public void getNavn_gittOpptellingskategoriPanel_returnererNavn() throws Exception {
		assertThat(panel.getNavn()).isEqualTo("@count.ballot.approve.rejected.category");
	}

	@Test
	public void getTabeller_gittOpptellingskategoriPanel_returnererEnTabell() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		panel.initOppsett(oppsett);
		List<OpptellingskategoriTabell> tabeller = panel.getTabeller();
		assertThat(tabeller).hasSize(1);
	}

	@Test
	public void velg_gittOpptellingskategoriPanel_velgerValgtCountCategory() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		when(valghierarkiPanel.valgtVerdi()).thenReturn(VALG_STI);
		panel.initOppsett(oppsett);
		panel.initTabeller(null);
		panel.getTabeller().get(0).setValgtRad(new OpptellingskategoriRad(FO));
		panel.velg("OPPTELLINGSKATEGORI");
		assertThat(panel.valgtVerdi()).isEqualTo(FO);
		verify(controller).redirectTilUrlEllerInitNestePanel();
	}

}
