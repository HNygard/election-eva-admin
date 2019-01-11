package no.valg.eva.admin.voting.domain.electoralroll;

public class InvalidElectoralRollBatchNumber extends RuntimeException {
    public InvalidElectoralRollBatchNumber(String message) {
        super(message);
    }
}
