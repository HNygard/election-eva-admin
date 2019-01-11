package no.valg.eva.admin.frontend.kontekstvelger;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KontekstTest {

	@Test(dataProvider = "serialize")
	public void serialize_withDataProvider_verifyExpected(Kontekst data, String expected) throws Exception {
		assertThat(data.serialize()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] serialize() {
		return new Object[][] {
				{kontekst(), null},
				{kontekst(ElectionPath.from("111111.22")), "hierarki|111111.22"},
				{kontekst(AreaPath.from("111111.33")), "geografi|111111.33"},
				{kontekst(FO), "countCategory|FO"},
				{kontekst(ElectionPath.from("111111.22"), AreaPath.from("111111.33"), FO), "hierarki|111111.22|geografi|111111.33|countCategory|FO"}
		};
	}

	private Kontekst kontekst() {
		return kontekst(null, null, null);
	}

	private Kontekst kontekst(ElectionPath electionPath) {
		return kontekst(electionPath, null, null);
	}

	private Kontekst kontekst(AreaPath areaPath) {
		return kontekst(null, areaPath, null);
	}

	private Kontekst kontekst(CountCategory countCategory) {
		return kontekst(null, null, countCategory);
	}

	private Kontekst kontekst(ElectionPath electionPath, AreaPath areaPath, CountCategory countCategory) {
		Kontekst kontekst = new Kontekst();
		if (electionPath != null) {
			kontekst.setValghierarkiSti(ValghierarkiSti.fra(electionPath));
		}
		if (areaPath != null) {
			kontekst.setValggeografiSti(ValggeografiSti.fra(areaPath));
		}
		if (countCategory != null) {
			kontekst.setCountCategory(countCategory);
		}
		return kontekst;
	}

	@Test(dataProvider = "deserializeInvalid")
	public void deserialize_withInvalidData_returnsNull(String s) throws Exception {
		assertThat(Kontekst.deserialize(s)).isNull();
	}

	@DataProvider
	public Object[][] deserializeInvalid() {
		return new Object[][] {
				{ null },
				{ "" },
				{ "unknown" },
				{ "hierarki|111111.22|unknown" },
				{ "hierarki|111111.22|geografi" },
				{ "hierarki|111111.22|wrong|value" }
		};
	}

	@Test
	public void deserialize_withValidData_returnsData() throws Exception {
		Kontekst data = Kontekst.deserialize("hierarki|111111.22|countCategory|FO|geografi|111111.33");

		assertThat(data).isNotNull();
		assertThat(data.getValghierarkiSti()).isNotNull();
		assertThat(data.getValghierarkiSti().electionPath().path()).isEqualTo("111111.22");
		assertThat(data.getCountCategory()).isNotNull();
		assertThat(data.getCountCategory()).isEqualTo(FO);
		assertThat(data.getValggeografiSti()).isNotNull();
		assertThat(data.getValggeografiSti().areaPath().path()).isEqualTo("111111.33");
	}

	@Test
	public void valggruppeSti_gittKontekstMedValggruppeSti_returnerValggruppeSti() throws Exception {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALGGRUPPE_STI);
		assertThat(kontekst.valggruppeSti()).isEqualTo(VALGGRUPPE_STI);
	}

	@Test
	public void kommuneSti_gittKontekstUtenValggeografiSti_returnererNull() throws Exception {
		assertThat(new Kontekst().kommuneSti()).isNull();
	}
}
