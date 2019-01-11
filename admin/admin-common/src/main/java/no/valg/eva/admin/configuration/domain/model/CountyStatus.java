package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;

/**
 * Status for county configuration
 */
@Entity
@Table(name = "county_status", uniqueConstraints = @UniqueConstraint(columnNames = "county_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "county_status_pk"))
public class CountyStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;

	@Column(name = "county_status_id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "county_status_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public CountyStatusEnum toEnumValue() {
		return CountyStatusEnum.fromId(id);
	}

}
