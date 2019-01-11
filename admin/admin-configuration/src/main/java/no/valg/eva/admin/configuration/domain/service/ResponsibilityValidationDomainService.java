package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.POLLING_PLACE_ELECTORAL_BOARD;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.ROLE;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.ROLE_COUNTY;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.ROLE_MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.ROLE_POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.ROLE_POLLING_PLACE;
import static no.valg.eva.admin.util.StringUtil.joinOnlyNonNullAndNonEmpty;

public class ResponsibilityValidationDomainService {

    @Inject
    private ResponsibleOfficerRepository responsibleOfficerRepository;
    @Inject
    private CandidateRepository candidateRepository;
    @Inject
    private ContestRepository contestRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private OperatorRoleRepository operatorRoleRepository;
    @Inject
    private RoleRepository roleRepository;

    public List<ResponsibilityConflict> checkIfCandidateHasBoardMemberOrRoleConflict(Candidate candidate, Affiliation affiliation) {
        List<ResponsibilityConflict> conflicts = new ArrayList<>();
        ElectionGroup electionGroup = affiliation.getBallot().getContest().getElection().getElectionGroup();
        if (electionGroup.isValidatePollingPlaceElectoralBoardAndListProposal()) {
            conflicts.addAll(checkIfCandidateHasBoardMemberConflict(candidate, affiliation));
        }
        if (electionGroup.isValidateRoleAndListProposal()) {
            conflicts.addAll(checkIfCandidateHasRoleConflict(candidate, affiliation));
        }
        return conflicts;
    }

    private List<ResponsibilityConflict> checkIfCandidateHasBoardMemberConflict(Candidate candidate, Affiliation affiliation) {
        MvArea mvArea = areaToSearchIn(affiliation);
        String candidateName = buildFullName(candidate.getFirstName(), candidate.getMiddleName(), candidate.getLastName());

        if (candidateName.isEmpty()) {
            return emptyList();
        }

        return responsibleOfficerRepository.findResponsibleOfficersMatchingName(mvArea.getPk(), candidateName).stream()
                .map(this::responsibleOfficerToResponsibilityConflict)
                .collect(toList());
    }

    private MvArea areaToSearchIn(Affiliation affiliation) {
        Contest contest = contestRepository.getReference(Contest.class, affiliation.getBallot().getContest().getPk());
        MvArea area = contest.getFirstContestArea().getMvArea();
        if (area.getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel()) {
            area = mvAreaRepository.findParentAreaByPk(area.getPk());
        }
        return area;
    }

    private ResponsibilityConflict responsibleOfficerToResponsibilityConflict(ResponsibleOfficer responsibleOfficer) {
        return ResponsibilityConflict.builder()
                .type(POLLING_PLACE_ELECTORAL_BOARD)
                .messageArguments(buildResponsibleOfficerConflictArguments(responsibleOfficer))
                .build();
    }

    private List<String> buildResponsibleOfficerConflictArguments(ResponsibleOfficer responsibleOfficer) {
        String fullName = buildFullName(responsibleOfficer);
        String responsibilityMessageProperty = ResponsibilityId.fromId(responsibleOfficer.getResponsibility().getId()).getName();
        MvArea area = responsibleOfficer.getReportingUnit().getMvArea();

        return asList(fullName,
                responsibilityMessageProperty,
                area.getAreaName(),
                areaLevelName(area.getActualAreaLevel()));
    }

    private String areaLevelName(AreaLevelEnum areaLevel) {
        return areaLevel == null ? "" : "@area_level[" + areaLevel.getLevel() + "].name.lowercase";
    }

    private List<ResponsibilityConflict> checkIfCandidateHasRoleConflict(Candidate candidate, Affiliation affiliation) {
        String candidateId = candidate.getId();
        if (StringUtils.isBlank(candidateId)) {
            return emptyList();
        }
        PersonId personId = new PersonId(candidateId);
        MvArea mvArea = areaToSearchIn(affiliation);
        List<ResponsibilityConflict> conflicts = new ArrayList<>();
        ElectionEvent electionEvent = affiliation.getParty().getElectionEvent();
        for (Role roleToCheckForCandidateConflict : roleRepository.findRolesByCheckCandidateConflict(electionEvent)) {
            for (OperatorRole conflictingOperatorRole : operatorRoleRepository.findConflictingOperatorRoles(personId, mvArea, roleToCheckForCandidateConflict)) {
                conflicts.add(createAreaSpecificResponsibilityConflict(roleToCheckForCandidateConflict, conflictingOperatorRole.getMvArea()));
            }
        }
        return conflicts;
    }

    private ResponsibilityConflict createAreaSpecificResponsibilityConflict(Role role, MvArea mvArea) {
        AreaLevelEnum areaLevel = mvArea.getActualAreaLevel();
        switch (areaLevel) {
            case COUNTY:
                return new ResponsibilityConflict(ROLE_COUNTY, role.getName(), mvArea.getCountyName());
            case MUNICIPALITY:
                return new ResponsibilityConflict(ROLE_MUNICIPALITY, role.getName(), mvArea.getMunicipalityName());
            case POLLING_DISTRICT:
                return new ResponsibilityConflict(ROLE_POLLING_DISTRICT, role.getName(), mvArea.getPollingDistrictName(), mvArea.getMunicipalityName());
            case POLLING_PLACE:
                return new ResponsibilityConflict(ROLE_POLLING_PLACE, role.getName(), mvArea.getPollingPlaceName(), mvArea.getMunicipalityName());
            default:
                return new ResponsibilityConflict(ROLE, role.getName());
        }
    }

    public List<ResponsibilityConflict> checkIfPersonHasCandidateConflict(PersonId personId, AreaPath areaPath, String roleId, ElectionEvent electionEvent) {
        if (roleCanBeConflicting(electionEvent, roleId)) {
            List<Candidate> candidateLists = candidateRepository.findCandidateAtCountyOrBelow(personId.getId(), areaPath);
            candidateLists = filterCandidateListAtLevelAndUp(candidateLists, areaPath);
            return toConflicts(candidateLists);
        } else {
            return emptyList();
        }
    }

    private boolean roleCanBeConflicting(ElectionEvent electionEvent, String roleId) {
        return roleRepository.findRolesByCheckCandidateConflict(electionEvent).stream()
                .filter(r -> r.getId().equals(roleId))
                .anyMatch(Role::isCheckCandidateConflicts);
    }

    private List<Candidate> filterCandidateListAtLevelAndUp(List<Candidate> candidateList, AreaPath areaPath) {
        return candidateList.stream()
                .filter(candidateWithinArea(areaPath))
                .collect(Collectors.toList());
    }

    private Predicate<Candidate> candidateWithinArea(AreaPath areaPath) {
        return c -> c.getBallot().getContest().getContestAreaSet().stream()
                .anyMatch(ca -> properAreaPathLevel(ca.getAreaPath()).contains(areaPath));
    }

    private AreaPath properAreaPathLevel(AreaPath areaPath) {
        return areaPath.getLevel().getLevel() >= AreaLevelEnum.MUNICIPALITY.getLevel() ? areaPath.toMunicipalityPath() : areaPath;
    }

    private List<ResponsibilityConflict> toConflicts(List<Candidate> candidateList) {
        final List<ResponsibilityConflict> responsibilityConflicts = new ArrayList<>();
        candidateList.forEach(currentCandidate -> responsibilityConflicts.add(
                new ResponsibilityConflict(ResponsibilityConflictType.CANDIDATE_ID,
                        buildFullName(currentCandidate),
                        currentCandidate.getBallot().partyName(),
                        currentCandidate.getMvArea().getAreaName(),
                        areaLevelName(currentCandidate.getMvArea().getActualAreaLevel())))
        );

        return responsibilityConflicts;
    }

    public List<ResponsibilityConflict> checkIfBoardMemberHasCandidateConflict(String firstName, String middleName, String lastName, AreaPath areaPath) {
        return candidateRepository.findCandidatesMatchingName(areaPath, buildFullName(firstName, middleName, lastName)).stream()
                .map(this::candidateToResponsibilityConflict)
                .collect(toList());
    }

    private String buildFullName(ResponsibleOfficer responsibleOfficer) {
        return buildFullName(responsibleOfficer.getFirstName(), responsibleOfficer.getMiddleName(), responsibleOfficer.getLastName());
    }

    private String buildFullName(Candidate candidate) {
        return buildFullName(candidate.getFirstName(), candidate.getMiddleName(), candidate.getLastName());
    }

    private String buildFullName(String firstName, String middleName, String lastName) {
        return joinOnlyNonNullAndNonEmpty(firstName, middleName, lastName);
    }

    private ResponsibilityConflict candidateToResponsibilityConflict(Candidate candidate) {
        return ResponsibilityConflict.builder()
                .type(ResponsibilityConflictType.CANDIDATE_NAME)
                .messageArguments(asList(candidate.getNameLine(),
                        candidate.getBallot().partyName(),
                        candidate.getBallot().getContest().getName(),
                        areaLevelName(candidate.getMvArea().getActualAreaLevel())))
                .build();
    }
}
