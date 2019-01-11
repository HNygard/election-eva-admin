package no.evote.dto;

import static no.evote.constants.EvoteConstants.NOT_IN_ELECTORAL_ROLL;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static org.testng.AssertJUnit.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 * Test case for ApproveElectionDayVotingStatisticsDto
 */


public class ApproveVotingStatisticsDtoTest {

	@Test
	public void testTotalAdvanceVotes() {
		List<VotingDto> votingList = new ArrayList<>();
		VotingDto voting = new VotingDto();
		voting.setNumberOfVotings(BigInteger.ONE);
		voting.setVotingCategoryId(FI.getId());
		votingList.add(voting);

		VotingDto voting2 = new VotingDto();
		voting2.setNumberOfVotings(BigInteger.ONE);
		voting2.setVotingCategoryId(FE.getId());
		votingList.add(voting2);

		List<PickListItem> pickList = new ArrayList<>();
		String status = NOT_IN_ELECTORAL_ROLL;
		Object[] object = new Object[] { "name", 1, status, "voterId", DateTime.now(), "votingCatId", "votingCatName", false };
		PickListItem item = new PickListItem(object);
		pickList.add(item);
		ApproveVotingStatisticsDto dto = new ApproveVotingStatisticsDto(votingList, pickList);
		assertEquals(2, dto.getTotalAdvanceVotes());
	}

	@Test
	public void testCreateInstanceForElectionDay() {
		List<VotingDto> votingList = new ArrayList<>();
		VotingDto voting = new VotingDto();
		voting.setNumberOfVotings(BigInteger.ONE);
		voting.setVotingCategoryId(VS.getId());
		votingList.add(voting);

		VotingDto voting2 = new VotingDto();
		voting2.setNumberOfVotings(BigInteger.ONE);
		voting2.setVotingCategoryId(VB.getId());
		votingList.add(voting2);

		List<PickListItem> pickList = new ArrayList<>();
		String status = NOT_IN_ELECTORAL_ROLL;
		Object[] object = new Object[] { "name", 1, status, "voterId", DateTime.now(), "votingCatId", "votingCatName", false };
		PickListItem item = new PickListItem(object);
		pickList.add(item);
		ApproveVotingStatisticsDto dto = new ApproveVotingStatisticsDto(votingList, pickList);
		assertEquals(1, dto.getTotalInCategory(VS.getId()));
		assertEquals(1, dto.getTotalInCategory(VB.getId()));
		assertEquals(1, dto.getTotalNotInElectoralRoll());
		assertEquals(0, dto.getTotalDead());
	}

	@Test
	public void testCreateInstanceForAdvance() {
		List<VotingDto> votingList = new ArrayList<>();
		VotingDto voting = new VotingDto();
		voting.setNumberOfVotings(BigInteger.valueOf(3L));
		voting.setVotingCategoryId(FB.getId());
		votingList.add(voting);

		VotingDto voting2 = new VotingDto();
		voting2.setNumberOfVotings(BigInteger.valueOf(5L));
		voting2.setVotingCategoryId(FI.getId());
		votingList.add(voting2);

		VotingDto voting3 = new VotingDto();
		voting3.setNumberOfVotings(BigInteger.valueOf(10L));
		voting3.setVotingCategoryId(FU.getId());
		votingList.add(voting3);

		List<PickListItem> pickList = new ArrayList<>();
		String status = NOT_IN_ELECTORAL_ROLL;
		Object[] object = new Object[] { "name", 1, status, "voterId", DateTime.now(), "votingCatId", "votingCatName", false };
		PickListItem item = new PickListItem(object);
		pickList.add(item);
		ApproveVotingStatisticsDto dto = new ApproveVotingStatisticsDto(votingList, pickList);
		assertEquals(3, dto.getTotalInCategory(FB.getId()));
		assertEquals(5, dto.getTotalInCategory(FI.getId()));
		assertEquals(10, dto.getTotalInCategory(FU.getId()));
		assertEquals(1, dto.getTotalNotInElectoralRoll());
		assertEquals(0, dto.getTotalDead());
	}

	@Test
	public void testCreateInstanceForElectionDayWithLateAdvanceVotings() {
		List<VotingDto> votingList = new ArrayList<>();
		VotingDto voting = new VotingDto();
		voting.setNumberOfVotings(BigInteger.valueOf(15L));
		voting.setVotingCategoryId(VS.getId());
		votingList.add(voting);

		VotingDto voting2 = new VotingDto();
		voting2.setNumberOfVotings(BigInteger.valueOf(2L));
		voting2.setVotingCategoryId(VB.getId());
		votingList.add(voting2);

		VotingDto voting3 = new VotingDto();
		voting3.setNumberOfVotings(BigInteger.valueOf(5L));
		voting3.setVotingCategoryId(FB.getId());
		votingList.add(voting3);

		VotingDto voting4 = new VotingDto();
		voting4.setNumberOfVotings(BigInteger.valueOf(3L));
		voting4.setVotingCategoryId(FI.getId());
		votingList.add(voting4);

		VotingDto voting5 = new VotingDto();
		voting5.setNumberOfVotings(BigInteger.valueOf(2L));
		voting5.setVotingCategoryId(FU.getId());
		votingList.add(voting5);

		List<PickListItem> pickList = new ArrayList<>();
		String status = NOT_IN_ELECTORAL_ROLL;
		Object[] object = new Object[] { "name", 1, status, "voterId", DateTime.now(), "votingCatId", "votingCatName", false };
		PickListItem item = new PickListItem(object);
		pickList.add(item);
		ApproveVotingStatisticsDto dto = new ApproveVotingStatisticsDto(votingList, pickList);
		assertEquals(15, dto.getTotalInCategory(VS.getId()));
		assertEquals(2, dto.getTotalInCategory(VB.getId()));
		assertEquals(5, dto.getTotalInCategory(FB.getId()));
		assertEquals(3, dto.getTotalInCategory(FI.getId()));
		assertEquals(2, dto.getTotalInCategory(FU.getId()));
		assertEquals(1, dto.getTotalNotInElectoralRoll());
		assertEquals(0, dto.getTotalDead());
	}

	@Test
	public void testCreateInstanceManyOfSameCategory() {
		List<VotingDto> votingList = new ArrayList<>();
		VotingDto voting = new VotingDto();
		voting.setNumberOfVotings(BigInteger.valueOf(15L));
		voting.setVotingCategoryId(VS.getId());
		votingList.add(voting);

		VotingDto voting2 = new VotingDto();
		voting2.setNumberOfVotings(BigInteger.valueOf(2L));
		voting2.setVotingCategoryId(VS.getId());
		votingList.add(voting2);

		VotingDto voting3 = new VotingDto();
		voting3.setNumberOfVotings(BigInteger.valueOf(5L));
		voting3.setVotingCategoryId(FB.getId());
		votingList.add(voting3);

		VotingDto voting4 = new VotingDto();
		voting4.setNumberOfVotings(BigInteger.valueOf(3L));
		voting4.setVotingCategoryId(FB.getId());
		votingList.add(voting4);

		VotingDto voting5 = new VotingDto();
		voting5.setNumberOfVotings(BigInteger.valueOf(2L));
		voting5.setVotingCategoryId(FU.getId());
		votingList.add(voting5);

		List<PickListItem> pickList = new ArrayList<>();
		String status = NOT_IN_ELECTORAL_ROLL;
		Object[] object = new Object[] { "name", 1, status, "voterId", DateTime.now(), "votingCatId", "votingCatName", false };
		PickListItem item = new PickListItem(object);
		pickList.add(item);
		ApproveVotingStatisticsDto dto = new ApproveVotingStatisticsDto(votingList, pickList);
		assertEquals(17, dto.getTotalInCategory(VS.getId()));

		assertEquals(8, dto.getTotalInCategory(FB.getId()));

		assertEquals(2, dto.getTotalInCategory(FU.getId()));
		assertEquals(1, dto.getTotalNotInElectoralRoll());
		assertEquals(0, dto.getTotalDead());
	}

}

