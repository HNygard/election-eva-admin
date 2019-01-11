package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Embeddable
public class CandidateAuditId implements java.io.Serializable {

	private int candidatePk;
	private int auditOplock;
	private DateTime auditTimestamp;

	@Column(name = "candidate_pk", nullable = false)
	public int getCandidatePk() {
		return this.candidatePk;
	}

	public void setCandidatePk(final int candidatePk) {
		this.candidatePk = candidatePk;
	}

	@Column(name = "audit_oplock", nullable = false)
	public int getAuditOplock() {
		return this.auditOplock;
	}

	public void setAuditOplock(final int auditOplock) {
		this.auditOplock = auditOplock;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "audit_timestamp", nullable = false, length = 29)
	public DateTime getAuditTimestamp() {
		return auditTimestamp;
	}

	public void setAuditTimestamp(DateTime auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
	}

	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof CandidateAuditId)) {
			return false;
		}
		CandidateAuditId castOther = (CandidateAuditId) other;

		return (this.getCandidatePk() == castOther.getCandidatePk())
				&& (this.getAuditOplock() == castOther.getAuditOplock())
				&& ((this.getAuditTimestamp() == castOther.getAuditTimestamp()) || (this.getAuditTimestamp() != null && castOther.getAuditTimestamp() != null && this
						.getAuditTimestamp().equals(castOther.getAuditTimestamp())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getCandidatePk();
		result = 37 * result + this.getAuditOplock();
		result = 37 * result + (getAuditTimestamp() == null ? 0 : this.getAuditTimestamp().hashCode());
		return result;
	}

}
