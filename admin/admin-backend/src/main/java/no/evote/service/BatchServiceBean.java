package no.evote.service;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.evote.exception.ErrorCode.ERROR_CODE_0201_NO_DATA;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VOTER_NUMBER;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.exception.EvoteSecurityException;
import no.evote.model.Batch;
import no.evote.model.BatchStatus;
import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.evote.service.counting.CountingImportServiceBean;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class BatchServiceBean {
	private static final Logger LOGGER = Logger.getLogger(BatchServiceBean.class);
	@Inject
	private CountingImportServiceBean countingImportService;
	@Inject
	private BinaryDataServiceBean binaryDataService;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private BatchRepository batchRepository;
	@Inject
	private BakgrunnsjobbDomainService bakgrunnsjobbService;

	public BatchServiceBean() {

	}

	public Batch saveFile(UserData userData, byte[] file, String fileName, Jobbkategori category) {
		if (file == null || file.length == 0) {
			throw new EvoteException(ERROR_CODE_0201_NO_DATA, null);
		}

		Batch batch = null;
		File zipFile = null;
		try {
			try {
				// Validate and make sure it's what we're expecting
				if (category == COUNT_UPLOAD) {
					zipFile = IOUtil.makeFile(file, "transfer.zip");
					countingImportService.validateCountEmlZip(userData, zipFile);
				}
				batch = makeBatch(userData, file, fileName, category);
			} catch (EvoteException e) {
				throw new EvoteNoRollbackException(e);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new EvoteNoRollbackException(ErrorCode.ERROR_CODE_0203_UNEXPECTED_BATCH_ERROR, e);
			}
		} finally {
			IOUtil.deleteContainingFolder(zipFile);
		}

		return batch;
	}

	/*
	 * Makes a batch job of a file
	 */
	private Batch makeBatch(UserData userData, byte[] bytes, String fileName, Jobbkategori category) throws IOException {
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());

		Batch batch = new Batch();
		batch.setOperatorRole(userData.getOperatorRole());
		batch.setElectionEvent(electionEvent);
		batch.setCategory(category);
		batch.setBatchStatus(getBatchStatusByID(EvoteConstants.BATCH_STATUS_IN_QUEUE_ID));
		batch = createBatch(userData, batch);

		BinaryData binaryData = binaryDataService.createBinaryData(userData, bytes, fileName, electionEvent, "batch", "batch_binary_data_pk", null);
		batch.setBinaryData(binaryData);

		return batchRepository.update(userData, batch);
	}

	public void importFile(UserData userData, int id, Long electionEventPk, Jobbkategori category) {
		Batch batch = batchRepository.findUniqueBatch(id, electionEventPk, category);

		LOGGER.info("importFile " + category);
		byte[] data = batch.getBinaryData().getBinaryData();
		File zipFile = null;
		try {
			try {
				if (category == COUNT_UPLOAD) {
					zipFile = IOUtil.makeFile(data, "transfer.zip");
					countingImportService.importCountEmlZip(userData, zipFile);
					batch.setBatchStatus(getBatchStatusByID(EvoteConstants.BATCH_STATUS_COMPLETED_ID));
				}
			} catch (EvoteSecurityException e) {
				LOGGER.error(e.getMessage(), e);
				batch.setBatchStatus(getBatchStatusByID(EvoteConstants.BATCH_STATUS_FAILED_ID));
				batch.setMessageText(e.getMessage());
				throw e;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				batch.setBatchStatus(getBatchStatusByID(EvoteConstants.BATCH_STATUS_FAILED_ID));
				batch.setMessageText(e.getMessage());
				if (e instanceof EvoteException) {
					throw new EvoteNoRollbackException((EvoteException) e);
				}
				throw new EvoteNoRollbackException(ErrorCode.ERROR_CODE_0203_UNEXPECTED_BATCH_ERROR, e);
			} finally {
				updateBatch(userData, batch);
			}
		} finally {
			IOUtil.deleteContainingFolder(zipFile);
		}
	}

	public int checkStatus(long batchPk) {
		Batch batch = batchRepository.findByPk(batchPk);
		if (batch == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0204_UNABLE_TO_FIND_BATCH_WITH_PK, null, batchPk);
		}
		return batch.getBatchStatus().getId();
	}

	public byte[] getBinaryDataFromBatch(Long batchPk) {
		byte[] bytes = null;

		Batch emlBatch = batchRepository.findByPk(batchPk);
		if (emlBatch != null) {
			bytes = emlBatch.getBinaryData().getBinaryData();
		}

		return bytes;
	}

	public Batch createBatch(UserData userData, Batch batch) {
		return batchRepository.create(userData, batch);
	}

	public Batch updateBatch(UserData userData, Batch batch) {
		return batchRepository.update(userData, batch);
	}

	public BatchStatus getBatchStatusByID(int id) {
		return batchRepository.findBatchStatusById(id);
	}

	public Batch createBatch(UserData userData, Jobbkategori category, Integer runNumber, String fileName) {
		String infoText = (runNumber != null) ? String.valueOf(runNumber.intValue()) : null;
		return bakgrunnsjobbService.lagBakgrunnsjobb(userData, category, BATCH_STATUS_STARTED_ID, infoText, fileName);
	}

	public Batch createBatchForGenerateVoterNumber(UserData userData) {
		if (bakgrunnsjobbService.erManntallsnummergenereringStartetEllerFullfort(userData.electionEvent())) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0205_GENERERING_MANNTALLSNUMRE_ALLEREDE_STARTET_ELLER_FULLFORT, null);
		} else {
			return bakgrunnsjobbService.lagBakgrunnsjobb(userData, VOTER_NUMBER, BATCH_STATUS_STARTED_ID, null, null);
		}
	}

	public Batch createBatchForDeleteVoters(UserData userData, Jobbkategori category, String mvAreaString) {
		return bakgrunnsjobbService.lagBakgrunnsjobb(userData, category, BATCH_STATUS_STARTED_ID, null, mvAreaString);
	}

	public List<Batch> listBatchesByEventAndCategory(Jobbkategori category, String electionEventId) {
		return batchRepository.findByElectionEventIdAndCategory(electionEventId, category);
	}
}
