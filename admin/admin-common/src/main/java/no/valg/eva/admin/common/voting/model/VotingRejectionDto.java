package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VotingRejectionDto implements Serializable {

    private static final long serialVersionUID = -1538876138598761863L;

    @EqualsAndHashCode.Include
    private String id;
    private boolean earlyVoting;
    private String name;
    private String suggestedRejectionName;
}
