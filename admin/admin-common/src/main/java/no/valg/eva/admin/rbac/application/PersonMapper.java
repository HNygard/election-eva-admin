package no.valg.eva.admin.rbac.application;

import no.valg.eva.admin.common.Address;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.rbac.domain.model.Operator;

import org.joda.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * Maps Voter to Person
 */
@Default
@ApplicationScoped
public final class PersonMapper {

	private PersonMapper() {
	}

	static Person toPerson(Voter voter) {
		return new Person(
				new PersonId(voter.getId()),
				new LocalDate(voter.getDateOfBirth()),
				voter.getFirstName(),
				voter.getMiddleName(),
				voter.getLastName(),
				new Address(
						voter.getAddressLine1(),
						voter.getAddressLine2(),
						voter.getAddressLine3(),
						voter.getPostalCode(),
						voter.getPostTown(),
						voter.getPostTown())).withTelephoneNumber(voter.getTelephoneNumber()).withEmail(voter.getEmail());
	}

	public static no.valg.eva.admin.common.rbac.Operator toOperator(Operator operator) {
		Person p = toPerson(operator);
		no.valg.eva.admin.common.rbac.Operator result = new no.valg.eva.admin.common.rbac.Operator(p);
		result.setActive(operator.isActive());
		result.setKeySerialNumber(operator.getKeySerialNumber());
		return result;
	}

	private static Person toPerson(Operator operator) {
		return new Person(
				new PersonId(operator.getId()),
				null,
				operator.getFirstName(),
				operator.getMiddleName(),
				operator.getLastName(),
				new Address(
						operator.getAddressLine1(),
						operator.getAddressLine2(),
						operator.getAddressLine3(),
						operator.getPostalCode(),
						operator.getPostTown(),
						operator.getPostTown())).withTelephoneNumber(operator.getTelephoneNumber()).withEmail(operator.getEmail());
	}

}
