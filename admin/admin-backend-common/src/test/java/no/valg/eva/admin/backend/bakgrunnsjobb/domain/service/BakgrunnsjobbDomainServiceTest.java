package no.valg.eva.admin.backend.bakgrunnsjobb.domain.service;

import no.evote.constants.EvoteConstants;
import no.evote.model.Batch;
import no.evote.model.BatchStatus;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VALGKORTUNDERLAG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class BakgrunnsjobbDomainServiceTest extends MockUtilsTestCase {

	@Test(dataProvider = "bakgrunnsjobber")
	public void erManntallsnummergenereringStartetEllerFullfort(int jobbstatusId, boolean jobbStartet, boolean jobbFullfort,
																boolean jobbStartetEllerFullfort) throws Exception {
		BakgrunnsjobbDomainService bakgrunnsjobbDomainService = initializeMocks(BakgrunnsjobbDomainService.class);
		when(getInjectMock(BatchRepository.class).findByElectionEventIdAndCategory(any(), any()))
			.thenReturn(Collections.singletonList(bakgrunnsjobb(jobbstatusId, VALGKORTUNDERLAG)));
		ElectionEvent electionEvent = electionEvent();
		
		assertEquals(bakgrunnsjobbDomainService.erManntallsnummergenereringStartet(electionEvent), jobbStartet);
		assertEquals(bakgrunnsjobbDomainService.erManntallsnummergenereringFullfortUtenFeil(electionEvent), jobbFullfort);
		assertEquals(bakgrunnsjobbDomainService.erManntallsnummergenereringStartetEllerFullfort(electionEvent), jobbStartetEllerFullfort);
	}

	@DataProvider
	private Object[][] bakgrunnsjobber() {
		return new Object[][]{
			{ BATCH_STATUS_STARTED_ID, true, false, true },
			{ BATCH_STATUS_COMPLETED_ID, false, true, true },
			{ BATCH_STATUS_FAILED_ID, false, false, false }
		};
	}

	private Batch bakgrunnsjobb(int statusId, Jobbkategori jobbkategori) {
		Batch bakgrunnsjobb = new Batch();
		bakgrunnsjobb.setBatchStatus(bakgrunnsjobbStatus(statusId));
		bakgrunnsjobb.setOperatorRole(operatorRole());
		bakgrunnsjobb.setElectionEvent(electionEvent());
		bakgrunnsjobb.setCategory(jobbkategori);
		return bakgrunnsjobb;
	}

	private BatchStatus bakgrunnsjobbStatus(int statusId) {
		BatchStatus jobbstatus = new BatchStatus();
		jobbstatus.setId(statusId);
		return jobbstatus;
	}

	private OperatorRole operatorRole() {
		return new OperatorRole();
	}

	private ElectionEvent electionEvent() {
		return new ElectionEvent();
	}

	@Test
	public void lagBakgrunnsjobb_alltid_leggerTilEnJobbIDatabasen() throws Exception {
		BakgrunnsjobbDomainService bakgrunnsjobbDomainService = initializeMocks(BakgrunnsjobbDomainService.class);
		BatchRepository batchRepository = getInjectMock(BatchRepository.class);
		UserData userData = userData();
		Jobbkategori jobbkategori = VALGKORTUNDERLAG;
		int jobbStatusId = EvoteConstants.BATCH_STATUS_STARTED_ID;
		BatchStatus jobbStatus = bakgrunnsjobbStatus(jobbStatusId);
		when(batchRepository.findBatchStatusById(jobbStatusId)).thenReturn(jobbStatus);
		Batch forventetBakgrunnsjobb = bakgrunnsjobb(jobbStatusId, jobbkategori);
		
		bakgrunnsjobbDomainService.lagBakgrunnsjobb(userData, jobbkategori, jobbStatusId, null, null);

		verify(batchRepository).create(userData, forventetBakgrunnsjobb);
	}

	private UserData userData() {
		UserData userData = mock(UserData.class);
		when(userData.electionEvent()).thenReturn(electionEvent());
		when(userData.getOperatorRole()).thenReturn(operatorRole());
		return userData;
	}

	@Test
	public void oppdaterBakgrunnsjobb_oppdatererStatusForJobben() throws Exception {
		BakgrunnsjobbDomainService bakgrunnsjobbDomainService = initializeMocks(BakgrunnsjobbDomainService.class);
		BatchRepository batchRepository = getInjectMock(BatchRepository.class);

		UserData userData = userData();
		Batch bakgrunnsjobbFoerOppdatering = bakgrunnsjobb(BATCH_STATUS_STARTED_ID, VALGKORTUNDERLAG);
		Batch bakgrunnsjobbEtterOppdatering = bakgrunnsjobb(BATCH_STATUS_COMPLETED_ID, VALGKORTUNDERLAG);

		bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, bakgrunnsjobbFoerOppdatering, BATCH_STATUS_COMPLETED_ID);

		verify(batchRepository).update(eq(userData), eq(bakgrunnsjobbEtterOppdatering));
	}

}
