package no.valg.eva.admin.backend.service.impl;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;
import no.valg.eva.admin.common.rbac.SecurityNone;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "AuditLogService")


@Default
@Remote(AuditLogService.class)
public class AuditLogServiceEjb implements AuditLogService {
	@Inject
	private AuditLogServiceBean auditLogServiceBean;

	@Override
	@SecurityNone
	public void addToAuditTrail(AbstractAuditEvent auditEvent) {
		auditLogServiceBean.addToAuditTrail(auditEvent);
	}

}
