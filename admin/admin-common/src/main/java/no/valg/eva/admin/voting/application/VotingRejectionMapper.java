package no.valg.eva.admin.voting.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VotingRejectionMapper {
    
    public static VotingRejectionDto toDto(no.valg.eva.admin.configuration.domain.model.VotingRejection votingRejection) {
        return VotingRejectionDto.builder()
                .id(votingRejection.getId())
                .earlyVoting(votingRejection.isEarlyVoting())
                .name(votingRejection.getName())
                .suggestedRejectionName(votingRejection.getSuggestedRejectionName())
                .build();
    }
}
