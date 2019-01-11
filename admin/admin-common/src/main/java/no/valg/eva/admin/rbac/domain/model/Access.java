package no.valg.eva.admin.rbac.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.util.Treeable;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * RBAC: Hierarchy of access to securable objects
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.rbac.domain.model.Access")
@Table(name = "access", uniqueConstraints = @UniqueConstraint(columnNames = "access_path"))
@AttributeOverride(name = "pk", column = @Column(name = "access_pk"))
@NamedQueries({
		@NamedQuery(
				name = "Access.findAccessById",
				query = "SELECT a FROM Access a WHERE a.path = :accessId",
				hints = { @QueryHint(name = "org.hibernate.cacheable", value = "true") })})
public class Access extends VersionedEntity implements java.io.Serializable, Treeable {

	private String path;
	private String name;

	public Access() {
	}

	@Override
	@Column(name = "access_path", nullable = false, length = 100)
	@StringNotNullEmptyOrBlanks
	@Size(max = 100)
	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	@Column(name = "access_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Access) {
			return ((Access) obj).getPath().equals(this.getPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public String toString() {
		return path;
	}

	/**
	 * @return this as view object
	 */
	public no.valg.eva.admin.common.rbac.Access toViewObject() {
		return new no.valg.eva.admin.common.rbac.Access(path);
	}
}
