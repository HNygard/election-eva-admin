package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static no.valg.eva.admin.common.rbac.OperatorMapper.toViewOperatorWithRoleAssociations;

import java.util.Collections;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

public class DomainOperatorAuditEvent extends UpdateOperatorAuditEvent {

	public DomainOperatorAuditEvent(UserData userData, Operator operator, List<OperatorRole> roles, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData,
				toViewOperatorWithRoleAssociations(operator, roles),
				null, toViewOperatorWithRoleAssociations(operator, roles).getRoleAssociations(),
				Collections.EMPTY_LIST, crudType, outcome, detail);
	}

	@Override
	public Class objectType() {
		return Operator.class;
	}

	public static Class[] objectClasses(AuditEventType type) {
		return new Class[] { Operator.class, List.class };
	}
}
