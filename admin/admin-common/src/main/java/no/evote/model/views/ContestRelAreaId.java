package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ContestRelAreaId implements java.io.Serializable {

	private Long mvElectionPk;
	private Long mvAreaPk;

	@Column(name = "mv_election_pk", nullable = false)
	public Long getMvElectionPk() {
		return this.mvElectionPk;
	}

	public void setMvElectionPk(final Long mvElectionPk) {
		this.mvElectionPk = mvElectionPk;
	}

	@Column(name = "mv_area_pk", nullable = false)
	public Long getMvAreaPk() {
		return this.mvAreaPk;
	}

	public void setMvAreaPk(final Long mvAreaPk) {
		this.mvAreaPk = mvAreaPk;
	}

	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof ContestRelAreaId)) {
			return false;
		}
		ContestRelAreaId castOther = (ContestRelAreaId) other;

		return (this.getMvElectionPk().equals(castOther.getMvElectionPk())) && (this.getMvAreaPk().equals(castOther.getMvAreaPk()));
	}

	public int hashCode() {
		Long result = 17L;

		result = 37 * result + this.getMvElectionPk();
		result = 37 * result + this.getMvAreaPk();
		return result.intValue();
	}

}
