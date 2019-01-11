package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class DomainOperatorAuditEventTest extends AbstractAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(UpdateOperatorAuditEvent.class,
				UpdateOperatorAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		assertThat(createEventForDomainOperator().objectType()).isEqualTo(Operator.class);
	}

	@Test
	public void objectClasses_whenUpdate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(DomainOperatorAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(
				new Class[] { Operator.class, List.class });
	}

	private DomainOperatorAuditEvent createEventForDomainOperator() {
		ArrayList<OperatorRole> roles = Lists.newArrayList(objectMother.createOperatorRole());
		return new DomainOperatorAuditEvent(objectMother.createUserData(),
				objectMother.createDomainOperator(), roles, AuditEventTypes.Create, Outcome.Success, null);
	}

	@Test
	public void toJson_whenUpdateDomainOperator_isCorrect() throws Exception {
		List<OperatorRole> domainOperatorRoles = Lists.newArrayList(objectMother.createOperatorRole());

		Operator domainOperator = objectMother.createDomainOperator();
		UpdateOperatorAuditEvent auditEvent = new DomainOperatorAuditEvent(objectMother.createUserData(),
				domainOperator, domainOperatorRoles, AuditEventTypes.Create, Outcome.Success, "");
		String json = auditEvent.toJson();
		with(json)
				.assertThat("$", hasEntry("personId", objectMother.UID))
				.assertThat("$", hasEntry("phone", objectMother.TELEPHONE_NUMBER))
				.assertThat("$", hasEntry("email", objectMother.EMAIL))
				.assertThat("$.addedRoles[*].areaPath", hasItems(objectMother.AREA_PATH.path()))
				.assertThat("$.addedRoles[*].roleName", hasItems(objectMother.ROLE_NAME));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return DomainOperatorAuditEvent.class;
	}
}
