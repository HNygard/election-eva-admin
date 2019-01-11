package no.valg.eva.admin.common.auditlog.auditevents;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.EnumSet.allOf;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.UpdateAll;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class ConfirmAdvanceVotingsApprovedAuditEventTest extends MockUtilsTestCase {

    private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

    @Test
    public void toJson_withNormalValues_isCorrect() {
        ConfirmAdvanceVotingsApprovedAuditEvent auditEvent = auditEvent();

        assertThat(auditEvent.objectType()).isEqualTo(Voting.class);

        assertEquals(auditEvent.toJson(), "{\"votings\":[{\"voterId\":\"VoterId\",\"votingNumber\":99,\"votingCategoryId\":\"VotingCategoryId\"}],\"municipalityPk\":9}");
    }

    @Test
    public void testObjectClasses_givenValidAuditEventTypes_verifiesClasses() {
        Class[] classes = ConfirmAdvanceVotingsApprovedAuditEvent.objectClasses(UpdateAll);
        assertEquals(classes[0], List.class);
        assertEquals(classes[1], Municipality.class);
    }

    @Test(dataProvider = "testObjectClassesInvalidTestTypes", expectedExceptions = IllegalArgumentException.class)
    public void testObjectClasses_givenInValidAuditEventTypes_verifiesException(AuditEventTypes auditEventType) {
        ConfirmAdvanceVotingsApprovedAuditEvent.objectClasses(auditEventType);
    }

    @DataProvider
    public static Object[][] testObjectClassesInvalidTestTypes() {
        return allOf(AuditEventTypes.class).stream()
                .filter(auditEventType -> UpdateAll != auditEventType)
                .map(auditEventType ->
                        new Object[]{auditEventType})
                .toArray(Object[][]::new);
    }

    private ConfirmAdvanceVotingsApprovedAuditEvent auditEvent() {
        return new ConfirmAdvanceVotingsApprovedAuditEvent(objectMother.createUserData(), votingDtoList(), municipality(), UpdateAll, Success, "detail");
    }

    private List<VotingDto> votingDtoList() {
        VotingDto votingDto = VotingDto.builder()
                .voterDto(voterDto())
                .votingNumber(99)
                .votingCategory(votingCategory())
                .build();
        return singletonList(votingDto);
    }

    private VoterDto voterDto() {
        return VoterDto.builder()
                .id("VoterId")
                .build();
    }

    private VotingCategory votingCategory() {
        VotingCategory votingCategory = new VotingCategory();
        votingCategory.setId("VotingCategoryId");
        return votingCategory;
    }

    private Municipality municipality() {
        return Municipality.builder()
                .pk(9L)
                .build();
    }
}