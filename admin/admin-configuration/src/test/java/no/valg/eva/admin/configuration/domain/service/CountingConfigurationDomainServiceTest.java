package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CountingConfigurationDomainServiceTest extends MockUtilsTestCase {
	private static final AreaPath DEFAULT_AREA_PATH = AreaPath.from("730001.47.01.0101.010100.0001");
	private static final ElectionPath DEFAULT_CONTEST_PATH = ElectionPath.from("730001.01.01.000001");

	private CountingConfigurationDomainService service;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(CountingConfigurationDomainService.class);
	}

	@Test
	public void getCountingConfigurationReturnsExpectedConfiguration() {
		CountContext countContext = new CountContext(DEFAULT_CONTEST_PATH, CountCategory.VO);
		MvArea mvArea = mock(MvArea.class);
		Municipality municipality = mock(Municipality.class);
		MvElection mvElection = mock(MvElection.class);
		Contest contest = mock(Contest.class);
		ElectionGroup electionGroup = mock(ElectionGroup.class);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);

		when(mvArea.getMunicipality()).thenReturn(municipality);
		when(municipality.isRequiredProtocolCount()).thenReturn(true);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(DEFAULT_AREA_PATH)).thenReturn(mvArea);
		when(contest.isContestOrElectionPenultimateRecount()).thenReturn(true);
		when(mvElection.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY.getLevel());
		when(mvElection.getContest()).thenReturn(contest);
		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(DEFAULT_CONTEST_PATH.tilValghierarkiSti())).thenReturn(mvElection);
		when(reportCountCategory.getCountingMode()).thenReturn(CountingMode.BY_POLLING_DISTRICT);
		when(getInjectMock(ReportCountCategoryRepository.class).findByMunicipalityElectionGroupAndVoteCountCategory(municipality, electionGroup, CountCategory.VO))
			.thenReturn(reportCountCategory);

		CountingConfiguration countingConfiguration = service.getCountingConfiguration(countContext, DEFAULT_AREA_PATH);

		CountingConfiguration expectedConfiguration = new CountingConfiguration();
		expectedConfiguration.setContestAreaLevel(AreaLevelEnum.MUNICIPALITY);
		expectedConfiguration.setCountingMode(CountingMode.BY_POLLING_DISTRICT);
		expectedConfiguration.setRequiredProtocolCount(true);
		expectedConfiguration.setPenultimateRecount(true);
		assertThat(countingConfiguration).isEqualTo(expectedConfiguration);
	}

	@Test
    public void getCountingConfiguration_whenBoroughContestAndVo_returnsCentralAndByPollingDistrict() {
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class)).getAreaLevel()).thenReturn(BOROUGH.getLevel());
		CountingConfiguration countingConfiguration = service.getCountingConfiguration(
			countContext(ElectionPath.from("111111.11.11.111111"), CountCategory.VO), AreaPath.from("111111.11.11.1111.111111.1111"));
		assertThat(countingConfiguration.getCountingMode()).isEqualTo(CENTRAL_AND_BY_POLLING_DISTRICT);
	}

	@Test
    public void getCountingConfiguration_whenBoroughContestAndNotVo_returnsCentral() {
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class)).getAreaLevel()).thenReturn(BOROUGH.getLevel());
		CountingConfiguration countingConfiguration = service.getCountingConfiguration(
			countContext(ElectionPath.from("111111.11.11.111111"), CountCategory.FO), AreaPath.from("111111.11.11.1111.111111"));
		assertThat(countingConfiguration.getCountingMode()).isEqualTo(CENTRAL);
	}

	private CountContext countContext(ElectionPath electionPath, CountCategory countCategory) {
		return new CountContext(electionPath, countCategory);
	}

}
