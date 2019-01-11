package no.valg.eva.admin.common.auditlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.auditevents.AffiliationAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionDayAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionEventAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionGroupAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.VotingAuditEvent;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class AuditEventFactoryTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();
	private Method someCreateMethod;
	private Method notAuditedMethod;
	private Method createMethodWithReturnObject;
	private Method createMethodWithPrimitive;
	private Method createMethodWithMultipleParametersOfSameType;
	private Method createMethodWithParametersAndReturnValue;
	private Object[] createParameters;

	@BeforeMethod
	public void setUp() throws Exception {
		UserData userData = objectMother.createUserData(objectMother.createOperatorRole());
		ElectionEvent electionEvent = objectMother.createElectionEvent();
		Set<Locale> locales = objectMother.createLocales();

		someCreateMethod = FakeClass.class.getDeclaredMethod("someCreateMethod", UserData.class, ElectionEvent.class, ElectionEvent.class, Set.class);
		notAuditedMethod = FakeClass.class.getDeclaredMethod("notAuditedMethod");
		createMethodWithReturnObject = FakeClass.class.getDeclaredMethod("createMethodWithAuditedReturnObject", UserData.class, Affiliation.class);
		createMethodWithPrimitive = FakeClass.class.getDeclaredMethod("createMethodWithPrimitive", UserData.class, Long.class, boolean.class);
		createMethodWithMultipleParametersOfSameType = FakeClass.class.getDeclaredMethod("createMethodWithMultipleParametersOfSameType", UserData.class,
				Affiliation.class, int.class, int.class);
		createMethodWithParametersAndReturnValue =
				FakeClass.class.getDeclaredMethod("createMethodWithParametersAndReturnValue", UserData.class, PollingPlace.class, Voter.class);
		createParameters = new Object[] { userData, electionEvent, electionEvent, locales };
	}

	@Test
	public void isAuditableEvent_whenMethodIsNotAnnotated_isFalse() {
		AuditEventFactory auditEventFactory = new AuditEventFactory(notAuditedMethod, createParameters);
		assertThat(auditEventFactory.isAuditedInvocation()).isFalse();
	}

	@Test
	public void isAuditableEvent_whenMethodIsAnnotated_isTrue() {
		AuditEventFactory auditEventFactory = new AuditEventFactory(someCreateMethod, createParameters);
		assertThat(auditEventFactory.isAuditedInvocation()).isTrue();
	}

	@Test
	public void buildSuccessfulAuditEvent_forCreate_buildsAuditEvent() {
		AuditEventFactory auditEventFactory = new AuditEventFactory(someCreateMethod, createParameters);

		AuditEvent auditEvent = (AuditEvent) auditEventFactory.buildSuccessfulAuditEvent(new Object());

		assertThat(auditEvent.objectType()).isEqualTo(ElectionEvent.class);
		assertThat(auditEvent.eventType()).isEqualTo(AuditEventTypes.Create);
		assertThat(auditEvent.outcome()).isEqualTo(Outcome.Success);
	}

	@Test
	public void buildErrorAuditEvent_forCreate_buildsAuditEvent() {
		AuditEventFactory auditEventFactory = new AuditEventFactory(someCreateMethod, createParameters);

		AuditEvent auditEvent = (AuditEvent) auditEventFactory.buildErrorAuditEvent("Fail");

		assertThat(auditEvent.objectType()).isEqualTo(ElectionEvent.class);
		assertThat(auditEvent.eventType()).isEqualTo(AuditEventTypes.Create);
		assertThat(auditEvent.outcome()).isEqualTo(Outcome.GenericError);
		assertThat(auditEvent.detail()).isEqualTo("Fail");
	}

	@Test
	public void buildSuccessfulAuditEvent_forCreateWithAuditObjectFromResponse_buildsAuditEvent() {
		Affiliation affiliationParameter = mock(Affiliation.class);
		Object[] parameters = { objectMother.createUserData(), affiliationParameter };
		AuditEventFactory auditEventFactory = new AuditEventFactory(createMethodWithReturnObject, parameters);

		Affiliation returnValue = new Affiliation();
		AffiliationAuditEvent auditEvent = (AffiliationAuditEvent) auditEventFactory.buildSuccessfulAuditEvent(returnValue);

		assertThat(auditEvent.getAffiliation()).isEqualTo(returnValue);
		assertThat(auditEvent.objectType()).isEqualTo(Affiliation.class);
		assertThat(auditEvent.eventType()).isEqualTo(AuditEventTypes.Create);
		assertThat(auditEvent.outcome()).isEqualTo(Outcome.Success);
	}

	@Test
	public void buildSuccessfulAuditEvent_forCreateWithAuditObjectsBothFromParametersAndResponse_buildsAuditEvent() {
		PollingPlace pollingPlaceParameter = mock(PollingPlace.class);
		Voter voterParameter = mock(Voter.class);
		Voting votingReturnParameter = mock(Voting.class);
		Object[] parameters = {objectMother.createUserData(), pollingPlaceParameter, voterParameter};

		AuditEventFactory auditEventFactory = new AuditEventFactory(createMethodWithParametersAndReturnValue, parameters);
		VotingAuditEvent votingAuditEvent = (VotingAuditEvent) auditEventFactory.buildSuccessfulAuditEvent(votingReturnParameter);

		assertThat(votingAuditEvent.objectType()).isEqualTo(Voting.class);
	}

	@Test
	public void getAuditedParameters_ShouldHandlePrimitives() {
		Long municipalityParameter = 1L;
		Object[] parameters = { objectMother.createUserData(), municipalityParameter, true };
		AuditEventFactory auditEventFactory = new AuditEventFactory(createMethodWithPrimitive, parameters);

		auditEventFactory.buildSuccessfulAuditEvent(null);
	}

	@Test
	public void getAuditedParameters_ShouldHandleMethodsWithMultipleParametersOfSameType() {
		Affiliation affiliationParameter = mock(Affiliation.class);
		int from = 1;
		
		int to = 5;
		
		Object[] parameters = { objectMother.createUserData(), affiliationParameter, from, to };
		AuditEventFactory auditEventFactory = new AuditEventFactory(createMethodWithMultipleParametersOfSameType, parameters);

		AffiliationAuditEvent auditEvent = (AffiliationAuditEvent) auditEventFactory.buildSuccessfulAuditEvent(null);

		assertThat(auditEvent.getReorderFrom()).isEqualTo(1);
		
		assertThat(auditEvent.getReorderTo()).isEqualTo(5);
		
	}

	private interface FakeClass {
		@AuditLog(eventClass = ElectionEventAuditEvent.class, eventType = AuditEventTypes.Create)
		@SuppressWarnings("unused")
		Boolean someCreateMethod(UserData userData, ElectionEvent electionEventTo, ElectionEvent electionEventFrom, Set<Locale> locales);

		@AuditLog(eventClass = ElectionEventAuditEvent.class, eventType = AuditEventTypes.Update)
		@SuppressWarnings("unused")
		Boolean someUpdateMethod(UserData userData, ElectionEvent electionEventTo, Set<Locale> locales);

		@AuditLog(eventClass = ElectionDayAuditEvent.class, eventType = AuditEventTypes.Delete)
		@SuppressWarnings("unused")
		void someDeleteMethod(UserData userData, Long pk);

		@SuppressWarnings("unused")
		Boolean notAuditedMethod();

		@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ReturnValue)
		@SuppressWarnings("unused")
		Affiliation createMethodWithAuditedReturnObject(UserData userData, Affiliation affiliation);

		@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.DisplayOrderChanged)
		@SuppressWarnings("unused")
		List<Affiliation> createMethodWithMultipleParametersOfSameType(UserData userData, Affiliation affiliation, int fromPosition, int toPosition);

		@AuditLog(eventClass = ElectionGroupAuditEvent.class, eventType = AuditEventTypes.PartialUpdate)
		@SuppressWarnings("unused")
		void createMethodWithPrimitive(UserData userData, Long pk, boolean isRequiredProtocolCount);

		@AuditLog(eventClass = VotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
		Voting createMethodWithParametersAndReturnValue(UserData userData, PollingPlace pollingPlace, Voter voter);

	}

}
