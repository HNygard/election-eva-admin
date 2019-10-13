package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.counting.repository.CandidateVoteRepository;
import no.valg.eva.admin.util.ExcelUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

@Default
@ApplicationScoped
public class ExportCandidateVotesDomainService {

    @Inject
    private CandidateVoteRepository candidateVoteRepository;

    public byte[] exportCandidateVotes(AreaPath areaPath, ElectionPath electionPath) {
        return ExcelUtil.createXlsxFromRowData(candidateVoteRepository.findCandidateVotes(areaPath, electionPath));
    }
}
