package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.validation.StringNotNullEmptyOrBlanks;

/**
 * Codes from Skattedirektoratet / Det sentrale folkeregisteret
 */
@Entity
@Table(name = "spes_reg_type", uniqueConstraints = @UniqueConstraint(columnNames = "spes_reg_type_id"))
@AttributeOverride(name = "pk", column = @Column(name = "spes_reg_type_pk"))
public class SpesRegType extends VersionedEntity implements java.io.Serializable {

	private char id;
	private String name;

	@Column(name = "spes_reg_type_id", nullable = false, length = 1)
	public char getId() {
		return this.id;
	}

	public void setId(final char id) {
		this.id = id;
	}

	@Column(name = "spes_reg_type_name", length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
