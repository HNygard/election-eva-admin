package no.valg.eva.admin.counting.domain.service.settlement;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountCategoryDomainServiceTest extends MockUtilsTestCase {

	@DataProvider(name = "conditions")
	public static Object[][] conditions() {
		return new Object[][] {
				{ true, true, FO },
				{ false, false, FO }
		};
	}

	@Test(dataProvider = "conditions")
	public void countCategories_returnsCategoriesForContest(boolean countyLevel, boolean singleArea, CountCategory countCategory) throws Exception {
		CountCategoryDomainService service = initializeMocks(CountCategoryDomainService.class);
		Contest contest = mock(Contest.class);
		List<VoteCountCategory> voteCountCategories = makeVoteCountCategories(countCategory);

		when(contest.isOnCountyLevel()).thenReturn(countyLevel);
		when(contest.isSingleArea()).thenReturn(singleArea);
		when(getInjectMock(VoteCountCategoryRepository.class).categoriesForContest(contest)).thenReturn(voteCountCategories);

		List<CountCategory> countCategories = service.countCategories(contest);
		assertThat(countCategories.get(0)).isEqualTo(countCategory);
	}
	
	@Test
	public void countCategories_contestNotOnCountyLevelAndNotSamiElection_returnsCategoriesForMunicipality() throws Exception {
		CountCategoryDomainService service = initializeMocks(CountCategoryDomainService.class);
		Contest contest = mock(Contest.class, RETURNS_DEEP_STUBS);
		Municipality municipality = mock(Municipality.class);
		List<VoteCountCategory> voteCountCategories = makeVoteCountCategories(VO);

		when(contest.isOnCountyLevel()).thenReturn(false);
		when(contest.isSingleArea()).thenReturn(true);
		when(contest.getFirstContestArea().getMvArea().getMunicipality()).thenReturn(municipality);
		when(getInjectMock(VoteCountCategoryRepository.class).categoriesForContestAndMunicipality(contest, municipality)).thenReturn(voteCountCategories);

		List<CountCategory> countCategories = service.countCategories(contest);
		assertThat(countCategories.get(0)).isEqualTo(VO);
	}

	private List<VoteCountCategory> makeVoteCountCategories(CountCategory countCategory) {
		List<VoteCountCategory> voteCountCategories = new ArrayList<>();
		voteCountCategories.add(makeVoteCountCategory(countCategory));
		return voteCountCategories;
	}

	private VoteCountCategory makeVoteCountCategory(CountCategory countCategory) {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(countCategory.getId());
		return voteCountCategory;
	}

	@Test
	public void countCategories_givenBoroughContestAndMunicipality_returnsCountCategories() throws Exception {
		CountCategoryDomainService service = initializeMocks(CountCategoryDomainService.class);
		Contest contest = createMock(Contest.class);
		Municipality municipality = createMock(Municipality.class);

		when(contest.isOnBoroughLevel()).thenReturn(true);
		when(getInjectMock(VoteCountCategoryRepository.class).categoriesForContest(contest)).thenReturn(voteCountCategories());

		assertThat(service.countCategories(contest, municipality)).containsExactly(VO, VS);
	}

	private List<VoteCountCategory> voteCountCategories() {
		return asList(voteCountCategory(VO), voteCountCategory(VS));
	}

	private VoteCountCategory voteCountCategory(CountCategory category) {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(category.getId());
		return voteCountCategory;
	}

	@Test
	public void countCategories_givenContestAndMunicipality_returnsCountCategories() throws Exception {
		CountCategoryDomainService service = initializeMocks(CountCategoryDomainService.class);
		Contest contest = createMock(Contest.class);
		Municipality municipality = createMock(Municipality.class);

		when(getInjectMock(ReportCountCategoryRepository.class).findByContestAndMunicipality(contest, municipality)).thenReturn(reportCountCategories());

		assertThat(service.countCategories(contest, municipality)).containsExactly(VO, VS);
	}

	private List<ReportCountCategory> reportCountCategories() {
		return asList(reportCountCategory(VO), reportCountCategory(VS));
	}

	private ReportCountCategory reportCountCategory(CountCategory category) {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setVoteCountCategory(voteCountCategory(category));
		return reportCountCategory;
	}
}
