package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValgnattrapportDomainServiceTest extends MockUtilsTestCase {

	private static final int ONE_FF_ONE_FE_AND_ONE_VE = 3;
	private static final int ONE_FF = 1;
	private static final int ONE_VF_AND_ONE_VE = 2;
	private static final int ONE_VF = 1;

	@Test
	public void grunnlagsdataRapporter_returnererRapporter() throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);

		assertThat(service.grunnlagsdataRapporter(mock(Election.class))).hasSize(2);
	}

	@DataProvider
	public Object[][] stemmeskjemaRapporter() {
		return new Object[][] {
				{ true, true, ONE_FF_ONE_FE_AND_ONE_VE },
				{ false, true, ONE_FF },
				{ true, false, ONE_VF_AND_ONE_VE },
				{ false, false, ONE_VF }
		};
	}

	@Test(dataProvider = "stemmeskjemaRapporter")
	public void stemmeskjemaRapporter_kommunenTellerEndelig_returnererRapporterForEndeligTelling(boolean kommunenTellerEndelig, boolean erKrets0000,
			int forventetAntallRapporter)
			throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);

		when(getInjectMock(ValgnattrapportRepository.class).byContestAndMunicipality(any(Contest.class), any(Municipality.class)))
				.thenReturn(new ArrayList<>());

		List<ReportConfiguration> reportConfigurationList = new ArrayList<>();
		ReportConfiguration fakeConfig = mock(ReportConfiguration.class);
		when(fakeConfig.isMunicipalityPollingDistrict()).thenReturn(erKrets0000);
		reportConfigurationList.add(fakeConfig);
		when(getInjectMock(ValgnattElectoralRollRepository.class).valgnattReportConfiguration(any(MvElection.class), any(Municipality.class)))
				.thenReturn(reportConfigurationList);

		MvElection fakeMvElection = mock(MvElection.class);
		when(fakeMvElection.electionPath()).thenReturn(mock(ElectionPath.class));
		Contest fakeContest = mock(Contest.class);
		when(fakeMvElection.getContest()).thenReturn(fakeContest);
		when(fakeContest.isContestOrElectionPenultimateRecount()).thenReturn(kommunenTellerEndelig);
		List<Valgnattrapport> valgnattrapportList = service.stemmeskjemaRapporter(fakeMvElection, mock(Municipality.class));
		assertThat(valgnattrapportList).hasSize(forventetAntallRapporter);
	}

	@Test
	public void stemmeskjemaRapporter_samleSamekommune_returnererRapportForForhand()
			throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);

		when(getInjectMock(ValgnattrapportRepository.class).byContestAndMunicipality(any(Contest.class), any(Municipality.class)))
				.thenReturn(new ArrayList<>());

		List<ReportConfiguration> reportConfigurationList = Collections.emptyList();
		when(getInjectMock(ValgnattElectoralRollRepository.class).valgnattReportConfiguration(any(MvElection.class), any(Municipality.class)))
				.thenReturn(reportConfigurationList);

		Municipality fakeMunicipality = mock(Municipality.class);
		when(fakeMunicipality.areaPath()).thenReturn(new AreaPath("773400.47.00.0001"));
		List<Valgnattrapport> valgnattrapportList = service.stemmeskjemaRapporter(createMock(MvElection.class), fakeMunicipality);
		assertThat(valgnattrapportList).hasSize(1);
	}

	@Test
	public void stemmeskjemaRapporter_valgoppgjorErKjort_rapportererIkkeKlarForRapportering() throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);

		List<Valgnattrapport> rapporter = new ArrayList<>();
		Valgnattrapport fakeRapport = mock(Valgnattrapport.class);
		when(fakeRapport.getMvArea()).thenReturn(mock(MvArea.class));
		rapporter.add(fakeRapport);

		when(getInjectMock(ValgnattrapportRepository.class).byContestAndMunicipality(any(Contest.class), any(Municipality.class))).thenReturn(rapporter);

		MvElection fakeMvElection = mock(MvElection.class);
		when(fakeMvElection.electionPath()).thenReturn(mock(ElectionPath.class));
		when(fakeMvElection.getContest()).thenReturn(mock(Contest.class));
		when(getInjectMock(SettlementRepository.class).erValgoppgjørKjørt(any(Contest.class))).thenReturn(true);
		List<Valgnattrapport> stemmeskjemaRapporter = service.stemmeskjemaRapporter(fakeMvElection, mock(Municipality.class));
		verify(stemmeskjemaRapporter.get(0), times(1)).setReadyForReport(false);
	}

	@Test
	public void oppgjorsskjemaRapporter_returnererRapporter() throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);
		List<Valgnattrapport> rapporter = new ArrayList<>();
		Valgnattrapport fakeRapport = mock(Valgnattrapport.class);
		rapporter.add(fakeRapport);
		when(getInjectMock(ValgnattrapportRepository.class).byContestAndReportType(any(Contest.class), eq(ReportType.VALGOPPGJOR))).thenReturn(rapporter);
		Valgnattrapport valgnattrapport = service.oppgjorsskjemaRapporter(mock(MvElection.class), mock(MvArea.class));
		assertThat(valgnattrapport.isReadyForReport()).isFalse();
	}

	@Test
	public void markerStemmeskjemaRapportert_oppdatererStemmeskjemaTilOkSendt() throws Exception {
		ValgnattrapportDomainService service = initializeMocks(ValgnattrapportDomainService.class);

		MvElection fakeMvElection = mock(MvElection.class);
		List<Valgnattrapport> fakeRapportList = new ArrayList<>();
		fakeRapportList.add(makeValgnattRapport(ValgnattrapportStatus.NOT_SENT));
		fakeRapportList.add(makeValgnattRapport(ValgnattrapportStatus.OK));
        when(getInjectMock(ValgnattrapportRepository.class).byContestAndReportType(any(), eq(ReportType.STEMMESKJEMA_VE)))
				.thenReturn(fakeRapportList);

		service.markerStemmeskjemaRapportert(fakeMvElection);
		assertThat(fakeRapportList.stream().anyMatch(Valgnattrapport::isNotSent)).isFalse();
	}

	private Valgnattrapport makeValgnattRapport(ValgnattrapportStatus status) {
		return new Valgnattrapport(null, null, null, null, ReportType.STEMMESKJEMA_VE, status, null, false);
	}
}
