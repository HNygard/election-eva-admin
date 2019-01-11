package no.valg.eva.admin.frontend.rbac.ctrls;

import java.io.Serializable;

import no.valg.eva.admin.common.rbac.Operator;

/**
 * Wrapper object for Operator to get equals/hashCode for use in OperatorListController.
 * 
 * @see OperatorListController
 */
public class OperatorWrapper implements Serializable {

	private Operator value;

	public OperatorWrapper(Operator value) {
		this.value = value;
	}

	public Operator getValue() {
		return value;
	}

	public void setValue(Operator value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OperatorWrapper that = (OperatorWrapper) o;

		if (!value.getPersonId().getId().equals(that.value.getPersonId().getId())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return value.getPersonId().getId().hashCode();
	}
}
