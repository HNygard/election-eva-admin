package no.valg.eva.admin.configuration.domain.model.manntall.papir;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RodefordelerMedSpesialtegnTest {

	@Test
	public void testNorwegianSpecificSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Å", "Ø", null));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("Æ", "Ø", null));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ø", "Ø", null));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("Ø", "Å", null));

		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("å", "Ø", null));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("æ", "Ø", null));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ø", "Æ", null));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("ø", "Å", null));

		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("AA", "Å", null));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("AA", "S", null));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("AA", "Æ", "Å"));

		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("AE", "A", "B"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ae", "A", "B"));
	}

	@Test
	public void testSwedishSpecificSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ä", "Æ", "Ø"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ö", "Ø", "Å"));
		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("Ö", "Å", null));

		Assert.assertFalse(Rodefordeler.erEtternavnIIntervall("ä", "Ø", null));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ö", "Ø", "Å"));
	}

	@Test
	public void testDiacriticSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ő", "Ø", "Å"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ő", "Ø", "Å"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ë", "E", "F"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ê", "E", "F"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ô", "O", "P"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("é", "E", "F"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("É", "E", "F"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("è", "E", "F"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("È", "e", "F"));
	}

	@Test
	public void testGermanSpecificSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ü", "X", "Y"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ü", "X", "Y"));

		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ß", "S", "T"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("SS", "S", "T"));
	}

	@Test
	public void testIcelandicSpecificSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ð", "D", "E"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("ð", "D", "E"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Þ", "T", "U"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("þ", "T", "U"));
	}

	@Test
	public void testOther() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("OE", "O", "P"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("œ", "O", "P"));
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Œ", "O", "P"));
	}

	@Test
	public void testSamiSpecificSigns() {
		Assert.assertTrue(Rodefordeler.erEtternavnIIntervall("Ŧ", "Å", null));
	}
}
