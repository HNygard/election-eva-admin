package no.valg.eva.admin.counting.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.common.counting.model.CountStatus;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Status for vote counts
 */
@Entity
@Table(name = "vote_count_status", uniqueConstraints = @UniqueConstraint(columnNames = "vote_count_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "vote_count_status_pk"))
public class VoteCountStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;

	public VoteCountStatus() {
		// For hibernate/JPA
	}

	public VoteCountStatus(int id) {
		this.id = id;
	}

	@Column(name = "vote_count_status_id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "vote_count_status_name", nullable = false, length = 50)
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
		VoteCountStatus rhs = (VoteCountStatus) obj;
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

	@Transient
	public CountStatus getCountStatus() {
		return CountStatus.fromId(getId());
	}
}
