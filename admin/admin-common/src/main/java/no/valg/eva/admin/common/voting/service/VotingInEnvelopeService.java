package no.valg.eva.admin.common.voting.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.PagedList;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public interface VotingInEnvelopeService extends Serializable {

    List<VotingCategoryStatus> votingCategoryStatuses(UserData userData, MvArea mvArea);

    List<ConfirmationCategoryStatus> confirmationCategoryStatuses(UserData userData, MvArea mvArea, ElectionGroup electionGroup);

    VotingConfirmationReportDto votingConfirmationReport(UserData userData, MvArea mvArea, ElectionGroup electionGroup, VotingCategory votingCategory,
                                                         VotingPhase votingPhase, LocalDateTime startDate, LocalDateTime endDate);

    void approveVotingList(UserData userData, List<VotingDto> votingDtoList, Municipality municipality);

    void rejectVotingList(UserData userData, List<VotingDto> votings, VotingRejectionDto votingRejectionDto, Municipality municipality);

    void moveVotingToSuggestedRejected(UserData userData, List<VotingDto> votingDtoList, VotingRejectionDto votingRejectionDto, Municipality municipality);

    List<VotingRejectionDto> votingRejections(UserData userData, VotingCategory votingCategory);

    List<VotingRejectionDto> votingRejections(UserData userData);

    List<VotingDto> approvedVotings(UserData userData, ElectionGroup electionGroup, String voterId);

    List<VotingDto> rejectedVotings(UserData userData, ElectionGroup electionGroup, String voterId);

    List<VotingDto> votingsToBeConfirmedForVoter(UserData userData, ElectionGroup electionGroup, String voterId);

    void rejectVoting(UserData userData, Municipality municipality, VotingDto selectedVoting, VotingRejectionDto votingRejectionDto);

    void approveVoting(UserData userData, VotingDto votingDto, Municipality municipality);

    void cancelRejection(UserData userData, VotingDto voting, Municipality municipality);

    VotingApprovalStatus checkIfSuggestedRejectedVotingCanBeApproved(UserData userData, ElectionGroup electionGroup, Municipality municipality, VotingDto votingDto);

    List<VoterDto> checkIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne(
            UserData userData, ElectionGroup electionGroup, List<VotingDto> votingsToBeRejected);

    PagedList<VotingDto> votings(UserData userData, MvArea selectedMvArea, ElectionGroup selectedElectionGroup,
                                 VotingFilters votingFilters,
                                 VotingSorting votingSorting,
                                 int offset,
                                 int limit);

    List<SuggestedProcessingDto> suggestedProcessingList(UserData userData);
}
