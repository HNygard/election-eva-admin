package no.valg.eva.admin.common.configuration.model.local;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;

import org.testng.annotations.Test;

/**
 * Test cases for ReportCountCategories.
 */
public class ReportCountCategoriesTest {

	public static final String EXPECTED_VALUE_FROM_LOCAL_CAT_VF = "Expected value from local cat VF";
	public static final String EXPECTED_RESULT_TO_BE_OVERRIDDEN_WITH_VALUE_FROM_CENTRAL_CAT_VF = "Expected result to be overridden with value from central cat VF";

	@Test
	public void samiParentShouldOnlyHaveAdvanceVotesAndLateAdvanceVotes() {
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		centralCats.add(createElectionVoteCountCategory(VO.getId(), true, true));
		centralCats.add(createElectionVoteCountCategory(VF.getId(), true, true));
		centralCats.add(createElectionVoteCountCategory(VB.getId(), true, true));
		centralCats.add(createElectionVoteCountCategory(FO.getId(), true, true));
		centralCats.add(createElectionVoteCountCategory(FS.getId(), true, true));

		ReportCountCategories.Criteria criteria = new ReportCountCategories.Criteria(false, true);
		List<ReportCountCategory> cats = new ReportCountCategories(Collections.<ReportCountCategory> emptyList(), centralCats).filter(criteria).list();
		assertEquals(2, cats.size());
		Set<String> idSet = new HashSet<>();
		idSet.add(cats.get(0).getVoteCountCategory().getId());
		idSet.add(cats.get(1).getVoteCountCategory().getId());
		assertTrue(idSet.contains(FO.getId()));
		assertTrue(idSet.contains(FS.getId()));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void invalidCategoryListSize() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		localCats.add(createReportCountCategory(FO.getId()));
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		new ReportCountCategories(localCats, centralCats);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void invalidCategoryListContent() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		localCats.add(createReportCountCategory(FO.getId()));
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		centralCats.add(createElectionVoteCountCategory(VO.getId(), true, true));
		new ReportCountCategories(localCats, centralCats);
	}

	@Test
	public void validCategoryListSize() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		new ReportCountCategories(localCats, centralCats);
	}

	@Test
	public void givenEditableAndEnabledCentralCategoryMostValuesAreRetrievedFromExistingLocalCategory() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		ReportCountCategory localVf = createReportCountCategory(VF.getId());
		localVf.setCentralPreliminaryCount(true);
		localVf.setPollingDistrictCount(true);
		localVf.setSpecialCover(true);
		localCats.add(localVf);

		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		ElectionVoteCountCategory centralVf = createElectionVoteCountCategory(VF.getId(), true, true);
		centralVf.setCentralPreliminaryCount(false);
		centralVf.setPollingDistrictCount(false);
		centralVf.setSpecialCover(false);
		centralVf.setTechnicalPollingDistrictCountConfigurable(true);
		centralCats.add(centralVf);

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		List<ReportCountCategory> countCategories = reportCountCategories.list();
		assertEquals(1, countCategories.size());
		assertTrue(EXPECTED_VALUE_FROM_LOCAL_CAT_VF, countCategories.get(0).isCentralPreliminaryCount());
		assertTrue(EXPECTED_VALUE_FROM_LOCAL_CAT_VF, countCategories.get(0).isPollingDistrictCount());
		assertTrue(EXPECTED_VALUE_FROM_LOCAL_CAT_VF, countCategories.get(0).isSpecialCover());
		assertTrue(EXPECTED_VALUE_FROM_LOCAL_CAT_VF, countCategories.get(0).isEditable());
		assertTrue("Expected technicalPollingDistrictConfigurable to be defined by central cat VF", countCategories.get(0)
				.isTechnicalPollingDistrictCountConfigurable());
	}

	@Test
	public void whenCentralCategoryIsNonEditableAndEnabledLocalCategoryIsOverriddenWithValuesFromCentral() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		ReportCountCategory localVf = createReportCountCategory(VF.getId());
		localVf.setCentralPreliminaryCount(true);
		localVf.setPollingDistrictCount(true);
		localVf.setSpecialCover(true);
		localVf.setEditable(true);
		localCats.add(localVf);

		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		ElectionVoteCountCategory centralVf = createElectionVoteCountCategory(VF.getId(), false, true);
		centralVf.setCentralPreliminaryCount(false);
		centralVf.setPollingDistrictCount(false);
		centralVf.setSpecialCover(false);
		centralCats.add(centralVf);

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		List<ReportCountCategory> countCategories = reportCountCategories.list();
		assertEquals(1, countCategories.size());
		assertFalse(EXPECTED_RESULT_TO_BE_OVERRIDDEN_WITH_VALUE_FROM_CENTRAL_CAT_VF, countCategories.get(0).isCentralPreliminaryCount());
		assertFalse(EXPECTED_RESULT_TO_BE_OVERRIDDEN_WITH_VALUE_FROM_CENTRAL_CAT_VF, countCategories.get(0).isPollingDistrictCount());
		assertFalse(EXPECTED_RESULT_TO_BE_OVERRIDDEN_WITH_VALUE_FROM_CENTRAL_CAT_VF, countCategories.get(0).isSpecialCover());
		assertFalse(EXPECTED_RESULT_TO_BE_OVERRIDDEN_WITH_VALUE_FROM_CENTRAL_CAT_VF, countCategories.get(0).isEditable());
	}

	@Test
	public void enabledCategoriesAreReturned() {
		List<ReportCountCategory> localCats = new ArrayList<>();

		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		ElectionVoteCountCategory vo = createElectionVoteCountCategory(VO.getId(), true, true);
		centralCats.add(vo);
		ElectionVoteCountCategory centralVf = createElectionVoteCountCategory(VF.getId(), true, false);
		centralCats.add(centralVf);

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		List<ReportCountCategory> countCategories = reportCountCategories.list();
		assertEquals(1, countCategories.size());
		assertEquals("Expected result to contain cat VO", VO.getId(), countCategories.get(0).getVoteCountCategory().getId());
	}

	@Test
	public void fremmedIsFilteredWhenUsingElectronicMarkOff() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		centralCats.add(createElectionVoteCountCategory(VF.getId(), true, true));

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		boolean electronicMarkOffs = true;
		assertTrue("Expected VF to be filtered and list empty",
				reportCountCategories.filter(new ReportCountCategories.Criteria(electronicMarkOffs, false)).list().isEmpty());
	}

	@Test
	public void beredskapIsFilteredWhenNotUsingElectronicMarkOff() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		centralCats.add(createElectionVoteCountCategory(VB.getId(), true, true));

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		boolean electronicMarkOffs = false;
		assertTrue("Expected VB to be filtered and list empty",
				reportCountCategories.filter(new ReportCountCategories.Criteria(electronicMarkOffs, false)).list().isEmpty());
	}

	@Test
	public void beredskapIsNotFilteredWhenUsingElectronicMarkOff() {
		List<ReportCountCategory> localCats = new ArrayList<>();
		List<ElectionVoteCountCategory> centralCats = new ArrayList<>();
		centralCats.add(createElectionVoteCountCategory(VB.getId(), true, true));

		ReportCountCategories reportCountCategories = new ReportCountCategories(localCats, centralCats);
		boolean electronicMarkOffs = true;
		assertEquals("Expected VB to be present and list of size 1", 1,
				reportCountCategories.filter(new ReportCountCategories.Criteria(electronicMarkOffs, false)).list().size());
	}

	private ReportCountCategory createReportCountCategory(final String voteCountCategoryId) {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(voteCountCategoryId);
		reportCountCategory.setVoteCountCategory(voteCountCategory);
		return reportCountCategory;
	}

	private ElectionVoteCountCategory createElectionVoteCountCategory(final String voteCountCategoryId, final boolean countCategoryEditable,
			final boolean countCategoryEnabled) {
		ElectionVoteCountCategory countCategory = new ElectionVoteCountCategory();
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(voteCountCategoryId);
		countCategory.setVoteCountCategory(voteCountCategory);
		countCategory.setCountCategoryEditable(countCategoryEditable);
		countCategory.setCountCategoryEnabled(countCategoryEnabled);
		return countCategory;
	}

}
