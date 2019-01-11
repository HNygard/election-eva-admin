package no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall;

import static no.evote.constants.EvoteConstants.BALLOT_BLANK;
import static no.evote.constants.EvoteConstants.VALGNATT_PARTY_ID_BLANKE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class StemmetallTest {

	private static final int FHS = 5;

	@Test
	public void asJsonObject_blank_replacedByBlanke() {
		Stemmetall stemmetall = new Stemmetall(BALLOT_BLANK, FHS, 0, true, false, FHS, 0, true, false, null);

		assertThat(stemmetall.asJsonObject().get("partikode").toString()).contains(VALGNATT_PARTY_ID_BLANKE);
	}

	@Test
	public void asJsonObject_whenNotVts_vtsIsNotIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, false, FHS, 0, true, false, null);
		
		assertThat(stemmetall.asJsonObject().containsKey("vts-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("vts-endelig")).isFalse();
	}

	@Test
	public void asJsonObject_whenNotFhs_fhsIsNotIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, false, false, FHS, 0, false, false, null);
		
		assertThat(stemmetall.asJsonObject().containsKey("fhs-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("fhs-endelig")).isFalse();
	}

	@Test
	public void asJsonObject_whenPreliminaryAndNotFinalFhsAndVts_fhsAndVtsAreIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, true, FHS, 0, false, false, null);
		
		assertThat(stemmetall.asJsonObject().containsKey("fhs-foreløpig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("vts-foreløpig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("fhs-endelig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("vts-endelig")).isFalse();
	}

	@Test
	public void asJsonObject_whenFinalFhsAndVts_fhsAndVtsAreIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, false, false, FHS, 0, true, true, null);

		assertThat(stemmetall.asJsonObject().containsKey("fhs-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("vts-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("fhs-endelig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("vts-endelig")).isTrue();
	}

	@Test
	public void asJsonObject_foreløpigFhsOgEndeligFhsForeløpigVts_endeligFhsogForeløpigVts() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, true, FHS, 0, true, false, null);

		assertThat(stemmetall.asJsonObject().containsKey("fhs-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("vts-foreløpig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("fhs-endelig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("vts-endelig")).isFalse();
	}

	@Test
	public void asJsonObject_foreløpigFhsOgEndeligFhsForeløpigOgEndeligVts_endeligFhsogForeløpigVts() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, true, FHS, 0, true, true, null);

		assertThat(stemmetall.asJsonObject().containsKey("fhs-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("vts-foreløpig")).isFalse();
		assertThat(stemmetall.asJsonObject().containsKey("fhs-endelig")).isTrue();
		assertThat(stemmetall.asJsonObject().containsKey("vts-endelig")).isTrue();
	}

	@Test
	public void asJsonObject_whenNotLis_lisIsNotIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, false, FHS, 0, true, false, null);

		assertThat(stemmetall.asJsonObject().containsKey("lis")).isFalse();
	}

	@Test
	public void asJsonObject_whenLis_lisIsIncluded() {
		Stemmetall stemmetall = new Stemmetall("A", FHS, 0, true, false, FHS, 0, true, false, 1);

		assertThat(stemmetall.asJsonObject().containsKey("lis")).isTrue();
	}

}
