package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.OPERATOR_ROLE_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

public final class OperatorRoleMockups {

	private static final long OPERATOR_ROLE_PK = OPERATOR_ROLE_PK_SERIES + 1;

	public static OperatorRole operatorRole(final Role role, final Operator operator, final MvElection mvElection, final MvArea mvArea) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setPk(OPERATOR_ROLE_PK);
		operatorRole.setRole(role);
        operatorRole.setOperator(operator);
		operatorRole.setMvElection(mvElection);
		operatorRole.setMvArea(mvArea);
		return operatorRole;
	}

	private OperatorRoleMockups() {
		// no instances allowed
	}
}
