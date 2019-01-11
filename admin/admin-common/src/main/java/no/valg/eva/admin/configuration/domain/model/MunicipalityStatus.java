package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;

/**
 * Status for municipality configuration
 */
@Entity
@Table(name = "municipality_status", uniqueConstraints = @UniqueConstraint(columnNames = "municipality_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "municipality_status_pk"))
public class MunicipalityStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;
	private String translatedName;
	
	@Column(name = "municipality_status_id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "municipality_status_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public MunicipalityStatusEnum toEnumValue() {
		return MunicipalityStatusEnum.fromId(id);
	}

	@Transient
	public String getTranslatedName() {
		return translatedName;
	}

	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}
}
