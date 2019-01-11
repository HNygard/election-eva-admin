package no.evote.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PartyNameValidatorTest {

	private static final String PARTY_NAME_VALID_CHARS = "partinavn123,.-'/";
	private static final String PARTY_NAME_NOT_VALID_1 = "partinavn123_";
	private static final String PARTY_NAME_NOT_VALID_2 = "partinavn123?";
	private static final String PARTY_NAME_NOT_VALID_3 = "partinavn123!";

	private PartyNameValidator partyNameValidator;

	@BeforeClass
	public void setUp() {
		PartyNameCharacters partyNameCharacters = mock(PartyNameCharacters.class);
		partyNameValidator = new PartyNameValidator();
		when(partyNameCharacters.extraChars()).thenReturn(" .,-'/");
		partyNameValidator.initialize(partyNameCharacters);
	}

	@Test
	public void partyNameWithCommaIsValid() throws Exception {
		boolean returnValue = partyNameValidator.isValid(PARTY_NAME_VALID_CHARS, null);
		Assert.assertTrue(returnValue);
	}

	@Test
	public void partyNameWithUnderscoreIsNotValid() {
		boolean returnValue = partyNameValidator.isValid(PARTY_NAME_NOT_VALID_1, null);
		Assert.assertFalse(returnValue);
	}

	@Test
	public void partyNameWithQuestionMarkIsNotValid() {
		boolean returnValue = partyNameValidator.isValid(PARTY_NAME_NOT_VALID_2, null);
		Assert.assertFalse(returnValue);
	}

	@Test
	public void partyNameWithExclamationMarkIsNotValid() {
		boolean returnValue = partyNameValidator.isValid(PARTY_NAME_NOT_VALID_3, null);
		Assert.assertFalse(returnValue);
	}

}
