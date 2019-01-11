package no.valg.eva.admin.voting.application;

import no.valg.eva.admin.common.voting.model.SuggestedProcessing;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.DEAD;
import static org.testng.Assert.assertEquals;

public class VotingMapperTest {

    @Test(dataProvider = "toDtoTestData")
    public void testToDto_givenDomainVoting_verifiesDto(Voting domainVoting, VotingDto expectedVotingDto) {
        VotingDto actualVotingDto = VotingMapper.toDto(domainVoting);
        assertVotingDto(expectedVotingDto, actualVotingDto);
    }

    @DataProvider
    public Object[][] toDtoTestData() {
        Voting voting = domainVoting();
        return new Object[][]{
                {voting, votingDto(voting)}
        };
    }

    @Test(dataProvider = "toDtoListTestData")
    public void testToDtoList_givenDomainVotingList_verifiesDtoList(List<Voting> domainVotingList, List<VotingDto> expectedVotingDtoList) {
        List<VotingDto> actualVotingDtoList = VotingMapper.toDtoList(domainVotingList);
        assertEquals(actualVotingDtoList.size(), expectedVotingDtoList.size());

        for (VotingDto actualVotingDto : actualVotingDtoList) {
            assertVotingDto(actualVotingDto, expectedVotingDtoList.get(actualVotingDtoList.indexOf(actualVotingDto)));
        }
    }

    @DataProvider
    public Object[][] toDtoListTestData() {
        Voting voting = domainVoting();
        return new Object[][]{
                {Collections.singletonList(voting), Collections.singletonList(votingDto(voting))},
                {null, Collections.emptyList()}
        };
    }

    @Test(dataProvider = "toDtoTestDataWithSuggestedProcessing")
    public void testToDtoWithSuggestedProcessing_givenDomainVoting_verifiesValidDto(Voting domainVoting, SuggestedProcessing suggestedProcessing, VotingDto expectedVotingDto) {
        VotingDto actualVotingDto = VotingMapper.toDto(domainVoting, suggestedProcessing);
        assertVotingDto(actualVotingDto, expectedVotingDto);
    }

    @DataProvider
    public Object[][] toDtoTestDataWithSuggestedProcessing() {
        Voting voting = domainVoting();
        return new Object[][]{
                {voting, SuggestedProcessing.APPROVE, votingDto(voting)},
                {voting, SuggestedProcessing.DEAD, votingDto(voting, DEAD)}
        };
    }

    private VotingDto votingDto(Voting domainVoting) {
        return votingDto(domainVoting, SuggestedProcessing.APPROVE);
    }

    private VotingDto votingDto(Voting domainVoting, SuggestedProcessing suggestedProcessing) {
        return VotingDto.builder()
                .votingNumber(domainVoting.getVotingNumber())
                .votingCategory(domainVoting.getVotingCategory())
                .electionGroup(domainVoting.getElectionGroup())
                .voterDto(voterDto(domainVoting.getVoter()))
                .pollingPlace(domainVoting.getPollingPlace())
                .approved(domainVoting.isApproved())
                .ballotBoxId(domainVoting.getBallotBoxId())
                .castTimestamp(domainVoting.getCastTimeStampAsJavaTime())
                .lateValidation(domainVoting.isLateValidation())
                .receivedTimestamp(domainVoting.getReceivedTimeStampAsJavaTime())
                .removalRequest(domainVoting.getRemovalRequest())
                .validationTimestamp(domainVoting.getValidationTimeStampAsJavaTime())
                .votingRejectionDto(votingRejectionDto(domainVoting.getVotingRejection()))
                .suggestedProcessing(suggestedProcessing(domainVoting, suggestedProcessing))
                .build();
    }

    private String suggestedProcessing(Voting domainVoting, SuggestedProcessing suggestedProcessing) {
        return domainVoting.getSuggestedVotingRejection() != null ? domainVoting.getSuggestedVotingRejection().getSuggestedRejectionName() : suggestedProcessing.getName();
    }

    private VotingRejectionDto votingRejectionDto(VotingRejection votingRejection) {
        return VotingRejectionDto.builder()
                .id("votingRejectionId")
                .build();
    }

    private VoterDto voterDto(Voter voter) {
        return VoterDto.builder()
                .id(voter.getId())
                .nameLine(voter.getNameLine())
                .firstName(voter.getFirstName())
                .lastName(voter.getLastName())
                .middleName(voter.getMiddleName())
                .mvArea(voter.getMvArea())
                .build();
    }

    private Voting domainVoting() {
        Voting voting = new Voting();
        voting.setPk(1L);
        voting.setVoter(voter());
        voting.setVotingRejection(domainVotingRejection());

        return voting;
    }

    private VotingRejection domainVotingRejection() {
        VotingRejection votingRejection = new VotingRejection();
        votingRejection.setId("votingRejectionId");

        return votingRejection;
    }

    private Voter voter() {
        return Voter.builder()
                .nameLine("Velger Andre Velgesen")
                .municipalityId("1")
                .fictitious(false)
                .lastName("Velgesen")
                .firstName("Velger")
                .middleName("Andre")
                .addressLine1("Adressen")
                .id("1")
                .number(3233944321L)
                .telephoneNumber("12345678")
                .postalCode("1111")
                .electionEvent(new ElectionEvent())
                .build();
    }

    private void assertVotingDto(VotingDto votingDto, VotingDto actualVotingDto) {
        assertEquals(actualVotingDto.getVotingNumber(), votingDto.getVotingNumber());
        assertEquals(actualVotingDto.getSuggestedProcessing(), votingDto.getSuggestedProcessing());
        assertEquals(actualVotingDto.getVoterDto(), votingDto.getVoterDto());
        assertEquals(actualVotingDto.getElectionGroup(), votingDto.getElectionGroup());
        assertEquals(actualVotingDto.getVotingCategory(), votingDto.getVotingCategory());
        assertEquals(actualVotingDto.getReceivedTimestamp(), votingDto.getReceivedTimestamp());
        assertEquals(actualVotingDto.getCastTimestamp(), votingDto.getCastTimestamp());
        assertEquals(actualVotingDto.getBallotBoxId(), votingDto.getBallotBoxId());
        assertEquals(actualVotingDto.getMvArea(), votingDto.getMvArea());
        assertEquals(actualVotingDto.getPollingPlace(), votingDto.getPollingPlace());
        assertEquals(actualVotingDto.getValidationTimestamp(), votingDto.getValidationTimestamp());
        assertEquals(actualVotingDto.getVotingRejectionDto(), votingDto.getVotingRejectionDto());
        assertEquals(actualVotingDto.isApproved(), votingDto.isApproved());
        assertEquals(actualVotingDto.getRemovalRequest(), votingDto.getRemovalRequest());
    }
}
