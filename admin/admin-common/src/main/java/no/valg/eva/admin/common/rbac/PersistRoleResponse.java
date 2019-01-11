package no.valg.eva.admin.common.rbac;

import java.io.Serializable;
import java.util.List;

import no.valg.eva.admin.rbac.domain.model.Role;

/**
 * Wraps the result of persist role.
 */
public class PersistRoleResponse implements Serializable {

	private final List<String> validationFeedback;
	private final Role role;

	public PersistRoleResponse(final List<String> validationFeedback, final Role role) {
		this.validationFeedback = validationFeedback;
		this.role = role;
	}

	public List<String> getValidationFeedback() {
		return validationFeedback;
	}

	public Role getRole() {
		return role;
	}
}
