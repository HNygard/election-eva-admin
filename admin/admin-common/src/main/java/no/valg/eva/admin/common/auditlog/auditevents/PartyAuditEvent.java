package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import org.joda.time.DateTime;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

public class PartyAuditEvent extends AuditEvent {

    protected final Parti parti;

    public PartyAuditEvent(UserData userData, Parti parti, AuditEventTypes crudType, Outcome outcome,
                           String detail) {
        super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
        this.parti = parti;
    }

    @Override
    public Class objectType() {
        return Parti.class;
    }
    
    @Override
    public String toJson() {
        return partyJsonBuilder().toJson();
    }

    protected JsonBuilder partyJsonBuilder() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("partyId", parti.getId());
        builder.add("partyName", parti.getOversattNavn());
		if (!AuditEventTypes.Delete.equals(eventType())) {
			builder.add("partyNumber", parti.getPartikode());
			builder.add("partyCategory", parti.getPartikategori().getName());
			builder.add("approved", parti.isGodkjent());
        }
        return builder;
    }
    
    public static Class[] objectClasses(AuditEventType auditEventType) {
        return new Class[] { Parti.class };
    }
}
