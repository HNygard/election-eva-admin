package no.valg.eva.admin.rbac.application;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.rbac.domain.model.Operator;

import org.testng.annotations.Test;

public class PersonMapperTest {

	private static final String ID = "11111111111";
	private static final String ADDRESS = "Oppsalveien";
	private static final String FIRST_NAME = "Ola";
	private static final String LAST_NAME = "Ola";
	private static final String EMAIL = "epost";

	@Test
	public void testMapVoterToPerson() {
		Voter voter = new Voter();
		voter.setId(ID);
		voter.setFirstName(FIRST_NAME);
		voter.setLastName(LAST_NAME);
		voter.setAddressLine1(ADDRESS);
		voter.setEmail(EMAIL);
		Person person = PersonMapper.toPerson(voter);
		assertThat(person.nameLine()).isEqualTo(LAST_NAME + ", " + FIRST_NAME);
		assertThat(person.getAddress().streetAddress()).isEqualTo(ADDRESS);
		assertThat(person.getEmail()).isEqualTo(EMAIL);
	}

	@Test
	public void testMapOperatorToPerson() {
		Operator operator = new Operator();
		operator.setId(ID);
		operator.setFirstName(FIRST_NAME);
		operator.setLastName(LAST_NAME);
		operator.setAddressLine1(ADDRESS);
		operator.setEmail(EMAIL);
		Person person = PersonMapper.toOperator(operator);
		assertThat(person.nameLine()).isEqualTo(LAST_NAME + ", " + FIRST_NAME);
		assertThat(person.getAddress().streetAddress()).isEqualTo(ADDRESS);
		assertThat(person.getEmail()).isEqualTo(EMAIL);
	}

}
