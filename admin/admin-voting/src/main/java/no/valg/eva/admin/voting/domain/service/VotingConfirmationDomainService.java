package no.valg.eva.admin.voting.domain.service;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Log4j
@Default
@ApplicationScoped
public class VotingConfirmationDomainService {

    @Inject
    private VotingRepository votingRepository;
    @Inject
    private VoterRepository voterRepository;
    @Inject
    private VotingRejectionRepository votingRejectionRepository;

        public void approveVotingList(List<VotingDto> votings, Municipality municipalityDto) {
        for (VotingDto votingDto : votings) {
            approveVoting(votingDto, municipalityDto);
        }
    }

    public void approveVoting(VotingDto votingDto, Municipality municipalityDto) {
        Voter voter = voterRepository.voterOfId(votingDto.getVoterDto().getId(), votingDto.getElectionGroup().getElectionEvent().getPk());
        verifyVoterHasNoOtherApprovedVoting(votingDto.getElectionGroup(), voter);

        Voting voting = votingRepository.findVotingByVotingNumber(municipalityDto, votingDto);
        verifyVotingIsNotAlreadyConfirmed(voting);
        voting.setValidationTimestamp(DateTime.now());
        voting.setApproved(true);
        voting.setMvArea(voter.getMvArea());
    }

    private void verifyVoterHasNoOtherApprovedVoting(ElectionGroup electionGroup, Voter voter) {
        List<Voting> existingVotings = votingRepository.getVotingsByElectionGroupAndVoter(voter.getPk(), electionGroup.getPk());
        Optional<Voting> approvedVotingOptional = existingVotings.stream()
                .filter(Voting::isApproved)
                .findFirst();

        if (approvedVotingOptional.isPresent()) {
            throw new EvoteException(new UserMessage("@voting.confirmation.voter.has.existing.approved.voting", approvedVotingOptional.get().getVotingNumber()));
        }
    }

    private void verifyVotingIsNotAlreadyConfirmed(Voting voting) {
        if (voting.getValidationTimestamp() != null) {
            throw new EvoteException(new UserMessage("@voting.confirmation.votingAlreadyConfirmed", voting.getVotingNumber()));
        }
    }

    public void suggestRejectVotings(List<VotingDto> votings, VotingRejectionDto votingSuggestedRejectionDto, Municipality municipalityDto) {
        for (VotingDto votingDto : votings) {
            suggestRejectVoting(votingDto, votingSuggestedRejectionDto, municipalityDto);
        }
    }

    private void suggestRejectVoting(VotingDto votingDto, VotingRejectionDto votingSuggestedRejectionDto, Municipality municipalityDto) {
        Voting voting = votingRepository.findVotingByVotingNumber(municipalityDto, votingDto);
        verifyVotingIsNotAlreadyConfirmed(voting);
        VotingRejection votingSuggestedRejection = votingRejectionRepository.findById(votingSuggestedRejectionDto.getId());
        voting.setSuggestedVotingRejection(votingSuggestedRejection);
    }

    public void rejectVotings(List<VotingDto> votings, VotingRejectionDto votingRejectionDto, Municipality municipalityDto) {
        for (VotingDto votingDto : votings) {
            rejectVoting(votingDto, votingRejectionDto, municipalityDto);
        }
    }

    public void rejectVoting(VotingDto votingDto, VotingRejectionDto votingRejectionDto, Municipality municipalityDto) {
        Voting voting = votingRepository.findVotingByVotingNumber(municipalityDto, votingDto);
        verifyVotingIsNotAlreadyConfirmed(voting);
        VotingRejection votingRejection = votingRejectionRepository.findById(votingRejectionDto.getId());
        voting.setVotingRejection(votingRejection);
        voting.setApproved(false);
        voting.setValidationTimestamp(DateTime.now());
    }

    public void cancelRejection(VotingDto votingDto, Municipality municipalityDto) {
        Voting voting = votingRepository.findVotingByVotingNumber(municipalityDto, votingDto);
        verifyVotingIsRejected(voting);
        voting.setVotingRejection(null);
        voting.setValidationTimestamp(null);
    }

    private void verifyVotingIsRejected(Voting voting) {
        if (voting.isApproved()) {
            throw new EvoteException(new UserMessage("@voting.confirmation.cannotCancelApprovedVoting", voting.getVotingNumber()));
        }
    }
}
