package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDELER_111111_11_11_1111_11111X;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111111;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.KommunerTabellTestData.kommunerTabell;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.BYDEL_RAD_111111_11_11_1111_111111;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BydelerTabellTest extends FellesValggeografiTabellTest {
	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittBydelerTabell_oppdatererBydelerTabellMedEnEllerIngenRader(
			KommunerTabell kommunerTabell, KommuneSti kommuneSti, ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiPanel.getKommunerTabell()).thenReturn(kommunerTabell);
		when(valggeografiService.bydeler(userData, kommuneSti, valghierarkiSti, null)).thenReturn(singletonList(BYDEL_111111_11_11_1111_111111));
		BydelerTabell bydelerTabell = new BydelerTabell(valggeografiPanel, valghierarkiSti, null);
		bydelerTabell.oppdater();
		if (kommunerTabell == null || kommuneSti != null) {
			assertThat(bydelerTabell).harAntallRaderLikMed(1);
			assertThat(bydelerTabell).harValgtRadLikMed(BYDEL_RAD_111111_11_11_1111_111111);
			assertThat(bydelerTabell).harValgtStiLikMed(BYDEL_STI_111111_11_11_1111_111111);
		} else {
			assertThat(bydelerTabell).harIngenRader();
			assertThat(bydelerTabell).harIkkeValgtRad();
			assertThat(bydelerTabell).harIkkeValgtSti();
		}
	}

	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittBydelerTabell_oppdatererBydelerTabellMedFlereEllerIngenRader(
			KommunerTabell kommunerTabell, KommuneSti kommuneSti, ValghierarkiSti valghierarkiSti) throws Exception {
		when(valggeografiPanel.getKommunerTabell()).thenReturn(kommunerTabell);
		when(valggeografiService.bydeler(userData, kommuneSti, valghierarkiSti, null)).thenReturn(BYDELER_111111_11_11_1111_11111X);
		BydelerTabell bydelerTabell = new BydelerTabell(valggeografiPanel, valghierarkiSti, null);
		bydelerTabell.oppdater();
		if (kommunerTabell == null || kommuneSti != null) {
			
			assertThat(bydelerTabell).harAntallRaderLikMed(3);
			
		} else {
			assertThat(bydelerTabell).harIngenRader();
		}
		assertThat(bydelerTabell).harIkkeValgtRad();
		assertThat(bydelerTabell).harIkkeValgtSti();
	}

	@DataProvider
	public Object[][] oppdaterTestData() {
		return new Object[][]{
				new Object[]{null, null, null},
				new Object[]{kommunerTabell(null), null, null},
				new Object[]{kommunerTabell(KOMMUNE_STI), KOMMUNE_STI, null},
				new Object[]{null, null, valghierarkiSti()},
				new Object[]{kommunerTabell(null), null, valghierarkiSti()},
				new Object[]{kommunerTabell(KOMMUNE_STI), KOMMUNE_STI, valghierarkiSti()}
		};
	}

	@Test
	public void testValgtRadSatt() throws Exception {

	}

}
