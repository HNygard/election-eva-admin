package no.valg.eva.admin.common.auditlog.auditevents;


import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyAuditEventTest {

    private AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

    @Test
    public void toJson_forSuccessfulCreateParty_isCorrect() {
        PartyAuditEvent auditEvent = new PartyAuditEvent(objectMother.createUserData(), stubParti(), AuditEventTypes.Create, Outcome.Success, null);
        assertThat(auditEvent.toJson()).isEqualTo("{\""
                + "partyId\":\"GP\",\""
                + "partyName\":\"Generelt pisspreikpartiet\",\""
                + "partyNumber\":1,\""
                + "partyCategory\":\"@party_category[3].name\",\""
                + "approved\":true"
                + "}");
    }

    @Test
    public void toJson_forSuccessfulDeleteParty_isCorrect() {
        PartyAuditEvent auditEvent = new PartyAuditEvent(objectMother.createUserData(), stubParti(), AuditEventTypes.Delete, Outcome.Success, null);
        assertThat(auditEvent.toJson()).isEqualTo("{\""
                + "partyId\":\"GP\",\""
                + "partyName\":\"Generelt pisspreikpartiet\"}");
    }

    @Test
    public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
        assertThat(PartyAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(new Class[] { Parti.class });
    }

    @Test
    public void constructor_mustMeetAuditInterceptorRequirements() throws Exception {
        assertThat(
                AuditEventFactory.getAuditEventConstructor(PartyAuditEvent.class,
                        PartyAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters))
                .isNotNull();
    }
    
    private Parti stubParti() {
        return Parti.builder()
                .id("GP")
                .partikategori(Partikategori.LOKALT)
                .oversattNavn("Generelt pisspreikpartiet")
                .partikode(1)
                .godkjent(true).build();
    }

}
