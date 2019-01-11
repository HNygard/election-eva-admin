package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EligibilityId implements java.io.Serializable {

	private long mvElectionPk;
	private long mvAreaPk;

	@Column(name = "mv_election_pk", nullable = false)
	public long getMvElectionPk() {
		return this.mvElectionPk;
	}

	public void setMvElectionPk(final long mvElectionPk) {
		this.mvElectionPk = mvElectionPk;
	}

	@Column(name = "mv_area_pk", nullable = false)
	public long getMvAreaPk() {
		return this.mvAreaPk;
	}

	public void setMvAreaPk(final long mvAreaPk) {
		this.mvAreaPk = mvAreaPk;
	}

	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof EligibilityId)) {
			return false;
		}
		EligibilityId castOther = (EligibilityId) other;

		return (this.getMvElectionPk() == castOther.getMvElectionPk()) && (this.getMvAreaPk() == castOther.getMvAreaPk());
	}

	public int hashCode() {
		int result = 17;

		result = (int) (37 * result + this.getMvElectionPk());
		result = (int) (37 * result + this.getMvAreaPk());
		return result;
	}

}
