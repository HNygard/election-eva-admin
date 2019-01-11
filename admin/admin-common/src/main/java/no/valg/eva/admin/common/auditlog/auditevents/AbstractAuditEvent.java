package no.valg.eva.admin.common.auditlog.auditevents;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

public abstract class AbstractAuditEvent implements Serializable {
	private final String uid;
	private final String electionEventId;
	private final String roleId;
	private final AreaPath roleAreaPath;
	private final ElectionPath roleElectionPath;
	private final InetAddress clientIpAddress;

	public AbstractAuditEvent(String uid, String electionEventId, String roleId, AreaPath roleAreaPath, ElectionPath roleElectionPath,
			InetAddress clientIpAddress) {
		this.uid = uid;
		this.electionEventId = electionEventId;
		this.roleId = roleId;
		this.roleAreaPath = roleAreaPath;
		this.roleElectionPath = roleElectionPath;
		this.clientIpAddress = clientIpAddress;
	}

	public AbstractAuditEvent(UserData userData) {
		this(userData.getUid(), userData.getElectionEventId(), userData.getRoleId(),
				userData.getOperatorAreaPath(), userData.getOperatorElectionPath(),
				userData.getClientAddress());
	}

	public String uid() {
		return uid;
	}

	public String electionEventId() {
		return electionEventId;
	}

	public String roleId() {
		return roleId;
	}

	public AreaPath roleAreaPath() {
		return roleAreaPath;
	}

	public ElectionPath roleElectionPath() {
		return roleElectionPath;
	}

	public InetAddress clientIpAddress() {
		return clientIpAddress;
	}

	public abstract List<AuditEvent> getAndClearAllEvents();

	/**
	 * Some audit events may choose not to be logged, e.g. if it is part of a multi-request poll scenario where only last event should actually be logged
	 * @return whether this event should be actually logged
	 */
	public boolean muteEvent() {
		return false;
	}
}
