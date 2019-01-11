package no.valg.eva.admin.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class PersonTest {
	
	@Test
	public void nameLine_givenFirstMiddleLastName_returnsNameFormattedForUI() {
		Person person = makePerson("First", "Middle", "Last");

		String result = person.nameLine();

		assertThat(result).isEqualTo("Last, First Middle");
	}

	private Person makePerson(final String firstName, final String middleName, final String lastName) {
		
		LocalDate date = new LocalDate(2000, 1, 1);
		
		Address address = new Address("Storgata 1", "", "", "4870", "Fevik", "Grimstad");

		return new Person(
				new PersonId("12345678901"),
				date,
				firstName,
				middleName,
				lastName,
				address);
	}

}
