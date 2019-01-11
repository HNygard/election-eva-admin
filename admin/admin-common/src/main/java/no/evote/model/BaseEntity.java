package no.evote.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Base entity for all entities (i.e. has pk field)
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	protected Long pk;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(unique = true, insertable = false, updatable = false, nullable = false)
	public Long getPk() {
		return this.pk;
	}

	public final void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BaseEntity)) {
			return false;
		}
		BaseEntity rhs = (BaseEntity) obj;
		return new EqualsBuilder()
				.append(this.pk, rhs.getPk())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(pk)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pk", pk)
				.toString();
	}
}
