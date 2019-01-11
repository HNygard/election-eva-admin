package no.valg.eva.admin.common.voter.service;

import no.evote.model.views.VoterAudit;
import no.evote.security.UserData;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;

public interface VoterAuditService extends Serializable {
    List<VoterAudit> getHistoryForMunicipality(final UserData userData, final String municipalityId, final char endringstype, LocalDate startDate,
                                               LocalDate endDate, final Long electionEventPk, final String selectedSearchMode, final Boolean searchOnlyApproved);

    List<VoterAudit> getHistoryForVoter(final UserData userData, final long voterPk);

    List<VoterAudit> getHistoryForVoter(final UserData userData, final String voterId);
}
