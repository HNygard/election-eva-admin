package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ContestVotingId implements java.io.Serializable {

	private Long contestPk;
	private Long votingPk;

	@Column(name = "contest_pk", nullable = false)
	public Long getContestPk() {
		return this.contestPk;
	}

	public void setContestPk(final Long contestPk) {
		this.contestPk = contestPk;
	}

	@Column(name = "voting_pk", nullable = false)
	public Long getVotingPk() {
		return this.votingPk;
	}

	public void setVotingPk(final Long votingPk) {
		this.votingPk = votingPk;
	}

	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof ContestVotingId)) {
			return false;
		}
		ContestVotingId castOther = (ContestVotingId) other;

		return (this.getContestPk().equals(castOther.getContestPk())) && (this.getVotingPk().equals(castOther.getVotingPk()));
	}

	public int hashCode() {
		Long result = 17L;

		result = 37 * result + this.getContestPk();
		result = 37 * result + this.getVotingPk();
		return result.intValue();
	}

}
