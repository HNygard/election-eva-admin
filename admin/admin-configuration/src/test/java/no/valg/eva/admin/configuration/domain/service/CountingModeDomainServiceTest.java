package no.valg.eva.admin.configuration.domain.service;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CountingModeDomainServiceTest extends MockUtilsTestCase {
	@Test
	public void countingModeMapper_givenBoroughContest_returnsMapperForBoroughContest() throws Exception {
		CountingModeDomainService service = initializeMocks(CountingModeDomainService.class);
		Contest contest = createMock(Contest.class);
		Municipality municipality = createMock(Municipality.class);

		when(contest.isOnBoroughLevel()).thenReturn(true);

		Function<CountCategory, CountingMode> mapper = service.countingModeMapper(contest, municipality);

		assertThat(mapper.apply(VO)).isEqualTo(BY_POLLING_DISTRICT);
		assertThat(mapper.apply(VS)).isEqualTo(CENTRAL);
		assertThat(mapper.apply(VB)).isEqualTo(CENTRAL);
		assertThat(mapper.apply(FO)).isEqualTo(CENTRAL);
		assertThat(mapper.apply(FS)).isEqualTo(CENTRAL);
		assertThat(mapper.apply(BF)).isEqualTo(CENTRAL);
	}

	@Test
	public void countingModeMapper_givenContest_returnsMapper() throws Exception {
		CountingModeDomainService service = initializeMocks(CountingModeDomainService.class);
		Contest contest = createMock(Contest.class);
		Municipality municipality = createMock(Municipality.class);

		when(getInjectMock(ReportCountCategoryRepository.class).findByContestAndMunicipality(contest, municipality)).thenReturn(reportCountCategories());

		Function<CountCategory, CountingMode> mapper = service.countingModeMapper(contest, municipality);

		assertThat(mapper.apply(VO)).isEqualTo(BY_POLLING_DISTRICT);
		CountCategory anotherCategory = VS;
		assertThat(mapper.apply(anotherCategory)).isEqualTo(CENTRAL);
	}

	private List<ReportCountCategory> reportCountCategories() {
		return asList(reportCountCategory(VO, BY_POLLING_DISTRICT), reportCountCategory(VS, CENTRAL));
	}

	private ReportCountCategory reportCountCategory(CountCategory category, CountingMode countingMode) {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(category.getId());
		reportCountCategory.setVoteCountCategory(voteCountCategory);
		reportCountCategory.setCentralPreliminaryCount(countingMode.isCentralPreliminaryCount());
		reportCountCategory.setPollingDistrictCount(countingMode.isPollingDistrictCount());
		reportCountCategory.setTechnicalPollingDistrictCount(countingMode.isTechnicalPollingDistrictCount());
		return reportCountCategory;
	}

	@Test
	public void findCountingMode_givenCountCategoryElectionPathAndAreaPath_findsCountingMode() throws Exception {
		CountingModeDomainService countingModeDomainService = initializeMocks(CountingModeDomainService.class);

		AreaPath areaPath = AreaPath.from("111111.11.11.1111");
		CountCategory countCategory = CountCategory.VO;
		CountingMode countingMode = CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
		MvElection mvElection = createMock(MvElection.class);
		ElectionGroup electionGroup = mockElectionGroup(mvElection);
		ElectionPath electionPath = mockElectionPath();
		MvArea mvArea = createMock(MvArea.class);
		Municipality municipality = mockMunicipality(mvArea);
		ReportCountCategory reportCountCategory = mockReportCountCategory(countingMode);
		mockMvAreaRepository(areaPath, mvArea);
		mockMvElectionRepository(mvElection, electionPath);
		mockReportCountCategoryRepository(countCategory, electionGroup, municipality, reportCountCategory);

		CountingMode actualCountingMode = countingModeDomainService.findCountingMode(countCategory, electionPath, areaPath);

		assertThat(actualCountingMode).isEqualTo(countingMode);
	}

	private void mockReportCountCategoryRepository(CountCategory countCategory, ElectionGroup electionGroup, Municipality municipality,
												   ReportCountCategory reportCountCategory) {
		ReportCountCategoryRepository reportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, electionGroup, countCategory))
			.thenReturn(reportCountCategory);
	}

	private void mockMvElectionRepository(MvElection mvElection, ElectionPath selectedElectionPath) {
		MvElectionRepository mvElectionRepository = getInjectMock(MvElectionRepository.class);
		when(mvElectionRepository.finnEnkeltMedSti(selectedElectionPath.toElectionGroupPath().tilValghierarkiSti())).thenReturn(mvElection);
	}

	private void mockMvAreaRepository(AreaPath areaPath, MvArea mvArea) {
		MvAreaRepository mvAreaRepository = getInjectMock(MvAreaRepository.class);
		when(mvAreaRepository.findSingleByPath(areaPath.toMunicipalityPath())).thenReturn(mvArea);
	}

	private ReportCountCategory mockReportCountCategory(CountingMode countingMode) {
		ReportCountCategory reportCountCategory = createMock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(countingMode);
		return reportCountCategory;
	}

	private Municipality mockMunicipality(MvArea mvArea) {
		Municipality municipality = createMock(Municipality.class);
		when(mvArea.getMunicipality()).thenReturn(municipality);
		return municipality;
	}

	private ElectionPath mockElectionPath() {
		ElectionPath selectedElectionPath = createMock(ElectionPath.class);
		when(selectedElectionPath.toElectionGroupPath()).thenReturn(selectedElectionPath);
		return selectedElectionPath;
	}

	private ElectionGroup mockElectionGroup(MvElection mvElection) {
		ElectionGroup electionGroup = createMock(ElectionGroup.class);
		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		return electionGroup;
	}

}
