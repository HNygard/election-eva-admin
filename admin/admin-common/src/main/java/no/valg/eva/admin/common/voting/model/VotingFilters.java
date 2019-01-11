package no.valg.eva.admin.common.voting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@AllArgsConstructor
@NoArgsConstructor(access = PRIVATE)
@EqualsAndHashCode
@ToString
@Builder
@Getter
public class VotingFilters implements Serializable {

    private static final long serialVersionUID = 40495125398199282L;

    @Singular("suggestedProcessing")
    private List<SuggestedProcessingDto> suggestedProcessingList;
    private VotingConfirmationStatus votingConfirmationStatus;
    @Singular
    private List<VotingRejectionDto> votingRejections;
    @Singular
    private List<VotingCategory> votingCategories;
    private LocalDateTime toDateIncluding;
    private boolean validatedVotings;
    private VotingPhase votingPhase;
    private LocalDateTime fromDate;
    private String processingType;
    private String searchQuery;

    public String getSearchQuery() {
        return isBlank(searchQuery) ? null : searchQuery.trim();
    }
}

