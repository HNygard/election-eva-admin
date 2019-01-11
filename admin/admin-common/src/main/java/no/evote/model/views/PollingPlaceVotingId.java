package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PollingPlaceVotingId implements java.io.Serializable {

	private Long pollingPlacePk;
	private boolean earlyVoting;

	@Column(name = "polling_place_pk", nullable = false)
	public Long getPollingPlacePk() {
		return this.pollingPlacePk;
	}

	public void setPollingPlacePk(final Long pollingPlacePk) {
		this.pollingPlacePk = pollingPlacePk;
	}

	@Column(name = "early_voting", nullable = false)
	public boolean isEarlyVoting() {
		return this.earlyVoting;
	}

	public void setEarlyVoting(final boolean earlyVoting) {
		this.earlyVoting = earlyVoting;
	}

	@Override
	public boolean equals(final Object other) {
		if ((this == other)) {
			return true;
		}
		if ((other == null)) {
			return false;
		}
		if (!(other instanceof PollingPlaceVotingId)) {
			return false;
		}
		PollingPlaceVotingId castOther = (PollingPlaceVotingId) other;

		return (this.getPollingPlacePk().equals(castOther.getPollingPlacePk())) && (this.isEarlyVoting() == castOther.isEarlyVoting());
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getPollingPlacePk().intValue();
		result = 37 * result + (this.isEarlyVoting() ? 1 : 0);
		return result;
	}

}
