package no.valg.eva.admin.configuration.application;


import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;

import java.io.Serializable;
import java.util.List;

public interface ResponsibilityValidationService extends Serializable {
    
    List<ResponsibilityConflict> checkIfRoleHasCandidateConflict(UserData userData, PersonId personId, AreaPath areaPath, String roleId);

    List<ResponsibilityConflict> checkIfCandidateHasBoardMemberOrRoleConflict(UserData userData, Candidate candidate, Affiliation affiliation);

    List<ResponsibilityConflict> checkIfBoardMemberHasCandidateConflict(UserData userData, String firstName, String middleName, String lastName,
                                                                        AreaPath areaPath);
}
