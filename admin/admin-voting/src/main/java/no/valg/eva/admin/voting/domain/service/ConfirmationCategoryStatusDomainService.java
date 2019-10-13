package no.valg.eva.admin.voting.domain.service;

import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.voting.application.VotingMapper;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Default
@ApplicationScoped
public class ConfirmationCategoryStatusDomainService {

    private static final boolean CONSIDER_END_OF_ELECTION = true;

    @Inject
    VotingCategoryStatusDomainService votingCategoryStatusDomainService;
    @Inject
    private VotingRepository votingRepository;

    public List<ConfirmationCategoryStatus> confirmationCategoryStatuses(MvArea mvArea, ElectionGroup electionGroup) {
        List<VotingCategoryStatus> votingCategoryStatuses = votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, CONSIDER_END_OF_ELECTION);
        return markVotingCategoriesWithUnconfirmedVotes(mvArea, electionGroup, votingCategoryStatuses);
    }

    private List<ConfirmationCategoryStatus> markVotingCategoriesWithUnconfirmedVotes(MvArea mvArea, ElectionGroup electionGroup, 
                                                                                      List<VotingCategoryStatus> votingCategoryStatuses) {
        return votingCategoryStatuses.stream()
                .map(votingCategoryStatus -> new ConfirmationCategoryStatus(
                        votingCategoryStatus.getMessageProperty(),
                        votingCategoryStatus.getVotingCategory(),
                        votingCategoryStatus.getVotingPhase(),
                        votingCategoryStatus.getTense(),
                        votingCategoryStatus.getLocked(),
                        votingCategoryStatus.getStartingDate(),
                        votingCategoryStatus.getEndingDate(),
                        hasUnconfirmedVotes(mvArea, electionGroup, votingCategoryStatus)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hasUnconfirmedVotes(MvArea mvArea, ElectionGroup electionGroup, VotingCategoryStatus votingCategoryStatus) {
        VotingCategory votingCategory = votingCategoryStatus.getVotingCategory();
        VotingPhase votingPhase = votingCategoryStatus.getVotingPhase();
        List<Voting> votingsToConfirm = votingRepository.findVotingsToConfirm(mvArea.getMunicipality(), electionGroup, votingCategory, isLateValidation(votingPhase));
        List<Voting> filteredVotingsToConfirm = removeVotingsFromOtherPhases(votingCategoryStatus, votingCategory, votingsToConfirm);
        return !filteredVotingsToConfirm.isEmpty();
    }

    private List<Voting> removeVotingsFromOtherPhases(VotingCategoryStatus votingCategoryStatus, VotingCategory votingCategory, final List<Voting> votingsToConfirm) {
        List<Voting> filteredVotings = new ArrayList<>(votingsToConfirm);
        if (votingCategory == VotingCategory.FI) {
            for (Iterator<Voting> iterator = filteredVotings.iterator(); iterator.hasNext();) {
                Voting voting = iterator.next();
                DateTime advanceVotingStartDate = voting.getElectionGroup().getElectionEvent().getAdvanceVotingStartDate().toDateTimeAtStartOfDay();
                if (votingIsCastInDifferentPhase(votingCategoryStatus, voting, advanceVotingStartDate)) {
                    iterator.remove();
                }
            }
        }
        return filteredVotings;
    }

    private boolean votingIsCastInDifferentPhase(VotingCategoryStatus votingCategoryStatus, Voting voting, DateTime advanceVotingStartDate) {
        return votingCategoryStatus.getVotingPhase() == VotingPhase.ADVANCE && voting.getCastTimestamp().isBefore(advanceVotingStartDate) || 
                votingCategoryStatus.getVotingPhase() == VotingPhase.EARLY && voting.getCastTimestamp().isAfter(advanceVotingStartDate);
    }
    
    public VotingConfirmationReportDto votingConfirmationReport(MvArea mvArea, ElectionGroup electionGroup, VotingCategory votingCategory, VotingPhase votingPhase, LocalDateTime startDate, LocalDateTime endDate) {
        boolean lateValidation = isLateValidation(votingPhase);
        long approvedVotings = votingRepository.countApprovedEnvelopeVotings(mvArea.getMunicipality(), electionGroup, votingCategory, lateValidation, startDate, endDate);
        long rejectedVotings = votingRepository.countRejectedEnvelopeVotings(mvArea.getMunicipality(), electionGroup, votingCategory, lateValidation, startDate, endDate);

        List<Voting> repositoryVotingsToConfirm = votingRepository.findVotingsToConfirm(mvArea.getMunicipality(), electionGroup, votingCategory, lateValidation, startDate, endDate, votingPhase);
        List<Voting> votingsToConfirm = repositoryVotingsToConfirm.stream()
                .filter(lateValidationPredicate(votingPhase))
                .collect(Collectors.toList());
        
        List<VotingDto> votingDtos = votingsToConfirm.stream()
                .map(VotingMapper::toDto)
                .collect(Collectors.toList());

        VotingConfirmationReportDto votingConfirmationReportDto = new VotingConfirmationReportDto(approvedVotings, rejectedVotings);
        votingConfirmationReportDto.addVotingsToVerify(votingDtos);
    
        return votingConfirmationReportDto;
    }

    public boolean isLateValidation(VotingPhase votingPhase) {
        return VotingPhase.LATE == votingPhase;
    }

    private Predicate<? super Voting> lateValidationPredicate(VotingPhase votingPhase) {
        return VotingPhase.LATE == votingPhase ? Voting::isLateValidation : Voting::isNotLateValidation;
    }
}
