package no.valg.eva.admin.common.voting;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VotingPhaseTest {

    @Test(dataProvider = "isBeforeTestData")
    public void testIsBefore(VotingPhase votingPhase, VotingPhase otherPhase, boolean expectsBefore) {
        assertEquals(votingPhase.isBefore(otherPhase), expectsBefore);
    }

    @DataProvider
    public Object[][] isBeforeTestData() {
        return new Object[][]{
                {VotingPhase.EARLY, VotingPhase.EARLY, false},
                {VotingPhase.EARLY, VotingPhase.ADVANCE, true},
                {VotingPhase.EARLY, VotingPhase.ELECTION_DAY, true},
                {VotingPhase.EARLY, VotingPhase.LATE, true},
                {VotingPhase.ADVANCE, VotingPhase.EARLY, false},
                {VotingPhase.ADVANCE, VotingPhase.ADVANCE, false},
                {VotingPhase.ADVANCE, VotingPhase.ELECTION_DAY, true},
                {VotingPhase.ADVANCE, VotingPhase.LATE, true},
                {VotingPhase.ELECTION_DAY, VotingPhase.EARLY, false},
                {VotingPhase.ELECTION_DAY, VotingPhase.ADVANCE, false},
                {VotingPhase.ELECTION_DAY, VotingPhase.ELECTION_DAY, false},
                {VotingPhase.ELECTION_DAY, VotingPhase.LATE, true},
                {VotingPhase.LATE, VotingPhase.EARLY, false},
                {VotingPhase.LATE, VotingPhase.ADVANCE, false},
                {VotingPhase.LATE, VotingPhase.ELECTION_DAY, false},
                {VotingPhase.LATE, VotingPhase.LATE, false},
        };
    }
}