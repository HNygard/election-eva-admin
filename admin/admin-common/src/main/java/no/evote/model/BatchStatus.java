package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The last assigned batch number in the election event
 */
@Entity
@Table(name = "batch_status", uniqueConstraints = @UniqueConstraint(columnNames = "batch_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "batch_status_pk"))
public class BatchStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;

	@Column(name = "batch_status_id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "batch_status_name", nullable = false, length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
