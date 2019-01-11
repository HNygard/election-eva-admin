package no.valg.eva.admin.frontend.configuration.ctrls;

import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.dto.BatchInfoDto;
import no.evote.security.UserData;
import no.evote.service.ExportService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

/**
 * Base class for controllers exporting electoral roll and EML
 */
public abstract class BaseExportController extends BaseController {
	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;

	@Inject
	private ExportService exportService;

	protected UserData getUserData() {
		return userData;
	}

	protected UserDataController getUserDataController() {
		return userDataController;
	}

	protected ExportService getExportService() {
		return exportService;
	}

	public boolean readyForDownload(BatchInfoDto batch) {
		return batch.getStatus() == EvoteConstants.BATCH_STATUS_COMPLETED_ID;
	}

	public String getBatchStatusMessage(int batchStatus) {
		return BatchStatusMap.getBatchStatusMessage(batchStatus);
	}

	protected ElectionEvent getElectionEvent() {
		return userDataController.getElectionEvent();
	}
}
