package no.valg.eva.admin.counting.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ReportingUnitDomainServiceTest {

	private static final String AN_ELECTION_EVENT_ID = "150001";
	private static final String POLLING_DISTRICT_PATH = "150001.47.01.0101.010100.0000";
	private static final String MUNICIPALITY_PATH = "150001.47.01.0101";
	private static final String COUNTY_PATH = "150001.47.01";
	private static final String MV_AREA_PATH_POLLING_DISTRICT = "730001.47.01.0101.010100.0001";

	@Test
	public void reportingUnitForFinalCount_opptellingsvalgstyret_returnsReportingUnitForOpptellingsvalgstyret() {

		ReportingUnitRepository fakeReportingUnitRepository = mock(ReportingUnitRepository.class);
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(fakeReportingUnitRepository);

		MvArea fakeOperatorMvArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(fakeOperatorMvArea.getElectionEventId()).thenReturn(AN_ELECTION_EVENT_ID);
		MvElection fakeMvElectionContest = mock(MvElection.class);
		when(fakeMvElectionContest.getElectionPath()).thenReturn(AN_ELECTION_EVENT_ID);

		reportingUnitDomainService.reportingUnitForFinalCount(OPPTELLINGSVALGSTYRET,
				AreaPath.from(AN_ELECTION_EVENT_ID), AreaPath.from(POLLING_DISTRICT_PATH), fakeMvElectionContest);
		verify(fakeReportingUnitRepository, times(1)).byAreaPathElectionPathAndType(
				AreaPath.from(AN_ELECTION_EVENT_ID), ElectionPath.from(AN_ELECTION_EVENT_ID), OPPTELLINGSVALGSTYRET);
	}

	@Test
	public void reportingUnitForFinalCount_valgstyretAndOperatorOnRootLevel_areaPathForReportingUnitIsMunicipalityPath() {
		testForLevel(VALGSTYRET, AreaPath.from(MUNICIPALITY_PATH), AN_ELECTION_EVENT_ID);
	}

	@Test
	public void reportingUnitForFinalCount_fylkesvalgstyretAndOperatorOnRootLevel_areaPathForReportingUnitIsCountyPath() {
		testForLevel(FYLKESVALGSTYRET, AreaPath.from(COUNTY_PATH), AN_ELECTION_EVENT_ID);
	}

	@Test
	public void reportingUnitForFinalCount_valgstyretAndOperatorOnMunicipalityLevel_areaPathForReportingUnitIsAreaForCountingAndAlsoOperatorArea() {
		testForLevel(VALGSTYRET, AreaPath.from(MUNICIPALITY_PATH), MUNICIPALITY_PATH);
	}

	@Test
	public void reportingUnitForFinalCount_valgstyretAndOperatorOnCountyLevel_areaPathForReportingUnitIsMunicipalityOfAreaForCounting() {
		testForLevel(VALGSTYRET, AreaPath.from(MUNICIPALITY_PATH), COUNTY_PATH);
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void reportingUnitForFinalCount_valgstyretAndOperatorOnPollingDistrictLevel_illegalState() {
		testForLevel(VALGSTYRET, null, POLLING_DISTRICT_PATH);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void reportingUnitForFinalCount_stemmestyret_illegalState() {
		testForLevel(STEMMESTYRET, null, MUNICIPALITY_PATH);
	}

	private void testForLevel(ReportingUnitTypeId reportingUnitTypeId, AreaPath expectedAreaPathForReportingUnit, String operatorAreaPath) {
		ReportingUnitRepository fakeReportingUnitRepository = mock(ReportingUnitRepository.class);
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(fakeReportingUnitRepository);

		MvArea fakeOperatorMvArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(fakeOperatorMvArea.getElectionEventId()).thenReturn(AN_ELECTION_EVENT_ID);
		when(fakeOperatorMvArea.getAreaPath()).thenReturn(operatorAreaPath);
		when(fakeOperatorMvArea.getAreaLevel()).thenReturn(AreaPath.from(operatorAreaPath).getLevel().getLevel());
		MvArea fakeCountingMvArea = mock(MvArea.class);
		when(fakeCountingMvArea.getAreaPath()).thenReturn(POLLING_DISTRICT_PATH);
		MvElection fakeMvElectionContest = mock(MvElection.class);
		when(fakeMvElectionContest.getElectionPath()).thenReturn(AN_ELECTION_EVENT_ID);

		reportingUnitDomainService.reportingUnitForFinalCount(reportingUnitTypeId,
				AreaPath.from(operatorAreaPath), AreaPath.from(POLLING_DISTRICT_PATH), fakeMvElectionContest);

		verify(fakeReportingUnitRepository, times(1)).findByAreaPathAndType(expectedAreaPathForReportingUnit, reportingUnitTypeId);
	}

	@Test
	public void areaPathForFindingReportingUnit_whenCentralCount_returnsMunicipalityAreaPath() {
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(mock(ReportingUnitRepository.class));

		MvArea pollingDistrict = makePollingDistrictMvArea();
		assertThat(reportingUnitDomainService.areaPathForFindingReportingUnit(ReportingUnitTypeId.VALGSTYRET,
				AreaPath.from(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY), pollingDistrict)).isEqualTo(AreaPath.from(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY));
	}

	@Test
	public void areaPathForFindingReportingUnit_whenPollingDistrictCountAndMunicipalityContainsPollingDistrict_returnsCountingAreaPath() {
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(mock(ReportingUnitRepository.class));
		AreaPath operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		MvArea pollingDistrict = makePollingDistrictMvArea();

		assertThat(reportingUnitDomainService.areaPathForFindingReportingUnit(ReportingUnitTypeId.STEMMESTYRET, operatorAreaPath, pollingDistrict))
				.isEqualTo(AreaPath.from(pollingDistrict.getAreaPath()));
	}

	@Test
	public void areaPathForFindingReportingUnit_whenSamiElectionAndOpptellingsvalgstyret_returnsAreaPathForElectionEvent() {
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(mock(ReportingUnitRepository.class));
		AreaPath operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		MvArea pollingDistrict = makePollingDistrictMvArea();

		assertThat(reportingUnitDomainService.areaPathForFindingReportingUnit(ReportingUnitTypeId.OPPTELLINGSVALGSTYRET, operatorAreaPath, pollingDistrict))
				.isEqualTo(AreaPath.from(operatorAreaPath.getElectionEventId()));
	}

	@Test
	public void reportingUnitForCountyFinalCount_samiElection_returnsOpptellingsvalgstyret() {
		ReportingUnitRepository fakeReportingUnitRepository = mock(ReportingUnitRepository.class);
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(fakeReportingUnitRepository);
		AreaPath operatorAreaPath = new AreaPath(AN_ELECTION_EVENT_ID);
		AreaPath fakeCountingAreaPath = mock(AreaPath.class);
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(fakeMvElectionContest.getContest().isSingleArea()).thenReturn(false);
		when(fakeMvElectionContest.getElectionPath()).thenReturn(AN_ELECTION_EVENT_ID);

		reportingUnitDomainService.reportingUnitForCountyFinalCount(operatorAreaPath, fakeCountingAreaPath, fakeMvElectionContest);
		verify(fakeReportingUnitRepository, times(1)).byAreaPathElectionPathAndType(any(AreaPath.class), any(ElectionPath.class), eq(OPPTELLINGSVALGSTYRET));
	}

	@Test
	public void reportingUnitForCountyFinalCount_notSamiElection_returnsFylkesvalgstyret() {
		ReportingUnitRepository fakeReportingUnitRepository = mock(ReportingUnitRepository.class);
		ReportingUnitDomainService reportingUnitDomainService = new ReportingUnitDomainService(fakeReportingUnitRepository);
		AreaPath operatorAreaPath = new AreaPath(AN_ELECTION_EVENT_ID);
		AreaPath fakeCountingAreaPath = mock(AreaPath.class);
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(fakeMvElectionContest.getContest().isSingleArea()).thenReturn(true);

		reportingUnitDomainService.reportingUnitForCountyFinalCount(operatorAreaPath, fakeCountingAreaPath, fakeMvElectionContest);
        verify(fakeReportingUnitRepository, times(1)).findByAreaPathAndType(any(), eq(FYLKESVALGSTYRET));
	}

	private MvArea makePollingDistrictMvArea() {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(MV_AREA_PATH_POLLING_DISTRICT);
		return mvArea;
	}

}
