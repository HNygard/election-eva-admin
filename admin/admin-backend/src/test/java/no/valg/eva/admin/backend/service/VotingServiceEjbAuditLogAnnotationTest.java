package no.valg.eva.admin.backend.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class VotingServiceEjbAuditLogAnnotationTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void updateAdvanceVotingApproved_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		Voting returnValue = objectMother.createVoting();
		Method method = VotingServiceEjb.class.getDeclaredMethod("updateAdvanceVotingApproved", UserData.class, Voting.class);

		AuditEventFactory auditEventFactory = new AuditEventFactory(method, new Object[] { objectMother.createUserData(), mock(Voting.class) });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(returnValue);

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void updateAdvanceVotingsApproved_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		Method method = VotingServiceEjb.class.getDeclaredMethod("updateAdvanceVotingsApproved", UserData.class, Long.class, Long.TYPE, Long.class,
				LocalDate.class, LocalDate.class, Integer.TYPE, Integer.TYPE);

		AuditEventFactory auditEventFactory = new AuditEventFactory(method,
				new Object[] { objectMother.createUserData(), 1L, 1L, 1L, LocalDate.now(), LocalDate.now(), 1, 1 });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(1);

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void updateElectionDayVotingsApproved_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		Method method = VotingServiceEjb.class.getDeclaredMethod("updateElectionDayVotingsApproved", UserData.class, Long.TYPE, Long.class,
				Integer.TYPE, Integer.TYPE, String[].class);

		AuditEventFactory auditEventFactory = new AuditEventFactory(method, new Object[] { objectMother.createUserData(), 1L, 1L, 1, 1, null });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(1);

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void deleteVotings_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		UserData operatorUserData = objectMother.createUserData();
		Method method = VotingServiceEjb.class.getDeclaredMethod("deleteVotings", UserData.class, MvElection.class, MvArea.class, Integer.class);

		AuditEventFactory auditEventFactory = new AuditEventFactory(method,
				new Object[] { operatorUserData, mock(MvElection.class), operatorUserData.getOperatorMvArea(), 1 });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(null);

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void markOffVoterAdvance_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		UserData operatorUserData = objectMother.createUserData();
		Method method = VotingServiceEjb.class.getDeclaredMethod("markOffVoterAdvance", UserData.class, PollingPlace.class, ElectionGroup.class, Voter.class,
				Boolean.TYPE, String.class, String.class, VotingPhase.ADVANCE.getClass());

		AuditEventFactory auditEventFactory = new AuditEventFactory(method, new Object[] { operatorUserData, mock(PollingPlace.class), mock(ElectionGroup.class), mock(Voter.class), 1L, true, "a", "b", VotingPhase.ADVANCE });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(objectMother.createVoting());

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void registerVoteCentrally_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		UserData operatorUserData = objectMother.createUserData();
		Method method = VotingServiceEjb.class.getDeclaredMethod("registerVoteCentrally", UserData.class, ElectionGroup.class, Voter.class, String.class,
				MvArea.class, VotingPhase.ELECTION_DAY.getClass());

		AuditEventFactory auditEventFactory = new AuditEventFactory(method,
				new Object[] { operatorUserData, mock(ElectionGroup.class), mock(Voter.class), "a", mock(MvArea.class), VotingPhase.ELECTION_DAY });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(objectMother.createVoting());

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void markOffVoter_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		UserData operatorUserData = objectMother.createUserData();
		Method method = VotingServiceEjb.class.getDeclaredMethod("markOffVoter", UserData.class, PollingPlace.class, ElectionGroup.class, Voter.class,
				Boolean.TYPE, VotingPhase.ADVANCE.getClass());

		AuditEventFactory auditEventFactory = new AuditEventFactory(method,
				new Object[] { operatorUserData, mock(PollingPlace.class), mock(ElectionGroup.class), mock(Voter.class),
						true, true, mock(MvArea.class), true, VotingPhase.ADVANCE });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(objectMother.createVoting());

		assertThat(auditEvent).isNotNull();
	}

	@Test
	public void markOffVoterAdvanceVoteInBallotBox_givenAuditlogAnnotation_auditEventCanBeConstructed() throws Exception {
		UserData operatorUserData = objectMother.createUserData();
		Method method = VotingServiceEjb.class.getDeclaredMethod("markOffVoterAdvanceVoteInBallotBox", UserData.class, PollingPlace.class, ElectionGroup.class,
				Voter.class, Boolean.TYPE, VotingPhase.ADVANCE.getClass());

		AuditEventFactory auditEventFactory = new AuditEventFactory(method,
				new Object[] { operatorUserData, mock(PollingPlace.class), mock(ElectionGroup.class), mock(Voter.class), true, VotingPhase.ADVANCE });
		AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(objectMother.createVoting());

		assertThat(auditEvent).isNotNull();
	}
}
