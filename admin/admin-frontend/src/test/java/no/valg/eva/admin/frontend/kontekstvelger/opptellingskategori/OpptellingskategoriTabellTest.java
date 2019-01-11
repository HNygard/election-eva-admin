package no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.OpptellingskategoriService;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.frontend.kontekstvelger.panel.OpptellingskategoriPanel;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpptellingskategoriTabellTest extends MockUtilsTestCase {
	private OpptellingskategoriPanel panel;
	private OpptellingskategoriService service;
	private UserData userData;
	private OpptellingskategoriTabell tabell;

	@BeforeMethod
	public void setUp() throws Exception {
		panel = createMock(OpptellingskategoriPanel.class);
		service = createMock(OpptellingskategoriService.class);
		userData = createMock(UserData.class);
		when(panel.getOpptellingskategoriService()).thenReturn(service);
		when(panel.getUserData()).thenReturn(userData);
		tabell = new OpptellingskategoriTabell(panel, VALG_STI);
	}

	@Test
	public void valgtRadSatt_gittOpptellingskategoriTabell_gjorIngenting() throws Exception {
		tabell.valgtRadSatt();
		verifyNoMoreInteractions(panel);
	}

	@Test
	public void oppdater_gittValgStiNull_setterRader() throws Exception {
		tabell = new OpptellingskategoriTabell(panel, null);
		when(service.countCategories(userData)).thenReturn(asList(FO, FS));
		tabell.oppdater();
		assertThat(tabell.getRader()).containsExactly(opptellingskategoriRad(FO), opptellingskategoriRad(FS));
	}

	@Test
	public void oppdater_gittValgStiNotNull_setterRader() throws Exception {
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		tabell.oppdater();
		assertThat(tabell.getRader()).containsExactly(opptellingskategoriRad(FO), opptellingskategoriRad(FS));
	}

	@Test
	public void getId_gittOpptellingskategoriTabell_returnererId() throws Exception {
		assertThat(tabell.getId()).isEqualTo("OPPTELLINGSKATEGORI");
	}

	@Test
	public void getNavn_gittOpptellingskategoriTabell_returnererNavn() throws Exception {
		assertThat(tabell.getNavn()).isEqualTo("@count.ballot.approve.rejected.category");
	}

	@Test
	public void isVisKnapp_gittOpptellingskategoriTabell_returnererTrue() throws Exception {
		assertThat(tabell.isVisKnapp()).isTrue();
	}

	@Test
	public void valgtCountCategory_gittIngenValgtRad_returnerNull() throws Exception {
		assertThat(tabell.valgtCountCategory()).isNull();
	}

	@Test
	public void valgtCountCategory_gittValgtRad_returnerNull() throws Exception {
		when(service.countCategoriesForValgSti(userData, VALG_STI)).thenReturn(asList(FO, FS));
		tabell.oppdater();
		tabell.setValgtRad(opptellingskategoriRad(FS));
		assertThat(tabell.valgtCountCategory()).isEqualTo(FS);
	}

	private OpptellingskategoriRad opptellingskategoriRad(CountCategory countCategory) {
		return new OpptellingskategoriRad(countCategory);
	}
}
