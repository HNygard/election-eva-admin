package no.valg.eva.admin.voting.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.SuggestedProcessing;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.Voting;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_APPROVED;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_REJECTED;
import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.APPROVE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VotingMapper {

    public static VotingDto toDto(Voting voting) {
        VotingRejection suggestedVotingRejection = voting.getSuggestedVotingRejection();
        String suggestedProcessingName = suggestedVotingRejection == null ? APPROVE.getName() : suggestedVotingRejection.getSuggestedRejectionName();
        return getVotingDto(voting, suggestedProcessingName);
    }

    private static VotingDto getVotingDto(Voting voting, String suggestedProcessingName) {
        Operator operator = voting.getOperator();
        return VotingDto.builder()
                .approved(voting.isApproved())
                .voterDto(VoterMapper.toDto(voting.getVoter()))
                .votingRejectionDto(voting.getVotingRejection() == null ? null : VotingRejectionMapper.toDto(voting.getVotingRejection()))
                .suggestedVotingRejectionDto(voting.getSuggestedVotingRejection() == null ? null : VotingRejectionMapper.toDto(voting.getSuggestedVotingRejection()))
                .ballotBoxId(voting.getBallotBoxId())
                .castTimestamp(DateUtil.convertToLocalDateTime(voting.getCastTimestamp()))
                .electionGroup(voting.getElectionGroup())
                .lateValidation(voting.isLateValidation())
                .mvArea(voting.getMvArea())
                .pollingPlace(voting.getPollingPlace())
                .receivedTimestamp(DateUtil.convertToLocalDateTime(voting.getReceivedTimestamp()))
                .removalRequest(voting.getRemovalRequest())
                .validationTimestamp(DateUtil.convertToLocalDateTime(voting.getValidationTimestamp()))
                .votingCategory(voting.getVotingCategory())
                .votingNumber(voting.getVotingNumber())
                .suggestedProcessing(suggestedProcessingName)
                .voteReceiverName(operator == null ? "" : operator.getFullName())
                .suggestedApproved(voting.getSuggestedVotingRejection() == null)
                .suggestedProcessingType(suggestedProcessingType(suggestedProcessingName))
                .build();
    }

    private static ProcessingType suggestedProcessingType(String suggestedProcessingName) {
        return !isBlank(suggestedProcessingName) && suggestedProcessingName.equals(APPROVE.getName()) ? SUGGESTED_APPROVED : SUGGESTED_REJECTED;
    }

    public static List<VotingDto> toDtoList(List<Voting> votingList) {
        if (votingList == null || votingList.isEmpty()) {
            return Collections.emptyList();
        }

        return votingList.stream()
                .map(VotingMapper::toDto)
                .collect(Collectors.toList());
    }

    public static VotingDto toDto(Voting voting, SuggestedProcessing suggestedProcessing) {
        String suggestedProcessingName = suggestedProcessing.getName();
        return getVotingDto(voting, suggestedProcessingName);
    }
}
