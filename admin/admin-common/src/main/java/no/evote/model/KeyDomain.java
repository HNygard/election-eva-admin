package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Signing domain for key in key ring
 */
@Entity
@Table(name = "key_domain", uniqueConstraints = @UniqueConstraint(columnNames = "key_domain_id"))
@AttributeOverride(name = "pk", column = @Column(name = "key_domain_pk"))
public class KeyDomain extends VersionedEntity implements java.io.Serializable {

	private String id;
	private String name;
	private boolean systemWideKey;

	@Column(name = "key_domain_id", unique = true, nullable = false, length = 20)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "key_domain_name", nullable = false, length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "system_wide_key", nullable = false)
	public boolean isSystemWideKey() {
		return this.systemWideKey;
	}

	public void setSystemWideKey(final boolean systemWideKey) {
		this.systemWideKey = systemWideKey;
	}

	@Transient
	public Boolean getPublicKey() {
		return getId().equals("SCANNING_COUNT");
	}
}
