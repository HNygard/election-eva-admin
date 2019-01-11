package no.evote.service;

import java.util.List;

import no.evote.dto.BatchInfoDto;
import no.evote.security.UserData;

public interface ExportService {
	/**
	 * Generating EML. Method returns upon completion.
	 */
	void generateEML(UserData userData, Long electionEventPk);

	/** Returns a list of EML batches for a specific election event */
	List<BatchInfoDto> getGeneratedEMLBatches(UserData userData, String electionEventId);

	/** Returns generated EML for a specific batch, null if it hasn't been generated. */
	byte[] getGeneratedEML(UserData userData, Long batchPk);

	boolean validateGeneratedEML(UserData userData, Long batchPk);
}
