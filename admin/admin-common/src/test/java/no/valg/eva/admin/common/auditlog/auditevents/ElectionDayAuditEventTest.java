package no.valg.eva.admin.common.auditlog.auditevents;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDto;
import static org.assertj.core.api.Assertions.assertThat;

public class ElectionDayAuditEventTest {

    private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

    @Test
    public void toJson_whenCreate_isCorrect()  {
        ElectionDayAuditEvent auditEvent = new ElectionDayAuditEvent(objectMother.createUserData(),
                toDto(objectMother.createElectionDay()), AuditEventTypes.Create, Outcome.Success, null);
        String json = auditEvent.toJson();
        assertThat(json).isEqualTo("{\"date\":\"2015-09-14\",\"startTime\":\"08:00\",\"endTime\":\"23:59\"}");
    }

    @Test
    public void toJson_whenDelete_isCorrect()  {
        ElectionDayAuditEvent auditEvent = new ElectionDayAuditEvent(objectMother.createUserData(), toDto(objectMother.createElectionDay()),
                AuditEventTypes.Delete, Outcome.Success, null);
        String json = auditEvent.toJson();
        assertThat(json).isEqualTo("{\"date\":\"2015-09-14\",\"startTime\":\"08:00\",\"endTime\":\"23:59\"}");
    }

    @Test
    public void objectType_mustReturnClassOfAuditedObject()  {
        ElectionDayAuditEvent auditEvent = new ElectionDayAuditEvent(objectMother.createUserData(),
                toDto(objectMother.createElectionDay()), AuditEventTypes.Create, Outcome.Success, null);

        assertThat(auditEvent.objectType()).isEqualTo(ElectionDay.class);
    }

    @Test
    public void objectClasses_whenCreate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
        assertThat(ElectionDayAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[]{ElectionDay.class});
    }

    @Test
    public void objectClasses_whenDelete_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
        assertThat(ElectionDayAuditEvent.objectClasses(AuditEventTypes.Delete)).isEqualTo(new Class[]{ElectionDay.class});
    }

    @Test
    public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
        Assertions.assertThat(AuditEventFactory.getAuditEventConstructor(ElectionDayAuditEvent.class, ElectionDayAuditEvent.objectClasses(AuditEventTypes.Create),
                AuditedObjectSource.Parameters)).isNotNull();
        assertThat(AuditEventFactory.getAuditEventConstructor(ElectionDayAuditEvent.class, ElectionDayAuditEvent.objectClasses(AuditEventTypes.Update),
                AuditedObjectSource.Parameters)).isNotNull();
        assertThat(AuditEventFactory.getAuditEventConstructor(ElectionDayAuditEvent.class, ElectionDayAuditEvent.objectClasses(AuditEventTypes.Delete),
                AuditedObjectSource.Parameters)).isNotNull();
    }
}
