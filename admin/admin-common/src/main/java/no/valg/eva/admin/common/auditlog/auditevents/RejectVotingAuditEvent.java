package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;

import java.util.List;

public class RejectVotingAuditEvent extends VotingsAuditEvent {

    private final VotingRejectionDto votingRejectionDto;
    private final Municipality municipality;

    public RejectVotingAuditEvent(UserData userData, List<VotingDto> votingDtoList, VotingRejectionDto votingRejectionDto, Municipality municipality,
                                  AuditEventTypes crudType, Outcome outcome, String detail) {
        super(userData, votingDtoList, crudType, outcome, detail);
        this.votingRejectionDto = votingRejectionDto;
        this.municipality = municipality;
    }

    @Override
    public String toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("votings", createVotingsJasonArray());
        builder.add("votingRejectionId", votingRejectionDto.getId());
        builder.add("municipalityPk", municipality.getPk());
        return builder.toJson();
    }

    public static Class[] objectClasses(AuditEventType auditEventType) {
        if (AuditEventTypes.UpdateAll.equals(auditEventType)) {
            return new Class[]{List.class, VotingRejectionDto.class, Municipality.class};
        } else {
            throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
        }
    }
}
