package no.valg.eva.admin.configuration.application;

import no.evote.exception.EvoteException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.service.ManntallsnummerDomainService;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus.ALLEREDE_GENERERT;
import static no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus.INGEN_VELGERE;
import static no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus.OK;
import static no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus.SKJARINGSDATO_I_FREMTIDEN;
import static no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus.VALGHENDELSE_LAAST;
import static no.valg.eva.admin.configuration.application.ManntallsnummerApplicationService.GENERERING_FEILET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ManntallsnummerApplicationServiceTest extends MockUtilsTestCase {

	private static final long KORT_MANNTALLSNUMMER = 12345L;
	private static final long ELECTION_EVENT_PK = 123L;

	@Test 
	public void genererManntallsnummer_vellykketGjennomforing_lagerBakgrunnsjobbOgTriggerGenerering() throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		UserData stubUserData = stubUserData();
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringStartetEllerFullfort(any())).thenReturn(false);
		Batch stubBatch = stubBatch();
		when(getInjectMock(BakgrunnsjobbDomainService.class).lagBakgrunnsjobb(any(), any(), anyInt(), any(), any())).thenReturn(stubBatch);
				
		manntallsnummerApplicationService.genererManntallsnumre(stubUserData, ELECTION_EVENT_PK);

		verify(getInjectMock(VoterRepository.class)).genererManntallsnumre(ELECTION_EVENT_PK);		
		verify(getInjectMock(BakgrunnsjobbDomainService.class)).oppdaterBakgrunnsjobb(stubUserData, stubBatch, BATCH_STATUS_COMPLETED_ID);
	}

	private Batch stubBatch() {
		return mock(Batch.class);
	}

	private UserData stubUserData() {
		UserData stubUserData = getUserData();
		when(stubUserData.getElectionEventId()).thenReturn(AREA_PATH_ROOT.path());
		when(stubUserData.getElectionEventPk()).thenReturn(ELECTION_EVENT_PK);
		return stubUserData;
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = ".*Generering.*fullført.*")
	public void genererManntallsnummer_dersomManntallsnumreAlleredeErGenerert_feiler() throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		UserData stubUserData = stubUserData();
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringStartetEllerFullfort(any())).thenReturn(true);

		manntallsnummerApplicationService.genererManntallsnumre(stubUserData, ELECTION_EVENT_PK);
	}
	
	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = ".*generering.*feilet.*")
	public void genererManntallsnummer_dersomGenereringFeilerUnderveis_feiler() throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		UserData stubUserData = stubUserData();
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringStartetEllerFullfort(any())).thenReturn(false);
		Batch stubBatch = stubBatch();
		when(getInjectMock(BakgrunnsjobbDomainService.class).lagBakgrunnsjobb(any(), any(), anyInt(), any(), any())).thenReturn(stubBatch);
		when(getInjectMock(VoterRepository.class).genererManntallsnumre(ELECTION_EVENT_PK)).thenReturn(GENERERING_FEILET);

		try {
			manntallsnummerApplicationService.genererManntallsnumre(stubUserData, ELECTION_EVENT_PK);
		} catch (Exception e) {
			verify(getInjectMock(BakgrunnsjobbDomainService.class)).oppdaterBakgrunnsjobb(stubUserData, stubBatch, BATCH_STATUS_FAILED_ID);
			throw e;
		}
	}
	
	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = ".*feil oppstod.*generering.*")
	public void genererManntallsnummer_dersomGenereringTrynerUnderveis_feiler() throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		UserData stubUserData = stubUserData();
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringStartetEllerFullfort(any())).thenReturn(false);
		Batch stubBatch = stubBatch();
		when(getInjectMock(BakgrunnsjobbDomainService.class).lagBakgrunnsjobb(any(), any(), anyInt(), any(), any())).thenReturn(stubBatch);
		when(getInjectMock(VoterRepository.class).genererManntallsnumre(ELECTION_EVENT_PK)).thenThrow(new RuntimeException());

		try {
			manntallsnummerApplicationService.genererManntallsnumre(stubUserData, ELECTION_EVENT_PK);
		} catch (Exception e) {
			verify(getInjectMock(BakgrunnsjobbDomainService.class)).oppdaterBakgrunnsjobb(stubUserData, stubBatch, BATCH_STATUS_FAILED_ID);
			throw e;
		}
	}
	
	@Test(dataProvider = "valgaarssiffer")
	public void erValgaarssifferGyldig_gittEnBrukerOgEtManntallsNummer_sjekkerValgaarssifferet(boolean returnverdiFraDomenetjeneste,
																							   boolean forventetReturverdiFraApplikasjonstjeneste) throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		ManntallsnummerDomainService manntallsnummerDomainService = getInjectMock(ManntallsnummerDomainService.class);
		when(manntallsnummerDomainService.erValgaarssifferGyldig(any(), any())).thenReturn(returnverdiFraDomenetjeneste);
		
		boolean erValgaarssifferGyldig = manntallsnummerApplicationService.erValgaarssifferGyldig(getUserData(), gyldigManntallsnummer());

		assertThat(erValgaarssifferGyldig).isEqualTo(forventetReturverdiFraApplikasjonstjeneste);
	}

	@DataProvider
	private Object[][] valgaarssiffer() {
		return new Object[][]{
			{ true, true },
			{ false, false }
		};
	}

	private Manntallsnummer gyldigManntallsnummer() {
		return new Manntallsnummer("123456789080");
	}

	@Test
	public void beregnFulltManntallsnummer_gittEnBrukerOgEtManntallsNummer_sjekkerValgaarssifferet() throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		ManntallsnummerDomainService manntallsnummerDomainService = getInjectMock(ManntallsnummerDomainService.class);
		when(manntallsnummerDomainService.beregnFulltManntallsnummer(any(), any())).thenReturn(gyldigManntallsnummer());
		Manntallsnummer forventetResultat = gyldigManntallsnummer();
		Manntallsnummer fulltManntallsnummer = manntallsnummerApplicationService.beregnFulltManntallsnummer(getUserData(), KORT_MANNTALLSNUMMER);

		assertThat(fulltManntallsnummer.getManntallsnummer()).isEqualTo(forventetResultat.getManntallsnummer());
	}

	private UserData getUserData() {
		return mock(UserData.class);
	}

	@Test(dataProvider = "manntallsnummergenereringStatusTestData")
	public void hentManntallsnummergenereringStatus(ElectionEvent valghendelse, boolean erVelgereIValghendelse, boolean erAlleredeGenerert,
													ManntallsnummergenereringStatus forventetResultat) throws Exception {
		ManntallsnummerApplicationService manntallsnummerApplicationService = initializeMocks(ManntallsnummerApplicationService.class);
		when(getInjectMock(VoterRepository.class).areVotersInElectionEvent(anyLong())).thenReturn(erVelgereIValghendelse);
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringStartetEllerFullfort(any(ElectionEvent.class))).thenReturn(erAlleredeGenerert);

		assertThat(manntallsnummerApplicationService.hentManntallsnummergenereringStatus(createMock(UserData.class), valghendelse)).isSameAs(forventetResultat);
	}

	@DataProvider
	public Object[][] manntallsnummergenereringStatusTestData() {
		return new Object[][] {
			{ electionEvent(ElectionEventStatusEnum.CLOSED, localeDate(0)), false, false, VALGHENDELSE_LAAST},
			{ electionEvent(ElectionEventStatusEnum.CENTRAL_CONFIGURATION, localeDate(0)), false, false, INGEN_VELGERE},
			{ electionEvent(ElectionEventStatusEnum.CENTRAL_CONFIGURATION, localeDate(10)), true, false, SKJARINGSDATO_I_FREMTIDEN},
			{ electionEvent(ElectionEventStatusEnum.CENTRAL_CONFIGURATION, localeDate(0)), true, true, ALLEREDE_GENERERT},
			{ electionEvent(ElectionEventStatusEnum.CENTRAL_CONFIGURATION, localeDate(0)), true, false, OK }
		};
	}

	private ElectionEvent electionEvent(ElectionEventStatusEnum status, LocalDate cutOffDate) {
		ElectionEvent result = createMock(ElectionEvent.class);
		when(result.getElectionEventStatus().getId()).thenReturn(status.id());
		when(result.getElectoralRollCutOffDate()).thenReturn(cutOffDate);
		return result;
	}

	private LocalDate localeDate(int plusDays) {
		return LocalDate.now().plusDays(plusDays);
	}
}
