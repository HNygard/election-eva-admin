package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models;

import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class for handling voting number interval when finding votes to approve.
 */
public class VotingNumberInterval {

	private boolean error;
	private int votingNumberStart;
	private int votingNumberEnd;

	public VotingNumberInterval(final String startVotingNumber, final String endVotingNumber) {
		votingNumberStart = 0;
		votingNumberEnd = 0;
		if (!StringUtils.isEmpty(startVotingNumber) && Integer.parseInt(startVotingNumber) > 0) {
			votingNumberStart = Integer.parseInt(startVotingNumber);
		}
		if (!StringUtils.isEmpty(endVotingNumber) && Integer.parseInt(endVotingNumber) > 0) {
			votingNumberEnd = Integer.parseInt(endVotingNumber);
		}
		if ((votingNumberStart > 0 && votingNumberEnd == 0) || (votingNumberStart == 0 && votingNumberEnd > 0)) {
			error = true;
		}
	}

	public boolean hasError() {
		return error;
	}

	public int start() {
		return votingNumberStart;
	}

	public int end() {
		return votingNumberEnd;
	}
}
