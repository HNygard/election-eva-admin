package no.evote.dto;

import java.io.Serializable;
import java.math.BigInteger;

public class VotingDto implements Serializable {

	private String votingCategoryId;
	private boolean approved;
	private BigInteger numberOfVotings;

	public String getVotingCategoryId() {
		return votingCategoryId;
	}
	public void setVotingCategoryId(final String votingCategoryId) {
		this.votingCategoryId = votingCategoryId;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(final boolean approved) {
		this.approved = approved;
	}
	public BigInteger getNumberOfVotings() {
		return numberOfVotings;
	}
	public void setNumberOfVotings(final BigInteger numberOfVotings) {
		this.numberOfVotings = numberOfVotings;
	}
}



