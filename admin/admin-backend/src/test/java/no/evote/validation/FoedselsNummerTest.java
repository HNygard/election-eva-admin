package no.evote.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FoedselsNummerTest {

	private static Validator validator;
	public static final String PROPERTY_ID = "id";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testNotNull() {
		Voter voter = buildVoter();
		voter.setId(null);

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testNotEmpty() {
		Voter voter = buildVoter();
		voter.setId("");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testNotBlank() {
		Voter voter = buildVoter();
		voter.setId(" ");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testNotUnderscore() {
		Voter voter = buildVoter();
		voter.setId("a_b");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testDot() {
		Voter voter = buildVoter();
		voter.setId("a.b");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testInternationalCharacters() {
		Voter voter = buildVoter();
		voter.setId("12å");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testSingleLetter() {
		Voter voter = buildVoter();
		voter.setId("c");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testSingleNumber() {
		Voter voter = buildVoter();
		voter.setId("1");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testLettersAndNumbers() {
		Voter voter = buildVoter();
		voter.setId("1a2b3c");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testOk() {
		Voter voter = buildVoter();
		voter.setId("26057511531");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIndividNumber19001999() {
		Voter voter = buildVoter();
		voter.setId("09023839889");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIndividNumber18551899() {
		Voter voter = buildVoter();
		voter.setId("09025850367");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIndividNumber18551899NotOk() {
		Voter voter = buildVoter();
		voter.setId("09025350372");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testIndividNumber20002039() {
		Voter voter = buildVoter();
		voter.setId("09023850300");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIndividNumber20002039NotOk() {
		Voter voter = buildVoter();
		voter.setId("09024275164");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testIndividNumber19401999() {
		Voter voter = buildVoter();
		voter.setId("09025090109");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIndividNumber19401999NotOk() {
		Voter voter = buildVoter();
		voter.setId("09024189985");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	@Test
	public void testIndividNumberNotOk() {
		Voter voter = buildVoter();
		voter.setId("04046783404");

		Set<ConstraintViolation<Voter>> constraintViolations = validator.validate(voter, ValideringVedManuellRegistrering.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), FoedselsNummer.MESSAGE);
	}

	private Voter buildVoter() {
		Voter voter = new Voter();
		voter.setElectionEvent(new ElectionEvent());
		voter.setFirstName("Firstname");
		voter.setLastName("Lastname");
		voter.setNameLine("Lastname Firstname");
		return voter;
	}
}
