package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.NIVAER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.FORHAND_ORDINAERE;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste.LAG_NYTT_VALGKORT;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant.ALT_VELG_BYDEL;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.SLETT_VALGOPPGJOER;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KontekstvelgerElementTest {

	@Test(dataProvider = "deserializeWithInvalidValue", expectedExceptions = IllegalArgumentException.class)
	public void getCfgs_withInvalidValues_throwsException(String s) throws Exception {
		KontekstvelgerElement.deserialize(s);
	}

	@DataProvider
	public Object[][] deserializeWithInvalidValue() {
		return new Object[][]{
				{null},
				{""},
				{"invalid"},
				{"ELECTION|hey|ho|INVALID"},
				{"hierarki|nivaaer"},
				{"hierarki|nivaaer|"}
		};
	}

	@Test
	public void election_withOneLevel_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.hierarki(VALGGRUPPE);

		assertThat(cfg.serialize()).isEqualTo("hierarki|nivaer|1");
	}

	@Test
	public void hierarki_medTjeneste_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.hierarki(VALGGRUPPE).medTjeneste(SLETT_VALGOPPGJOER);

		assertThat(cfg.serialize()).isEqualTo("hierarki|nivaer|1|tjeneste|SLETT_VALGOPPGJOER");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hierarki_medGeografiTjeneste_kasterException() throws Exception {
		KontekstvelgerElement.hierarki(VALGGRUPPE).medTjeneste(LAG_NYTT_VALGKORT);
	}

	@Test
	public void election_withTwoLevels_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.hierarki(VALGHENDELSE, VALGGRUPPE);

		assertThat(cfg.serialize()).isEqualTo("hierarki|nivaer|0,1");
	}

	@Test
	public void area_withOneLevel_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.geografi(KOMMUNE);

		assertThat(cfg.serialize()).isEqualTo("geografi|nivaer|3");
	}

	@Test
	public void geografi_medTjeneste_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.geografi(KOMMUNE).medTjeneste(LAG_NYTT_VALGKORT);

		assertThat(cfg.serialize()).isEqualTo("geografi|nivaer|3|tjeneste|LAG_NYTT_VALGKORT");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void geografi_medHierarkiTjeneste_kasterException() throws Exception {
		KontekstvelgerElement.geografi(KOMMUNE).medTjeneste(SLETT_VALGOPPGJOER);
	}

	@Test
	public void geografi_medFilter_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.geografi(KOMMUNE).medFilter(FORHAND_ORDINAERE);

		assertThat(cfg.serialize()).isEqualTo("geografi|nivaer|3|filter|FORHAND_ORDINAERE");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hierarki_medGeografiFilter_kasterException() throws Exception {
		KontekstvelgerElement.hierarki(VALG).medFilter(FORHAND_ORDINAERE);
	}

	@Test
	public void geografi_medVariant_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.geografi(BYDEL, STEMMEKRETS).medVariant(ALT_VELG_BYDEL);

		assertThat(cfg.serialize()).isEqualTo("geografi|nivaer|4,5|variant|ALT_VELG_BYDEL");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hierarki_medGeografiVariant_kasterException() throws Exception {
		KontekstvelgerElement.hierarki(VALG).medVariant(ALT_VELG_BYDEL);
	}

	@Test
	public void area_withTwoLevels_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.geografi(FYLKESKOMMUNE, KOMMUNE);

		assertThat(cfg.serialize()).isEqualTo("geografi|nivaer|2,3");
	}

	@Test
	public void opptellingskategori_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.opptellingskategori();

		assertThat(cfg.serialize()).isEqualTo("opptellingskategori");
	}

	@Test
	public void redirect_withUri_verifySerialize() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.side("/mu/uri");

		assertThat(cfg.serialize()).isEqualTo("side|uri|/mu/uri");
	}

	@Test
	public void deserialize_withAreaString_verifyCfg() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.deserialize("geografi|nivaer|2,3");

		assertThat(cfg.getType()).isSameAs(GEOGRAFI);
		assertThat(cfg.get(NIVAER)).isEqualTo("2,3");
	}

	@Test
	public void deserialize_withElectionString_verifyCfg() throws Exception {
		KontekstvelgerElement cfg = KontekstvelgerElement.deserialize("hierarki|nivaer|2");

		assertThat(cfg.getType()).isSameAs(HIERARKI);
		assertThat(cfg.get(NIVAER)).isEqualTo("2");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_girForventetResultat(KontekstvelgerElement element, Object o, boolean resultat) throws Exception {
		assertThat(element.equals(o)).isEqualTo(resultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_girForventetResultat(KontekstvelgerElement element, Object o, boolean resultat) throws Exception {
		assertThat(element.hashCode() == hashCode(o)).isEqualTo(resultat);
	}

	private int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		KontekstvelgerElement element = KontekstvelgerElement.deserialize("hierarki|nivaer|2");
		return new Object[][]{
				{element, null, false},
				{element, new Object(), false},
				{element, element, true},
				{element, KontekstvelgerElement.deserialize("hierarki|nivaer|2"), true},
				{element, KontekstvelgerElement.deserialize("hierarki|nivaer|3"), false}
		};
	}
}
