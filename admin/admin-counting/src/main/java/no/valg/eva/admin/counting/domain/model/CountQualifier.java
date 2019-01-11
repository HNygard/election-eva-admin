package no.valg.eva.admin.counting.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Qualifiers for separation of counts, e.g. preliminary and final count
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.counting.domain.model.CountQualifier")
@Table(name = "count_qualifier", uniqueConstraints = @UniqueConstraint(columnNames = "count_qualifier_id"))
@AttributeOverride(name = "pk", column = @Column(name = "count_qualifier_pk"))
public class CountQualifier extends VersionedEntity implements java.io.Serializable {

	private String id;
	private String name;

	@Column(name = "count_qualifier_id", nullable = false, length = 10)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "count_qualifier_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		CountQualifier rhs = (CountQualifier) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.id, rhs.id)
				.append(this.name, rhs.name)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(id)
				.append(name)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.appendSuper(super.toString())
				.toString();
	}
}
