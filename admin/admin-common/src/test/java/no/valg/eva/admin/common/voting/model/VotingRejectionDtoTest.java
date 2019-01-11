package no.valg.eva.admin.common.voting.model;

import no.valg.eva.admin.common.configuration.status.ContestStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VotingRejectionDtoTest {

    @Test
    public void testGetters() {
        String name = ContestStatus.FINISHED_CONFIGURATION.name();
        String id = "id";
        boolean earlyVoting = true;

        VotingRejectionDto votingRejectionDto = VotingRejectionDto.builder()
                .name(name)
                .id(id)
                .earlyVoting(earlyVoting)
                .build();

        assertEquals(votingRejectionDto.getId(), id);
        assertEquals(votingRejectionDto.getName(), name);
        assertEquals(votingRejectionDto.isEarlyVoting(), earlyVoting);
    }
}