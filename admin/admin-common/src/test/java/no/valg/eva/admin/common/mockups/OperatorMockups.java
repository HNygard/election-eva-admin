package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.OPERATOR_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;


public final class OperatorMockups {

	public static final long OPERATOR_PK = OPERATOR_PK_SERIES + 1;

	public static Operator operator(final ElectionEvent electionEvent) {
		Operator operator = new Operator();
		operator.setPk(OPERATOR_PK);
		operator.setElectionEvent(electionEvent);
		return operator;
	}

	public static Operator defaultOperator() {
		return operator(electionEvent());
	}

	private OperatorMockups() {
		// no instances allowed
	}
}
