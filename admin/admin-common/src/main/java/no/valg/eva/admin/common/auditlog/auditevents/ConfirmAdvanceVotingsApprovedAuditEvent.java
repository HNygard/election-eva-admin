package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VotingDto;

import java.util.List;

/**
 * Audit event for mass-approval of advance votings.
 */
public class ConfirmAdvanceVotingsApprovedAuditEvent extends VotingsAuditEvent {

    private final Municipality municipality;

    public ConfirmAdvanceVotingsApprovedAuditEvent(UserData userData, List<VotingDto> votingDtoList, Municipality municipality,
                                                   AuditEventTypes crudType, Outcome outcome, String detail) {
        super(userData, votingDtoList, crudType, outcome, detail);
        this.municipality = municipality;
    }

    @Override
    public String toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("votings", createVotingsJasonArray());
        builder.add("municipalityPk", municipality.getPk());
        return builder.toJson();
    }

    public static Class[] objectClasses(AuditEventType auditEventType) {
        if (AuditEventTypes.UpdateAll.equals(auditEventType)) {
            return new Class[]{List.class, Municipality.class};
        } else {
            throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
        }
    }
}
