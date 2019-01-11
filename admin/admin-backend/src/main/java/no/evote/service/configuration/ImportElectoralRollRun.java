package no.evote.service.configuration;

import lombok.Data;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.voting.domain.electoralroll.ImportElectoralRollType;
import no.valg.eva.admin.voting.domain.electoralroll.SkdVoterFileParser;
import no.valg.eva.admin.voting.domain.electoralroll.VoterConverter;
import org.joda.time.DateTime;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representerer en kjøring av en manntallsimport (full eller inkrementell)
 */
@Data
public class ImportElectoralRollRun {

    // Kontekst-informasjon
    private final UserData userData;
    private final ElectionEvent electionEvent;
    private final String filePath;
    private final ImportElectoralRollType importelectoralRollType;
    
    // Telling av manntallsinnslag
    private final int numberOfRowsSKD;
    private final int batchNumberSKD;
    private int numberOfRecordsTilgang = 0;
    private int numberOfRecordsEndring = 0;
    private int numberOfRecordsAvgang = 0;
    private int numberOfRecordsSkip = 0;

    // Tidtaking
    private final DateTime timeStarted;
    private DateTime timeEnded;

    // Velgere og auditlogging som blir prosessert
    private List<Voter> voterList;
    private List<AuditEvent> queuedAuditEvents;
    private List<LegacyPollingDistrict> legacyPollingDistrictList;

    // Jobbene som brukes i denne kjøringen
    private VoterImportBatch voterImportBatch;
    private Batch batch;
    
    // Parsing/mapping/validering brukt underveis
    private final SkdVoterFileParser skdVoterFileParser;
    private final VoterConverter voterConverter;
    private final Validator validator;
    
    public ImportElectoralRollRun(UserData userData, ElectionEvent electionEvent, ImportElectoralRollType importElectoralRollType,
                                  String filePath, VoterImportBatch voterImportBatch,
                                  int maxNoOfVotersPerTransaction, MvAreaRepository mvAreaRepository) throws IOException {
        this.userData = userData;
        this.electionEvent = electionEvent;
        this.importelectoralRollType = importElectoralRollType;
        this.filePath = filePath;
        
        timeStarted = DateTime.now();
        voterList = new ArrayList<>(maxNoOfVotersPerTransaction);
        legacyPollingDistrictList = new ArrayList<>(maxNoOfVotersPerTransaction);
        queuedAuditEvents = new ArrayList<>();
        this.voterImportBatch = voterImportBatch;

        skdVoterFileParser = new SkdVoterFileParser(filePath);
        batchNumberSKD = Integer.valueOf(skdVoterFileParser.kjorenr());
        numberOfRowsSKD = Integer.valueOf(skdVoterFileParser.antall());
        voterConverter = new VoterConverter(electionEvent, mvAreaRepository);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    public boolean isFinalImport() {
        return importelectoralRollType.equals(ImportElectoralRollType.FULL_FINAL);
    }

    public void copyDataToVoterImportBatch() {
        voterImportBatch.setElectionEvent(electionEvent);
        voterImportBatch.setLastImportBatchNumber(batchNumberSKD);
        voterImportBatch.setLastImportStart(timeStarted);
        voterImportBatch.setLastImportEnd(timeEnded);
        voterImportBatch.setLastImportRecordsTotal(numberOfRecordsTilgang + numberOfRecordsEndring + numberOfRecordsAvgang + numberOfRecordsSkip);
        voterImportBatch.setLastImportRecordsInsert(numberOfRecordsTilgang);
        voterImportBatch.setLastImportRecordsUpdate(numberOfRecordsEndring);
        voterImportBatch.setLastImportRecordsDelete(numberOfRecordsAvgang);
        voterImportBatch.setLastImportRecordsSkip(numberOfRecordsSkip);
    }
    
    public void incrementNumberOfRecordsSkip() {
        numberOfRecordsSkip++;
    }

    public void incrementNumberOfRecordsTilgang() {
        numberOfRecordsTilgang++;
    }

    public void incrementNumberOfRecordsEndring() {
        numberOfRecordsEndring++;
    }
    
    public void incrementNumberOfRecordsAvgang() {
        numberOfRecordsAvgang++;
    }
}
