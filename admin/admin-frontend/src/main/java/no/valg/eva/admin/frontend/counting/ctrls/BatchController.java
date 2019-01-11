package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.UPLOAD_BATCHES;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.model.Batch;
import no.evote.model.BinaryData;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.evote.service.BinaryDataService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.google.common.io.ByteStreams;

@Named
@ViewScoped
public class BatchController extends BaseController {

	private static final Logger LOGGER = Logger.getLogger(BatchController.class);

	@Inject
	private BatchService batchService;
	@Inject
	private BinaryDataService binaryDataService;
	@Inject
	private UserData userData;
	@Inject
	private MessageProvider messageProvider;

	private List<Batch> batches;
	private UploadedFile uploadedFile;

	private String cid;

	@PostConstruct
	public void init() {
		updateBatches();
	}

	/**
	 * handles file upload, creates new batch job and updates gui
	 */
	public void handleFileUpload() {
		uploadFile(getUploadedFile());
	}

	private void uploadFile(UploadedFile uploadedFile) {
		if (uploadedFile == null) {
			MessageUtil.buildDetailMessage(messageProvider.get("@listProposal.candidateList.couldNotRead"), FacesMessage.SEVERITY_ERROR);
			return;
		}
		if (uploadedFile.getSize() < EvoteConstants.MAX_FILE_SIZE) {
			Batch batch;
			InputStream stream = null;
			try {
				stream = uploadedFile.getInputstream();
				batch = batchService.saveFile(userData, ByteStreams.toByteArray(stream), uploadedFile.getFileName(), COUNT_UPLOAD);
				if (batch != null) {
					batchService.importFile(userData, batch.getNumber(), userData.getElectionEventPk(), COUNT_UPLOAD);
					MessageUtil.buildDetailMessage(messageProvider.get("@count.success.upload"), FacesMessage.SEVERITY_INFO);
					updateBatches();
				} else {
					MessageUtil.buildDetailMessage(messageProvider.get("@count.error.upload.io"), FacesMessage.SEVERITY_ERROR);
				}
				getUploadBatchesDialog().closeAndUpdate("batchForm");
			} catch (EvoteException e) {
				buildFacesMessageAndLogException(e);
			} catch (EvoteNoRollbackException e) {
				buildFacesMessageAndLogException(e);
			} catch (Exception e) {
				String message = messageProvider.get("@count.error.upload.io") + ": " + e.getMessage();
				LOGGER.error(message, e);
				MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_ERROR);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		} else {
			MessageUtil.buildDetailMessage(messageProvider.get("@common.message.error.file_size"), FacesMessage.SEVERITY_ERROR);
		}
		setUploadedFile(null);
	}

	private void buildFacesMessageAndLogException(EvoteException e) {
		ErrorCode errorCode = e.getErrorCode();
		String[] params = e.getParams() != null ? e.getParams() : new String[0];
		String exceptionMessage = e.getMessage();

		buildFacesMessageAndLogException(errorCode, params, exceptionMessage);
	}

	private void buildFacesMessageAndLogException(EvoteNoRollbackException e) {
		ErrorCode errorCode = e.getErrorCode();
		String[] params = e.getParams() != null ? e.getParams() : new String[0];
		String exceptionMessage = e.getMessage();

		buildFacesMessageAndLogException(errorCode, params, exceptionMessage);
	}

	private void buildFacesMessageAndLogException(ErrorCode errorCode, String[] params, String exceptionMessage) {
		String message = messageProvider.get("@count.error.upload.io") + ": ";
		if (errorCode != null) {
			message += messageProvider.get(errorCode.formatMessage(params), params);
		} else {
			message += messageProvider.get(exceptionMessage, params);
		}

		// hvis dette er en teknisk feil back end så er den allerede logget som ERROR der
		LOGGER.info(message);

		MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_ERROR);
	}

	public void getFile(final Batch batch) {
		if (batch.getBinaryData() != null) {
			long pk = batch.getBinaryData().getPk();
			BinaryData binaryData = binaryDataService.findByPk(userData, pk);
			try {
				FacesUtil.sendFile(binaryData);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public boolean readyForDownload(final Batch batch) {
		int batchStatus = batch.getBatchStatus().getId();
		return batch.getBinaryData() != null && batch.getBinaryData().getPk() != null
				&& (batchStatus == EvoteConstants.BATCH_STATUS_COMPLETED_ID || batchStatus == EvoteConstants.BATCH_STATUS_FAILED_ID);
	}

	public void updateBatches() {
		batches = batchService.listMyBatches(userData, COUNT_UPLOAD);
	}

	public void fileImport(FileUploadEvent fileUploadEvent) {
		uploadFile(fileUploadEvent.getFile());
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(final UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public List<Batch> getBatches() {
		return batches;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public Dialog getUploadBatchesDialog() {
		return UPLOAD_BATCHES;
	}
}
