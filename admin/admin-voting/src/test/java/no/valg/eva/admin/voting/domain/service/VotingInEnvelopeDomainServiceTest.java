package no.valg.eva.admin.voting.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.PagedList;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VotingApprovalState;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.application.VotingMapper;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.PagingVotingRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.voting.application.VotingMapper.toDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class VotingInEnvelopeDomainServiceTest extends MockUtilsTestCase {
    private no.valg.eva.admin.configuration.domain.model.Municipality municipality;
    private VotingInEnvelopeDomainService domainService;
    private ElectionGroup electionGroup;
    private UserData userData;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        domainService = initializeMocks(VotingInEnvelopeDomainService.class);
        electionGroup = createMock(ElectionGroup.class);
        municipality = mockedMunicipality();
        userData = createMock(UserData.class);
        
        when(userData.getJavaLocale()).thenReturn(new Locale("nb", "NO"));

        HashMap languageVotingCategoryMap = mockField("languageVotingCategoryMap", HashMap.class);
        when(languageVotingCategoryMap.get(userData.getJavaLocale())).thenReturn(emptyMap());

        HashMap languageVotingRejectionMap = mockField("languageVotingRejectionMap", HashMap.class);
        when(languageVotingRejectionMap.get(userData.getJavaLocale())).thenReturn(emptyMap());
    }

    @Test
    public void testPostConstruct() throws NoSuchFieldException, IllegalAccessException {
        HashMap languageVotingCategoryMap = getPrivateField("languageVotingCategoryMap", HashMap.class);
        HashMap languageVotingRejectionMap = getPrivateField("languageVotingRejectionMap", HashMap.class);
        
        domainService.postConstruct();
        
        verify(languageVotingCategoryMap, times(2)).put(any(Locale.class), any(HashMap.class));
        verify(languageVotingRejectionMap, times(2)).put(any(Locale.class), any(HashMap.class));
    }

    @Test(dataProvider = "votingsToBeConfirmedForVoterTestData")
    public void testVotingsToBeConfirmedForVoter(Voter voter, List<Voting> votingsForVoter, int expectedCountOfUnconfirmedVotings) {
        mockVotingsByElectionGroupAndVoter(voter, votingsForVoter);

        List<Voting> votings = domainService.unconfirmedVotingsForVoter(electionGroup, voter);

        assertEquals(votings.size(), expectedCountOfUnconfirmedVotings);
    }

    private void mockVotingsByElectionGroupAndVoter(Voter voter, List<Voting> mockedVotingList) {
        when(getInjectMock(VotingRepository.class).getVotingsByElectionGroupAndVoter(voter.getPk(), electionGroup.getPk()))
                .thenReturn(mockedVotingList);
    }

    @DataProvider
    public Object[][] votingsToBeConfirmedForVoterTestData() {
        Voter voter = voter();

        return new Object[][]{
                {voter, asList(approvedVoting(),
                        rejectedVoting(),
                        unconfirmedVoting()), 1},
                {voter, singletonList(approvedVoting()), 0},
                {voter, asList(rejectedVoting(),
                        rejectedVoting()), 0},
                {voter, asList(approvedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 2},
                {voter, asList(unconfirmedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 3},
                {voter, emptyList(), 0},
        };
    }

    private Voter voter() {
        Voter voter = new Voter();
        voter.setId("123123123");
        voter.setPk(1L);
        return voter;
    }

    private Voting approvedVoting() {
        return voting(voter(), true, null);
    }

    private Voting rejectedVoting() {
        return voting(voter(), false, new VotingRejection());
    }

    private Voting unconfirmedVoting() {
        return voting(voter(), false, null);
    }

    private Voting voting(Voter voter, boolean approved, VotingRejection votingRejection) {
        Voting voting = new Voting();
        voting.setApproved(approved);
        voting.setVotingRejection(votingRejection);
        voting.setVoter(voter);
        return voting;
    }


    @Test(dataProvider = "rejectedVotingsForVoterTestData")
    public void testRejectedVotings(Voter voter, List<Voting> votingsForVoter, int expectedCountOfRejectedVotings) {
        mockVotingsByElectionGroupAndVoter(voter, votingsForVoter);

        List<Voting> votings = domainService.rejectedVotingsForVoter(electionGroup, voter);

        assertEquals(votings.size(), expectedCountOfRejectedVotings);
    }

    @DataProvider
    public Object[][] rejectedVotingsForVoterTestData() {
        Voter voter = voter();

        return new Object[][]{
                {voter, asList(approvedVoting(),
                        rejectedVoting(),
                        unconfirmedVoting()), 1},
                {voter, singletonList(approvedVoting()), 0},
                {voter, asList(rejectedVoting(),
                        rejectedVoting()), 2},
                {voter, asList(approvedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 0},
                {voter, asList(unconfirmedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 0},
                {voter, emptyList(), 0},
        };
    }

    @Test(dataProvider = "approvedVotingsForVoterTestData")
    public void testApprovedVotings(Voter voter, List<Voting> votingsForVoter, int expectedCountOfApprovedVotings) {
        mockVotingsByElectionGroupAndVoter(voter, votingsForVoter);

        List<Voting> votings = domainService.approvedVotingsForVoter(electionGroup, voter);

        assertEquals(votings.size(), expectedCountOfApprovedVotings);
    }

    @DataProvider
    public Object[][] approvedVotingsForVoterTestData() {
        Voter voter = voter();

        return new Object[][]{
                {voter, asList(approvedVoting(),
                        rejectedVoting(),
                        unconfirmedVoting()), 1},
                {voter, singletonList(approvedVoting()), 1},
                {voter, asList(rejectedVoting(),
                        rejectedVoting()), 0},
                {voter, asList(approvedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 1},
                {voter, asList(unconfirmedVoting(),
                        unconfirmedVoting(),
                        unconfirmedVoting()), 0},
                {voter, emptyList(), 0},
        };
    }

    @Test(dataProvider = "resolveSuggestedRejectedApprovalTestData")
    public void testResolveSuggestedRejectedApproval(List<Voting> votings, VotingDto votingDto, VotingApprovalStatus expectedStatus, int expectedCallsToVotingsForVoter) {

        Voter voter = theVoter();
        when(getInjectMock(VotingRepository.class).getVotingsByElectionGroupAndVoter(voter.getPk(), electionGroup.getPk()))
                .thenReturn(votings);
        when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(no.valg.eva.admin.common.configuration.model.Municipality.class), eq(votingDto)))
                .thenReturn(selectedUnconfirmedVoting());

        VotingApprovalStatus status = domainService.resolveSuggestedRejectedVotingApproval(electionGroup, mock(Municipality.class), votingDto, voter);

        int expectedCallToFindVotingByNumber = (status.getState() == VotingApprovalState.NO_OTHER_VOTINGS || status.getState() == VotingApprovalState.MULTIPLE_UNCONFIRMED_VOTINGS) ? 1 : 0;
        verify(getInjectMock(VotingRepository.class), times(expectedCallToFindVotingByNumber)).findVotingByVotingNumber(any(), eq(votingDto));

        if (status.getState() == VotingApprovalState.PREVIOUSLY_APPROVED_VOTING) {
            assertNotNull(status.getPreviouslyApprovedVoting());
            assertEquals(status.getPreviouslyApprovedVoting(), expectedStatus.getPreviouslyApprovedVoting());
        } else {
            assertNull(status.getPreviouslyApprovedVoting());
        }
        assertEquals(status.getState(), expectedStatus.getState());

        verify(getInjectMock(VotingRepository.class), times(expectedCallsToVotingsForVoter)).getVotingsByElectionGroupAndVoter(any(), any());
    }

    @DataProvider
    private Object[][] resolveSuggestedRejectedApprovalTestData() {

        VotingDto selectedVoting = toDto(selectedUnconfirmedVoting());

        Voting otherUnconfirmedVoting = new Voting();
        otherUnconfirmedVoting.setPk(2L);
        otherUnconfirmedVoting.setVotingNumber(2);
        otherUnconfirmedVoting.setVoter(theVoter());
        otherUnconfirmedVoting.setApproved(false);
        otherUnconfirmedVoting.setVotingRejection(null);

        Voting approvedVoting = new Voting();
        approvedVoting.setPk(3L);
        approvedVoting.setVotingNumber(3);
        approvedVoting.setVoter(theVoter());
        approvedVoting.setApproved(true);

        Voting rejectedVoting = new Voting();
        rejectedVoting.setPk(4L);
        rejectedVoting.setVotingNumber(4);
        rejectedVoting.setVoter(theVoter());
        rejectedVoting.setVotingRejection(new VotingRejection());

        VotingApprovalStatus statusNoOtherVotings = VotingApprovalStatus.builder()
                .state(VotingApprovalState.NO_OTHER_VOTINGS)
                .build();

        VotingApprovalStatus statusMultipleUnconfirmed = VotingApprovalStatus.builder()
                .state(VotingApprovalState.MULTIPLE_UNCONFIRMED_VOTINGS)
                .build();

        VotingApprovalStatus statusPrevApprovedVoting = VotingApprovalStatus.builder()
                .state(VotingApprovalState.PREVIOUSLY_APPROVED_VOTING)
                .previouslyApprovedVoting(toDto(approvedVoting))
                .build();

        return new Object[][]{
                {votingListWith(), selectedVoting, statusNoOtherVotings, 2},
                {votingListWith(rejectedVoting), selectedVoting, statusNoOtherVotings, 2},
                {votingListWith(otherUnconfirmedVoting), selectedVoting, statusMultipleUnconfirmed, 2},
                {votingListWith(otherUnconfirmedVoting, rejectedVoting), selectedVoting, statusMultipleUnconfirmed, 2},
                {votingListWith(otherUnconfirmedVoting, rejectedVoting, approvedVoting), selectedVoting, statusPrevApprovedVoting, 1},
                {votingListWith(rejectedVoting, approvedVoting), selectedVoting, statusPrevApprovedVoting, 1},
                {votingListWith(approvedVoting), selectedVoting, statusPrevApprovedVoting, 1}
        };
    }

    private List<Voting> votingListWith(Voting... otherVotings) {
        final List<Voting> votings = new ArrayList<>(otherVotings.length + 1);
        votings.add(selectedUnconfirmedVoting());
        votings.addAll(asList(otherVotings));
        return votings;
    }

    private Voting selectedUnconfirmedVoting() {
        Voting vo = new Voting();
        vo.setPk(123L);
        vo.setVotingNumber(1);
        vo.setVoter(theVoter());
        vo.setApproved(false);
        vo.setVotingRejection(null);
        return vo;
    }

    private Voter theVoter() {
        Voter v = new Voter();
        v.setId("321321321");
        return v;
    }


    @Test(dataProvider = "votersThatNeedToBeHandledOneByOneTestData")
    public void testVotersThatNeedToBeHandledOneByOne(List<Voting> votingsInRepository, List<VotingDto> votingsToBeRejected, List<Voter> expectedVotersThatNeedToBeHandledOneByOne) {
        stubVoterAndVotingRepositoryWith(votingsInRepository);

        List<Voter> votersThatNeedToBeHandledOneByOne = domainService.resolveVotersThatNeedToBeHandledOneByOne(electionGroup, votingsToBeRejected);

        assertEquals(votersThatNeedToBeHandledOneByOne.size(), expectedVotersThatNeedToBeHandledOneByOne.size());
        for (int i = 0; i < expectedVotersThatNeedToBeHandledOneByOne.size(); i++) {
            Voter actualVoter = votersThatNeedToBeHandledOneByOne.get(i);
            Voter expectedVoter = expectedVotersThatNeedToBeHandledOneByOne.get(i);
            assertEquals(actualVoter.getPk(), expectedVoter.getPk());
        }
    }

    private void stubVoterAndVotingRepositoryWith(List<Voting> votings) {
        Voter voterOne = voterOne();
        when(getInjectMock(VoterRepository.class).voterOfId(voterOne.getId(), electionGroup.getElectionEvent().getPk())).thenReturn(voterOne);

        when(getInjectMock(VotingRepository.class).getVotingsByElectionGroupAndVoter(voterOne.getPk(), electionGroup.getPk()))
                .thenReturn(votings.stream().filter(v -> v.getVoter().getPk().equals(voterOne.getPk())).collect(Collectors.toList()));

        Voter voterTwo = voterTwo();
        when(getInjectMock(VoterRepository.class).voterOfId(voterTwo.getId(), electionGroup.getElectionEvent().getPk())).thenReturn(voterTwo);

        when(getInjectMock(VotingRepository.class).getVotingsByElectionGroupAndVoter(voterTwo.getPk(), electionGroup.getPk()))
                .thenReturn(votings.stream().filter(v -> v.getVoter().getPk().equals(voterTwo.getPk())).collect(Collectors.toList()));

        votings.forEach(v -> {
            VotingDto dto = toDto(v);
            when(getInjectMock(VotingRepository.class).findVotingByVotingNumber(any(), eq(dto))).thenReturn(v);
        });
    }

    @DataProvider
    private Object[][] votersThatNeedToBeHandledOneByOneTestData() {

        Voter voterOne = voterOne();
        Voting unconfirmedVotingOnVoterOne = voting(voterOne, unconfirmedVoting(), 1L);
        Voting otherUnconfirmedVotingOnVoterOne = voting(voterOne, unconfirmedVoting(), 2L);
        Voting yetAnotherUnconfirmedVotingOnVoterOne = voting(voterOne, unconfirmedVoting(), 12L);
        Voting rejectedVotingOnVoterOne = voting(voterOne, rejectedVoting(), 3L);
        Voting otherRejectedVotingOnVoterOne = voting(voterOne, rejectedVoting(), 4L);
        Voting approvedVotingOnVoterOne = voting(voterOne, approvedVoting(), 5L);

        Voter voterTwo = voterTwo();
        Voting unconfirmedVotingOnVoterTwo = voting(voterTwo, unconfirmedVoting(), 6L);
        Voting otherUnconfirmedVotingOnVoterTwo = voting(voterTwo, unconfirmedVoting(), 7L);
        Voting rejectedVotingOnVoterTwo = voting(voterTwo, rejectedVoting(), 8L);
        Voting otherRejectedVotingOnVoterTwo = voting(voterTwo, rejectedVoting(), 9L);
        Voting approvedVotingOnVoterTwo = voting(voterTwo, approvedVoting(), 10L);


        return new Object[][]{
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne
                        ),
                        shouldReturn(voterOne)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                rejectedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                rejectedVotingOnVoterTwo,
                                otherRejectedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                rejectedVotingOnVoterTwo,
                                otherRejectedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                approvedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                rejectedVotingOnVoterTwo,
                                otherRejectedVotingOnVoterTwo,
                                otherUnconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturn(voterTwo)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                otherUnconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturn(voterOne, voterTwo)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                approvedVotingOnVoterTwo,
                                otherUnconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne
                        ),
                        shouldReturn(voterOne)
                },
                {
                        existingVotings(
                                rejectedVotingOnVoterOne,
                                otherRejectedVotingOnVoterOne,
                                unconfirmedVotingOnVoterOne
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne
                        ),
                        shouldReturnNoVotersNeedHandled()
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                otherRejectedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo, otherUnconfirmedVotingOnVoterOne
                        ),
                        shouldReturn(voterOne)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                yetAnotherUnconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterOne, unconfirmedVotingOnVoterTwo, otherUnconfirmedVotingOnVoterOne
                        ),
                        shouldReturn(voterOne, voterTwo)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo,
                                yetAnotherUnconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterTwo, unconfirmedVotingOnVoterOne, otherUnconfirmedVotingOnVoterOne
                        ),
                        shouldReturn(voterOne, voterTwo)
                },
                {
                        existingVotings(
                                unconfirmedVotingOnVoterOne,
                                otherUnconfirmedVotingOnVoterOne,
                                yetAnotherUnconfirmedVotingOnVoterOne,
                                unconfirmedVotingOnVoterTwo
                        ),
                        votingsToBeRejected(
                                unconfirmedVotingOnVoterTwo
                        ),
                        shouldReturnNoVotersNeedHandled()
                }
        };
    }

    @Test(dataProvider = "pagedVotingsTestData")
    public void testPagedVotings_givenContextAndFilters_verifiesPagedList(List<Voting> votings, VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) throws NoSuchFieldException, IllegalAccessException {
        mockPagedVotings(votings);

        PagedList<VotingDto> pagedList = domainService.votings(userData, municipality, electionGroup, votingFilters, votingSorting, offset, limit);
        assertEquals(pagedList.getTotalNumberOfObjects(), votings.size());
        assertEquals(pagedList.getOffset(), offset);
        assertEquals(pagedList.getLimit(), limit);
        assertEquals(pagedList.getObjects().size(), votings.size());
    }

    private no.valg.eva.admin.configuration.domain.model.Municipality mockedMunicipality() {
        return createMock(no.valg.eva.admin.configuration.domain.model.Municipality.class);
    }

    @Test(dataProvider = "pagedVotingsTestData")
    public void testPagedVotings_givenContextAndFilters_verifiesRepositoryFindVotings(List<Voting> votings, VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) throws NoSuchFieldException, IllegalAccessException {
        mockPagedVotings(votings);

        domainService.votings(userData, municipality, electionGroup, votingFilters, votingSorting, offset, limit);

        verify(getInjectMock(PagingVotingRepository.class), times(1)).findVotings(any(no.valg.eva.admin.configuration.domain.model.Municipality.class), any(ElectionGroup.class), any(VotingFilters.class), any(VotingSorting.class), 
                anyMap(), anyMap(), anyInt(), anyInt());
    }

    @Test(dataProvider = "pagedVotingsTestData")
    public void testPagedVotings_givenContextAndFilters_verifiesRepositoryCountVotings(List<Voting> votings, VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) throws NoSuchFieldException, IllegalAccessException {
        mockPagedVotings(votings);

        domainService.votings(userData, municipality, electionGroup, votingFilters, votingSorting, offset, limit);

        verify(getInjectMock(PagingVotingRepository.class), times(1))
                .countVotings(municipality, electionGroup, votingFilters);
    }

    @DataProvider
    public Object[][] pagedVotingsTestData() {
        List<Voting> votings = new ArrayList<>();
        Voting voting = voting(voter(), true, null);
        votings.add(voting);

        return new Object[][]{
                {votings, VotingFilters.builder().build(), VotingSorting.builder().build(), 0, 100}
        };
    }

    private void mockPagedVotings(List<Voting> votings) {
        when(getInjectMock(PagingVotingRepository.class).findVotings(any(no.valg.eva.admin.configuration.domain.model.Municipality.class), any(ElectionGroup.class), any(VotingFilters.class), any(VotingSorting.class), anyMap(), anyMap(), anyInt(), anyInt()))
                .thenReturn(votings);
        when(getInjectMock(PagingVotingRepository.class).countVotings(any(no.valg.eva.admin.configuration.domain.model.Municipality.class), any(ElectionGroup.class), any(VotingFilters.class))).thenReturn(votings.size());
    }

    private Voter voterOne() {
        Voter vo = new Voter();
        vo.setPk(10L);
        vo.setId("101010");
        vo.setFirstName("Reidar");
        vo.setLastName("Fossdal");
        return vo;
    }

    private Voting voting(Voter castByVoter, Voting v, long votingPk) {
        v.setPk(votingPk);
        v.setVotingNumber(toIntExact(votingPk));
        v.setVoter(castByVoter);
        return v;
    }

    private Voter voterTwo() {
        Voter vo = new Voter();
        vo.setPk(20L);
        vo.setId("202020");
        vo.setFirstName("Alf");
        vo.setLastName("Olsen");
        return vo;
    }

    private List<Voting> existingVotings(Voting... votings) {
        return asList(votings);
    }

    private List<VotingDto> votingsToBeRejected(Voting... votings) {
        return VotingMapper.toDtoList(asList(votings));
    }

    private List<Voter> shouldReturn(Voter... voters) {
        return asList(voters);
    }

    private List<Voter> shouldReturnNoVotersNeedHandled() {
        return emptyList();
    }
}