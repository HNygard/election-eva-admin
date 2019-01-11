package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valghierarki.ValghierarkiPanelTestData.valghierarkiPanel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiTabellTest extends MockUtilsTestCase {
	private ValghierarkiService service;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = createMock(ValghierarkiService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void getId_gittTabell_returnererNivaa() throws Exception {
		assertThat(valghierarkiTabell().getId()).isEqualTo(VALG);
	}

	@Test
	public void getNavn_gittTabell_returnerVisningsnavnPaaNivaa() throws Exception {
		assertThat(valghierarkiTabell().getNavn()).isEqualTo(VALG.visningsnavn());
	}

	@Test(dataProvider = "trueFalse")
	public void isVisKnapp_gittTabell_returnererForventetVerdi(boolean visKnappResultat) throws Exception {
		ValghierarkiPanel panel = valghierarkiPanel();
		when(panel.visKnapp(VALG)).thenReturn(visKnappResultat);
		assertThat(valghierarkiTabell(panel).isVisKnapp()).isEqualTo(visKnappResultat);
	}

	@DataProvider
	public Object[][] trueFalse() {
		return new Object[][]{
				{true},
				{false}
		};
	}

	@Test
	public void valgSti_gittIngenValgtRad_returnererNull() throws Exception {
		assertThat(valghierarkiTabell().valgtSti()).isNull();
	}

	@Test
	public void initIngenRader_gittTabell_girTomRadListe() throws Exception {
		ValghierarkiTabell<ValgSti, Valg> tabell = valghierarkiTabell();
		tabell.initIngenRader();
		assertThat(tabell.getRader()).isEmpty();
	}

	private ValghierarkiTabell<ValgSti, Valg> valghierarkiTabell() {
		return valghierarkiTabell(valghierarkiPanel());
	}

	private ValghierarkiTabell<ValgSti, Valg> valghierarkiTabell(ValghierarkiPanel panel) {
		return new ValghierarkiTabell<ValgSti, Valg>(panel, VALG, service, userData) {
			@Override
			protected void valgtRadSatt() {
			}

			@Override
			public void oppdater() {
			}
		};
	}

}
