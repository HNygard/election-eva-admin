package no.valg.eva.admin.common.configuration.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("unused")

public class ManntallsnummerTest {

	private static final String KORREKT_MANNTALLSNUMMER = "123456789080";

	@Test(dataProvider = "beregningKontrollsiffer")
	public void constructor_gittKortManntallsnummerOgValgårssiffer_beregnerKontrollsifferet(Long kortManntallsnummer, int valgårssiffer,
																							String forventetManntallsnummer) {
		assertThat(new Manntallsnummer(kortManntallsnummer, valgårssiffer).getManntallsnummer()).isEqualTo(forventetManntallsnummer);
	}

	@DataProvider
	private Object[][] beregningKontrollsiffer() {
		return new Object[][]{
			{ 1234567890L, 	8, "123456789080" },
			{ 12345L, 		8, "000001234582" },
			{ 123L, 		4, "000000012344" }
		};
	}

	@Test(dataProvider = "korrekteManntallsnumre")
	public void constructor_gittKorrektManntallsnummer_validererLengdeOgKontrollsiffer(String fulltManntallsnummer) {
		Manntallsnummer manntallsnummer = new Manntallsnummer(fulltManntallsnummer);
		assertThat(manntallsnummer.getManntallsnummer()).isEqualTo(fulltManntallsnummer);
	}

	@DataProvider
	private Object[][] korrekteManntallsnumre() {
		return new Object[][]{
			{ KORREKT_MANNTALLSNUMMER },
			{ "000001234582" },
			{ "000000012344" }
		};
	}

	@Test(dataProvider = "ugyldigeManntallsnumre", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittGaltManntallsnummer_kasterIllegalArgumentException(String fulltManntallsnummer) {
		new Manntallsnummer(fulltManntallsnummer);
	}

	@DataProvider
	private Object[][] ugyldigeManntallsnumre() {
		return new Object[][]{
			{ null },
			{ "000000012343" },
			{ "012343" },
			{ "A00000012344" }
		};
	}
	
	@Test(dataProvider = "ugyldigeKorteManntallsnumreOgValgaarssifre",
		expectedExceptions = IllegalArgumentException.class,
		expectedExceptionsMessageRegExp = ".*Ugyldig.*")
	public void constructor_gittGaltKortManntallsnummerEllerValgaarssiffer_kasterIllegalArgumentException(Long kortManntallsnummer, int valgaarssiffer) {
		new Manntallsnummer(kortManntallsnummer, valgaarssiffer);
	}

	@DataProvider
	private Object[][] ugyldigeKorteManntallsnumreOgValgaarssifre() {
		return new Object[][]{
			{ null, 1 },
			{ -3L, 1 },
			{ 3L, -1 },
			{ 3L, 10 }
		};
	}

	@Test(dataProvider = "kortManntallsnummer")
	public void getKortManntallsnummer_forEtManntallsnummer_henterDeTiFørsteSifrene(String manntallsnummer, Long forventetKortManntallsnummer) {
		assertThat(new Manntallsnummer(manntallsnummer).getKortManntallsnummer()).isEqualTo(forventetKortManntallsnummer);
	}

	@DataProvider
	private Object[][] kortManntallsnummer() {
		return new Object[][]{
			{ "123456789080", 1234567890L },
			{ "000001234582", 12345L }
		};
	}

	@Test(dataProvider = "kortManntallsnummerMedZeroPadding")
	public void getKortManntallsnummerMedZeroPadding_forEtManntallsnummer_henterDeTiFørsteSifrene(String manntallsnummer, String forventetKortManntallsnummer) {
		assertThat(new Manntallsnummer(manntallsnummer).getKortManntallsnummerMedZeroPadding()).isEqualTo(forventetKortManntallsnummer);
	}

	@DataProvider
	private Object[][] kortManntallsnummerMedZeroPadding() {
		return new Object[][]{
			{ "123456789080", "1234567890" },
			{ "000001234582", "0000012345" }
		};
	}

	@Test(dataProvider = "valgårssiffer")
	public void getValgårssiffer_forEtManntallsnummer_henterSifferNummer11(String manntallsnummer, Integer forventetValgårssiffer) {
		assertThat(new Manntallsnummer(manntallsnummer).getValgaarssiffer()).isEqualTo(forventetValgårssiffer);
	}

	@DataProvider
	private Object[][] valgårssiffer() {
		return new Object[][]{
			{ "012345678911", 1 },
			{ "012345678929", 2 },
			{ "012345678937", 3 }
		};
	}

	@Test(dataProvider = "kontrollsiffer")
	public void getKontrollsiffer_forEtManntallsnummer_henterSifferNummer12(String manntallsnummer, Integer forventetKontrollsiffer) {
		assertThat(new Manntallsnummer(manntallsnummer).getKontrollsiffer()).isEqualTo(forventetKontrollsiffer);
	}

	@DataProvider
	private Object[][] kontrollsiffer() {
		return new Object[][]{
			{ "012345678911", 1 },
			{ "123456789122", 2 }
		};
	}

	@Test(dataProvider = "sluttsifre")
	public void getSluttsifre_forEtManntallsnummer_henterDeToSisteSifrene(String manntallsnummer, String forventedeSluttsifre) {
		assertThat(new Manntallsnummer(manntallsnummer).getSluttsifre()).isEqualTo(forventedeSluttsifre);
	}

	@DataProvider
	private Object[][] sluttsifre() {
		return new Object[][]{
			{ "012345678911", "11" },
			{ "123456789122", "22" }
		};
	}

	@Test(dataProvider = "korrekteManntallsnumre")
	public void equals_gittLikeManntallsnumre_returnererTrue(String gyldigManntallsnummer) {
		Manntallsnummer manntallsnummer1 = new Manntallsnummer(gyldigManntallsnummer);
		Manntallsnummer manntallsnummer2 = new Manntallsnummer(gyldigManntallsnummer);
		assertThat(manntallsnummer1).isEqualTo(manntallsnummer2);
	}

	@Test(dataProvider = "ikkeManntallsobjekter")
	public void equals_gittNoeAnnetEnnManntallsnumre_returnererFalse(Object ikkeManntallsobjekt) {
		Manntallsnummer manntallsnummer = new Manntallsnummer(KORREKT_MANNTALLSNUMMER);
		assertThat(manntallsnummer).isNotEqualTo(ikkeManntallsobjekt);
	}

	@DataProvider
	private Object[][] ikkeManntallsobjekter() {
		return new Object[][]{
			{ null },
			{ "tullOgTøys" }
		};
	}

	@Test
	public void equals_gittSammeObjekt_returnererTrue() {
		Manntallsnummer manntallsnummer = new Manntallsnummer(KORREKT_MANNTALLSNUMMER);
		assertThat(manntallsnummer).isEqualTo(manntallsnummer);
	}

	@Test(dataProvider = "korrekteManntallsnumre")
	public void hashcode_gittLikeManntallsnumre_generererLikKode(String gyldigManntallsnummer) {
		Manntallsnummer manntallsnummer1 = new Manntallsnummer(gyldigManntallsnummer);
		Manntallsnummer manntallsnummer2 = new Manntallsnummer(gyldigManntallsnummer);
		assertThat(manntallsnummer1.hashCode()).isEqualTo(manntallsnummer2.hashCode());
	}
}

