package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.service.ResponsibilityValidationDomainService;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

@Stateless(name = "ResponsibilityValidationService")
@Remote(ResponsibilityValidationService.class)
public class ResponsibilityValidationApplicationService implements ResponsibilityValidationService {

    @Inject
    private ResponsibilityValidationDomainService responsibilityValidationDomainService;
    
    @Inject
    private ElectionGroupRepository electionGroupRepository;
    
    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public List<ResponsibilityConflict> checkIfRoleHasCandidateConflict(UserData userData, PersonId personId, AreaPath areaPath, String roleId) {
        if (findElectionGroup(userData).isValidateRoleAndListProposal()) {
            return responsibilityValidationDomainService.checkIfPersonHasCandidateConflict(personId, areaPath, roleId, userData.electionEvent());
        }
        return emptyList();
    } 

    // Derfor hentes den fra databasen for å forsikre at man bruker en "fersk" versjon av ElectionGroup som har riktige innstillinger.
    private ElectionGroup findElectionGroup(UserData userData) {
        return electionGroupRepository.findByPk(userData.electionEvent().getElectionGroups().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ElectionEvent uten ElectionGroup"))
                .getPk());
    }

    @Override
    @Security(accesses = Listeforslag_Rediger, type = READ)
    public List<ResponsibilityConflict> checkIfCandidateHasBoardMemberOrRoleConflict(UserData userData, Candidate candidate, Affiliation affiliation) {
        return responsibilityValidationDomainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public List<ResponsibilityConflict> checkIfBoardMemberHasCandidateConflict(UserData userData, String firstName, String middleName, String lastName,
                                                                               AreaPath areaPath) {
        if (findElectionGroup(userData).isValidatePollingPlaceElectoralBoardAndListProposal()) {
            return responsibilityValidationDomainService.checkIfBoardMemberHasCandidateConflict(firstName, middleName, lastName, areaPath);
        }
        return emptyList();
    }
}
