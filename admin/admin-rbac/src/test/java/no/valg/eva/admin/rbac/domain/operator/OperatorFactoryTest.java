package no.valg.eva.admin.rbac.domain.operator;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.Address;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class OperatorFactoryTest {
	
	@Test
	public void testCreateOperator() {
		ElectionEvent electionEvent = new ElectionEvent();
		Address address = new Address("Akersgata 33", "", "", "0333", "Oslo", "Oslo");
		Person person = new Person(new PersonId("11111111111"), new LocalDate(), "Ola", "M.", "Nordmann", address);
		Operator operator = OperatorFactory.create(electionEvent, person);
		assertThat(operator.getFirstName()).isEqualTo("Ola");
		assertThat(operator.getAddressLine1()).isEqualTo(address.streetAddress());
		// Er nameLine riktig, egentlig..?  Gjøres slik i DefaultImportOperatorService
		assertThat(operator.getNameLine()).isEqualTo("Ola Nordmann");
		
		
	}
}
