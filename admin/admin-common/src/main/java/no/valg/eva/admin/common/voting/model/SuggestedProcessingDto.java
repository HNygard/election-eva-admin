package no.valg.eva.admin.common.voting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PRIVATE)
public class SuggestedProcessingDto implements Serializable {
    private static final long serialVersionUID = 5200825067578251954L;
    private String id;
    private ProcessingType processingType;
    private String textProperty;
}
