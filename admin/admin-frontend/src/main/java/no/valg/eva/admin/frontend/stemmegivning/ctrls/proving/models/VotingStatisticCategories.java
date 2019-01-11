package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models;

import no.valg.eva.admin.common.voting.VotingCategory;

/**
 * Handles categories to include in query for finding statistics related to approval of votes (prøving).
 */
public class VotingStatisticCategories {

	public static final String ALL_VOTING_CATEGORIES = "ALL";
	public static final String LATE_ADVANCE_VOTES = "LATE_ADVANCE_VOTES";

	private final String selectedVotingCategoryId;
	private final boolean includeLateAdvanceVotes;

	/**
	 * @param selectedVotingCategoryId category selected by user
	 */
	public VotingStatisticCategories(final String selectedVotingCategoryId) {
		this.selectedVotingCategoryId = selectedVotingCategoryId;
		includeLateAdvanceVotes = selectedVotingCategoryId.equals(ALL_VOTING_CATEGORIES) || selectedVotingCategoryId.equals(LATE_ADVANCE_VOTES);
	}

	public String[] electionDayVotingCategoriesForStatistics() {
		return selectedVotingCategoryId.equals(ALL_VOTING_CATEGORIES) ? new String[] { VotingCategory.VS.getId(), VotingCategory.VB.getId() }
				: selectedVotingCategoryId
						.equals(LATE_ADVANCE_VOTES) ? new String[] {} : new String[] { selectedVotingCategoryId };
	}

	public boolean includeLateAdvanceVotes() {
		return includeLateAdvanceVotes;
	}
}
