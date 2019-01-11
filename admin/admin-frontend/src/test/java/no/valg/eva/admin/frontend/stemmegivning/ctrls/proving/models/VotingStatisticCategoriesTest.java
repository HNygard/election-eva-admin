package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import no.valg.eva.admin.common.voting.VotingCategory;

import org.testng.annotations.Test;

/**
 * Test cases for VotingStatisticCategories
 */
public class VotingStatisticCategoriesTest {

	@Test
	public void allCategoriesIncludeSaerskiltBeredskapWithSentInnkomneTrue() {
		VotingStatisticCategories votingStatisticCategories = new VotingStatisticCategories(VotingStatisticCategories.ALL_VOTING_CATEGORIES);
		String[] cats = votingStatisticCategories.electionDayVotingCategoriesForStatistics();
		assertEquals(Arrays.asList(new String[] { VotingCategory.VS.getId(), VotingCategory.VB.getId() }), Arrays.asList(cats));
		assertTrue("Expected includeLateAdvanceVotes true", votingStatisticCategories.includeLateAdvanceVotes());
	}

	@Test
	public void saerskiltImpliesSaerskiltWithSentInnkomneFalse() {
		VotingStatisticCategories votingStatisticCategories = new VotingStatisticCategories(VotingCategory.VS.getId());
		String[] cats = votingStatisticCategories.electionDayVotingCategoriesForStatistics();
		assertEquals(Arrays.asList(new String[] { VotingCategory.VS.getId() }), Arrays.asList(cats));
		assertFalse("Expected includeLateAdvanceVotes false", votingStatisticCategories.includeLateAdvanceVotes());
	}

	@Test
	public void sentInnkomneImpliesNoCategoriesWithSentInnkomneTrue() {
		VotingStatisticCategories votingStatisticCategories = new VotingStatisticCategories(VotingStatisticCategories.LATE_ADVANCE_VOTES);
		String[] cats = votingStatisticCategories.electionDayVotingCategoriesForStatistics();
		assertEquals(Collections.emptyList(), Arrays.asList(cats));
		assertTrue("Expected includeLateAdvanceVotes true", votingStatisticCategories.includeLateAdvanceVotes());
	}
}
