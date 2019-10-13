package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.service.ExportCandidateVotesService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.service.ExportCandidateVotesDomainService;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgoppgjør_Se;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ExportCandidateVotesService")

@Default
@Remote(ExportCandidateVotesService.class)
@NoArgsConstructor
public class ExportCandidateVotesApplicationService implements ExportCandidateVotesService {

    private static final long serialVersionUID = 1967040637683763584L;
    
    @Inject
    private ExportCandidateVotesDomainService exportCandidateVotesService;

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public byte[] exportCandidateVotes(UserData userData, AreaPath areaPath, ElectionPath electionPath) {
        return exportCandidateVotesService.exportCandidateVotes(areaPath, electionPath);
    }
}
