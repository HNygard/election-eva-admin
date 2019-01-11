package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNER_111111_11_11_111X;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.FylkeskommunerTabellTestData.fylkeskommunerTabell;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.KOMMUNE_RAD_111111_11_11_1111;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KommunerTabellTest extends FellesValggeografiTabellTest {
	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittKommunerTabell_oppdatererKommunerTabellMedEnEllerIngenRader(
			FylkeskommunerTabell fylkeskommunerTabell, FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiPanel.getFylkeskommunerTabell()).thenReturn(fylkeskommunerTabell);
		when(valggeografiService.kommuner(userData, fylkeskommuneSti, valghierarkiSti, null)).thenReturn(singletonList(KOMMUNE_111111_11_11_1111));
		KommunerTabell kommunerTabell = new KommunerTabell(valggeografiPanel, valghierarkiSti, null);
		kommunerTabell.oppdater();
		if (fylkeskommunerTabell == null || fylkeskommuneSti != null) {
			assertThat(kommunerTabell).harAntallRaderLikMed(1);
			assertThat(kommunerTabell).harValgtRadLikMed(KOMMUNE_RAD_111111_11_11_1111);
			assertThat(kommunerTabell).harValgtStiLikMed(KOMMUNE_STI_111111_11_11_1111);
		} else {
			assertThat(kommunerTabell).harIngenRader();
			assertThat(kommunerTabell).harIkkeValgtRad();
			assertThat(kommunerTabell).harIkkeValgtSti();
		}
	}

	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittKommunerTabell_oppdatererKommunerTabellMedFlereEllerIngenRader(
			FylkeskommunerTabell fylkeskommunerTabell, FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiPanel.getFylkeskommunerTabell()).thenReturn(fylkeskommunerTabell);
		when(valggeografiService.kommuner(userData, fylkeskommuneSti, valghierarkiSti, null)).thenReturn(KOMMUNER_111111_11_11_111X);
		KommunerTabell kommunerTabell = new KommunerTabell(valggeografiPanel, valghierarkiSti, null);
		kommunerTabell.oppdater();
		if (fylkeskommunerTabell == null || fylkeskommuneSti != null) {
			
			assertThat(kommunerTabell).harAntallRaderLikMed(3);
			
		} else {
			assertThat(kommunerTabell).harIngenRader();
		}
		assertThat(kommunerTabell).harIkkeValgtRad();
		assertThat(kommunerTabell).harIkkeValgtSti();
	}

	@DataProvider
	public Object[][] oppdaterTestData() {
		return new Object[][]{
				new Object[]{null, null, null},
				new Object[]{fylkeskommunerTabell(null), null, null},
				new Object[]{fylkeskommunerTabell(FYLKESKOMMUNE_STI), FYLKESKOMMUNE_STI, null},
				new Object[]{null, null, valghierarkiSti()},
				new Object[]{fylkeskommunerTabell(null), null, valghierarkiSti()},
				new Object[]{fylkeskommunerTabell(FYLKESKOMMUNE_STI), FYLKESKOMMUNE_STI, valghierarkiSti()}
		};
	}

	@Test
	public void valgtRadSatt_gittKommunerTabell_oppdatererBydelerTabell() throws Exception {
		KommunerTabell kommunerTabell = new KommunerTabell(valggeografiPanel, null, null);
		kommunerTabell.valgtRadSatt();
		verify(valggeografiPanel).oppdaterBydelerTabell();
	}

}
