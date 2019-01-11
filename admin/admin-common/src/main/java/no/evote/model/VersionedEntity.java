package no.evote.model;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.VersionedObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

import static java.lang.String.format;
import static no.evote.exception.ErrorCode.ERROR_CODE_0504_STALE_OBJECT;

/**
 * Base entity for all versioned entities (i.e. has audit* fields)
 */
@MappedSuperclass
public abstract class VersionedEntity extends BaseEntity implements Serializable {
	private int auditOplock;
	private String auditOperation;
	private DateTime auditTimestamp;
	private String auditOperator;

	@Version
	@Column(name = "audit_oplock", nullable = false)
	public int getAuditOplock() {
		return this.auditOplock;
	}

	public void setAuditOplock(final int auditOplock) {
		this.auditOplock = auditOplock;
	}
	
	public void setDirty() {
		setAuditOplock(getAuditOplock() + 1);
	}

	@Column(name = "audit_operation", length = 1)
	public String getAuditOperation() {
		return this.auditOperation;
	}

	public void setAuditOperation(final String auditOperation) {
		this.auditOperation = auditOperation;
	}

	@Column(name = "audit_timestamp", length = 29)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	public DateTime getAuditTimestamp() {
		return auditTimestamp;
	}

	public void setAuditTimestamp(DateTime auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
	}

	@Column(name = "audit_operator")
	public String getAuditOperator() {
		return this.auditOperator;
	}

	public void setAuditOperator(final String auditOperator) {
		this.auditOperator = auditOperator;
	}

	public void checkVersion(VersionedObject versionedObject) {
		int version = versionedObject.getVersion();
		if (version != getAuditOplock()) {
			throw new EvoteException(
				ERROR_CODE_0504_STALE_OBJECT,
				new IllegalStateException(format("Outdated version %s. Expected version <%d>, but got <%d> for object with pk<%s>", getClass().getName(),
					getAuditOplock(), version, getPk())));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof VersionedEntity)) {
			return false;
		}
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.toHashCode();
	}
}
