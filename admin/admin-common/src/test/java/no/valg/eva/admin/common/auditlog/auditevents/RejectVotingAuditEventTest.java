package no.valg.eva.admin.common.auditlog.auditevents;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.allOf;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.UpdateAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;

public class RejectVotingAuditEventTest {

    private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();


    @Test
    public void toJson_withNormalValues_isCorrect() {
        RejectVotingAuditEvent auditEvent = buildAuditEvent();
        System.out.println("auditEvent.toJson() = " + auditEvent.toJson());
        assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
        with(auditEvent.toJson())
                .assertThat("$.votings[*].voterId", contains("VoterId"))
                .assertThat("$.votings[*].voterId", contains("VoterId"))
                .assertThat("$.votings[*].voterId", contains("VoterId"))
                .assertThat("$", hasEntry("votingRejectionId", "VotingRejectionId"))
                .assertThat("$", hasEntry("municipalityPk", 9));
    }

    private RejectVotingAuditEvent buildAuditEvent() {
        return new RejectVotingAuditEvent(objectMother.createUserData(), votingDtoList(), votingRejectionDto(), municipality(),
                AuditEventTypes.UpdateAll, Outcome.Success, "details, details");
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

    private VotingRejectionDto votingRejectionDto() {
        return VotingRejectionDto.builder()
                .id("VotingRejectionId")
                .build();
    }

    private Municipality municipality() {
        return Municipality.builder()
                .pk(9L)
                .build();
    }

    @Test
    public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
        assertThat(RejectVotingAuditEvent.objectClasses(AuditEventTypes.UpdateAll)).isEqualTo(new Class[]{List.class, VotingRejectionDto.class, Municipality.class});
    }

    @Test(dataProvider = "testObjectClassesInvalidTestTypes", expectedExceptions = IllegalArgumentException.class)
    public void testObjectClasses_givenInValidAuditEventTypes_verifiesException(AuditEventTypes auditEventType) {
        RejectVotingAuditEvent.objectClasses(auditEventType);
    }

    @DataProvider
    public static Object[][] testObjectClassesInvalidTestTypes() {
        return allOf(AuditEventTypes.class).stream()
                .filter(auditEventType -> UpdateAll != auditEventType)
                .map(auditEventType -> new Object[]{auditEventType})
                .toArray(Object[][]::new);
    }
}