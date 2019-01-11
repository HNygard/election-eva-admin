package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Configuration parameters
 */
@Entity
@Table(name = "configuration", uniqueConstraints = { @UniqueConstraint(columnNames = "version_id") })
@AttributeOverride(name = "pk", column = @Column(name = "configuration_pk"))
public class Configuration extends VersionedEntity implements java.io.Serializable {

	private String versionId;

	@Column(name = "version_id", unique = true, nullable = false, length = 10)
	public String getVersionId() {
		return this.versionId;
	}

	public void setVersionId(final String versionId) {
		this.versionId = versionId;
	}

}
