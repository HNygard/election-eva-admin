package no.valg.eva.admin.common.voting.model;

import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static org.testng.Assert.assertEquals;

public class VotingDtoTest {

    @Test
    public void testGetters_GivenVoting_verifiesFields() {
        VotingRejectionDto votingRejectionDto = VotingRejectionDto.builder().build();
        boolean approved = false;
        VoterDto voterDto = VoterDto.builder().build();
        String ballotBoxId = "ballotBoxId";
        LocalDateTime castTimeStamp = LocalDateTime.now();
        Integer votingNumber = 1;
        ElectionGroup electionGroup = new ElectionGroup();
        boolean lateValidation = true;
        MvArea mvArea = new MvArea();
        PollingPlace pollingPlace = new PollingPlace();
        LocalDateTime receivedTimeStamp = LocalDateTime.now();
        String removalRequest = "removalRequest";
        LocalDateTime validationTimeStamp = LocalDateTime.now();
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();

        VotingDto votingDto = VotingDto.builder()
                .approved(approved)
                .votingRejectionDto(votingRejectionDto)
                .voterDto(voterDto)
                .ballotBoxId(ballotBoxId)
                .castTimestamp(castTimeStamp)
                .votingNumber(votingNumber)
                .electionGroup(electionGroup)
                .lateValidation(lateValidation)
                .mvArea(mvArea)
                .pollingPlace(pollingPlace)
                .receivedTimestamp(receivedTimeStamp)
                .removalRequest(removalRequest)
                .validationTimestamp(validationTimeStamp)
                .votingCategory(votingCategory)
                .build();

        assertEquals(votingDto.getVotingRejectionDto(), votingRejectionDto);
        assertEquals(votingDto.isApproved(), approved);
        assertEquals(votingDto.getVoterDto(), voterDto);
        assertEquals(votingDto.getBallotBoxId(), ballotBoxId);
        assertEquals(votingDto.getCastTimestamp(), castTimeStamp);
        assertEquals(votingDto.getVotingNumber(), votingNumber);
        assertEquals(votingDto.getElectionGroup(), electionGroup);
        assertEquals(votingDto.isLateValidation(), lateValidation);
        assertEquals(votingDto.getMvArea(), mvArea);
        assertEquals(votingDto.getPollingPlace(), pollingPlace);
        assertEquals(votingDto.getReceivedTimestamp(), receivedTimeStamp);
        assertEquals(votingDto.getRemovalRequest(), removalRequest);
        assertEquals(votingDto.getValidationTimestamp(), validationTimeStamp);
        assertEquals(votingDto.getVotingCategory(), votingCategory);
    }
}