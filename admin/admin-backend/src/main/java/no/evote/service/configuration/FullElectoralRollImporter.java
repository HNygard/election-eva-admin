package no.evote.service.configuration;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.validation.ValideringVedManuellRegistrering;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.VoterAuditEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.domain.service.VelgerDomainService;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.voting.domain.electoralroll.IllegalVoterRecord;
import no.valg.eva.admin.voting.domain.electoralroll.ImportElectoralRollType;
import no.valg.eva.admin.voting.domain.electoralroll.InvalidElectoralRollBatchNumber;
import no.valg.eva.admin.voting.domain.electoralroll.StemmerettIKommune;
import no.valg.eva.admin.voting.domain.electoralroll.VoterRecord;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.persistence.FlushModeType;
import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import static java.lang.String.format;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL;
import static no.valg.eva.admin.voting.domain.electoralroll.ImportElectoralRollType.FULL_FINAL;
import static no.valg.eva.admin.voting.domain.electoralroll.ImportElectoralRollType.FULL_PRELIMINARY;

/**
 * Tjeneste for import av midlertidige og endelige manntall fra SKD ("fulle" manntall)
 * 
 * DEV-NOTES:
 * - Merk at denne imorten ikke nevner voterNumber eller manntallsnummer. Grunnen til dette er at logikken for dette p.t. 
 *   ligger i databasen. Se funksjonene voter_insert() og voter_update() for å se hvordan dette fungerer.
 * - Koden her var opprinnelig svært rotete, men har blitt ryddet en del i. Det er nok fortsatt stort poensiale for opprydning
 * - DDD: Koden her er i stor grad en "mapper" som sørger for integrasjon mot SKDs format. Det kan dermed diskuteres om
 *   denne koden i ganske stor grad ikke hører hjemme i domene-laget, men i et ingegrasjons/ports/adapters-lag.
 */
public class FullElectoralRollImporter extends ElectoralRollImporter {
	private static final Logger LOGGER = Logger.getLogger(FullElectoralRollImporter.class);

	@Inject
	private VelgerDomainService velgerDomainService;
    @Inject
	private VoterImportBatchRepository voterImportBatchRepository;

    /**
	 * Main service method for performing full imports of electoral roll.
	 *
	 * Preliminary initial electoral roll import is performed monthly, after first deleting all existing electoral roll entries in the system The last full
	 * electoral roll import before the election is called "final", after first deleting all existing electoral roll entries in the system. The final import is
	 * audit-logged more rigorously than the preliminary imports
	 */
	public void fullImportElectoralRoll(UserData userData, ElectionEvent electionEvent, String filePath, SessionContext context, boolean isFinalImport) {
		validateImportFile(electionEvent, filePath);
		// Ytelsesfiks
		FlushModeType oldFlushMode = em.getFlushMode();
		em.setFlushMode(FlushModeType.COMMIT);

		boolean success = false;
		ImportElectoralRollRun run = null;
		try {
            run = prepareFullImport(userData, electionEvent, filePath, isFinalImport);
			doFullImport(run);

			success = true;
			recordCompletionOfImport(run);
			sendManntallsimportFullfortEvent(userData, electionEvent);
		} catch (Exception e) {
			recordFailureOfImport(userData, filePath, e);
		} finally {
			finalizeImport(run, success, context);
			em.setFlushMode(oldFlushMode);
		}
	}

    public void validateImportFile(ElectionEvent electionEvent, String filePath) {
        if (!doesFileExist(filePath)) {
            throw new EvoteException("@electoralRoll.importElectoralRoll.fileDoesNotExist");
        }
        if (!isFileInitialBatchFile(filePath)) {
            throw new EvoteException("@electoralRoll.importElectoralRoll.fileNotInitBatch");
        }
        VoterImportBatch aVoterImportBatch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
        if (aVoterImportBatch != null) {
            throw new EvoteException("@electoralRoll.importElectoralRoll.alreadyImported");
        }
        if (voterRepository.areVotersInElectionEvent(electionEvent.getPk())) {
            throw new EvoteException("@electoralRoll.importElectoralRoll.votersForElectionEvent");
        }
    }

    private ImportElectoralRollRun prepareFullImport(UserData userData, ElectionEvent electionEvent, String filePath, boolean isFinalImport) throws IOException {
        recordStartOfImport(userData, electionEvent, filePath);
        validateFileName(electionEvent, filePath);
        ImportElectoralRollType importType = isFinalImport ? FULL_FINAL : FULL_PRELIMINARY;
        ImportElectoralRollRun run = new ImportElectoralRollRun(userData, electionEvent, importType, filePath, new VoterImportBatch(),
                MAX_NO_OF_VOTERS_PER_TRANSACTION, mvAreaRepository);
        checkForInvalidBatchNumber(run);
        return run;
    }

    private void recordStartOfImport(UserData userData, ElectionEvent electionEvent, String filePath) {
		LOGGER.info(format("fullImportElectoralRoll started for '%s'", electionEvent.getName()));
		auditLogFileExecution(userData, filePath, AuditEventTypes.FullElectoralImportStarted, Outcome.Success, null);
	}

    private void validateFileName(ElectionEvent electionEvent, String filePath) throws IOException {
		if (!doesFileExist(filePath)) { // This check is also done in controller, so there's something wrong if this method is called
            String message = format("Electoral roll import file does not exist%nfullImportElectoralRoll ended for '%s'", electionEvent.getName());
			LOGGER.warn(message);
			throw new IOException(message);
		}
	}

	private void checkForInvalidBatchNumber(ImportElectoralRollRun run) {
		if (run.getBatchNumberSKD() != 0) { // This check also exists in controller
            String message = format("Wrong batch number: %d. Was expecting: 0%nfullImportElectoralRoll ended for '%s'", run.getBatchNumberSKD(),
                    run.getElectionEvent().getName());
			LOGGER.warn(message);
			auditLogFailedFileImport(run.getUserData(), run.getFilePath(), "Wrong batch number in file. Expected 0, but got " + run.getBatchNumberSKD());
			throw new InvalidElectoralRollBatchNumber(message);
		}
	}

	private void doFullImport(ImportElectoralRollRun run) throws ParseException {
		run.setBatch(batchService.createBatch(run.getUserData(), ELECTORAL_ROLL, run.getBatchNumberSKD(), run.getFilePath()));
		StemmerettIKommune stemmerettIKommune = eligibleVoterDomainService.buildEligibilityMap(run.getElectionEvent());
		try {
			for (VoterRecord skdVoterRecord : run.getSkdVoterFileParser()) {
				Voter voter = getVoter(skdVoterRecord, run);
				LegacyPollingDistrict legacyPollingDistrict = getLegacyPollingDistrict(skdVoterRecord, voter, run);
				if (!stemmerettIKommune.forVelger(voter)) {
					processNonEligibleVoterInFullImport(run, skdVoterRecord, voter);
				} else {
					processVoterInFullImport(run, skdVoterRecord, voter, legacyPollingDistrict);
				}
			}

			if (!run.getVoterList().isEmpty()) {
				commitVotersAndAuditLog(run);
			}
		} catch (IllegalVoterRecord e) {
			handleRecordLengthError(run, e);
		}
	}

	private Voter getVoter(VoterRecord skdVoterRecord, ImportElectoralRollRun run) throws ParseException {
        return run.getVoterConverter().fromVoterRecord(skdVoterRecord);
    }

	private LegacyPollingDistrict getLegacyPollingDistrict(VoterRecord skdVoterRecord, Voter voter, ImportElectoralRollRun run) {
		return run.getVoterConverter().fromVoterRecord(skdVoterRecord, voter);
	}

	private void processNonEligibleVoterInFullImport(ImportElectoralRollRun run, VoterRecord skdVoterRecord, Voter voter) {
		logFilteredOutVoterInfo(skdVoterRecord);
		if (run.isFinalImport()) {
			queueAuditLogEvent(run, voter, AuditEventTypes.EntrySkipped, Outcome.Success, "Voter is skipped because it is not eligible");
		}
		run.incrementNumberOfRecordsSkip();
	}

	private void queueAuditLogEvent(ImportElectoralRollRun run, Voter voter, AuditEventTypes auditEventType, Outcome outcome, String detail) {
        VoterAuditEvent voterAuditEvent = new VoterAuditEvent(run.getUserData(), voter, auditEventType, outcome, detail);
		run.getQueuedAuditEvents().add(voterAuditEvent);
	}
	
    private void commitVotersAndAuditLog(ImportElectoralRollRun run) {
		validerVelgere(run);
		commitVoters(run);
		commitAuditLogEntries(run);
	}

    //           Velgere som ikke validerer blir fortsatt lagt til, men vi vil ha logginnslag som gjør at vi kan få oversikt over omfanget
	private void validerVelgere(ImportElectoralRollRun run) {
		run.getVoterList().forEach(velger -> validerVelger(run, velger));
	}

	private void validerVelger(ImportElectoralRollRun run, Voter velger) {
		Set<ConstraintViolation<Voter>> valideringsfeil = run.getValidator().validate(velger, ValideringVedManuellRegistrering.class);
		valideringsfeil.forEach(feil -> loggValideringsfeil(velger, feil));
	}

	private void loggValideringsfeil(Voter velger, ConstraintViolation<Voter> valideringsfeil) {
		LOGGER.warn("Velger " + velger.getId() + " importert fra SKD har valideringsfeil. "
			+ "Felt=" + valideringsfeil.getPropertyPath()
			+ ", verdi=" + valideringsfeil.getInvalidValue()
			+ ", valideringsregel=" + valideringsfeil.getMessage());
	}

	private void commitVoters(ImportElectoralRollRun run) {
		velgerDomainService.updateVoterInNewTransaction(run.getUserData(), run.getVoterList(), run.getLegacyPollingDistrictList());
		LOGGER.info("Processed row number: " + run.getSkdVoterFileParser().getRowNumber() + ", Committed " + run.getVoterList().size() + " records to database.");
		run.getVoterList().clear();
		run.getLegacyPollingDistrictList().clear();
	}

	private void processVoterInFullImport(ImportElectoralRollRun run, VoterRecord skdVoterRecord, Voter voter, LegacyPollingDistrict legacyPollingDistrict) {
		if (skdVoterRecord.isInitialEntry()) {
			run.getVoterList().add(voter);
			run.getLegacyPollingDistrictList().add(legacyPollingDistrict);
			if (run.isFinalImport()) {
				queueAuditLogEvent(run, voter, AuditEventTypes.Create, Outcome.Success, null);
			}
			run.incrementNumberOfRecordsTilgang();
		} else {
            run.incrementNumberOfRecordsSkip();
		}

		if (run.getVoterList().size() % MAX_NO_OF_VOTERS_PER_TRANSACTION == 0) {
			commitVotersAndAuditLog(run);
		}
	}

	private void recordFailureOfImport(UserData userData, String filePath, Exception e) {
		LOGGER.error(e.getMessage(), e);
		auditLogFailedFileImport(userData, filePath, "Error while processing electoral roll import file: " + e.getMessage());
	}

	private void recordCompletionOfImport(ImportElectoralRollRun run) throws IOException {
		run.setTimeEnded(DateTime.now());
		buildAndVerifyVoterImportBatch(run);
		voterImportBatchServiceLocalEjb.update(run.getUserData(), run.getVoterImportBatch());
		LOGGER.info(format("fullImportElectoralRoll ended for '%s'", run.getElectionEvent().getName()));
		auditLogSuccessfulFileExecution(run);
	}
}
