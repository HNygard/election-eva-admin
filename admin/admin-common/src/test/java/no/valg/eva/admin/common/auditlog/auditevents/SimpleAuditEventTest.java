package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.testng.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.Test;

public class SimpleAuditEventTest {

	private static final String UID = "221100123456";
	private static final SecurityLevel SECURITY_LEVEL = SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC;
	private static final Locale LOCALE = new Locale();
	private static final String ROLE = "valgansvarlig_kommune";
	private static final AreaPath AREA_PATH = AreaPath.from("950000.47.03.0301");
	private static final ElectionPath ELECTION_PATH = ElectionPath.from("950000.01");
	private static final InetAddress INET_ADDRESS;

	static {
		try {
			INET_ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void createEvent_withoutOperatorRole_isAllowed() throws Exception {
		UserData userData = createUserData(null);

		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.SubConfigEvent).withOutcome(Outcome.GenericError).build();

		assertThat(auditEvent.uid()).isEqualTo(UID);
		assertThat(auditEvent.clientIpAddress()).isEqualTo(INET_ADDRESS);
		assertThat(auditEvent.timestamp()).isNotNull();
	}

	@Test
	public void createEvent_withOperatorRole_isAllowed() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorSelectedRole).withOutcome(Success).build();

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void createEvent_withoutOutcomeForSingleOutcomeEventType_isAllowed() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorLoggedOut).build();

		assertThat(auditEvent).isNotNull();
	}

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Outcome is required", enabled = false)
	// no simple audit event types currently have multiple outcomes
	public void createEvent_withoutOutcomeForMultipleOutcomeEventType_isNotAllowed() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorLoggedOut).build();
	}

	@Test
	public void enumValue_isSerializable() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorLoggedOut).build();

		assertNotNull(SerializationUtils.deserialize(SerializationUtils.serialize(auditEvent)));
	}

	@Test
	public void enumValue_toString() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorLoggedOut).build();

		assertThat(auditEvent.toString()).isEqualTo("SimpleAuditEvent[uid=221100123456,roleId=valgansvarlig_kommune,"
				+ "roleAreaPath=950000.47.03.0301,eventType=OperatorLoggedOut,outcome=Success]");
	}

	@Test
	public void detail_isIncludedInLogStatement() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.AccessDeniedInBackend).withDetail("Something bad").build();
		assertThat(auditEvent.toString()).contains("detail=Something bad");
	}

	@Test
	public void jsonFields_arePresent() throws Exception {
		UserData userData = createUserData(createOperatorRole());
		AuditEvent auditEvent = SimpleAuditEvent
				.from(userData)
				.withProcess(Process.CENTRAL_CONFIGURATION)
				.ofType(Create)
				.withAuditObjectProperty("field", "value")
				.withOutcome(Success)
				.build();
		with(auditEvent.toJson()).assertThat("$", hasEntry("field", "value"));
	}

	private UserData createUserData(OperatorRole operatorRole) throws UnknownHostException {
		UserData userData = new UserData(UID, SECURITY_LEVEL, LOCALE, INET_ADDRESS);
		userData.setOperatorRole(operatorRole);
		return userData;
	}

	private OperatorRole createOperatorRole() {
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent(1L));

		Role role = new Role();
		role.setId(ROLE);

		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(AREA_PATH.path());

		MvElection mvElection = new MvElection();
		mvElection.setElectionPath(ELECTION_PATH.path());

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(operator);
		operatorRole.setRole(role);
		operatorRole.setMvArea(mvArea);
		operatorRole.setMvElection(mvElection);

		return operatorRole;
	}
}
