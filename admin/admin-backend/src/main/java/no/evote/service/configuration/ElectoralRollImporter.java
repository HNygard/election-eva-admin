package no.evote.service.configuration;

import lombok.extern.log4j.Log4j;
import no.evote.security.UserData;
import no.evote.service.BatchServiceBean;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.ElectoralRollAuditEvent;
import no.valg.eva.admin.configuration.application.VoterImportBatchServiceLocalEjb;
import no.valg.eva.admin.configuration.domain.event.ManntallsimportFullfortEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.util.ImportElectoralRollUtil;
import no.valg.eva.admin.voting.domain.electoralroll.EligibleVoterDomainService;
import no.valg.eva.admin.voting.domain.electoralroll.IllegalVoterRecord;
import no.valg.eva.admin.voting.domain.electoralroll.VoterRecord;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.SessionContext;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;

@Log4j
public class ElectoralRollImporter {

	/** For chunking voter updates in several transactions */
	static final int MAX_NO_OF_VOTERS_PER_TRANSACTION = 20000;

	@Inject
	BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
	@Inject
	AuditLogServiceBean auditLogService;
	@Inject
	private Event<ManntallsimportFullfortEvent> manntallsimportFullfortEvent;
	@Inject
	protected VoterRepository voterRepository;
	@Inject
	protected MvAreaRepository mvAreaRepository;
	@Inject
	VoterImportBatchServiceLocalEjb voterImportBatchServiceLocalEjb;
	@Inject
	BatchServiceBean batchService;
	@PersistenceContext(unitName = "evotePU")
	EntityManager em;
	@Inject
	EligibleVoterDomainService eligibleVoterDomainService;

	boolean doesFileExist(String filePath) {
        if (!StringUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return file.isFile();
        }
        return false;
    }

    boolean isFileInitialBatchFile(String filePath) {
        return !StringUtils.isEmpty(filePath) && ImportElectoralRollUtil.isFileInitialBatchFile(filePath);
    }

	void auditLogFileExecution(UserData userData, String fileName, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		ElectoralRollAuditEvent auditEvent = new ElectoralRollAuditEvent(userData, fileName, auditEventType, outcome, detail);
		auditLogService.addToAuditTrail(auditEvent);
	}

	void auditLogFailedFileImport(UserData userData, String fileName, String detail) {
		auditLogFileExecution(userData, fileName, AuditEventTypes.ElectoralRollImportCompleted, Outcome.GenericError, detail);
	}

	void logFilteredOutVoterInfo(VoterRecord skdVoterRecord) {
		log.info("Filtrerte bort " + skdVoterRecord.foedselsnr() + " i kommune " + skdVoterRecord.kommunenr() + ", krets " + skdVoterRecord.valgkrets());
	}

	void commitAuditLogEntries(ImportElectoralRollRun run) {
		for (AuditEvent auditEvent : run.getQueuedAuditEvents()) {
			auditLogService.addToAuditTrail(auditEvent);
		}
		run.getQueuedAuditEvents().clear();
	}

    void finalizeImport(ImportElectoralRollRun run, boolean success, SessionContext context) {
        if (run != null && run.getSkdVoterFileParser() != null) {
            run.getSkdVoterFileParser().release();
        }
        if (success) {
            if (run != null && run.getBatch() != null) {
                bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(run.getUserData(), run.getBatch(), BATCH_STATUS_COMPLETED_ID);
            }
        } else {
            if (run != null && run.getBatch() != null) {
                bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(run.getUserData(), run.getBatch(), BATCH_STATUS_FAILED_ID);
            }
            context.setRollbackOnly();
        }
    }

    void handleRecordLengthError(ImportElectoralRollRun run, IllegalVoterRecord e) {
		int wrongRecord = run.getSkdVoterFileParser().getRowNumber() + 1;
		
		run.getBatch().setMessageText(run.getBatch().getMessageText() + ": Wrong record length for record " + wrongRecord);
		throw e;
	}

	void buildAndVerifyVoterImportBatch(ImportElectoralRollRun run) throws IOException {
	    run.copyDataToVoterImportBatch();
		log.info("numberOfRecordsTilgang=" + run.getNumberOfRecordsTilgang()
				+ ", numberOfRecordsEndring=" + run.getNumberOfRecordsEndring()
				+ ", numberOfRecordsAvgang=" + run.getNumberOfRecordsAvgang()
				+ ", numberOfRecordsSkip=" + run.getNumberOfRecordsSkip());
		if (run.getVoterImportBatch().getLastImportRecordsTotal() != run.getNumberOfRowsSKD()) {
			run.getBatch().setMessageText(String.format("%s: Mismatch between total number of rows from SKD (%d) and total number of rows processed (%d)",
                    run.getBatch().getMessageText(), run.getNumberOfRowsSKD(), run.getVoterImportBatch().getLastImportRecordsTotal()));
			throw new IOException(String.format("Mismatch between total number of rows from SKD: %d. And total number of rows processed: %d",
                    run.getNumberOfRowsSKD(), run.getVoterImportBatch().getLastImportRecordsTotal()));
		}
	}

	void auditLogSuccessfulFileExecution(ImportElectoralRollRun run) {
		auditLogFileExecution(run.getUserData(), run.getFilePath(), AuditEventTypes.ElectoralRollImportCompleted, Outcome.Success, null);
	}

    
	void sendManntallsimportFullfortEvent(UserData userData, ElectionEvent electionEvent) {
        manntallsimportFullfortEvent.fire(new ManntallsimportFullfortEvent(userData, electionEvent));
    }
}
