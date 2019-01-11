package no.valg.eva.admin.counting.domain.service.countingoverview;

import static java.util.Collections.addAll;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.MUNICIPALITY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PRELIMINARY_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.counting.domain.builder.CountingOverviewRootBuilder;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewDomainServiceTest extends MockUtilsTestCase {
	@DataProvider
	public static Object[][] countingOverviewForValgstyretTestData() {
		return new Object[][] {
				new Object[] { false, true, true, true, BY_POLLING_DISTRICT,
						statusTypes(PROTOCOL_COUNT_STATUS, PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS) },
				new Object[] { false, true, true, null, CENTRAL,
						statusTypes(PROTOCOL_COUNT_STATUS, PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS) },
				new Object[] { false, true, true, null, null,
						statusTypes(PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS) },
				new Object[] { false, true, false, null, CENTRAL,
						statusTypes(PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS) },
				new Object[] { true, true, null, null, CENTRAL,
						statusTypes(PROTOCOL_COUNT_STATUS, PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS) }
		};
	}

	@DataProvider
	public static Object[][] countingOverviewForFylkesalgstyretTestData() {
		return new Object[][] {
				new Object[] { false, statusTypes(PRELIMINARY_COUNT_STATUS, COUNTY_FINAL_COUNT_STATUS, COUNTY_REJECTED_BALLOTS_STATUS) },
				new Object[] { true, statusTypes(MUNICIPALITY_FINAL_COUNT_STATUS, COUNTY_FINAL_COUNT_STATUS, COUNTY_REJECTED_BALLOTS_STATUS) }
		};
	}

	private static List<StatusType> statusTypes(StatusType... statusTypes) {
		ArrayList<StatusType> statusTypesList = new ArrayList<>();
		addAll(statusTypesList, statusTypes);
		return statusTypesList;
	}

	@Test
	public void countingOverviewForOpptellingsvalgstyret_givenContestArea_countingOverviewRoot() throws Exception {
		CountingOverviewDomainService service = initializeMocks(CountingOverviewDomainService.class);
		ContestArea contestArea = mock(ContestArea.class);
		Contest contest = mock(Contest.class);
		MvArea mvArea = mock(MvArea.class);
		Municipality municipality = mock(Municipality.class);
		List<VoteCountDigest> voteCountDigests = createListMock();
		List<CountCategory> countCategories = createListMock();
		Function<CountCategory, CountingMode> countingModeMapper = createFunctionMock();
		List<StatusType> statusTypes = statusTypes(PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS);
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);

		when(contestArea.getContest()).thenReturn(contest);
		when(contestArea.getMvArea()).thenReturn(mvArea);
		when(mvArea.getMunicipality()).thenReturn(municipality);
		when(getInjectMock(VoteCountService.class).voteCountDigestsForOpptellingsvalgstyret(contest, mvArea)).thenReturn(voteCountDigests);
		when(getInjectMock(CountCategoryDomainService.class).countCategories(contest, municipality)).thenReturn(countCategories);
		when(getInjectMock(CountingModeDomainService.class).countingModeMapper(contest, municipality)).thenReturn(countingModeMapper);
		when(getInjectMock(CountingOverviewRootBuilder.class)
				.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper))
						.thenReturn(countingOverviewRoot);

		assertThat(service.countingOverviewForOpptellingsvalgstyret(contestArea)).isSameAs(countingOverviewRoot);
	}

	@Test(dataProvider = "countingOverviewForValgstyretTestData")
	public void countingOverviewForValgstyret_givenContestAndMvArea_returnsCountingOverviewRoot(
			boolean contestOnBoroughLevel, boolean penultimateRecount, Boolean requiredProtocolCount, Boolean hasParentPollingDistricts,
			CountingMode countingMode, List<StatusType> statusTypes) throws Exception {
		CountingOverviewDomainService service = initializeMocks(CountingOverviewDomainService.class);
		ReportCountCategoryRepository reportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		Contest contest = mock(Contest.class);
		MvArea mvArea = mock(MvArea.class);
		Municipality municipality = mock(Municipality.class);
		ReportCountCategory reportCountCategoryForVo = mock(ReportCountCategory.class);
		List<VoteCountDigest> voteCountDigests = createListMock();
		List<CountCategory> countCategories = createListMock();
		Function<CountCategory, CountingMode> countingModeMapper = createFunctionMock();
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);

		when(contest.isOnBoroughLevel()).thenReturn(contestOnBoroughLevel);
		when(contest.isContestOrElectionPenultimateRecount()).thenReturn(penultimateRecount);
		if (requiredProtocolCount != null) {
			when(municipality.isRequiredProtocolCount()).thenReturn(requiredProtocolCount);
		}
		if (hasParentPollingDistricts != null) {
			when(municipality.hasParentPollingDistricts()).thenReturn(hasParentPollingDistricts);
		}
		when(mvArea.getMunicipality()).thenReturn(municipality);
		if (countingMode != null) {
			when(reportCountCategoryForVo.getCountingMode()).thenReturn(countingMode);
			when(reportCountCategoryRepository.findByContestAndMunicipalityAndCategory(contest, municipality, VO)).thenReturn(reportCountCategoryForVo);
		} else {
			when(reportCountCategoryRepository.findByContestAndMunicipalityAndCategory(contest, municipality, VO)).thenReturn(null);
		}
		when(getInjectMock(VoteCountService.class).voteCountDigestsForValgstyret(contest, mvArea)).thenReturn(voteCountDigests);
		when(getInjectMock(CountCategoryDomainService.class).countCategories(contest, municipality)).thenReturn(countCategories);
		when(getInjectMock(CountingModeDomainService.class).countingModeMapper(contest, municipality)).thenReturn(countingModeMapper);
		when(getInjectMock(CountingOverviewRootBuilder.class)
				.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper))
						.thenReturn(countingOverviewRoot);

		assertThat(service.countingOverviewForValgstyret(contest, mvArea)).isSameAs(countingOverviewRoot);
	}

	@Test(dataProvider = "countingOverviewForFylkesalgstyretTestData")
	public void countingOverviewForFylkesvalgstyret_givenContestAndMvArea_returnsCountingOverviewRoot(
			boolean penultimateRecount, List<StatusType> statusTypes) throws Exception {

		CountingOverviewDomainService service = initializeMocks(CountingOverviewDomainService.class);
		Contest contest = mock(Contest.class);
		MvArea mvArea = mock(MvArea.class);
		Municipality municipality = mock(Municipality.class);
		List<VoteCountDigest> voteCountDigests = createListMock();
		List<CountCategory> countCategories = createListMock();
		Function<CountCategory, CountingMode> countingModeMapper = createFunctionMock();
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);

		when(contest.isContestOrElectionPenultimateRecount()).thenReturn(penultimateRecount);
		when(mvArea.getMunicipality()).thenReturn(municipality);
		when(getInjectMock(VoteCountService.class).voteCountDigestsForFylkesvalgstyret(contest, mvArea)).thenReturn(voteCountDigests);
		when(getInjectMock(CountCategoryDomainService.class).countCategories(contest, municipality)).thenReturn(countCategories);
		when(getInjectMock(CountingModeDomainService.class).countingModeMapper(contest, municipality)).thenReturn(countingModeMapper);
		when(getInjectMock(CountingOverviewRootBuilder.class)
				.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper))
						.thenReturn(countingOverviewRoot);

		assertThat(service.countingOverviewForFylkesvalgstyret(contest, mvArea)).isSameAs(countingOverviewRoot);
	}
}
