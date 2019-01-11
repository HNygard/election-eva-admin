package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.util.EqualsHashCodeUtil;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Broad categorisation of election (Proportional representation or Referendum)
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.ElectionType")
@Table(name = "election_type", uniqueConstraints = @UniqueConstraint(columnNames = "election_type_id"))
@AttributeOverride(name = "pk", column = @Column(name = "election_type_pk"))
public class ElectionType extends VersionedEntity implements java.io.Serializable {
	public static final String TYPE_REFERENDUM = "R";
	public static final String TYPE_PROPORTIONAL_REPRESENTATION = "F";

	private String id;
	private String name;

	public ElectionType() {
		super();
	}

	@Column(name = "election_type_id", nullable = false, length = 8)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "election_type_name", nullable = false, length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsHashCodeUtil.genericEquals(this, obj);
	}

	@Override
	public String toString() {
		return id + " " + name;
	}

	@Transient
	public boolean isReferendum() {
		return TYPE_REFERENDUM.equals(id);
	}

	/**
	 * Sjekker om valget er av typen forholdstallsvalg.
	 * @return true if election_type_id equals 'F'
	 */
	@Transient
	public boolean isPropotionalRepresentation() {
		return TYPE_PROPORTIONAL_REPRESENTATION.equals(id);
	}
}
