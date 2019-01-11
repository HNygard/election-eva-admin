package no.evote.service.configuration;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.VoterAuditEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.domain.service.VelgerDomainService;
import no.valg.eva.admin.util.ImportElectoralRollUtil;
import no.valg.eva.admin.voting.domain.electoralroll.IllegalVoterRecord;
import no.valg.eva.admin.voting.domain.electoralroll.StemmerettIKommune;
import no.valg.eva.admin.voting.domain.electoralroll.VoterRecord;
import no.valg.eva.admin.voting.domain.service.VotingRegistrationDomainService;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.ejb.SessionContext;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL;
import static no.valg.eva.admin.util.IOUtil.deleteDirectory;
import static no.valg.eva.admin.voting.domain.electoralroll.ImportElectoralRollType.INCREMENTAL;

/**
 * Tjeneste for ajourhold av manntall / "inkrementell" manntallsimport.
 * 
 * DEV-NOTES:
 * - Denne tjenesten er ikke riktig plassert og navngitt DDD-messig. Her trengs det en opprydning
 * - Merk at denne imorten ikke nevner voterNumber eller manntallsnummer. Grunnen til dette er at logikken for dette p.t. 
 *   ligger i databasen. Se funksjonene voter_insert() og voter_update() for å se hvordan dette fungerer.
 * - Koden her var opprinnelig svært rotete, men har blitt ryddet en del i. Det er nok fortsatt stort poensiale for opprydning
 * - DDD: Koden her er i stor grad en "mapper" som sørger for integrasjon mot SKDs format. Det kan dermed diskuteres om
 *   denne koden i ganske stor grad ikke hører hjemme i domene-laget, men i et ingegrasjons/ports/adapters-lag.
 */
@Log4j
public class IncrementalElectoralRollImporter extends ElectoralRollImporter {

	@Inject
	private VotingRepository votingRepository;
	@Inject
    private VotingRegistrationDomainService votingRegistrationDomainService;
    @Inject
    private VelgerDomainService velgerDomainService;

    /**
	 * Tjeneste for å utføre "schedulert"/inkrementell manntallsimport. 
     * Den inkrementelle manntallsimporten inneholder avganger/tilganger/endringer som brukes til å oppdatere det eksisterende manntallet i systemet.
	 */
	public void incrementalImportElectoralRoll(UserData userData, ElectionEvent electionEvent, SessionContext context) {
		try {
			log.info(format("Incremental electoral roll import started for '%s'", electionEvent.getName()));
            prepareAndRunIncrementalImport(userData, electionEvent, context);
            log.info("Incremental electoral roll import ended for '" + electionEvent.getName() + "'");
		} catch (RuntimeException e) {
			// Det skal ikke forekomme at exceptions kastes tilbake til den eksekverende jobben, siden det gjør at jobben blir fjernet fra applikasjonsserveren
			log.error(format("Incremental electoral roll import ended unexpectedly with message '%s'", e.getMessage()), e);
			context.setRollbackOnly();
		}
	}

    private void prepareAndRunIncrementalImport(UserData userData, ElectionEvent electionEvent, SessionContext context) {
        List<File> importFiles = getFilesForIncrementalImport(electionEvent);

        for (File file : importFiles) {
            if (!runIncrementalImportOfFile(userData, electionEvent, context, file)) {
                break; // Ikke nødvendig å loope gjennom flere filer når noe har gått feil
            }
        }
    }

    private List<File> getFilesForIncrementalImport(ElectionEvent electionEvent) {
        if (StringUtils.isEmpty(electionEvent.getVoterImportDirName())) {
            log.warn("No folder for import files is specified");
            return emptyList();
        }

        File folderPath = new File(electionEvent.getVoterImportDirName());
        if (!folderPath.isDirectory()) {
            log.warn("Folder for import files does not exist");
            return emptyList();
        }

        List<File> importFiles = ImportElectoralRollUtil.dirListByAscendingFileName(folderPath);

        log.info(format("Number of files to import: '%d'", importFiles.size()));
        return importFiles;
    }

    private boolean runIncrementalImportOfFile(UserData userData, ElectionEvent electionEvent, SessionContext context, File file) {
        File temporaryProcessingFolder = null;
        ImportElectoralRollRun run = null;
        try {
            validateFilename(file);
            temporaryProcessingFolder = makeTmpFolder(electionEvent);
            File tmpFileDest = moveFileToTemporaryFolder(temporaryProcessingFolder, file);
            auditLogFileExecution(userData, tmpFileDest.getName(), AuditEventTypes.IncrementalElectoralImportStarted, Outcome.Success, null);
            run = prepareScheduledImportFileRun(electionEvent, tmpFileDest, userData);
            validateBatchNumber(run);
            doScheduledImport(run);
            endScheduledImport(run);
            auditLogSuccessfulFileExecution(run);
            sendManntallsimportFullfortEvent(userData, electionEvent);
            finalizeImport(run, true, context);
            deleteDirectory(temporaryProcessingFolder); // The files viewed by admin are already copies, so the files are actually archived outside admin.
            return true;
        } catch (Exception e) {
            log.error(format("Import of incremental electoral roll file %s failed with message %s in election event %s",
                                file.getName(), e.getMessage(), electionEvent.getId()), e);
            lagreBakgrunnsjobbSomFeilet(userData, file.getName(), e.getMessage());
            auditLogFailedFileImport(userData, file.getName(), e.getMessage());
            finalizeImport(run, false, context);
            deleteDirectory(temporaryProcessingFolder); // The files viewed by admin are already copies, so the files are actually archived outside admin.
            return false;
        }
    }

    private void validateFilename(File file) {
        if (!file.getName().matches("^\\w.*")) {
            throw new IllegalArgumentException(format("File name %s is not alpha numeric", file.getName()));
        }
    }

    private File makeTmpFolder(ElectionEvent electionEvent) throws IOException {
        String tmpFolderName = String.format("%s/history_%s/", electionEvent.getVoterImportDirName(), electionEvent.getId());
        File tmpFolder = new File(tmpFolderName);
        if (tmpFolder.mkdir()) {
            return tmpFolder;
        } else {
            throw new IOException(format("Failed to create temporary folder '%s'", tmpFolder.getAbsolutePath()));
        }
    }

    private File moveFileToTemporaryFolder(File tmpFolder, File file) throws IOException {
        File tmpFileDest = new File(tmpFolder, file.getName());
        boolean fileSuccessfullyMoved = file.renameTo(tmpFileDest);
        if (!fileSuccessfullyMoved) {
            throw new IOException(format("Could not move file %s to %s. Electoral roll import cannot continue", file.getAbsolutePath(), tmpFileDest.getAbsolutePath()));
        }
        return tmpFileDest;
    }

    private ImportElectoralRollRun prepareScheduledImportFileRun(ElectionEvent electionEvent, File tmpFileDest, UserData userData) throws IOException {
        VoterImportBatch lastVoterImportBatch = voterImportBatchServiceLocalEjb.findSingleByElectionEvent(electionEvent);
        String fileName = tmpFileDest.getAbsolutePath().replace('\\', '/');
        return new ImportElectoralRollRun(userData, electionEvent, INCREMENTAL, fileName, lastVoterImportBatch, MAX_NO_OF_VOTERS_PER_TRANSACTION, mvAreaRepository);
    }

    private void validateBatchNumber(ImportElectoralRollRun run) {
        if (run.getBatchNumberSKD() <= run.getVoterImportBatch().getLastImportBatchNumber()) {
            String errorMessage = format("Wrong batch number: '%d'. Was expecting bigger than: %d", run.getBatchNumberSKD(),
                    run.getVoterImportBatch().getLastImportBatchNumber());
            throw new EvoteException(errorMessage);
        }
    }

    private void doScheduledImport(ImportElectoralRollRun run) throws ParseException {
        log.info("Starting import of " + run.getFilePath());
        run.setBatch(batchService.createBatch(run.getUserData(), ELECTORAL_ROLL, run.getBatchNumberSKD(), run.getFilePath()));
        StemmerettIKommune stemmerettIKommune = eligibleVoterDomainService.buildEligibilityMap(run.getElectionEvent());
        try {
            for (VoterRecord skdVoterRecord : run.getSkdVoterFileParser()) {
                if (stemmerettIKommune.forVoterRecord(skdVoterRecord)) {
                    processVoterForIncrementalImport(run, skdVoterRecord);
                } else {
                    logNonEligibleVoterForIncrementalImport(run, skdVoterRecord);
                }
            }
        } catch (IllegalVoterRecord e) {
            handleRecordLengthError(run, e);
        }
    }

    private void processVoterForIncrementalImport(ImportElectoralRollRun run, VoterRecord skdVoterRecord) throws ParseException {
        if (!skdVoterRecord.isInitialEntry()) {
            handleEndringsType(run, skdVoterRecord);
        } else {
            run.incrementNumberOfRecordsSkip();
        }
    }

    private void handleEndringsType(ImportElectoralRollRun run, VoterRecord skdVoterRecord) throws ParseException {
        Voter voter = new Voter();
        boolean voterExists = false;
        List<Voter> aVoterList = voterRepository.findByElectionEventAndId(run.getElectionEvent().getPk(), skdVoterRecord.foedselsnr());
        if (!aVoterList.isEmpty()) {
            voter = aVoterList.get(0);
            voterExists = true;
            // Refreshing the state of voter to update the hibernate cache. #3754
            em.refresh(voter);
        }

        boolean voterUpdated = false;
        Timestamp timeStampSKD = skdVoterRecord.timestampAsTimestamp();
        LegacyPollingDistrict legacyPollingDistrict = run.getVoterConverter().fromVoterRecord(skdVoterRecord, voter);
        if (skdVoterRecord.isEndringstypeTilgang()) {
            if (checkLogAndUpdateVoterIfRecordIsValid(run, voter, timeStampSKD, skdVoterRecord, voterExists)) {
                run.incrementNumberOfRecordsTilgang();
                velgerDomainService.updateVoter(run.getUserData(), voter, legacyPollingDistrict);
                voterUpdated = true;
            }
        } else if (skdVoterRecord.isEndringstypeEndring()) {
            if (checkLogAndUpdateVoterIfRecordIsValid(run, voter, timeStampSKD, skdVoterRecord, voterExists)) {
                run.incrementNumberOfRecordsEndring();
                velgerDomainService.updateVoter(run.getUserData(), voter, legacyPollingDistrict);
                voterUpdated = true;
            }
        } else if (skdVoterRecord.isEndringstypeAvgang()) {
            if (checkLogAndUpdateVoterIfRecordIsValid(run, voter, timeStampSKD, skdVoterRecord, voterExists)) {
                run.incrementNumberOfRecordsAvgang();
                voter.setEligible(false);
                voter.setApproved(false);
                velgerDomainService.updateVoter(run.getUserData(), voter);
                voterUpdated = true;
            }
        } else {
            run.incrementNumberOfRecordsSkip();
        }
        
        if(voterUpdated){
            votingRegistrationDomainService.voterUpdated(run.getUserData(), voter);
        }
    }

    private boolean checkLogAndUpdateVoterIfRecordIsValid(ImportElectoralRollRun run, Voter voter, Timestamp timeStampSKD,
                                                          VoterRecord skdVoterRecord, boolean voterExists) throws ParseException {
        if (voterExists) {
            if (voterEntryHasBeenProcessed(voter, timeStampSKD)) {
                run.incrementNumberOfRecordsSkip();
                return false;
            } else {
                if (hasApprovedVoting(voter)) {
                    auditLogDirectly(run.getUserData(), voter, AuditEventTypes.EntrySkipped, Outcome.Success, "Voter has already voted. Electoral roll update skipped");
                    run.incrementNumberOfRecordsSkip();
                    return false;
                }
            }
        }
        populateVoterWithAuditLogging(run, skdVoterRecord, voter);
        return true;
    }

    private boolean voterEntryHasBeenProcessed(Voter voter, Timestamp timeStampSKD) {
        return !(timeStampSKD != null && timeStampSKD.after(voter.getDateTimeSubmitted()));
    }

    private boolean hasApprovedVoting(Voter voter) {
        return votingRepository.hasApprovedVoting(voter);
    }

    private void auditLogDirectly(UserData userData, Voter voter, AuditEventTypes auditEventType, Outcome outcome, String detail) {
        VoterAuditEvent auditEvent = new VoterAuditEvent(userData, voter, auditEventType, outcome, detail);
        auditLogService.addToAuditTrail(auditEvent);
    }

    private void populateVoterWithAuditLogging(ImportElectoralRollRun run, VoterRecord skdVoterRecord, Voter voter) throws ParseException {
        run.getVoterConverter().populateFromVoterRecord(voter, skdVoterRecord);
        auditLogDirectly(run.getUserData(), voter, AuditEventTypes.Update, Outcome.Success, null);
    }

    private void endScheduledImport(ImportElectoralRollRun run) throws IOException {
        run.setTimeEnded(DateTime.now());
        run.setVoterImportBatch(voterImportBatchServiceLocalEjb.findSingleByElectionEvent(run.getElectionEvent()));
        buildAndVerifyVoterImportBatch(run);
        voterImportBatchServiceLocalEjb.update(run.getUserData(), run.getVoterImportBatch());
        run.getSkdVoterFileParser().release();
        bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(run.getUserData(), run.getBatch(), BATCH_STATUS_COMPLETED_ID);
        commitAuditLogEntries(run);
    }

	private void lagreBakgrunnsjobbSomFeilet(UserData userData, String filnavn, String melding) {
		String meldingsTekst = filnavn + ":" + melding;
		bakgrunnsjobbDomainService.lagBakgrunnsjobb(userData, ELECTORAL_ROLL, BATCH_STATUS_FAILED_ID, null, meldingsTekst);
	}

	private void logNonEligibleVoterForIncrementalImport(ImportElectoralRollRun run, VoterRecord skdVoterRecord) throws ParseException {
		run.incrementNumberOfRecordsSkip();
		if (skdVoterRecord.isElectoralRollChange()) {
			logFilteredOutVoterInfo(skdVoterRecord);
			Voter voter = run.getVoterConverter().fromVoterRecord(skdVoterRecord);
			auditLogDirectly(run.getUserData(), voter, AuditEventTypes.EntrySkipped, Outcome.Success, "Voter is skipped because it is not eligible");
		}
	}
}
