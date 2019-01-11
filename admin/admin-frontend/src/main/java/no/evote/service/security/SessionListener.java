package no.evote.service.security;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class SessionListener implements HttpSessionListener {

	private static final Logger LOGGER = Logger.getLogger(SessionListener.class);

	@Inject
	private Instance<UserData> userDataInstance;

	@Inject
	private AuditLogService auditLogService;

	@Override
	public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		session.setAttribute("time", DateTime.now());
		UserData userData = getUserDataIfPossible();
		StringBuilder sb = new StringBuilder();
		sb.append("Session created (").append(session.getId()).append(")");
		if (userData != null) {
			sb.append(" for bruker uid ").append(userData.getUid()).append(", og rolle ").append(userData.getOperatorRole());
		}
		LOGGER.debug(sb.toString());
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		UserData userData = getUserDataIfPossible();
		StringBuilder sb = new StringBuilder();
		sb.append("Session destroyed");
		if (session != null) {
			sb.append(" (").append(session.getId()).append(")");
		}
		if (userData != null) {
			sb.append(" for bruker uid ").append(userData.getUid()).append(", og rolle ").append(userData.getOperatorRole());
		}
		LOGGER.debug(sb.toString());
		if (!sessionWasDestroyedBecauseOperatorIsSwitchingRoles(httpSessionEvent)
				&& userData != null) {
			auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorLoggedOut).build());
		}
	}

	private UserData getUserDataIfPossible() {
		try {
			return userDataInstance.get();
		} catch (RuntimeException e) {
			LOGGER.trace("Unable to get UserData object while destroying context");
		}
		return null;
	}

	private boolean sessionWasDestroyedBecauseOperatorIsSwitchingRoles(HttpSessionEvent sessionEvent) {
		return sessionEvent.getSession().getAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY) != null;
	}

}
