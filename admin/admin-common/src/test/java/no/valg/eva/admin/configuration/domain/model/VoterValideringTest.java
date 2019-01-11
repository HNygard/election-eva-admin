package no.valg.eva.admin.configuration.domain.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import no.evote.validation.ValideringVedManuellRegistrering;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/* Tester relatert til validering av velgere (se forøvrig VoterTest for andre tester relatert til Voter-klassen) */
public class VoterValideringTest {
	private static Validator validator;
	public static final String PROPERTY_ID = "id";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test(dataProvider = "velgerValideringManuellRegistreringTestData")
	public void valideringAvVelgerManuellRegistrering(Voter velger, int forventetAntallValideringsfeil, List<String> forventedeFeltMedFeil) {
		testValidering(velger, forventetAntallValideringsfeil, forventedeFeltMedFeil, ValideringVedManuellRegistrering.class);
	}

	private void testValidering(Voter velger, int forventetAntallValideringsfeil, List<String> forventedeFeltMedFeil, Class valideringVedManuellRegistreringClass) {
		Set<ConstraintViolation<Voter>> valideringsfeil = validator.validate(velger, valideringVedManuellRegistreringClass);

		assertEquals(valideringsfeil.size(), forventetAntallValideringsfeil);

		Iterator<ConstraintViolation<Voter>> valideringsfeilIterator = valideringsfeil.iterator();
		for (String forventetFeltMedFeil : forventedeFeltMedFeil) {
			assertEquals(valideringsfeilIterator.next().getPropertyPath().toString(), forventetFeltMedFeil);
		}
	}

	@DataProvider
	private Object[][] velgerValideringManuellRegistreringTestData() {
		return new Object[][] {
			// ID
			{ buildVoter().id("12312312312").build(), 	1, singletonList(PROPERTY_ID) },
			{ buildVoter().id("1231231231xz").build(), 	1, singletonList(PROPERTY_ID) },
			{ buildVoter().id(null).build(), 			1, singletonList(PROPERTY_ID) },
			{ buildVoter().build(),						0, emptyList() },
			
			// Navn
			{ buildVoter().firstName(null).build(), 		1, singletonList("firstName") },
			{ buildVoter().firstName("Navn231").build(), 	1, singletonList("firstName") },
			{ buildVoter().firstName("stringTooLongqqwqwqwqwqwqwqwqwqwqwqwqwqwqwqwqwwwqqwasasasasasasssasaasasasaasas").build(), 1, singletonList("firstName") },
			{ buildVoter().lastName(null).build(),  		1, singletonList("lastName") },
			{ buildVoter().lastName("Navn231").build(), 	1, singletonList("lastName") },
			
			// Adresse
			{ buildVoter().postalCode("123123").build(), 	1, singletonList("postalCode") },
			{ buildVoter().postalCode("ab12").build(), 		1, singletonList("postalCode") },
			{ buildVoter().addressLine1("  gata").build(), 	0, emptyList() },
			{ buildVoter().addressLine1(" --gata").build(), 	0, emptyList() },
			{ buildVoter().addressLine1("ssdsdksodksldksldøsklldskldskdlkdlskldskldsksdsdsdsdsdsdssdsdsdsdsdsdsdsdsdsdsdsdsdsdsdsddlskdlskdlsldklskdslsd").build(),
				1, singletonList("addressLine1") },
			{ buildVoter().addressLine1("ssdsdksodksldksldøsklldskldskdlkdlskldskldskdlskdlskdlsldklskdslsd").build(), 1, singletonList("addressLine1") },
			
			// Telefon
			{ buildVoter().telephoneNumber("1234567890123456789012345678901234567890").build(), 2, singletonList("telephoneNumber") },
			{ buildVoter().telephoneNumber("+1234acd").build(), 1, singletonList("telephoneNumber") },
			{ buildVoter().telephoneNumber("+4712345678").build(), 0, emptyList() },
		};
	}

	@Test(dataProvider = "velgerValideringDefaultTestData")
	public void valideringAvVelgerDefault(Voter velger, int forventetAntallValideringsfeil, List<String> forventedeFeltMedFeil) {
		testValidering(velger, forventetAntallValideringsfeil, forventedeFeltMedFeil, Default.class);
	}

	@DataProvider
	private Object[][] velgerValideringDefaultTestData() {
		return new Object[][] {
			// Name line
			{ buildVoter().nameLine(null).build(), 0, emptyList() },
			{ buildVoter().nameLine("StringOver152qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
				"ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssttttttttttttttttttttttt").build(), 1, singletonList("nameLine") },
		};
	}
	
	private Voter.VoterBuilder buildVoter() {
		return Voter.builder()
			.electionEvent(new ElectionEvent())
			.id("25038014008")
			.firstName("Firstname")
			.lastName("Lastname")
			.nameLine("Lastname Firstname");
	}
}
