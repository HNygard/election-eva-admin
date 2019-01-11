package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.model.ProcessingType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingProcessingTypeTab {

    @Getter
    private ProcessingType processingType;

    public String getDisplayName() {
        return processingType != null ? processingType.getDisplayName() : null;
    }
}
