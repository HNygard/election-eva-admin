package no.valg.eva.admin.voting.domain.model;

import lombok.Builder;
import lombok.ToString;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import org.joda.time.LocalDate;

@ToString
public class VotingCategoryStatus extends CategoryStatus {

    @Builder
    public VotingCategoryStatus(String messageProperty, VotingCategory votingCategory, VotingPhase votingPhase, Tense tense, LockType locked, 
                                LocalDate startingDate, LocalDate endingDate) {
        super(messageProperty, votingCategory, votingPhase, tense, locked, startingDate, endingDate);
    }

    public VotingCategoryStatus(String messageProperty, VotingCategory votingCategory, VotingPhase votingPhase, Tense tense, LockType locked, LocalDate startingDate) {
        super(messageProperty, votingCategory, votingPhase, tense, locked, startingDate, null);
    }
}
