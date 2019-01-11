package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;

import org.testng.annotations.Test;

public class UpdateOperatorAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(UpdateOperatorAuditEvent.class,
				UpdateOperatorAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		AreaPath areaPath = null;
		Collection<RoleAssociation> addedRoles = null;
		Collection<RoleAssociation> deletedRoles = null;
		UpdateOperatorAuditEvent auditEvent = new UpdateOperatorAuditEvent(objectMother.createUserData(),
				objectMother.createOperator(), areaPath, addedRoles, deletedRoles, AuditEventTypes.Update, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Operator.class);
	}

	@Test
	public void objectClasses_whenUpdate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(UpdateOperatorAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(
				new Class[] { Operator.class, AreaPath.class, Collection.class, Collection.class });
	}

	@Test
	public void toJson_whenUpdate_isCorrect() throws Exception {
		Collection<RoleAssociation> addedRoles = objectMother.createRoleAssociations();
		Collection<RoleAssociation> deletedRoles = objectMother.createRoleAssociations();
		UpdateOperatorAuditEvent auditEvent = new UpdateOperatorAuditEvent(objectMother.createUserData(),
				objectMother.createOperator(), objectMother.AREA_PATH, addedRoles, deletedRoles, AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).contains("{\"personId\":\"" + objectMother.UID);
		assertThat(json).contains("areaPath\":\"" + objectMother.AREA_PATH);
		assertThat(json).contains("areaPath\":\"" + objectMother.AREA_PATH_1);
		assertThat(json).contains("areaPath\":\"" + objectMother.AREA_PATH_2);
	}

}
