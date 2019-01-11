package no.valg.eva.admin.voting.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.domain.service.ConfirmationCategoryStatusDomainService;
import no.valg.eva.admin.voting.domain.service.VotingCategoryStatusDomainService;
import no.valg.eva.admin.voting.domain.service.VotingConfirmationDomainService;
import no.valg.eva.admin.voting.domain.service.VotingInEnvelopeDomainService;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.allOf;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static no.valg.eva.admin.configuration.application.MunicipalityMapper.toDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class VotingInEnvelopeApplicationServiceTest extends MockUtilsTestCase {

    private ConfirmationCategoryStatusDomainService confirmationCategoryStatusDomainService;
    private VotingInEnvelopeApplicationService votingInEnvelopeService;
    private ElectionGroup electionGroup;
    private Municipality municipality;
    private UserData userData;
    private MvArea mvArea;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        votingInEnvelopeService = initializeMocks(VotingInEnvelopeApplicationService.class);
        confirmationCategoryStatusDomainService = getInjectMock(ConfirmationCategoryStatusDomainService.class);
        userData = createMock(UserData.class);
        mvArea = createMock(MvArea.class);
        electionGroup = createMock(ElectionGroup.class);
        municipality = createMock(Municipality.class);

        when(mvArea.getMunicipality()).thenReturn(municipality);
    }

    @Test
    public void votingCategoryStatuses_givenUserDataAndMvArea_callsVotingCategoryStatuses() {
        votingInEnvelopeService.votingCategoryStatuses(userData, mvArea);
        verify(getInjectMock(VotingCategoryStatusDomainService.class)).votingCategoryStatuses(mvArea, false);
    }

    @Test
    public void confirmationCategoryStatuses_givenUserDataAndMvArea_callsVotingCategoryStatuses() {
        votingInEnvelopeService.confirmationCategoryStatuses(userData, mvArea, electionGroup);
        verify(getInjectMock(ConfirmationCategoryStatusDomainService.class)).confirmationCategoryStatuses(mvArea, electionGroup);
    }

    @Test
    public void votingConformationReport_givenUserDataAndMvArea_callsVotingCategoryStatuses() {
        VotingCategory votingCategory = VO;
        VotingPhase votingPhase = ELECTION_DAY;
        votingInEnvelopeService.votingConfirmationReport(userData, mvArea, electionGroup, votingCategory, votingPhase, null, null);
        verify(getInjectMock(ConfirmationCategoryStatusDomainService.class)).votingConfirmationReport(mvArea, electionGroup, votingCategory, votingPhase, null, null);
    }

    @Test(dataProvider = "votingRejectionsVotingCategoryTestData")
    public void testVotingRejections_givenVotingCategory_verifiesRepositoryCall(VotingCategory votingCategory, List<VotingRejection> domainVotingRejectionList,
                                                                                List<VotingRejectionDto> expectedVotingRejectionList) {
        when(getInjectMock(VotingRejectionRepository.class).findAll()).thenReturn(domainVotingRejectionList);

        List<VotingRejectionDto> votingRejectionDtoList = votingInEnvelopeService.votingRejections(userData, votingCategory);

        assertEquals(votingRejectionDtoList, expectedVotingRejectionList);
        verify(getInjectMock(VotingRejectionRepository.class), times(1)).findAll();
    }

    @DataProvider
    public Object[][] votingRejectionsVotingCategoryTestData() {
        List<VotingRejection> votingRejectionList = new ArrayList<>();

        EnumSet.allOf(no.valg.eva.admin.common.voting.VotingRejection.class).forEach(votingRejection -> {
            VotingRejection domainVotingRejection = votingRejection(votingRejection);
            votingRejectionList.add(domainVotingRejection);
        });

        return new Object[][]{
                {FA, votingRejectionList, advanceVotingRejections(votingRejectionList)},
                {FE, votingRejectionList, advanceVotingRejections(votingRejectionList)},
                {FU, votingRejectionList, advanceVotingRejections(votingRejectionList)},
                {FI, votingRejectionList, advanceVotingRejections(votingRejectionList)},
                {FB, votingRejectionList, advanceVotingRejections(votingRejectionList)},
                {VF, votingRejectionList, electionDayVotingRejections(votingRejectionList)},
                {VO, votingRejectionList, electionDayVotingRejections(votingRejectionList)},
                {VB, votingRejectionList, electionDayVotingRejections(votingRejectionList)},
                {VS, votingRejectionList, electionDayVotingRejections(votingRejectionList)},
        };
    }

    @Test(dataProvider = "votingRejectionsTestData")
    public void testVotingRejections_verifiesRepositoryCall(List<VotingRejection> domainVotingRejectionList, List<VotingRejectionDto> expectedVotingRejectionList) {
        when(getInjectMock(VotingRejectionRepository.class).findAll()).thenReturn(domainVotingRejectionList);

        List<VotingRejectionDto> votingRejectionDtoList = votingInEnvelopeService.votingRejections(userData);

        assertEquals(votingRejectionDtoList, expectedVotingRejectionList);
        verify(getInjectMock(VotingRejectionRepository.class), times(1)).findAll();
    }

    @DataProvider
    public Object[][] votingRejectionsTestData() {
        List<VotingRejection> votingRejectionList = new ArrayList<>();

        EnumSet.allOf(no.valg.eva.admin.common.voting.VotingRejection.class).forEach(votingRejection -> {
            VotingRejection domainVotingRejection = votingRejection(votingRejection);
            votingRejectionList.add(domainVotingRejection);
        });

        List<VotingRejectionDto> votingRejectionDtos = votingRejectionList.stream()
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());

        return new Object[][]{
                {votingRejectionList, votingRejectionDtos},
        };
    }

    private List<VotingRejectionDto> advanceVotingRejections(List<VotingRejection> votingRejectionList) {
        return votingRejectionList.stream()
                .filter(votingRejection -> no.valg.eva.admin.common.voting.VotingRejection.getById(votingRejection.getId()).isAdvanceRejection())
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<VotingRejectionDto> electionDayVotingRejections(List<VotingRejection> votingRejectionList) {
        return votingRejectionList.stream()
                .filter(votingRejection -> no.valg.eva.admin.common.voting.VotingRejection.getById(votingRejection.getId()).isElectionDayRejection())
                .map(VotingRejectionMapper::toDto)
                .collect(Collectors.toList());
    }

    private VotingRejection votingRejection(no.valg.eva.admin.common.voting.VotingRejection votingRejection) {
        VotingRejection domainVotingRejection = new VotingRejection();
        domainVotingRejection.setName("name");
        domainVotingRejection.setId(votingRejection.getId());
        return domainVotingRejection;
    }

    @Test(dataProvider = "approveVotingListTestData")
    public void testApproveVotingList_givenVotingConfirmation_verifiesApproveVotingListServiceCall(VotingConfirmationDto votingConfirmationDto) {
        votingInEnvelopeService.approveVotingList(userData, votingConfirmationDto.getVotingDtoListToConfirm(), toDto(mvArea.getMunicipality()));
        verify(getInjectMock(VotingConfirmationDomainService.class)).approveVotingList(eq(votingConfirmationDto.getVotingDtoListToConfirm()),
                any(no.valg.eva.admin.common.configuration.model.Municipality.class));
    }

    @DataProvider
    public Object[][] approveVotingListTestData() {
        ElectionGroup eg = new ElectionGroup();

        return allOf(VotingCategory.class).stream()
                .map(votingCategory -> new Object[]{votingConfirmation(eg, votingCategory)})
                .toArray(Object[][]::new);
    }

    private VotingConfirmationDto votingConfirmation(ElectionGroup electionGroup, VotingCategory votingCategory) {
        VotingDto votingDto = VotingDto.builder()
                .votingNumber(1)
                .build();

        return VotingConfirmationDto.builder()
                .electionGroup(electionGroup)
                .startDate(LocalDate.of(2018, 1, 1))
                .endDate(LocalDate.of(2018, 12, 31))
                .municipality(createMock(Municipality.class))
                .pollingPlace(createMock(no.valg.eva.admin.common.configuration.model.local.PollingPlace.class))
                .votingCategory(votingCategory)
                .votingDtoListToConfirm(singletonList(votingDto))
                .build();
    }

    @Test
    public void rejectVotingList_givenAllArgs_verifiesDomainServiceCall() {
        List<VotingDto> votingDtoList = singletonList(VotingDto.builder().build());
        VotingRejectionDto votingRejectionDto = votingRejectionDto();
        no.valg.eva.admin.common.configuration.model.Municipality municipality = no.valg.eva.admin.common.configuration.model.Municipality.builder().build();

        votingInEnvelopeService.rejectVotingList(userData, votingDtoList, votingRejectionDto, municipality);

        verify(getInjectMock(VotingConfirmationDomainService.class)).rejectVotings(eq(votingDtoList), eq(votingRejectionDto), eq(municipality));
    }


    @Test
    public void moveVotingToSuggestedRejected_givenAllArgs_verifiesDomainServiceCall() {
        List<VotingDto> votingDtoList = singletonList(VotingDto.builder().build());
        VotingRejectionDto votingRejectionDto = votingRejectionDto();
        no.valg.eva.admin.common.configuration.model.Municipality municipality = no.valg.eva.admin.common.configuration.model.Municipality.builder().build();

        votingInEnvelopeService.moveVotingToSuggestedRejected(userData, votingDtoList, votingRejectionDto, municipality);

        verify(getInjectMock(VotingConfirmationDomainService.class)).suggestRejectVotings(eq(votingDtoList), eq(votingRejectionDto), eq(municipality));
    }

    private List<Voting> mockVotings() {
        List<Voting> votings = new ArrayList<>();
        Voting voting = new Voting();
        voting.setLateValidation(false);
        voting.setVoter(voter());
        votings.add(voting);
        return votings;
    }

    private Voter voter() {
        Voter voter = new Voter();
        voter.setId("123123123");
        voter.setPk(1L);
        return voter;
    }

    @Test
    public void testVotingsToBeConfirmed_verifiesDomainServiceCall() {
        Voter voter = voter();
        mockVoterById(voter);
        when(getInjectMock(VotingInEnvelopeDomainService.class).unconfirmedVotingsForVoter(any(), any())).thenReturn(emptyList());

        votingInEnvelopeService.votingsToBeConfirmedForVoter(userData, electionGroup, voter.getId());

        verify(getInjectMock(VotingInEnvelopeDomainService.class)).unconfirmedVotingsForVoter(electionGroup, voter);
    }

    @Test
    public void testRejectVoting_givenVoting_verifiesDomainServiceCall() {
        VotingDto voting = votingDto();
        VotingRejectionDto votingRejection = votingRejectionDto();
        votingInEnvelopeService.rejectVoting(userData, toDto(mvArea.getMunicipality()), voting, votingRejection);
        verify(getInjectMock(VotingConfirmationDomainService.class)).rejectVoting(voting, votingRejection, toDto(mvArea.getMunicipality()));
    }

    @Test
    public void testApproveVoting_givenVoting_verifiesDomainServiceCall() {
        VotingDto voting = votingDto();
        votingInEnvelopeService.approveVoting(userData, voting, toDto(mvArea.getMunicipality()));
        verify(getInjectMock(VotingConfirmationDomainService.class)).approveVoting(voting, toDto(mvArea.getMunicipality()));
    }

    @Test
    public void testRejectedVotings_verifiesDomainServiceCall() {
        Voter voter = voter();
        mockVoterById(voter);
        when(getInjectMock(VotingInEnvelopeDomainService.class).rejectedVotingsForVoter(any(), any())).thenReturn(emptyList());

        votingInEnvelopeService.rejectedVotings(userData, electionGroup, voter.getId());

        verify(getInjectMock(VotingInEnvelopeDomainService.class)).rejectedVotingsForVoter(electionGroup, voter);
    }

    @Test
    public void testApprovedVotings_verifiesDomainServiceCall() {
        Voter voter = voter();
        mockVoterById(voter);
        when(getInjectMock(VotingInEnvelopeDomainService.class).approvedVotingsForVoter(any(), any())).thenReturn(emptyList());

        votingInEnvelopeService.approvedVotings(userData, electionGroup, voter.getId());

        verify(getInjectMock(VotingInEnvelopeDomainService.class)).approvedVotingsForVoter(electionGroup, voter);
    }

    @Test
    public void testCancelVotingRejection_givenVoting_verifiesDomainServiceCall() {
        VotingDto voting = votingDto();
        votingInEnvelopeService.cancelRejection(userData, voting, toDto(mvArea.getMunicipality()));
        verify(getInjectMock(VotingConfirmationDomainService.class)).cancelRejection(voting, toDto(mvArea.getMunicipality()));
    }

    private void mockVoterById(Voter voter) {
        when(getInjectMock(VoterRepository.class).voterOfId(voter.getId(), electionGroup.getElectionEvent().getPk())).thenReturn(voter);
    }

    private VotingRejectionDto votingRejectionDto() {
        return VotingRejectionDto.builder().build();
    }

    private VotingDto votingDto() {
        return VotingDto.builder().build();
    }

    @Test
    public void testSuggestedRejectedVotingCanBeApproved_callsDomainService() {
        VotingDto votingDto = VotingDto.builder()
                .voterDto(VoterDto.builder().id("123").build())
                .build();

        no.valg.eva.admin.common.configuration.model.Municipality municipality = toDto(mvArea.getMunicipality());
        votingInEnvelopeService.checkIfSuggestedRejectedVotingCanBeApproved(userData, electionGroup, municipality, votingDto);

        verify(getInjectMock(VotingInEnvelopeDomainService.class))
                .resolveSuggestedRejectedVotingApproval(eq(electionGroup), eq(municipality), eq(votingDto), any());
    }

    @Test
    public void testCheckIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne_verifiesDomainServiceCall() {

        List<VotingDto> votingsToBeRejected = asList(
                VotingDto.builder()
                        .voterDto(VoterDto.builder().id("123").build())
                        .build()
        );
        when(getInjectMock(VotingInEnvelopeDomainService.class).resolveVotersThatNeedToBeHandledOneByOne(any(), any())).thenReturn(Collections.emptyList());

        votingInEnvelopeService.checkIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne(userData, electionGroup, votingsToBeRejected);

        verify(getInjectMock(VotingInEnvelopeDomainService.class))
                .resolveVotersThatNeedToBeHandledOneByOne(electionGroup, votingsToBeRejected);
    }

    @Test
    public void testVotings_verifiesDomainServiceCall() {
        VotingFilters votingFilters = createMock(VotingFilters.class);
        VotingSorting votingSorting = createMock(VotingSorting.class);
        
        votingInEnvelopeService.votings(userData, mvArea, electionGroup, votingFilters, votingSorting, 0, 100);

        verify(getInjectMock(VotingInEnvelopeDomainService.class))
                .votings(any(UserData.class), any(Municipality.class), any(ElectionGroup.class), any(VotingFilters.class), any(VotingSorting.class), anyInt(), anyInt());
    }
}
