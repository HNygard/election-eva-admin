package no.valg.eva.admin.voting.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.configuration.domain.model.Voter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoterMapper {

    public static VoterDto toDto(Voter voter) {
        return VoterDto.builder()
                .id(voter.getId())
                .number(voter.getNumber())
                .firstName(voter.getFirstName())
                .middleName(voter.getMiddleName())
                .lastName(voter.getLastName())
                .nameLine(voter.getNameLine())
                .mvArea(voter.getMvArea())
                .approved(voter.isApproved())
                .eligible(voter.isEligible())
                .fictitious(voter.isFictitious())
                .build();
    }


    public static List<VoterDto> toDtoList(List<Voter> voterList) {
        if (voterList == null || voterList.isEmpty()) {
            return Collections.emptyList();
        }

        return voterList.stream()
                .map(VoterMapper::toDto)
                .collect(Collectors.toList());
    }
}
