package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.settlement.domain.event.OppgjorEndrerStatus;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OppgjorEndrerStatusDomainServiceTest extends MockUtilsTestCase {

	private static final AreaPath AN_AREA_PATH = AreaPath.from("971001.47.01");
	private static final ElectionPath AN_ELECTION_PATH = ElectionPath.from("971001.01.01.000001");

	@Test
	public void oppdaterValgnattRapport_detFinnesValgnattrapportSomErSendtForOppgjoret_valgnattrapportMaaRapporteresPaaNytt() throws Exception {
		OppgjorEndrerStatusDomainService oppgjorEndrerStatusDomainService = initializeMocks(OppgjorEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		MvElection mockElection = createMock(MvElection.class);
		when(getInjectMock(ValgnattrapportRepository.class).byContestAndReportType(any(Contest.class), eq(ReportType.VALGOPPGJOR)))
				.thenReturn(Collections.singletonList(mockValgnattrapport));
		when(mockValgnattrapport.isOk()).thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(Collections.singletonList(mockElection));

		oppgjorEndrerStatusDomainService.oppdaterValgnattRapport(new OppgjorEndrerStatus(AN_AREA_PATH, AN_ELECTION_PATH));

		verify(mockValgnattrapport).maaRapporteresPaaNytt();
	}

	@Test
	public void oppdaterValgnattRapport_detFinnesIkkeValgnattrapportForOppgjoret_avslutter() throws Exception {
		OppgjorEndrerStatusDomainService oppgjorEndrerStatusDomainService = initializeMocks(OppgjorEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		when(getInjectMock(ValgnattrapportRepository.class).byContestAndReportType(any(Contest.class), eq(ReportType.VALGOPPGJOR)))
				.thenReturn(null);

		oppgjorEndrerStatusDomainService.oppdaterValgnattRapport(new OppgjorEndrerStatus(AN_AREA_PATH, AN_ELECTION_PATH));

		verify(mockValgnattrapport, never()).maaRapporteresPaaNytt();
	}

	@Test
	public void oppdaterValgnattRapport_oppgjorBlirSlettetForHeleLandet_valgnattrapportMaaRapporteresPaaNytt() throws Exception {
		OppgjorEndrerStatusDomainService oppgjorEndrerStatusDomainService = initializeMocks(OppgjorEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		MvArea mockArea = createMock(MvArea.class);
		when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(new ArrayList<>());
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class)))
				.thenReturn(mockArea);
		when(mockArea.getActualAreaLevel()).thenReturn(AreaLevelEnum.COUNTRY);
		when(getInjectMock(ValgnattrapportRepository.class).finnFor(any(Election.class), eq(ReportType.VALGOPPGJOR)))
				.thenReturn(Collections.singletonList(mockValgnattrapport));
		when(mockValgnattrapport.isOk()).thenReturn(true);

		oppgjorEndrerStatusDomainService.oppdaterValgnattRapport(new OppgjorEndrerStatus(AN_AREA_PATH, AN_ELECTION_PATH));

		verify(mockValgnattrapport).maaRapporteresPaaNytt();
	}

	@Test
	public void oppdaterValgnattRapport_oppgjorBlirSlettetForFylkeIKommunestyrevalg_valgnattrapportMaaRapporteresPaaNytt() throws Exception {
		OppgjorEndrerStatusDomainService oppgjorEndrerStatusDomainService = initializeMocks(OppgjorEndrerStatusDomainService.class);

		Valgnattrapport mockValgnattrapport = createMock(Valgnattrapport.class);
		MvArea mockArea = createMock(MvArea.class);
		MvElection mockElection = createMock(MvElection.class);
		when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(new ArrayList<>());
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class)))
				.thenReturn(mockArea);
		when(mockArea.getActualAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getInjectMock(MvAreaRepository.class).findByPathAndChildLevel(any(AreaPath.class)))
				.thenReturn(Collections.singletonList(mockArea));
		when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class)))
				.thenReturn(Collections.singletonList(mockElection));
		when(getInjectMock(ValgnattrapportRepository.class).byContestAndReportType(any(Contest.class), eq(ReportType.VALGOPPGJOR)))
				.thenReturn(Collections.singletonList(mockValgnattrapport));
		when(mockValgnattrapport.isOk()).thenReturn(true);

		oppgjorEndrerStatusDomainService.oppdaterValgnattRapport(new OppgjorEndrerStatus(AN_AREA_PATH, AN_ELECTION_PATH));

		verify(mockValgnattrapport).maaRapporteresPaaNytt();
	}
}
