package no.valg.eva.admin.rbac.domain.operator;

import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;

/**
 * Creates Operator instances
 */
public final class OperatorFactory {

	private OperatorFactory() {
	}

	public static Operator create(ElectionEvent electionEvent, Person person) {
		Operator newOperator = new Operator();
		newOperator.setElectionEvent(electionEvent);
		newOperator.setId(person.getPersonId().getId());
		newOperator.setNameLine(person.getFirstName() + " " + person.getLastName());
		newOperator.setFirstName(person.getFirstName());
		newOperator.setLastName(person.getLastName());
		newOperator.setEmail(person.getEmail());
		newOperator.setTelephoneNumber(person.getTelephoneNumber());
		newOperator.setActive(true);
		newOperator.setAddressLine1(person.getAddress().getAddressLine1());
		newOperator.setAddressLine2(person.getAddress().getAddressLine2());
		newOperator.setAddressLine3(person.getAddress().getAddressLine3());
		newOperator.setPostalCode(person.getAddress().getPostalCode());
		newOperator.setPostTown(person.getAddress().getPostTown());
		if (person instanceof no.valg.eva.admin.common.rbac.Operator) {
			no.valg.eva.admin.common.rbac.Operator op = (no.valg.eva.admin.common.rbac.Operator) person;
			newOperator.setActive(op.isActive());
			newOperator.setKeySerialNumber(op.getKeySerialNumber());
		}
		return newOperator;
	}

}
