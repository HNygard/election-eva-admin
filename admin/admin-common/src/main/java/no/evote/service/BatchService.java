package no.evote.service;

import java.io.Serializable;
import java.util.List;

import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

public interface BatchService extends Serializable {

	/**
	 * Returns the status id for a batch job. The meaning of a status id is defined in EvoteConstants
	 */
	int checkStatus(long batchPk);

	/**
	 * Makes a batch job of a file, if is a count eml zip it gets validated
	 */
	Batch saveFile(UserData userData, byte[] file, String fileName, Jobbkategori category);

	/**
	 * Get batch jobs added by the specified user
	 */
	List<Batch> listMyBatches(UserData userData, Jobbkategori category);

	List<Batch> listBatchesByEventAndCategory(UserData userData, Jobbkategori category, String electionEventId);

	/**
	 * Asynchronous import of a batch job
	 */
	void importFile(UserData userData, int id, Long electionEventPk, Jobbkategori category);

}
