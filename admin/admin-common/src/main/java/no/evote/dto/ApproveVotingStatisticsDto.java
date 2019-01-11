package no.evote.dto;

import static no.evote.constants.EvoteConstants.DEAD_VOTER;
import static no.evote.constants.EvoteConstants.MULTIPLE_VOTES;
import static no.evote.constants.EvoteConstants.NOT_IN_ELECTORAL_ROLL;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApproveVotingStatisticsDto implements Serializable {

	private int totalVotes;
	private int totalApproved;
	private int totalNotInElectoralRoll;
	private int totalDead;
	private int totalMultipleVotes;

	private final Map<String, Integer> votingCategoryCounts = new HashMap<>();

	/**
	 * Creates instance populated from parameter lists
	 * @param advanceVoting list of votingDto
	 */
	public ApproveVotingStatisticsDto(final List<VotingDto> advanceVoting, final List<PickListItem> pickList) {
		for (VotingDto av : advanceVoting) {

			String votingCategoryId = av.getVotingCategoryId();
			if (!votingCategoryCounts.containsKey(votingCategoryId)) {
				votingCategoryCounts.put(votingCategoryId, av.getNumberOfVotings().intValue());
			} else {
				votingCategoryCounts.put(votingCategoryId, votingCategoryCounts.get(votingCategoryId) + av.getNumberOfVotings().intValue());
			}

			if (av.isApproved()) {
				totalApproved = totalApproved + av.getNumberOfVotings().intValue();
			}

			totalVotes += av.getNumberOfVotings().intValue();

		}

		for (PickListItem pl : pickList) {
			if (pl.getStatus().equals(NOT_IN_ELECTORAL_ROLL)) {
				totalNotInElectoralRoll = totalNotInElectoralRoll + 1;
			}
			if (pl.getStatus().equals(DEAD_VOTER)) {
				totalDead = totalDead + 1;
			}
			if (pl.getStatus().equals(MULTIPLE_VOTES)) {
				totalMultipleVotes = totalMultipleVotes + 1;
			}
		}
	}

	public boolean isTotalEqualToApproved() {
		return totalVotes == totalApproved;
	}

	public int getTotalVotings() {
		return totalVotes;
	}

	public int getTotalApproved() {
		return totalApproved;
	}

	public int getTotalInCategory(final String category) {
		if (votingCategoryCounts.get(category) == null) {
			return 0;
		} else {
			return votingCategoryCounts.get(category);
		}

	}

	public int getTotalNotInElectoralRoll() {
		return totalNotInElectoralRoll;
	}

	public int getTotalDead() {
		return totalDead;
	}

	public int getTotalMultipleVotes() {
		return totalMultipleVotes;
	}

	public int getTotalAdvanceVotes() {
		return getTotalInCategory(FI.getId()) + getTotalInCategory(FU.getId()) + getTotalInCategory(FB.getId()) + getTotalInCategory(FE.getId());
	}
}
