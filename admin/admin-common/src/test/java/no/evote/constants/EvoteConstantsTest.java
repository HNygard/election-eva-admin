package no.evote.constants;

import static no.evote.constants.EvoteConstants.VALID_EMAIL_REGEXP;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class EvoteConstantsTest {
	@Test
	public void testEmailPattern() throws Exception {
		assertTrue("abla.babla@baba.com".matches(VALID_EMAIL_REGEXP));
		assertTrue("abla@baba.com".matches(VALID_EMAIL_REGEXP));
		assertFalse("abla@baba.".matches(VALID_EMAIL_REGEXP));
		assertFalse("abla@.".matches(VALID_EMAIL_REGEXP));
		assertFalse("abla.babla.no".matches(VALID_EMAIL_REGEXP));
		assertTrue("kåre.klævold@elkjøp.no".matches(VALID_EMAIL_REGEXP), "Should allow norwegian letters æøå");
		assertTrue("Åge.Æsdahl@Økokrim.no".matches(VALID_EMAIL_REGEXP), "Should allow capital norwegian letters ÆØÅ");
	}
}
