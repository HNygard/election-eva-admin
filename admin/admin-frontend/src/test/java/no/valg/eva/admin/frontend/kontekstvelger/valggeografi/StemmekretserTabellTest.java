package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETSER_111111_11_11_1111_111111_111X;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.BydelerTabellTestData.bydelerTabell;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.STEMMEKRETS_RAD_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StemmekretserTabellTest extends FellesValggeografiTabellTest {
	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittStemmekretserTabell_oppdatererStemmekretserTabellMedEnEllerIngenRader(
			BydelerTabell bydelerTabell, BydelSti bydelSti) throws Exception {
		when(valggeografiPanel.getBydelerTabell()).thenReturn(bydelerTabell);
		when(valggeografiService.stemmekretser(userData, bydelSti, null, null)).thenReturn(singletonList(STEMMEKRETS_111111_11_11_1111_111111_1111));
		StemmekretserTabell stemmekretserTabell = new StemmekretserTabell(valggeografiPanel, null, null);
		stemmekretserTabell.oppdater();
		if (bydelerTabell == null || bydelSti != null) {
			assertThat(stemmekretserTabell).harAntallRaderLikMed(1);
			assertThat(stemmekretserTabell).harValgtRadLikMed(STEMMEKRETS_RAD_111111_11_11_1111_111111_1111);
			assertThat(stemmekretserTabell).harValgtStiLikMed(STEMMEKRETS_STI_111111_11_11_1111_111111_1111);
		} else {
			assertThat(stemmekretserTabell).harIngenRader();
			assertThat(stemmekretserTabell).harIkkeValgtRad();
			assertThat(stemmekretserTabell).harIkkeValgtSti();
		}
	}

	@Test(dataProvider = "oppdaterTestData")
	public void oppdater_gittStemmekretserTabell_oppdatererStemmekretserTabellMedFlereEllerIngenRader(
			BydelerTabell bydelerTabell, BydelSti bydelSti) throws Exception {
		when(valggeografiPanel.getBydelerTabell()).thenReturn(bydelerTabell);
		when(valggeografiService.stemmekretser(userData, bydelSti, null, null)).thenReturn(STEMMEKRETSER_111111_11_11_1111_111111_111X);
		StemmekretserTabell stemmekretserTabell = new StemmekretserTabell(valggeografiPanel, null, null);
		stemmekretserTabell.oppdater();
		if (bydelerTabell == null || bydelSti != null) {
			
			assertThat(stemmekretserTabell).harAntallRaderLikMed(3);
			
		} else {
			assertThat(stemmekretserTabell).harIngenRader();
		}
		assertThat(stemmekretserTabell).harIkkeValgtRad();
		assertThat(stemmekretserTabell).harIkkeValgtSti();
	}

	@DataProvider
	public Object[][] oppdaterTestData() {
		return new Object[][]{
				new Object[]{null, null},
				new Object[]{bydelerTabell(null), null},
				new Object[]{bydelerTabell(BYDEL_STI), BYDEL_STI},
				new Object[]{null, null},
				new Object[]{bydelerTabell(null), null},
				new Object[]{bydelerTabell(BYDEL_STI), BYDEL_STI}
		};
	}

	@Test
	public void testValgtRadSatt() throws Exception {

	}

}
