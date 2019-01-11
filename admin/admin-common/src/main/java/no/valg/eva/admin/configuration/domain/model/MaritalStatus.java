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
 * Marital status codes from http://ssb.no/stabas/
 */
@Entity
@Table(name = "marital_status", uniqueConstraints = @UniqueConstraint(columnNames = "marital_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "marital_status_pk"))
public class MaritalStatus extends VersionedEntity implements java.io.Serializable {
	public static final String UOPPGITT = "0";

	private String id;
	private String name;

	@Column(name = "marital_status_id", nullable = false, length = 2)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "marital_status_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPk() == null) ? 0 : getPk().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MaritalStatus other = (MaritalStatus) obj;
		if (getPk() == null) {
			if (other.getPk() != null) {
				return false;
			}
		} else if (!getPk().equals(other.getPk())) {
			return false;
		}
		return true;
	}

}
