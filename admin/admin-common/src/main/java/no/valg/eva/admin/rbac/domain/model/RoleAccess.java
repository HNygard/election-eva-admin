package no.valg.eva.admin.rbac.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.evote.model.VersionedEntity;

/**
 * RBAC: Access to securable objects per role
 */
@Entity
@Table(name = "role_access")
@AttributeOverride(name = "pk", column = @Column(name = "role_access_pk"))
public class RoleAccess extends VersionedEntity implements java.io.Serializable {

	private Access access;
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "access_pk", nullable = false)
	public Access getAccess() {
		return this.access;
	}

	public void setAccess(final Access access) {
		this.access = access;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_pk", nullable = false)
	public Role getRole() {
		return this.role;
	}

	public void setRole(final Role role) {
		this.role = role;
	}

}
