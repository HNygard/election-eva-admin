package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.event.TellingEndrerStatus;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TellingEndrerStatusDomainServiceTest extends MockUtilsTestCase {

	private static final AreaPath AN_AREA_PATH = AreaPath.from("150001.47.01.0101");
	private static final ElectionPath A_CONTEST_PATH = ElectionPath.from("150001.01.01.000001");

	@Test
	public void oppdaterValgnattRapport_categorySomIkkeHarReportType_avslutter() throws Exception {
		TellingEndrerStatusDomainService tellingEndrerStatusDomainService = initializeMocks(TellingEndrerStatusDomainService.class);

		tellingEndrerStatusDomainService.oppdaterValgnattrapport(new TellingEndrerStatus(AN_AREA_PATH, PRELIMINARY, A_CONTEST_PATH, BF, VALGSTYRET));
		verify(getInjectMock(RapporteringsområdeDomainService.class), never()).kretsForRapportering(AN_AREA_PATH);
	}

	@Test
	public void oppdaterValgnattRapport_detFinnesValgnattrapportSomErSendtForTellingen_valgnattrapportMaaRapporteresPaaNytt() throws Exception {
		TellingEndrerStatusDomainService tellingEndrerStatusDomainService = initializeMocks(TellingEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		when(getInjectMock(ValgnattrapportRepository.class).byContestReportTypeAndMvArea(any(Contest.class), eq(ReportType.STEMMESKJEMA_FE), any(MvArea.class)))
				.thenReturn(mockValgnattrapport);
		when(mockValgnattrapport.isOk()).thenReturn(true);

		tellingEndrerStatusDomainService.oppdaterValgnattrapport(new TellingEndrerStatus(AN_AREA_PATH, FINAL, A_CONTEST_PATH, FO, VALGSTYRET));

		verify(mockValgnattrapport).maaRapporteresPaaNytt();
	}

	@Test(dataProvider = "styrerDetIkkeSkalRapporteresFor")
	public void oppdaterValgnattRapport_detFinnesValgnattrapportForKommunenMenTellingenErPaaFylket_valgnattrapportMaaIkkeRapporteresPaaNytt(
			ReportingUnitTypeId reportingUnitTypeId
	) throws Exception {
		TellingEndrerStatusDomainService tellingEndrerStatusDomainService = initializeMocks(TellingEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		when(getInjectMock(ValgnattrapportRepository.class).byContestReportTypeAndMvArea(any(Contest.class), eq(ReportType.STEMMESKJEMA_FE), any(MvArea.class)))
				.thenReturn(mockValgnattrapport);
		when(mockValgnattrapport.isOk()).thenReturn(true);

		tellingEndrerStatusDomainService.oppdaterValgnattrapport(new TellingEndrerStatus(AN_AREA_PATH, FINAL, A_CONTEST_PATH, FO, reportingUnitTypeId));

		verify(mockValgnattrapport, never()).maaRapporteresPaaNytt();
	}

	@DataProvider
	private Object[][] styrerDetIkkeSkalRapporteresFor() {
		return new Object[][] {
				new Object[] { FYLKESVALGSTYRET },
				new Object[] { OPPTELLINGSVALGSTYRET }
		};
	}

	@Test
	public void oppdaterValgnattRapport_detFinnesIkkeValgnattrapportForTellingen_avslutter() throws Exception {
		TellingEndrerStatusDomainService tellingEndrerStatusDomainService = initializeMocks(TellingEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		when(getInjectMock(ValgnattrapportRepository.class).byContestReportTypeAndMvArea(any(Contest.class), eq(ReportType.STEMMESKJEMA_FE), any(MvArea.class)))
				.thenReturn(null);

		tellingEndrerStatusDomainService.oppdaterValgnattrapport(new TellingEndrerStatus(AN_AREA_PATH, FINAL, A_CONTEST_PATH, FO, VALGSTYRET));

		verify(mockValgnattrapport, never()).maaRapporteresPaaNytt();
	}
}
