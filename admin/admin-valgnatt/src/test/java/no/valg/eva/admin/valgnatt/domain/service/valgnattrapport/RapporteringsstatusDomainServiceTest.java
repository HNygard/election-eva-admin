package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class RapporteringsstatusDomainServiceTest extends MockUtilsTestCase {

	@Test
	public void brukStatusForStemmeskjema_reportingAreaErKommune_true() throws Exception {
		RapporteringsstatusDomainService rapporteringsstatusDomainService = initializeMocks(RapporteringsstatusDomainService.class);
        assertThat(rapporteringsstatusDomainService.brukStatusForStemmeskjema(AREA_PATH_MUNICIPALITY)).isTrue();
	}

	@Test
	public void kanFylketRapportere_sjekkerSettlementRepo() throws Exception {
		RapporteringsstatusDomainService rapporteringsstatusDomainService = initializeMocks(RapporteringsstatusDomainService.class);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class)).getContest()).thenReturn(mock(Contest.class));
		
		rapporteringsstatusDomainService.kanFylketRapportere(mock(ElectionPath.class));
		
		verify(getInjectMock(SettlementRepository.class)).erValgoppgjørKjørt(any(Contest.class));
	}

	@Test
	public void brukStatusForOppgjorsskjema_fylkeOgValgoppgjorKjort_true() throws Exception {
		RapporteringsstatusDomainService rapporteringsstatusDomainService = initializeMocks(RapporteringsstatusDomainService.class);
		when(getInjectMock(SettlementRepository.class).erValgoppgjørKjørt(any(Contest.class))).thenReturn(true);
		
		assertThat(rapporteringsstatusDomainService.brukStatusForOppgjorsskjema(mock(ElectionPath.class), AREA_PATH_COUNTY)).isTrue();
	}

	@Test
    public void antallRapporterbareOppgjorsskjema() {
		assertThat(new RapporteringsstatusDomainService(null, null).antallRapporterbareOppgjorsskjema()).isEqualTo(1L);
	}

	@Test
	public void brukStatusForOppgjorOgStemmeskjema_kommuneSomGjorOppgjor_true() throws Exception {
		RapporteringsstatusDomainService rapporteringsstatusDomainService = initializeMocks(RapporteringsstatusDomainService.class);
		MvElection mvElectionMock = createMock(MvElection.class);
		Contest contestMock = createMock(Contest.class);

		when(mvElectionMock.getActualAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(mvElectionMock.getContest()).thenReturn(contestMock);
		when(contestMock.isSingleArea()).thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(mvElectionMock);

		assertThat(rapporteringsstatusDomainService.brukStatusforOppgjorOgStemmeskjema(mock(ElectionPath.class), AREA_PATH_MUNICIPALITY)).isTrue();
	}

	@Test
	public void brukStatusForOppgjorOgStemmeskjema_kommuneSomIkkeGjorOppgjor_false() throws Exception {
		RapporteringsstatusDomainService rapporteringsstatusDomainService = initializeMocks(RapporteringsstatusDomainService.class);
		MvElection mvElectionMock = createMock(MvElection.class);

		when(mvElectionMock.getActualAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(mvElectionMock);

		assertThat(rapporteringsstatusDomainService.brukStatusforOppgjorOgStemmeskjema(mock(ElectionPath.class), AREA_PATH_MUNICIPALITY)).isFalse();
	}

}
