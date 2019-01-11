package no.evote.dto;

import static no.evote.dto.ElectionDayPickListItem.ADVANCE_VOTE_TEXT_ID;
import static org.testng.AssertJUnit.assertEquals;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 * Test cases for ElectionDayPickListItem
 */
public class ElectionDayPickListItemTest {

	@Test
	public void advanceVotesShouldHaveCommonAdvanceVoteUpdatedVotingCategoryName() {
		Object object = new Object[] { "", 0, "", "", null, "FI", "@voting_category[FI].name", false };
		assertEquals(ADVANCE_VOTE_TEXT_ID, new ElectionDayPickListItem(object).getVotingCategoryName());
	}

	@Test
	public void electionDayVotesShouldHaveOriginalVotingCategoryName() {
		Object object = new Object[] { "", 0, "", "", null, "VO", "@voting_category[VO].name", false };
		assertEquals("@voting_category[VO].name", new ElectionDayPickListItem(object).getVotingCategoryName());
	}

	@Test
	public void rowKeyShouldBeBasedOnHashOfVoterIdAndReceivedTimestamp() {
		DateTime receicedDate = DateTime.now();
		String voterId = "1234567890";
		Object object = new Object[] { "", 0, "", voterId, receicedDate, "VO", "@voting_category[VO].name", false };
		ElectionDayPickListItem electionDayPickListItem = new ElectionDayPickListItem(object);

		String forventetKey = Integer.valueOf(receicedDate.hashCode() + voterId.hashCode()).toString();
		assertEquals(forventetKey, electionDayPickListItem.rowKey());
	}
}
