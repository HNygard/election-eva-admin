package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNER_111111_11_1X;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_11;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.FYLKESKOMMUNE_RAD_111111_11_11;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FylkeskommunerTabellTest extends FellesValggeografiTabellTest {
	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittFylkeskommunerTabell_oppdatererTabellMedEnRad(ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiService.fylkeskommuner(userData, valghierarkiSti, null)).thenReturn(Collections.singletonList(FYLKESKOMMUNE_111111_11_11));
		FylkeskommunerTabell fylkeskommunerTabell = new FylkeskommunerTabell(valggeografiPanel, valghierarkiSti, null);
		fylkeskommunerTabell.oppdater();
		assertThat(fylkeskommunerTabell).harAntallRaderLikMed(1);
		assertThat(fylkeskommunerTabell).harValgtRadLikMed(FYLKESKOMMUNE_RAD_111111_11_11);
		assertThat(fylkeskommunerTabell).harValgtStiLikMed(FYLKESKOMMUNE_STI_111111_11_11);
	}

	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittFylkeskommunerTabell_oppdatererTabellMedFlereRader(ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiService.fylkeskommuner(userData, valghierarkiSti, null)).thenReturn(FYLKESKOMMUNER_111111_11_1X);
		FylkeskommunerTabell fylkeskommunerTabell = new FylkeskommunerTabell(valggeografiPanel, valghierarkiSti, null);
		fylkeskommunerTabell.oppdater();
		
		assertThat(fylkeskommunerTabell).harAntallRaderLikMed(3);
		
		assertThat(fylkeskommunerTabell).harIkkeValgtRad();
		assertThat(fylkeskommunerTabell).harIkkeValgtSti();
	}

	@DataProvider
	public Object[][] oppdaterTestData() {
		return new Object[][]{
				new Object[]{null},
				new Object[]{valghierarkiSti()}
		};
	}

	@Test
	public void valgtRadSatt_gittFylkeskommunerTabell_oppdatererKommunerTabell() throws Exception {
		FylkeskommunerTabell fylkeskommunerTabell = new FylkeskommunerTabell(valggeografiPanel, null, null);
		fylkeskommunerTabell.valgtRadSatt();
		verify(valggeografiPanel).oppdaterKommunerTabell();
	}

}
