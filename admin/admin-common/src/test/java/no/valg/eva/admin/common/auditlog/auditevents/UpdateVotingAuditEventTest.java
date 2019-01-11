package no.valg.eva.admin.common.auditlog.auditevents;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateVotingAuditEventTest {

    private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

    @Test
    public void toJson_withNoVoting_retunsJson() {
        UpdateVotingAuditEvent updateVotingAuditEvent = createUpdateVotingAuditEvent(null);
        assertThat(updateVotingAuditEvent.toJson()).isNotEmpty();
    }

    @Test
    public void toJson_withVoting_retunsJson() {
        UpdateVotingAuditEvent updateVotingAuditEvent = createUpdateVotingAuditEvent(objectMother.createVoting());
        assertThat(updateVotingAuditEvent.toJson()).isNotEmpty();
    }

    private UpdateVotingAuditEvent createUpdateVotingAuditEvent(Voting voting) {
        return new UpdateVotingAuditEvent(objectMother.createUserData(), voting, AuditEventTypes.Update, Success, "detail");
    }

}