package no.valg.eva.admin.counting.domain.service;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ContestReportDomainServiceTest extends MockUtilsTestCase {
	@DataProvider
	public static Object[][] testData() {
		return new Object[][] {
				new Object[] { contest(COUNTY, true), contestReport(VALGSTYRET), contestReport(FYLKESVALGSTYRET) },
				new Object[] { contest(MUNICIPALITY, true), contestReport(STEMMESTYRET), contestReport(VALGSTYRET) },
				new Object[] { contest(BOROUGH, true), contestReport(STEMMESTYRET), contestReport(VALGSTYRET) },
				new Object[] { contest(MUNICIPALITY, false), contestReport(VALGSTYRET), contestReport(OPPTELLINGSVALGSTYRET) }
		};
	}

	private static Contest contest(AreaLevelEnum areaLevelEnum, boolean singleArea) {
		Contest contest = mock(Contest.class);
		switch (areaLevelEnum) {
		case COUNTY:
			when(contest.isOnCountyLevel()).thenReturn(true);
			break;
		case MUNICIPALITY:
			when(contest.isOnMunicipalityLevel()).thenReturn(true);
			break;
		case BOROUGH:
			when(contest.isOnBoroughLevel()).thenReturn(true);
			break;
		default:
			throw new IllegalArgumentException();
		}
		when(contest.isSingleArea()).thenReturn(singleArea);
		return contest;
	}

	private static ContestReport contestReport(ReportingUnitTypeId reportingUnitTypeId) {
		ContestReport contestReport = mock(ContestReport.class, RETURNS_DEEP_STUBS);
		when(contestReport.getReportingUnit().getReportingUnitType().reportingUnitTypeId()).thenReturn(reportingUnitTypeId);
		return contestReport;
	}

	@Test(dataProvider = "testData")
	public void findFinalContestReportsByContest_givenTestData_returnsContestReport(
			Contest contest, ContestReport penultimateContestReport, ContestReport finalContestReport) throws Exception {
		ContestReportDomainService contestReportDomainService = initializeMocks(ContestReportDomainService.class);
		when(getInjectMock(ContestReportRepository.class).findByContest(contest)).thenReturn(asList(penultimateContestReport, finalContestReport));
		assertThat(contestReportDomainService.findFinalContestReportsByContest(contest)).containsExactly(finalContestReport);
	}
}
