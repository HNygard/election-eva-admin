package no.valg.eva.admin.voting.domain.service;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

public class VotingConfirmationDomainServiceTest extends MockUtilsTestCase {

    private VotingRejectionRepository votingRejectionRepository;
    private VotingConfirmationDomainService votingDomainService;
    private VotingRepository votingRepository;
    private VoterRepository voterRepository;
    private Municipality municipalityDto;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        votingDomainService = initializeMocks(VotingConfirmationDomainService.class);

        votingRejectionRepository = getInjectMock(VotingRejectionRepository.class);
        votingRepository = getInjectMock(VotingRepository.class);
        voterRepository = getInjectMock(VoterRepository.class);
        municipalityDto = municipalityDto();
    }

    private Municipality municipalityDto() {
        return Municipality.builder().build();
    }

    @Test
    public void approveVotingList_givenListOfVotings_updateVotings() {
        List<VotingDto> votingDtos = new ArrayList<>();
        votingDtos.add(buildVotingDto(1));
        votingDtos.add(buildVotingDto(2));

        Voter voter = createMock(Voter.class);
        when(voterRepository.voterOfId(isNull(), anyLong())).thenReturn(voter);

        Voting voting = createMock(Voting.class);
        mockFindVotingByVotingNumber(voting);

        votingDomainService.approveVotingList(votingDtos, municipalityDto);

        DateTime now = DateTime.now();
        ArgumentCaptor<DateTime> dateTimeArgumentCaptor = ArgumentCaptor.forClass(DateTime.class);

        verify(voting, times(2)).setMvArea(voter.getMvArea());
        verify(voting, times(2)).setApproved(true);
        verify(voting, times(2)).setValidationTimestamp(dateTimeArgumentCaptor.capture());

        for (DateTime validationTimestamp : dateTimeArgumentCaptor.getAllValues()) {
            assertThat(validationTimestamp).isBetween(now.minusSeconds(1), now);
        }
    }

    @Test(expectedExceptions = EvoteException.class)
    public void approveVotingList_givenListOfVotingsWithExistingVoting_updateVotings() {
        List<VotingDto> votingDtos = new ArrayList<>();
        votingDtos.add(buildVotingDto(1));
        votingDtos.add(buildVotingDto(2));

        Voter voter = createMock(Voter.class);
        when(voterRepository.voterOfId(isNull(), anyLong())).thenReturn(voter);

        Voting approvedVoting = createMock(Voting.class);
        when(approvedVoting.isApproved()).thenReturn(true);
        when(votingRepository.getVotingsByElectionGroupAndVoter(anyLong(), anyLong())).thenReturn(singletonList(approvedVoting));

        votingDomainService.approveVotingList(votingDtos, municipalityDto);
    }

    @Test
    public void suggestRejectVotings_givenListOfVotings_updateVotings() {
        List<VotingDto> votingDtos = new ArrayList<>();
        votingDtos.add(buildVotingDto(1));
        votingDtos.add(buildVotingDto(2));

        VotingRejectionDto votingSuggestedRejectionDto = VotingRejectionDto.builder().id("ID").build();

        Voting voting = createMock(Voting.class);
        mockFindVotingByVotingNumber(voting);

        VotingRejection votingSuggestedRejection = createMock(VotingRejection.class);
        votingSuggestedRejection.setSuggestedRejectionName("Voting Suggested Rejection");
        when(votingRejectionRepository.findById(votingSuggestedRejectionDto.getId())).thenReturn(votingSuggestedRejection);

        votingDomainService.suggestRejectVotings(votingDtos, votingSuggestedRejectionDto, municipalityDto);

        verify(voting, times(2)).setSuggestedVotingRejection(votingSuggestedRejection);
    }

    @Test
    public void rejectVotings_givenListOfVotings_updateVotings() {
        List<VotingDto> votingDtos = new ArrayList<>();
        votingDtos.add(buildVotingDto(1));
        votingDtos.add(buildVotingDto(2));

        VotingRejectionDto votingRejectionDto = VotingRejectionDto.builder().id("ID").build();

        Voting voting = createMock(Voting.class);
        mockFindVotingByVotingNumber(voting);

        VotingRejection votingRejection = createMock(VotingRejection.class);
        votingRejection.setName("Voting Rejection");
        when(votingRejectionRepository.findById(votingRejectionDto.getId())).thenReturn(votingRejection);

        votingDomainService.rejectVotings(votingDtos, votingRejectionDto, municipalityDto);

        DateTime now = DateTime.now();
        ArgumentCaptor<DateTime> dateTimeArgumentCaptor = ArgumentCaptor.forClass(DateTime.class);

        verify(voting, times(2)).setVotingRejection(votingRejection);
        verify(voting, times(2)).setApproved(false);
        verify(voting, times(2)).setValidationTimestamp(dateTimeArgumentCaptor.capture());

        for (DateTime validationTimestamp : dateTimeArgumentCaptor.getAllValues()) {
            assertThat(validationTimestamp).isBetween(now.minusSeconds(1), now);
        }

    }

    @Test
    public void testCancelRejection_givenVoting_verifiesVotingReset() {
        VotingDto votingDto = buildVotingDto(1);

        Voting voting = new Voting();
        voting.setVotingRejection(new VotingRejection());
        voting.setValidationTimestamp(new DateTime());

        mockFindVotingByVotingNumber(voting);

        votingDomainService.cancelRejection(votingDto, municipalityDto);

        verify(getInjectMock(VotingRepository.class), times(1)).findVotingByVotingNumber(municipalityDto, votingDto);
        assertNull(voting.getVotingRejection());
        assertNull(voting.getValidationTimestamp());
    }

    private void mockFindVotingByVotingNumber(Voting voting) {
        when(votingRepository.findVotingByVotingNumber(any(Municipality.class), any(VotingDto.class))).thenReturn(voting);
    }

    private VotingDto buildVotingDto(int votingNumber) {
        ElectionGroup electionGroup = createMock(ElectionGroup.class);
        VotingCategory votingCatategory = createMock(VotingCategory.class);
        VoterDto voterDto = createMock(VoterDto.class);
        return VotingDto.builder()
                .votingNumber(votingNumber)
                .electionGroup(electionGroup)
                .votingCategory(votingCatategory)
                .voterDto(voterDto)
                .build();
    }

    @Test(dataProvider = "confirmedVotings_testData", expectedExceptions = EvoteException.class)
    public void testApproveVoting_whenVotingAlreadyConfirmed_throwsEvoteException(Voting voting) {
        when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(), any())).thenReturn(voting);

        votingDomainService.approveVoting(
                createMock(VotingDto.class),
                createMock(Municipality.class)
        );
    }

    @DataProvider
    private Object[][] confirmedVotings_testData() {
        return new Object[][]{
                {approvedVoting()},
                {rejectedVoting()}
        };
    }

    private Voting approvedVoting() {
        Voting voting = new Voting();
        voting.setValidationTimestamp(new DateTime());
        voting.setVotingRejection(null);
        voting.setApproved(true);
        return voting;
    }

    private Voting rejectedVoting() {
        Voting voting = new Voting();
        voting.setValidationTimestamp(new DateTime());
        voting.setVotingRejection(new VotingRejection());
        voting.setApproved(false);
        return voting;
    }

    @Test(dataProvider = "confirmedVotings_testData", expectedExceptions = EvoteException.class)
    public void testRejectVoting_whenVotingAlreadyConfirmed_throwsEvoteException(Voting voting) {
        when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(), any())).thenReturn(voting);

        votingDomainService.rejectVoting(
                createMock(VotingDto.class),
                createMock(VotingRejectionDto.class),
                createMock(Municipality.class)
        );
    }

    @Test(dataProvider = "confirmedVotings_testData", expectedExceptions = EvoteException.class)
    public void testSuggestRejectVoting_whenVotingAlreadyConfirmed_throwsEvoteException(Voting voting) {
        when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(), any())).thenReturn(voting);

        votingDomainService.suggestRejectVotings(
                singletonList(createMock(VotingDto.class)),
                createMock(VotingRejectionDto.class),
                createMock(Municipality.class)
        );
    }

    @Test(expectedExceptions = EvoteException.class)
    public void testCancelRejectVoting_whenVotingAlreadyApproved_throwsEvoteException() {
        when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(), any())).thenReturn(approvedVoting());

        votingDomainService.cancelRejection(
                createMock(VotingDto.class),
                createMock(Municipality.class)
        );
    }
}