package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.SIDE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett.kontekstvelgerKonfigs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class KontekstvelgerOppsettTest {

	@DataProvider
	public static Object[][] equalsHashCodeTestData() {
		KontekstvelgerOppsett oppsettMedKommune = new KontekstvelgerOppsett();
		oppsettMedKommune.leggTil(geografi(KOMMUNE));
		KontekstvelgerOppsett oppsettOgsaaMedKommune = new KontekstvelgerOppsett();
		oppsettOgsaaMedKommune.leggTil(geografi(KOMMUNE));
		KontekstvelgerOppsett oppsettMedValggruppe = new KontekstvelgerOppsett();
		oppsettMedValggruppe.leggTil(hierarki(VALGGRUPPE));
		return new Object[][]{
				{new KontekstvelgerOppsett(), new KontekstvelgerOppsett(), true},
				{new KontekstvelgerOppsett(), new Object(), false},
				{new KontekstvelgerOppsett(), null, false},
				{oppsettMedKommune, oppsettMedKommune, true},
				{oppsettMedKommune, oppsettOgsaaMedKommune, true},
				{oppsettMedKommune, new KontekstvelgerOppsett(), false},
				{oppsettMedKommune, oppsettMedValggruppe, false}
		};
	}

	@Test(dataProvider = "ugyldigeKonfigurasjoner", expectedExceptions = IllegalArgumentException.class)
	public void kontekstvelgerKonfigs_gittUgyldigeVerdier_kasterException(String s) throws Exception {
		kontekstvelgerKonfigs(s);
	}

	@DataProvider
	public Object[][] ugyldigeKonfigurasjoner() {
		return new Object[][]{
				{null},
				{"one"},
				{"one]"},
				{"[one"},
				{"[one["},
				{"[one][two"},
				{"[one][two["},
				{"[one]two]"},
		};
	}

	@Test(dataProvider = "gyldigeKonfigurasjoner")
	public void kontekstvelgerKonfigs_gittGyldigeVerdier_returnererForventet(String s, List<String> forventet) throws Exception {
		List<String> result = kontekstvelgerKonfigs(s);
		assertThat(result).isEqualTo(forventet);
	}

	@DataProvider
	public Object[][] gyldigeKonfigurasjoner() {
		return new Object[][]{
				{"[one]", singletonList("one")},
				{"[one][two]", asList("one", "two")},
				{"[one=[one]][two]", asList("one=[one]", "two")},
				{"[one=[one=[one]]][two=two[two]]", asList("one=[one=[one]]", "two=two[two]")}
		};
	}

	@Test
	public void serialize_gittEnKonfigurasjon_returnererForventetSerialisertForm() throws Exception {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(geografi(KOMMUNE));
		assertThat(setup.serialize()).isEqualTo("[geografi|nivaer|3]");
	}

	@Test
	public void serialize_gittToKonfigurasjonenr_returnererForventetSerialisertForm() throws Exception {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALG));
		setup.leggTil(geografi(KOMMUNE));
		assertThat(setup.serialize()).isEqualTo("[hierarki|nivaer|2][geografi|nivaer|3]");
	}

	@Test
	public void deserialize_gittToKonfigurasjoner_returnererForventetObjektstruktur() throws Exception {
		KontekstvelgerOppsett setup = KontekstvelgerOppsett
				.deserialize("[hierarki|nivaer|3][geografi|nivaer|3][side|uri|/secure/something?test=test]");
		assertThat(setup.getElementer()).hasSize(3);
		assertThat(setup.getElementer().get(0).getType()).isSameAs(HIERARKI);
		assertThat(setup.getElementer().get(1).getType()).isSameAs(GEOGRAFI);
		assertThat(setup.getElementer().get(2).getType()).isSameAs(SIDE);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestDate_returnererForventetResultat(KontekstvelgerOppsett oppsett, Object o, boolean resultat) throws Exception {
		assertThat(oppsett.equals(o)).isEqualTo(resultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestDate_returnererForventetResultat(KontekstvelgerOppsett oppsett, Object o, boolean resultat) throws Exception {
		assertThat(oppsett.hashCode() == hashCode(o)).isEqualTo(resultat);
	}

	private int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}
}

