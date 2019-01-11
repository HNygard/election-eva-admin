package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Embeddable
public class VoterAuditId implements java.io.Serializable {

	private long voterPk;
	private int auditOplock;
	private DateTime auditTimestamp;

	@Column(name = "voter_pk", nullable = false)
	public long getVoterPk() {
		return this.voterPk;
	}

	public void setVoterPk(final long voterPk) {
		this.voterPk = voterPk;
	}

	@Column(name = "audit_oplock", nullable = false)
	public int getAuditOplock() {
		return this.auditOplock;
	}

	public void setAuditOplock(final int auditOplock) {
		this.auditOplock = auditOplock;
	}

	@Column(name = "audit_timestamp", nullable = false, length = 29)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	public DateTime getAuditTimestamp() {
		return auditTimestamp;
	}

	public void setAuditTimestamp(DateTime auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
	}

	@Override
	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof VoterAuditId)) {
			return false;
		}
		VoterAuditId castOther = (VoterAuditId) other;

		return (this.getVoterPk() == castOther.getVoterPk())
				&& (this.getAuditOplock() == castOther.getAuditOplock())
				&& ((this.getAuditTimestamp() == castOther.getAuditTimestamp()) || (this.getAuditTimestamp() != null && castOther.getAuditTimestamp() != null && this
						.getAuditTimestamp().equals(castOther.getAuditTimestamp())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (int) this.getVoterPk();
		result = 37 * result + this.getAuditOplock();
		result = 37 * result + (getAuditTimestamp() == null ? 0 : this.getAuditTimestamp().hashCode());
		return result;
	}

}
