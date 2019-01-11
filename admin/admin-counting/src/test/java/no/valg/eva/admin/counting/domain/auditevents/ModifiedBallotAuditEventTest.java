package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.ModifiedBallotAuditEvent;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class ModifiedBallotAuditEventTest {
    
    private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

    protected ModifiedBallot modifiedBallot;
    private static final String ID = "ID_1234";
    private static final int SERIAL_NUMBER = 123456_1;
    private static final String AP_AFFILIATION = "AP";
    
    @BeforeMethod
    public void setUp() {
        modifiedBallot = new ModifiedBallot(new BatchId(ID), SERIAL_NUMBER, AP_AFFILIATION, new BallotId(ID), false);
        modifiedBallot.addCandidatesForPersonVotes(mockCandidates());
        modifiedBallot.setWriteIns(mockCandidates());
    }

    private Set<Candidate> mockCandidates() {
        Set<Candidate> candidates = new HashSet<>();
        candidates.add(new Candidate(new CandidateRef(1L)));
        candidates.add(new Candidate(new CandidateRef(2L)));
        return candidates;
    }

    @Test
    public void toJson_withNormalValues_isCorrect() throws Exception {
        ModifiedBallotAuditEvent auditEvent = new ModifiedBallotAuditEvent(objectMother.createUserData(), modifiedBallot, AuditEventTypes.Update,
                Outcome.Success, "details, details");

        assertThat(auditEvent.objectType()).isEqualTo(ModifiedBallot.class);
        JsonAssert.with(auditEvent.toJson())
                .assertThat("$", Matchers.hasEntry("ballotId", ID))
                .assertThat("$", Matchers.hasEntry("batchId", ID))
                .assertThat("$", Matchers.hasEntry("writeInCandidate_1", 1))
                .assertThat("$", Matchers.hasEntry("writeInCandidate_2", 2));
    }

    @Test
    public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
        assertThat(ModifiedBallotAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(new Class[] {ModifiedBallot.class });
    }

    @Test
    public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
        assertThat(AuditEventFactory.getAuditEventConstructor(ModifiedBallotAuditEvent.class,
                ModifiedBallotAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
    }
}
