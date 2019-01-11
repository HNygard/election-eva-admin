package no.valg.eva.admin.voting.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import org.joda.time.LocalDate;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class CategoryStatus implements Serializable {
    protected String messageProperty;
    protected VotingCategory votingCategory;
    protected VotingPhase votingPhase;
    protected Tense tense;
    protected LockType locked;
    protected LocalDate startingDate;
    protected LocalDate endingDate;
}
