package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Proposer (signer) roles
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "")
@Table(name = "proposer_role", uniqueConstraints = @UniqueConstraint(columnNames = "proposer_role_id"))
@AttributeOverride(name = "pk", column = @Column(name = "proposer_role_pk"))
@NamedQueries({ @NamedQuery(name = "ProposerRole.findNotSingle", query = "select p from ProposerRole p where p.single = false") })
public class ProposerRole extends VersionedEntity implements java.io.Serializable {

	private String id;
	private boolean single;
	private String name;

	@Column(name = "proposer_role_id", nullable = false, length = 4)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "single", nullable = false)
	public boolean isSingle() {
		return this.single;
	}

	public void setSingle(final boolean single) {
		this.single = single;
	}

	@Column(name = "proposer_role_name", nullable = false, length = 50)
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
		ProposerRole other = (ProposerRole) obj;
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
