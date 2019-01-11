package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;

import org.joda.time.DateTime;

public class ModifiedBallotAuditEvent extends AuditEvent {

    private ModifiedBallot modifiedBallot;

    public ModifiedBallotAuditEvent(UserData userData, ModifiedBallot modifiedBallot, AuditEventTypes crudType, Outcome outcome, String detail) {
        super(userData, new DateTime(), crudType, Process.COUNTING, outcome, detail);
        this.modifiedBallot = modifiedBallot;
    }

    @Override
    public Class objectType() {
        return ModifiedBallot.class;
    }

    @Override
    public String toJson() {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.add("ballotId", modifiedBallot.getBallotId().getId());
        jsonBuilder.add("serialNumber", modifiedBallot.getSerialNumber());
        jsonBuilder.add("batchId", modifiedBallot.getBatchId().getId());

        for (Candidate candidate : modifiedBallot.getPersonVotes()) {
            jsonBuilder.add("personVoteCandidate", candidate.toString());
        }

        int writeInIndex = 1;
        for (Candidate candidate : modifiedBallot.getWriteIns()) {
            jsonBuilder.add("writeInCandidate_" + writeInIndex, candidate.getCandidateRef().getPk());
            writeInIndex++;
        }
        return jsonBuilder.toJson();
    }

    public static Class[] objectClasses(AuditEventType auditEventType) {
        if (AuditEventTypes.Update.equals(auditEventType)) {
            return new Class[] { ModifiedBallot.class };
        } else {
            throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
        }
    }
}
