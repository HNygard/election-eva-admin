package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.List;

public abstract class VotingsAuditEvent extends AuditEvent {

    private static final long serialVersionUID = -5842439157377557111L;
    protected final List<VotingDto> votingDtoList;

    VotingsAuditEvent(UserData userData, List<VotingDto> votingDtoList,
                      AuditEventTypes crudType, Outcome outcome, String detail) {
        super(userData, DateTime.now(), crudType, Process.VOTING, outcome, detail);
        this.votingDtoList = votingDtoList;
    }

    @Override
    public Class objectType() {
        return Voting.class;
    }

    JsonArray createVotingsJasonArray() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (VotingDto votingDto : votingDtoList) {
            arrayBuilder.add(toVotingDtoJsonObject(votingDto));
        }
        return arrayBuilder.build();
    }

    private JsonObject toVotingDtoJsonObject(VotingDto votingDto) {
        JsonBuilder builder = new JsonBuilder();
        builder.add("voterId", votingDto.getVoterDto().getId());
        builder.add("votingNumber", votingDto.getVotingNumber());
        builder.add("votingCategoryId", votingDto.getVotingCategory().getId());
        return builder.asJsonObject();
    }
}
