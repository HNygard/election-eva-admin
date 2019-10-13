package no.valg.eva.admin.backend.bakgrunnsjobb.domain.service;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VOTER_NUMBER;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

/**
 * DEV-NOTE: Det kan diskuteres om denne klassen egentlig i sin helhet er en applikasjons- eller infrastrukturtjeneste
 *           Begrunnelsen for dette er at bakgrunnsjobber er en mekanisme som applikasjonen EVA Admin har nytte av, mens
 *           valgdomenet er rennende likegyldig til de tekniske aspektene for hvordan eksekveringen av tjenester kjøres.
 */
@Default
@ApplicationScoped
public class BakgrunnsjobbDomainService {

	@Inject
	private BatchRepository batchRepository;
	public BakgrunnsjobbDomainService() {

	}

	public BakgrunnsjobbDomainService(BatchRepository batchRepository) {
		this.batchRepository = batchRepository;
	}

	public boolean erManntallsnummergenereringStartet(ElectionEvent valghendelse) {
		return finnesJobb(valghendelse, BATCH_STATUS_STARTED_ID, VOTER_NUMBER);
	}

	private boolean finnesJobb(ElectionEvent valghendelse, int jobbStatus, Jobbkategori jobbkategori) {
		List<Batch> manntallsnummergenereringsjobber = batchRepository.findByElectionEventIdAndCategory(valghendelse.getId(), jobbkategori);
		for (Batch genereringsjobb : manntallsnummergenereringsjobber) {
			if (genereringsjobb.getBatchStatus().getId() == jobbStatus) {
				return true;
			}
		}
		return false;
	}

	public boolean erManntallsnummergenereringFullfortUtenFeil(ElectionEvent valghendelse) {
		return finnesJobb(valghendelse, BATCH_STATUS_COMPLETED_ID, VOTER_NUMBER);
	}

	public boolean erManntallsnummergenereringStartetEllerFullfort(ElectionEvent electionEvent) {
		return erManntallsnummergenereringStartet(electionEvent)
			|| erManntallsnummergenereringFullfortUtenFeil(electionEvent);
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Batch lagBakgrunnsjobb(UserData userData, Jobbkategori jobbkategori, int jobbstatusId, String infoTekst, String meldingsTekst) {
		Batch bakgrunnsjobb = new Batch();
		bakgrunnsjobb.setOperatorRole(userData.getOperatorRole());
		bakgrunnsjobb.setElectionEvent(userData.electionEvent());
		bakgrunnsjobb.setCategory(jobbkategori);
		bakgrunnsjobb.setInfoText(infoTekst);
		bakgrunnsjobb.setMessageText(meldingsTekst);
		bakgrunnsjobb.setBatchStatus(batchRepository.findBatchStatusById(jobbstatusId));

		return batchRepository.create(userData, bakgrunnsjobb);
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Batch oppdaterBakgrunnsjobb(UserData userData, Batch bakgrunnsjobb, int jobbstatusId) {
		bakgrunnsjobb.setBatchStatus(batchRepository.findBatchStatusById(jobbstatusId));
		return batchRepository.update(userData, bakgrunnsjobb);
	}

}
