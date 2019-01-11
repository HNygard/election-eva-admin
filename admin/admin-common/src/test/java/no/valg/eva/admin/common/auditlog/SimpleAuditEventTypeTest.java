package no.valg.eva.admin.common.auditlog;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleAuditEventTypeTest {
	@Test(enabled = false) // no simple audit event types currently have multiple outcomes
	public void enumValue_withMultipleOutcomes() {
		SimpleAuditEventType multipleOutcomeType = SimpleAuditEventType.OperatorLoggedOut;
		assertThat(multipleOutcomeType.hasSingleOutcome()).isFalse();
		assertThat(multipleOutcomeType.isValidOutcome(Outcome.Success));
	}

	@Test
	public void enumValue_withSingleOutcome() {
		SimpleAuditEventType singleOutcomeType = SimpleAuditEventType.OperatorLoggedOut;
		assertThat(singleOutcomeType.hasSingleOutcome()).isTrue();
		assertThat(singleOutcomeType.isValidOutcome(Outcome.Success)).isTrue();
		assertThat(singleOutcomeType.isValidOutcome(Outcome.GenericError)).isFalse();
		assertThat(singleOutcomeType.getSingleOutcome()).isEqualTo(Outcome.Success);
	}
}
