package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.APPROVE;

@Getter
@NoArgsConstructor
public class VotingConfirmationReportDto implements Serializable {

    private static final long serialVersionUID = -9018911656464890098L;

    private long numberOfApprovedVotings;
    private long numberOfRejectedVotings;
    private List<VotingDto> votingDtoListToConfirm = new ArrayList<>();

    @Builder
    public VotingConfirmationReportDto(long numberOfApprovedVotings, long numberOfRejectedVotings) {
        this.numberOfApprovedVotings = numberOfApprovedVotings;
        this.numberOfRejectedVotings = numberOfRejectedVotings;
    }

    private static boolean isSuggestedForApproval(VotingDto votingDto) {
        String suggestedProcessing = votingDto.getSuggestedProcessing();
        return APPROVE.getName().equals(suggestedProcessing);
    }

    private static boolean isSuggestedForRejection(VotingDto votingDto) {
        return !isSuggestedForApproval(votingDto);
    }

    public void addVotingToVerify(VotingDto votingDto) {
        votingDtoListToConfirm.add(votingDto);
    }

    public void addVotingsToVerify(List<VotingDto> votings) {
        votingDtoListToConfirm.addAll(votings);
    }

    public int getNumberOfVotingsToConfirm() {
        return votingDtoListToConfirm.size();
    }

    public boolean hasVotingsToConfirm() {
        return !votingDtoListToConfirm.isEmpty();
    }
    
    public List<VotingDto> suggestedRejectedVotings() {
        return votingDtoListToConfirm.stream()
                .filter(VotingConfirmationReportDto::isSuggestedForRejection)
                .collect(Collectors.toList());
    }

    public List<VotingDto> suggestedApprovedVotings() {
        return votingDtoListToConfirm.stream()
                .filter(VotingConfirmationReportDto::isSuggestedForApproval)
                .collect(Collectors.toList());
    }
}
