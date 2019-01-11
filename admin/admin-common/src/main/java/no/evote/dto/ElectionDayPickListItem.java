package no.evote.dto;

import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.voting.VotingCategory;

/**
 * Item for pick list on the "samlet prøving valgting" page
 */
public class ElectionDayPickListItem extends PickListItem {

	public static final String ADVANCE_VOTE_TEXT_ID = "@voting.approveVoting.advanceVote";
	private static Set<String> advanceCats = new HashSet<>();
	{
		advanceCats.add(VotingCategory.FI.getId());
		advanceCats.add(VotingCategory.FU.getId());
		advanceCats.add(VotingCategory.FB.getId());
		advanceCats.add(VotingCategory.FE.getId());
	}
	
	private final String updatedVotingCategoryName;

	/**
	 * Creates instance
	 *
	 * @param object result of native queries in VotingServiceImpl
	 */
	public ElectionDayPickListItem(final Object object) {
		super(object);
		updatedVotingCategoryName = advanceCats.contains(votingCategoryId) ? ADVANCE_VOTE_TEXT_ID : votingCategoryName;
	}

	@Override
	public String getVotingCategoryName() {
		return updatedVotingCategoryName;
	}
}
