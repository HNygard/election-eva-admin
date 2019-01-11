package no.valg.eva.admin.common.voting.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.APPROVED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.REJECTED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.TO_BE_CONFIRMED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.votingConfirmationStatuses;
import static org.testng.Assert.assertEquals;

public class VotingConfirmationStatusTest {

    @Test(dataProvider = "getNameTestData")
    public void testGetName_givenStatus_verifiesName(VotingConfirmationStatus votingConfirmationStatus, String expectedName) {
        assertEquals(votingConfirmationStatus.getName(), expectedName);
    }

    @DataProvider
    public Object[][] getNameTestData() {
        return new Object[][]{
                {TO_BE_CONFIRMED, "@voting.envelope.overview.heading.toBeConfirmed"},
                {REJECTED, "@voting.envelope.overview.heading.rejected"},
                {APPROVED, "@voting.envelope.overview.heading.approved"},
        };
    }

    @Test(dataProvider = "isValidTestData")
    public void testIsValidatedStatus_givenStatus_verifiesIsValid(VotingConfirmationStatus votingConfirmationStatus, boolean expectingIsValid) {
        assertEquals(votingConfirmationStatus.isValidatedStatus(), expectingIsValid);
    }

    @DataProvider
    public Object[][] isValidTestData() {
        return new Object[][]{
                {TO_BE_CONFIRMED, false},
                {REJECTED, true},
                {APPROVED, true},
        };
    }

    @Test(dataProvider = "votingConfirmationStatusesTestData")
    public void testVotingConfirmationStatuses_givenValidated_verifiesList(boolean validated, List<VotingConfirmationStatus> expectedList) {
        assertEquals(votingConfirmationStatuses(validated), expectedList);
    }

    @DataProvider
    public Object[][] votingConfirmationStatusesTestData() {
        return new Object[][]{
                {false, votingConfirmationStatusList(TO_BE_CONFIRMED)},
                {true, votingConfirmationStatusList(APPROVED, REJECTED)},
        };
    }

    private List<VotingConfirmationStatus> votingConfirmationStatusList(VotingConfirmationStatus... categories) {
        return Arrays.asList(categories);
    }
}