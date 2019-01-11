package no.valg.eva.admin.common.rbac;

import java.io.Serializable;

/**
 * Request object for performing circular reference checks
 */
public class CircularReferenceCheckRequest implements Serializable {

	private final Role role;
	private final String newIncludedRoleId;

	public CircularReferenceCheckRequest(final Role currentRole, final String newIncludedRoleId) {
		this.role = currentRole;
		this.newIncludedRoleId = newIncludedRoleId;
	}

	public Role getRole() {
		return role;
	}

	public String getNewIncludedRoleId() {
		return newIncludedRoleId;
	}
}
