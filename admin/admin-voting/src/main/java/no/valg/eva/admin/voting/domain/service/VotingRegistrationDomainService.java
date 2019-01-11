package no.valg.eva.admin.voting.domain.service;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaServiceBean;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_ALREADY_VOTED_FF;
import static no.valg.eva.admin.common.voting.VotingRejection.VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA;
import static no.valg.eva.admin.configuration.domain.model.VoterStatus.DECEASED;
import static org.joda.time.DateTime.now;

@Log4j
public class VotingRegistrationDomainService {

    @Inject
    private VotingRepository votingRepository;
    @Inject
    private VotingRejectionRepository votingRejectionRepository;
    @Inject
    private PollingDistrictRepository pollingDistrictRepository;
    @Inject
    private PollingPlaceRepository pollingPlaceRepository;
    @Inject
    private MvAreaServiceBean mvAreaService;

    public Voting registerAdvanceVotingInEnvelope(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Municipality municipality, Voter voter, VotingCategory votingCategory, boolean isLate, VotingPhase votingPhase) {
        assertMvAreaWhenCreatingVoting(voter.getMvArea());

        DateTime now = now();
        VotingRejection suggestedVotingRejection = checkForSuggestedRejectionReasons(userData, voter, electionGroup, municipality);

        Voting voting = Voting.builder()
                .pollingPlace(pollingPlace)
                .electionGroup(electionGroup)
                .castTimestamp(now)
                .voter(voter)
                .approved(false)
                .phase(votingPhase)
                .lateValidation(isLate)
                .mvArea(voter.getMvArea())
                .suggestedVotingRejection(suggestedVotingRejection)
                .build();

        if (isVoterInMunicipality(municipality, voter) || voter.isFictitious()) {
            voting.setVotingCategory(votingRepository.findVotingCategoryById(votingCategory.getId()));
            voting.setReceivedTimestamp(now);
            voting.setBallotBoxId(null);
        } else {
            voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
            voting.setReceivedTimestamp(null);
        }

        return votingRepository.create(userData, voting);
    }

    private VotingRejection checkForSuggestedRejectionReasons(UserData userData, Voter voter, ElectionGroup electionGroup, Municipality municipality) {
        boolean voterHasExistingVotings = voterHasExistingVotings(voter, electionGroup, municipality);

        if (voterHasExistingVotings) {
            updateVotingsToConfirmWithSuggestedVotingRejection(userData, voter, VOTER_ALREADY_VOTED_FF);
            return votingRejectionRepository.findById(VOTER_ALREADY_VOTED_FF.getId());
        }

        if (!voterApprovedAndInElectoralRoll(voter, municipality)) {
            return votingRejectionRepository.findById(VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA.getId());
        }

        return null;
    }

    private boolean voterApprovedAndInElectoralRoll(Voter voter, Municipality municipality) {
        return voter.isApproved() && isVoterInMunicipality(municipality, voter);
    }

    private boolean isVoterInMunicipality(Municipality municipality, Voter voter) {
        return voter.getMunicipalityId().equals(municipality.getId());
    }

    private void assertMvAreaWhenCreatingVoting(MvArea mvArea) {
        if (mvArea == null) {
            throw new EvoteException("Missing MvArea when creating Voting");
        }
    }

    private boolean voterHasExistingVotings(Voter voter, ElectionGroup electionGroup, Municipality municipality) {
        List<Voting> existingVotings = votingRepository.getVotingsByElectionGroupVoterAndMunicipality(voter.getPk(), electionGroup.getPk(), municipality.getPk());
        return !existingVotings.isEmpty();
    }

    public Voting registerElectionDayVotingInEnvelopeCentrally(UserData userData, ElectionGroup electionGroup, Municipality municipality, Voter voter, VotingCategory votingCategory, VotingPhase votingPhase) {
        PollingDistrict municipalityDistrict = pollingDistrictRepository.findMunicipalityProxy(municipality.getPk());
        PollingPlace envelopePollingPlace = pollingPlaceRepository.findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox(municipalityDistrict.getPk(), false);

        MvArea mvArea = VF == votingCategory ? voter.getMvArea() : mvAreaService.findByPollingDistrict(municipalityDistrict.getPk());
        assertMvAreaWhenCreatingVoting(mvArea);

        DateTime now = now();
        VotingRejection suggestedVotingRejection = checkForSuggestedRejectionReasons(userData, voter, electionGroup, municipality);
        
        Voting voting = Voting.builder()
                .electionGroup(electionGroup)
                .castTimestamp(now)
                .receivedTimestamp(now)
                .voter(voter)
                .votingCategory(votingRepository.findVotingCategoryById(votingCategory.getId()))
                .approved(false)
                .pollingPlace(envelopePollingPlace)
                .mvArea(mvArea)
                .phase(votingPhase)
                .suggestedVotingRejection(suggestedVotingRejection)
                .build();
        
        return votingRepository.create(userData, voting);
    }

    public void voterUpdated(UserData userData, Voter voter) {
        if (DECEASED.getStatusCode() == voter.getStatuskode()) {
            updateVotingsToConfirmWithSuggestedVotingRejection(userData, voter, VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA);
        }
    }

    private void updateVotingsToConfirmWithSuggestedVotingRejection(UserData userData, Voter voter, no.valg.eva.admin.common.voting.VotingRejection votingRejection) {
        List<Voting> votings = votingRepository.findVotingsToConfirmForVoter(voter);
        votings.forEach(voting -> {
            VotingRejection deceasedRejectionId = votingRejectionRepository.findById(votingRejection.getId());
            voting.setSuggestedVotingRejection(deceasedRejectionId);
            votingRepository.update(userData, voting);
            log.debug(format("Updated voting %s for voter %s", voting, voter));
        });
    }
}
