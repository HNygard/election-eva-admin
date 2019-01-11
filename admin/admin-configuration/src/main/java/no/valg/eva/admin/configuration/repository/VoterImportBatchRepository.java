package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;

public class VoterImportBatchRepository extends BaseRepository {
	public VoterImportBatchRepository() {
	}

	public VoterImportBatchRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VoterImportBatch update(UserData userData, VoterImportBatch voterImportBatch) {
		return super.updateEntity(userData, voterImportBatch);
	}

	public VoterImportBatch findSingleByElectionEvent(Long electionEventPk) {
		List<VoterImportBatch> vib = findEntitiesByElectionEvent(VoterImportBatch.class, electionEventPk);
		if (!vib.isEmpty()) {
			return vib.get(0);
		} else {
			return null;
		}
	}

	public void delete(UserData userData, Long voterImportBatchPk) {
		super.deleteEntity(userData, VoterImportBatch.class, voterImportBatchPk);
	}
}
