package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.counting.domain.model.report.ReportType.STEMMESKJEMA_FE;
import static no.valg.eva.admin.counting.domain.model.report.ReportType.STEMMESKJEMA_FF;
import static no.valg.eva.admin.counting.domain.model.report.ReportType.STEMMESKJEMA_VE;
import static no.valg.eva.admin.counting.domain.model.report.ReportType.STEMMESKJEMA_VF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ReadyForReportingDomainServiceTest extends MockUtilsTestCase {

	@Test(expectedExceptions = IllegalArgumentException.class)
    public void erStemmeskjemaKlarForRapportering_ikkeStemmeskjemarapport_kasterException() {
		ReadyForReportingDomainService readyForReportingDomainService = new ReadyForReportingDomainService(null, null, null);

		readyForReportingDomainService.erStemmeskjemaKlarForRapportering(new Valgnattrapport(null, null, null, null, ReportType.VALGOPPGJOR, null, "", false),
				null);
	}

	@Test
	public void erStemmeskjemaKlarForRapportering_forhaandsstemmeskjemarapportIngenTelling_false() throws Exception {
		ReadyForReportingDomainService readyForReportingDomainService = initializeMocks(ReadyForReportingDomainService.class);

		when(getInjectMock(ContestReportRepository.class).findByContestAndMunicipality(any(Contest.class), any(Municipality.class)))
				.thenReturn(Collections.emptyList());

		assertThat(readyForReportingDomainService.erStemmeskjemaKlarForRapportering(
				new Valgnattrapport(createMock(MvArea.class), createMock(Municipality.class), null, null, STEMMESKJEMA_FF, null, "", false),
				createMock(MvElection.class))).isFalse();
	}

	@DataProvider
	public Object[][] forhaand() {
		return new Object[][] {
				{ VALGSTYRET, CENTRAL, PRELIMINARY, STEMMESKJEMA_FF, true },
				{ VALGSTYRET, CENTRAL, FINAL, STEMMESKJEMA_FE, false },
				{ VALGSTYRET, BY_TECHNICAL_POLLING_DISTRICT, PRELIMINARY, STEMMESKJEMA_FF, true }
		};
	}

	@Test(dataProvider = "forhaand")
	public void erStemmeskjemaKlarForRapportering_forhaand(ReportingUnitTypeId valgstyret, CountingMode countingMode,
			CountQualifier qualifier, ReportType reportType, boolean expected) throws Exception {
		ReadyForReportingDomainService readyForReportingDomainService = initializeMocks(ReadyForReportingDomainService.class);

		List<ContestReport> contestReports = new ArrayList<>();
		ContestReport fakeContestReport = createMock(ContestReport.class);
		when(fakeContestReport.getReportingUnit().reportingUnitTypeId()).thenReturn(valgstyret);
		contestReports.add(fakeContestReport);
		when(getInjectMock(ContestReportRepository.class).findByContestAndMunicipality(any(Contest.class), any(Municipality.class))).thenReturn(contestReports);
		when(getInjectMock(VoteCountService.class).countingMode(eq(FO), any(Municipality.class), any(MvElection.class))).thenReturn(countingMode);
		List<VoteCount> voteCounts = new ArrayList<>();
		VoteCount fakeVoteCount = createMock(VoteCount.class);
		voteCounts.add(fakeVoteCount);
		when(fakeContestReport.findVoteCountsByAreaQualifierAndCategory(any(MvArea.class), eq(qualifier), eq(FO))).thenReturn(voteCounts);
		when(fakeVoteCount.isApproved()).thenReturn(true);
		Set<MvArea> fakeKretsSet = new HashSet<>();
		when(getInjectMock(RapporteringsområdeDomainService.class).kretserForRapporteringAvForhåndsstemmerOgSentralValgting(any(Municipality.class),
				any(MvElection.class))).thenReturn(fakeKretsSet);
		when(fakeContestReport.finnesRapporterbareTellingerForAlle(eq(fakeKretsSet), eq(qualifier))).thenReturn(true);

		assertThat(readyForReportingDomainService.erStemmeskjemaKlarForRapportering(
				new Valgnattrapport(createMock(MvArea.class), createMock(Municipality.class), null, null, reportType, null, "", false),
				createMock(MvElection.class))).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] valgting() {
		return new Object[][] {
				{ VALGSTYRET, CENTRAL, true, PRELIMINARY, STEMMESKJEMA_VF, true },
				{ STEMMESTYRET, CountingMode.BY_POLLING_DISTRICT, false, PRELIMINARY, STEMMESKJEMA_VF, true },
				{ VALGSTYRET, CountingMode.BY_POLLING_DISTRICT, false, FINAL, STEMMESKJEMA_VE, true },
				{ VALGSTYRET, CENTRAL, true, FINAL, STEMMESKJEMA_VE, false }
		};
	}

	@Test(dataProvider = "valgting")
	public void erStemmeskjemaKlarForRapportering_valgting(ReportingUnitTypeId reportingUnitTypeId, CountingMode countingModeVo,
			boolean krets0000, CountQualifier qualifier, ReportType reportType, boolean expected)
			throws Exception {
		ReadyForReportingDomainService readyForReportingDomainService = initializeMocks(ReadyForReportingDomainService.class);

		List<ContestReport> contestReports = new ArrayList<>();
		ContestReport fakeContestReport = createMock(ContestReport.class);
		when(fakeContestReport.getReportingUnit().reportingUnitTypeId()).thenReturn(reportingUnitTypeId);
		contestReports.add(fakeContestReport);
		when(getInjectMock(ContestReportRepository.class).findByContestAndMunicipality(any(Contest.class), any(Municipality.class))).thenReturn(contestReports);
		when(getInjectMock(ContestReportRepository.class).findByContestAndMvArea(any(Contest.class), any(MvArea.class))).thenReturn(contestReports);
		when(getInjectMock(VoteCountService.class).countingMode(eq(VO), any(Municipality.class), any(MvElection.class))).thenReturn(countingModeVo);
		List<VoteCount> voteCounts = new ArrayList<>();
		VoteCount fakeVoteCount = createMock(VoteCount.class);
		voteCounts.add(fakeVoteCount);
		when(fakeContestReport.findVoteCountsByAreaQualifierAndCategory(any(MvArea.class), eq(qualifier), eq(VO))).thenReturn(voteCounts);
		when(fakeVoteCount.isApproved()).thenReturn(true);

		MvArea fakeMvArea = createMock(MvArea.class);
		when(fakeMvArea.areaPath().isMunicipalityPollingDistrict()).thenReturn(krets0000);

		assertThat(readyForReportingDomainService.erStemmeskjemaKlarForRapportering(
				new Valgnattrapport(fakeMvArea, createMock(Municipality.class), null, null, reportType, null, "", false),
				createMock(MvElection.class))).isEqualTo(expected);
	}
}
