package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.Builder;
import lombok.Getter;
import no.valg.eva.admin.common.voting.model.ProcessingType;

@Builder
@Getter
public class SuggestedProcessingViewModel {
    private String id;
    private ProcessingType processingType;
    private String textProperty;
}
