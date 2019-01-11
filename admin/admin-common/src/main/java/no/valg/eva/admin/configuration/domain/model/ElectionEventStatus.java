package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;

/**
 * Status for election event
 */
@Entity
@Table(name = "election_event_status", uniqueConstraints = @UniqueConstraint(columnNames = "election_event_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "election_event_status_pk"))
public class ElectionEventStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;
	
	@Column(name = "election_event_status_id", nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "election_event_status_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ElectionEventStatusEnum toEnumValue() {
		return ElectionEventStatusEnum.fromId(id);
	}
}
