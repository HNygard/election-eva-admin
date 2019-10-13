package no.valg.eva.admin.voter.application;

import lombok.NoArgsConstructor;
import no.evote.model.views.VoterAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.VoterAuditRepository;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import org.joda.time.LocalDate;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Historikk;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "VoterAuditService")


@Default
@Remote(VoterAuditService.class)
@NoArgsConstructor //CDI
public class VoterAuditApplicationService implements VoterAuditService {

    private static final long serialVersionUID = -8930157086756044462L;

    @Inject
    private VoterAuditRepository voterAuditRepository;

    /**
     * Gets history from the manual and/or automatic updates for voters in a municipality
     */
    @Override
    @Security(accesses = Manntall_Historikk, type = READ)
    public List<VoterAudit> getHistoryForMunicipality(
            UserData userData, String municipalityId, char endringstype, LocalDate startDate, LocalDate endDate,
            Long electionEventPk, String selectedSearchMode, Boolean searchOnlyApproved) {
        return voterAuditRepository.getHistoryForMunicipality(
                municipalityId, endringstype, startDate, endDate, electionEventPk, selectedSearchMode, searchOnlyApproved);
    }

    @Override
    @Security(accesses = Manntall_Historikk, type = READ)
    public List<VoterAudit> getHistoryForVoter(final UserData userData, final long voterPk) {
        return voterAuditRepository.getHistoryForVoter(voterPk);
    }

    @Override
    @Security(accesses = Manntall_Historikk, type = READ)
    public List<VoterAudit> getHistoryForVoter(final UserData userData, final String voterId) {
        return voterAuditRepository.getHistoryForVoter(voterId);
    }
}
