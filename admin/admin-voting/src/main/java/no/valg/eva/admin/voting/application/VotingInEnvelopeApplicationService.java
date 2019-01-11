package no.valg.eva.admin.voting.application;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.common.PagedList;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ConfirmAdvanceVotingsApprovedAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.RejectVotingAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.SuggestRejectVotingAuditEvent;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.VotingRejection;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import no.valg.eva.admin.voting.domain.service.ConfirmationCategoryStatusDomainService;
import no.valg.eva.admin.voting.domain.service.VotingCategoryStatusDomainService;
import no.valg.eva.admin.voting.domain.service.VotingConfirmationDomainService;
import no.valg.eva.admin.voting.domain.service.VotingInEnvelopeDomainService;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving_Prøving;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Oversikt_Konvolutt;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Prøving;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_APPROVED;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_REJECTED;
import static no.valg.eva.admin.voting.application.VoterMapper.toDtoList;
import static no.valg.eva.admin.voting.application.VotingMapper.toDtoList;

@Stateless(name = "VotingInEnvelopeService")
@Remote(VotingInEnvelopeService.class)
@NoArgsConstructor //CDI
public class VotingInEnvelopeApplicationService implements VotingInEnvelopeService {

    private static final long serialVersionUID = -8020664742972491965L;
    private static final boolean DISREGARD_END_OF_ELECTION = false;

    @Inject
    private VotingCategoryStatusDomainService votingCategoryStatusDomainService;

    @Inject
    private ConfirmationCategoryStatusDomainService confirmationCategorystatusDomainService;

    @Inject
    private VotingConfirmationDomainService votingDomainService;

    @Inject
    private VotingRejectionRepository votingRejectionRepository;

    @Inject
    private VoterRepository voterRepository;

    @Inject
    private VotingInEnvelopeDomainService votingInEnvelopeDomainService;

    @Override
    @Security(accesses = Stemmegiving, type = READ)
    public List<VotingCategoryStatus> votingCategoryStatuses(UserData userData, MvArea mvArea) {
        return votingCategoryStatusDomainService.votingCategoryStatuses(mvArea, DISREGARD_END_OF_ELECTION);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<ConfirmationCategoryStatus> confirmationCategoryStatuses(UserData userData, MvArea mvArea, ElectionGroup electionGroup) {
        return confirmationCategorystatusDomainService.confirmationCategoryStatuses(mvArea, electionGroup);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public VotingConfirmationReportDto votingConfirmationReport(UserData userData, MvArea mvArea, ElectionGroup electionGroup, VotingCategory votingCategory, VotingPhase votingPhase, LocalDateTime startDate, LocalDateTime endDate) {
        return confirmationCategorystatusDomainService.votingConfirmationReport(mvArea, electionGroup, votingCategory, votingPhase, startDate, endDate);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    @AuditLog(eventClass = ConfirmAdvanceVotingsApprovedAuditEvent.class, eventType = AuditEventTypes.UpdateAll)
    public void approveVotingList(UserData userData, List<VotingDto> votingDtoList, Municipality municipalityDto) {
        votingDomainService.approveVotingList(votingDtoList, municipalityDto);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    @AuditLog(eventClass = RejectVotingAuditEvent.class, eventType = AuditEventTypes.UpdateAll)
    public void rejectVotingList(UserData userData, List<VotingDto> votings, VotingRejectionDto votingRejectionDto, Municipality municipalityDto) {
        votingDomainService.rejectVotings(votings, votingRejectionDto, municipalityDto);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    @AuditLog(eventClass = SuggestRejectVotingAuditEvent.class, eventType = AuditEventTypes.UpdateAll)
    public void moveVotingToSuggestedRejected(UserData userData, List<VotingDto> votingDtos, VotingRejectionDto votingSuggestedRejectionDto, Municipality municipalityDto) {
        votingDomainService.suggestRejectVotings(votingDtos, votingSuggestedRejectionDto, municipalityDto);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VotingRejectionDto> votingRejections(UserData userData, VotingCategory votingCategory) {
        return votingRejectionRepository.findAll().stream()
                .filter(withCategory(votingCategory))
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VotingRejectionDto> votingRejections(UserData userData) {
        return votingRejectionRepository.findAll().stream()
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());
    }

    private Predicate<no.valg.eva.admin.configuration.domain.model.VotingRejection> withCategory(VotingCategory votingCategory) {
        return currentVotingRejection ->
                VotingRejection.fromVotingCategory(votingCategory).stream()
                        .anyMatch(currentRejection -> currentRejection.getId().equals(currentVotingRejection.getId()));
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VotingDto> votingsToBeConfirmedForVoter(UserData userData, ElectionGroup electionGroup, String voterId) {
        Voter voter = voterRepository.voterOfId(voterId, electionGroup.getElectionEvent().getPk());
        List<Voting> votingList = votingInEnvelopeDomainService.unconfirmedVotingsForVoter(electionGroup, voter);
        return toDtoList(votingList);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    public void rejectVoting(UserData userData, Municipality municipality, VotingDto votingDto, VotingRejectionDto votingRejectionDto) {
        votingDomainService.rejectVoting(votingDto, votingRejectionDto, municipality);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    public void approveVoting(UserData userData, VotingDto votingDto, Municipality municipality) {
        votingDomainService.approveVoting(votingDto, municipality);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VotingDto> rejectedVotings(UserData userData, ElectionGroup electionGroup, String voterId) {
        Voter voter = voterRepository.voterOfId(voterId, electionGroup.getElectionEvent().getPk());
        List<Voting> votingList = votingInEnvelopeDomainService.rejectedVotingsForVoter(electionGroup, voter);
        return toDtoList(votingList);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VotingDto> approvedVotings(UserData userData, ElectionGroup electionGroup, String voterId) {
        Voter voter = voterRepository.voterOfId(voterId, electionGroup.getElectionEvent().getPk());
        List<Voting> votingList = votingInEnvelopeDomainService.approvedVotingsForVoter(electionGroup, voter);
        return toDtoList(votingList);
    }

    @Override
    @Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
    public void cancelRejection(UserData userData, VotingDto voting, Municipality municipalityDto) {
        votingDomainService.cancelRejection(voting, municipalityDto);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public VotingApprovalStatus checkIfSuggestedRejectedVotingCanBeApproved(UserData userData, ElectionGroup electionGroup,
                                                                            Municipality municipality, VotingDto votingDto) {
        Voter voter = voterRepository.voterOfId(votingDto.getVoterDto().getId(), electionGroup.getElectionEvent().getPk());
        return votingInEnvelopeDomainService.resolveSuggestedRejectedVotingApproval(electionGroup, municipality, votingDto, voter);
    }

    @Override
    @Security(accesses = Stemmegiving_Prøving, type = READ)
    public List<VoterDto> checkIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne(UserData userData, ElectionGroup electionGroup, List<VotingDto> votingsToBeRejected) {
        return toDtoList(votingInEnvelopeDomainService.resolveVotersThatNeedToBeHandledOneByOne(electionGroup, votingsToBeRejected));
    }

    @Override
    @Security(accesses = Stemmegiving_Oversikt_Konvolutt, type = READ)
    public PagedList<VotingDto> votings(UserData userData, MvArea mvArea, ElectionGroup electionGroup, VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) {
        return votingInEnvelopeDomainService.votings(userData, mvArea.getMunicipality(), electionGroup, votingFilters, votingSorting, offset, limit);
    }

    @Override
    @Security(accesses = Stemmegiving_Oversikt_Konvolutt, type = READ)
    public List<SuggestedProcessingDto> suggestedProcessingList(UserData userData) {
        List<VotingRejectionDto> votingRejections = votingRejectionRepository.findAll().stream()
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());

        List<SuggestedProcessingDto> suggestedProcessingList = new ArrayList<>();
        suggestedProcessingList.add(SuggestedProcessingDto.builder()
                .id("TG")
                .processingType(SUGGESTED_APPROVED)
                .textProperty("Til godkjenning")
                .build());

        votingRejections.forEach(votingRejection -> suggestedProcessingList.add(SuggestedProcessingDto.builder()
                .id(votingRejection.getId())
                .processingType(SUGGESTED_REJECTED)
                .textProperty(votingRejection.getName())
                .build()));

        return suggestedProcessingList;
    }
}
