package no.valg.eva.admin.configuration.application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;

@Default
@ApplicationScoped
public class VoterImportBatchServiceLocalEjb {

	@Inject
	private VoterImportBatchRepository voterImportBatchRepository;

	public VoterImportBatchServiceLocalEjb() {

	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public VoterImportBatch update(UserData userData, VoterImportBatch voterImportBatch) {
		return voterImportBatchRepository.update(userData, voterImportBatch);
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public VoterImportBatch findSingleByElectionEvent(ElectionEvent electionEvent) {
		return voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
	}

}
