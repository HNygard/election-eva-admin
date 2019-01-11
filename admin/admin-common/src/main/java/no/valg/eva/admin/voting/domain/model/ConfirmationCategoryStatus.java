package no.valg.eva.admin.voting.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import org.joda.time.LocalDate;

@Getter
@ToString
public class ConfirmationCategoryStatus extends CategoryStatus{

    private boolean needsVerification;

    @Builder
    public ConfirmationCategoryStatus(String messageProperty, VotingCategory votingCategory, VotingPhase votingPhase, Tense tense, LockType locked, 
                                       LocalDate startingDate, LocalDate endingDate, boolean needsVerification) {
        super(messageProperty, votingCategory, votingPhase, tense, locked, startingDate, endingDate);
        this.needsVerification = needsVerification;
    }
}
