package no.valg.eva.admin.common.voting.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VotingConfirmationDtoReportTest {

    @Test
    public void testReport_givenReport_verifiesFields() {
        int numberOfApprovedVotings = 10;
        int numberOfRejectedVotings = 20;
        VotingConfirmationReportDto votingConfirmationReportDto = VotingConfirmationReportDto.builder()
                .numberOfApprovedVotings(numberOfApprovedVotings)
                .numberOfRejectedVotings(numberOfRejectedVotings)
                .build();

        VotingDto votingDto = VotingDto.builder()
                .build();
        votingConfirmationReportDto.addVotingToVerify(votingDto);

        assertEquals(votingConfirmationReportDto.getNumberOfApprovedVotings(), numberOfApprovedVotings);
        assertEquals(votingConfirmationReportDto.getNumberOfRejectedVotings(), numberOfRejectedVotings);
        assertEquals(votingConfirmationReportDto.getNumberOfVotingsToConfirm(), 1);
        assertEquals(votingConfirmationReportDto.getVotingDtoListToConfirm().get(0), votingDto);
    }
}