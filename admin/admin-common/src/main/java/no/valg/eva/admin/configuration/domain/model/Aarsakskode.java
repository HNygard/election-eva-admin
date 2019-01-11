package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

/**
 * Reason codes for updates from Skattedirektoratet / Det sentrale folkeregisteret
 */
@Entity
@Table(name = "aarsakskode", uniqueConstraints = @UniqueConstraint(columnNames = "aarsakskode_id"))
@AttributeOverride(name = "pk", column = @Column(name = "aarsakskode_pk"))
public class Aarsakskode extends VersionedEntity implements java.io.Serializable {

	private String id;
	private String name;

	@Column(name = "aarsakskode_id", nullable = false, length = 2)
	@StringNotNullEmptyOrBlanks
	@Size(max = 2)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "aarsakskode_name", length = 100)
	@StringNotNullEmptyOrBlanks
	@Size(max = 100)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
