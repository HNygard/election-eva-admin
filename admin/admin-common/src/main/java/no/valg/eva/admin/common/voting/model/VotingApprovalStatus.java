package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class VotingApprovalStatus implements Serializable {
    private VotingApprovalState state;
    private VotingDto previouslyApprovedVoting;
}
