package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasEntry;

import java.util.TimeZone;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

public class VotingAuditEventTest extends AbstractAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		Voter voter = objectMother.createVoter();
		Voting voting = objectMother.createVoting(voter, "FI");
		voting.setPk(1L);
		voting.setVotingNumber(1);
		voting.setApproved(true);

		VotingAuditEvent auditEvent = buildVotingAuditEvent(voter, voting);

		assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("voterId", AuditLogTestsObjectMother.UID))
				.assertThat("$", hasEntry("votingCategory", "FI"))
				.assertThat("$", hasEntry("votingNumber", 1))
				.assertThat("$", hasEntry("electoralRollArea", AuditLogTestsObjectMother.AREA_PATH_1.path()))
				.assertThat("$", hasEntry("pollingPlaceId", voting.getPollingPlace().getId()))
				.assertThat("$", hasEntry("pollingPlaceName", voting.getPollingPlace().getName()))
				.assertThat("$", hasEntry("approved", true))
				.assertThat("$", hasEntry("votingPk", 1));
	}

	@Test
	public void toJson_whenVoterIsNotInElectoralRoll_voterIdIsNull() throws Exception {
		Voter voter = objectMother.createVoter();
		voter.setFictitious(true); // not in electoral roll
		Voting voting = objectMother.createVoting(voter, "FI");

		VotingAuditEvent auditEvent = buildVotingAuditEvent(voter, voting);

		with(auditEvent.toJson()).assertThat("$", hasEntry("voterId", null));
	}

	@Test
	public void toJson_forAdvanceVoting_lateValidationIsPresent() throws Exception {
		Voter voter = objectMother.createVoter();
		Voting voting = objectMother.createVoting(voter, "FI");
		assertThat(voting.isAdvanceVoting()).isTrue();
		assertThat(voting.isLateValidation()).isFalse();

		VotingAuditEvent auditEvent = buildVotingAuditEvent(voter, voting);

		with(auditEvent.toJson())
				.assertThat("$", hasEntry("lateValidation", false));
	}

	@Test
	public void toJson_forElectionDayVoting_lateValidationIsNotPresent() throws Exception {
		Voter voter = objectMother.createVoter();
		Voting voting = objectMother.createVoting(voter, "VO");
		assertThat(voting.isAdvanceVoting()).isFalse();
		assertThat(voting.isLateValidation()).isFalse(); // default value - makes no sense for VO

		VotingAuditEvent auditEvent = buildVotingAuditEvent(voter, voting);

		with(auditEvent.toJson())
				.assertThat("$", not(hasEntry("lateValidation", false)));
	}

	
	@Test
	public void toJson_timestampsAreIso8601() {
		Voter voter = objectMother.createVoter();
		Voting voting = objectMother.createVoting(voter, "FI");
		DateTime dateTime = new DateTime(1900, 12, 24, 14, 50, DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Oslo")));
		voting.setCastTimestamp(dateTime);
		voting.setReceivedTimestamp(dateTime);
		voting.setValidationTimestamp(dateTime);

		VotingAuditEvent auditEvent = buildVotingAuditEvent(voter, voting);

		with(auditEvent.toJson())
				.assertThat("$", hasEntry("castTime", "1900-12-24T14:50:00+0100"))
				.assertThat("$", hasEntry("receivedTime", "1900-12-24T14:50:00+0100"))
				.assertThat("$", hasEntry("validatedTime", "1900-12-24T14:50:00+0100"));
	}


	private VotingAuditEvent buildVotingAuditEvent(Voter voter, Voting voting) {
		return new VotingAuditEvent(objectMother.createUserData(), voting.getPollingPlace(), voter, voting,
				AuditEventTypes.Create, Outcome.Success, "details, details");
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return VotingAuditEvent.class;
	}
}
