package no.valg.eva.admin.common.configuration.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

import java.io.Serializable;

public interface ExportCandidateVotesService extends Serializable {
    byte[] exportCandidateVotes(UserData userData, AreaPath areaPath, ElectionPath electionPath);
}
