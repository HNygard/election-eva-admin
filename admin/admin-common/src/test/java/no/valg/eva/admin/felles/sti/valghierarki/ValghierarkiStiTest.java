package no.valg.eva.admin.felles.sti.valghierarki;

import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiStiTest {
	@DataProvider
	public static Object[][] electionPathOgValghierarkiSti() {
		return new Object[][]{
				new Object[]{ELECTION_PATH_111111, VALGHENDELSE_STI},
				new Object[]{ELECTION_PATH_111111_11, VALGGRUPPE_STI},
				new Object[]{ELECTION_PATH_111111_11_11, VALG_STI},
				new Object[]{ELECTION_PATH_111111_11_11_111111, VALGDISTRIKT_STI}
		};
	}

	@Test(dataProvider = "electionPathOgValghierarkiSti")
	public void fra_gittElectionPath_returnererValghierarkiSti(ElectionPath electionPath, ValghierarkiSti valghierarkiSti) throws Exception {
		ValghierarkiSti resultat = ValghierarkiSti.fra(electionPath);
		assertThat(resultat).isEqualTo(valghierarkiSti);
	}

	@Test(dataProvider = "electionPathOgValghierarkiSti")
	public void electionPath_gittValghierarkiSti_returnerElectionPath(ElectionPath electionPath, ValghierarkiSti valghierarkiSti) throws Exception {
		assertThat(valghierarkiSti.electionPath()).isEqualTo(electionPath);
	}

	@Test(dataProvider = "stierOgNivaaer")
	public void nivaa_gittSti_returnererRiktigNivaa(ValghierarkiSti sti, ValghierarkiNivaa nivaa) throws Exception {
		assertThat(sti.nivaa()).isEqualTo(nivaa);
	}

	@DataProvider
	public Object[][] stierOgNivaaer() {
		return new Object[][]{
				new Object[]{VALGHENDELSE_STI, VALGHENDELSE},
				new Object[]{VALGGRUPPE_STI, VALGGRUPPE},
				new Object[]{VALG_STI, VALG},
				new Object[]{VALGDISTRIKT_STI, VALGDISTRIKT}
		};
	}

	@Test
	public void tilValgSti_gittValgSti_returnererValgSti() throws Exception {
		assertThat(VALG_STI.tilValgSti()).isSameAs(VALG_STI);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void tilValgSti_gittIkkeValgSti_kasterException() throws Exception {
		assertThat(VALGDISTRIKT_STI.tilValgSti());
	}
}
