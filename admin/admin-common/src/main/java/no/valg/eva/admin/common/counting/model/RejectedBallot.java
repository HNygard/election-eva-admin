package no.valg.eva.admin.common.counting.model;

import static no.valg.eva.admin.common.counting.model.RejectedBallot.State.REJECTED;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class RejectedBallot implements Serializable {
	private final String id;
	private final String ballotRejectionId;
	private State state;
	private String selectedBallotRejectionId;
	private String selectedBallotId;

	public RejectedBallot(RejectedBallot rejectedBallot) {
		this.id = rejectedBallot.getId();
		this.ballotRejectionId = rejectedBallot.getBallotRejectionId();
		this.state = rejectedBallot.getState();
		this.selectedBallotRejectionId = rejectedBallot.getSelectedBallotRejectionId();
		this.selectedBallotId = rejectedBallot.getSelectedBallotId();
	}

	public RejectedBallot(String id, String ballotRejectionId) {
		this.id = id;
		this.ballotRejectionId = ballotRejectionId;
		this.state = REJECTED;
	}

	public String getId() {
		return id;
	}

	public String getBallotRejectionId() {
		return ballotRejectionId;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isRejected() {
		return state == REJECTED;
	}

	public String getSelectedBallotRejectionId() {
		return selectedBallotRejectionId == null ? ballotRejectionId : selectedBallotRejectionId;
	}

	public void setSelectedBallotRejectionId(String selectedBallotRejectionId) {
		this.selectedBallotRejectionId = selectedBallotRejectionId;
	}

	public String getSelectedBallotId() {
		return selectedBallotId;
	}

	public void setSelectedBallotId(String selectedBallotId) {
		this.selectedBallotId = selectedBallotId;
	}

	public boolean isBallotRejectionChanged() {
		return selectedBallotRejectionId != null && !selectedBallotRejectionId.equals(ballotRejectionId);
	}

	public boolean isConfirmed() {
		return selectedBallotRejectionId != null || selectedBallotId != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RejectedBallot that = (RejectedBallot) o;
		return new EqualsBuilder()
				.append(id, that.id)
				.append(ballotRejectionId, that.ballotRejectionId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.append(ballotRejectionId)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("ballotRejectionId", ballotRejectionId)
				.append("state", state)
				.append("selectedBallotRejectionId", selectedBallotRejectionId)
				.append("selectedBallotId", selectedBallotId)
				.toString();
	}

	public enum State {
		REJECTED, MODIFIED, UNMODIFIED
	}
}
