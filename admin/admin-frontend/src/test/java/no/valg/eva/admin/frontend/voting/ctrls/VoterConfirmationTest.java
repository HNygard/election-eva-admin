package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.frontend.common.UpdatableComponent;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.frontend.voting.ctrls.VoterConfirmation.VoterConfirmationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.util.MockUtils.setPrivateField;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.configuration.application.MunicipalityMapper.toDto;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingConfirmationTestData.mvArea;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingConfirmationTestData.voter;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingConfirmationTestData.voterConfirmationViewModel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class VoterConfirmationTest extends BaseFrontendTest {

    private VoterConfirmation voterConfirmation;
    private ElectionGroup electionGroup;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        voterConfirmation = initializeMocks(VoterConfirmation.class);

        electionGroup = createMock(ElectionGroup.class);
    }

    @Test(dataProvider = "initComponent")
    public void testInitComponent_givenVoterConfirmationViewModel_verifiesVoterConfirmationViewModel(VoterConfirmationContext voterConfirmationViewModel) {
        when(getInjectMock(VotingInEnvelopeService.class).approvedVotings(getUserDataMock(), voterConfirmationViewModel.getElectionGroup(),
                voterConfirmationViewModel.getVoterDto().getId())).thenReturn(emptyList());
        voterConfirmation.initComponent(voterConfirmationViewModel);

        assertEquals(voterConfirmation.getContextViewModel(), voterConfirmationViewModel);
    }

    @DataProvider
    public Object[][] initComponent() {
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup)}
        };
    }

    @Test(dataProvider = "initComponent")
    public void testApproveSelectedVoting_GivenVoterConfirmationViewModel_verifiesApproveVotingServiceCall(VoterConfirmationContext voterConfirmationViewModel) {
        VotingDto votingDto = voting();

        voterConfirmation.initComponent(voterConfirmationViewModel);
        voterConfirmation.approveSelectedVoting(votingDto);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .approveVoting(getUserDataMock(), votingDto,
                        toDto(voterConfirmationViewModel
                                .getMvArea()
                                .getMunicipality()));
    }

    @Test(dataProvider = "approveSelectedVoting")
    public void testApproveSelectedVoting_GivenVoterConfirmationViewModel_verifiesFacesMessage(VoterConfirmationContext voterConfirmationViewModel,
                                                                                               VotingDto votingDto,
                                                                                               String expectedSummary)
            throws NoSuchFieldException, IllegalAccessException {

        mockContextViewModelField(voterConfirmationViewModel);
        voterConfirmation.approveSelectedVoting(votingDto);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .approveVoting(getUserDataMock(), votingDto, toDto(voterConfirmationViewModel.getMvArea().getMunicipality()));

        assertFacesMessage(FacesMessage.SEVERITY_INFO, expectedSummary);
    }

    @DataProvider
    public Object[][] approveSelectedVoting() {
        VoterDto voter = voter();
        VotingDto votingDto = voting();

        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup),
                        votingDto,
                        format("[@voting.confirmation.voter.voting.approved, %s, %s]", votingDto.getVotingNumberDisplay(), voter.getNameLine())}
        };
    }

    @Test
    public void testRenderApproveVotingLink_givenCanApproveVotingFalse_verifiesFalse() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "canApproveVoting", false);
        assertFalse(voterConfirmation.renderApproveVotingLink(voting()));
    }

    @Test(dataProvider = "renderApproveVotingLinkWithElectronicMarkoffs")
    public void testRenderApproveVotingLink_givenElectronicMarkoffs_verifyShouldRenderApproveVotingLink(VoterConfirmationContext viewModel)
            throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "canApproveVoting", true);
        mockContextViewModelField(viewModel);
        assertTrue(voterConfirmation.renderApproveVotingLink(voting()));
    }

    @DataProvider
    public Object[][] renderApproveVotingLinkWithElectronicMarkoffs() {
        VoterConfirmationContext viewModelWithElectronicMarkoffs = voterConfirmationViewModel(getUserDataMock(), electionGroup);
        //Setting electronic markoffs to true just in case!
        viewModelWithElectronicMarkoffs.getMvArea().getMunicipality().setElectronicMarkoffs(true);

        return new Object[][]{
                {viewModelWithElectronicMarkoffs}
        };
    }

    @Test(dataProvider = "renderApproveVotingLinkWithoutElectronicMarkoffs")
    public void testRenderApproveVotingLink_givenVotingWithoutElectronicMarkoffs_verifyShouldRenderApproveVotingLink(VoterConfirmationContext voterConfirmationViewModel,
                                                                                                                     VotingDto votingDto,
                                                                                                                     boolean expectsRenderApproveVotingLink)
            throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "canApproveVoting", true);
        mockContextViewModelField(voterConfirmationViewModel);
        assertEquals(voterConfirmation.renderApproveVotingLink(votingDto), expectsRenderApproveVotingLink);
    }

    @DataProvider
    public Object[][] renderApproveVotingLinkWithoutElectronicMarkoffs() {
        VoterConfirmationContext voterConfirmationViewModelNotElectronicMarkoffs = voterConfirmationViewModel(getUserDataMock(), electionGroup);
        voterConfirmationViewModelNotElectronicMarkoffs.getMvArea().getMunicipality().setElectronicMarkoffs(false);

        return new Object[][]{
                {voterConfirmationViewModelNotElectronicMarkoffs,
                        voting(true, FI, true, false), true},
                {voterConfirmationViewModelNotElectronicMarkoffs,
                        voting(true, FI, false, false), false},

                {voterConfirmationViewModelNotElectronicMarkoffs,
                        voting(true, FI, true, true), true}
        };
    }

    @Test(dataProvider = "isShowUnconfirmedAdvanceVotingsLink")
    public void testIsShowAdvanceUnconfirmedLinks(VotingDto votingDto, boolean hasAccessToAdvanceSingleConfirmation, boolean expectedResult) {
        when(getInjectMock(UserDataController.class).getUserAccess().isStemmegivingPrøvingForhåndEnkelt()).thenReturn(true);
        voterConfirmation.isShowUnconfirmedLinks(votingDto);
    }

    @DataProvider
    public Object[][] isShowUnconfirmedAdvanceVotingsLink() {
        return new Object[][]{
                {voting(true, FI, true, false), false, false},
                {voting(true, FI, true, false), true, true},
        };
    }

    @Test(dataProvider = "isShowUnconfirmedElectionDayVotingsLink")
    public void testIsShowElectionDayUnconfirmedLinks(VotingDto votingDto, boolean hasAccessToElectionDaySingleConfirmation, boolean expectedResult) {
        when(getInjectMock(UserDataController.class).getUserAccess().isStemmegivingPrøvingValgtingEnkelt()).thenReturn(hasAccessToElectionDaySingleConfirmation);
        voterConfirmation.isShowUnconfirmedLinks(votingDto);
    }

    @DataProvider
    public Object[][] isShowUnconfirmedElectionDayVotingsLink() {
        return new Object[][]{
                {voting(true, FI, true, false), false, false},
                {voting(true, FI, true, false), true, true},
        };
    }

    @Test(dataProvider = "votingToReject")
    public void testSetVotingToReject_givenVotingDto_verifiesSetSelectedVoting(VoterConfirmationContext voterConfirmationViewModel, VotingDto votingDto)
            throws NoSuchFieldException, IllegalAccessException {
        doNothing().when(getInjectMock(VotingSuggestedRejectedDialog.class)).initComponent(any(VotingSuggestedRejectedDialog.ContextViewModel.class));
        mockContextViewModelField(voterConfirmationViewModel);

        voterConfirmation.setVotingToReject(votingDto);

        assertEquals(voterConfirmation.getSelectedVoting(), votingDto);
    }

    @Test(dataProvider = "votingToReject")
    public void testSetVotingToReject_givenVotingDto_verifiesInitRejectionDialog(VoterConfirmationContext voterConfirmationViewModel, VotingDto votingDto)
            throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationViewModel);

        voterConfirmation.setVotingToReject(votingDto);

        verify(getInjectMock(VotingSuggestedRejectedDialog.class), times(1)).initComponent(any(VotingSuggestedRejectedDialog.ContextViewModel.class));
    }

    @Test(dataProvider = "votingToReject")
    public void testSetVotingToReject_givenVotingDto_verifiesShowRejectionDialog(VoterConfirmationContext voterConfirmationViewModel, VotingDto votingDto)
            throws NoSuchFieldException, IllegalAccessException {
        doNothing().when(getInjectMock(VotingSuggestedRejectedDialog.class)).initComponent(any(VotingSuggestedRejectedDialog.ContextViewModel.class));
        mockContextViewModelField(voterConfirmationViewModel);

        voterConfirmation.setVotingToReject(votingDto);

        verify(getInjectMock(VotingSuggestedRejectedDialog.class), times(1)).show();
    }

    @DataProvider
    public Object[][] votingToReject() {
        VotingDto voting = voting();
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup), voting}
        };
    }

    @Test(dataProvider = "hasRejectedVotings")
    public void testHasRejectedVotings(List<VotingDto> rejectedVotings, boolean expectsRejectedVotings) throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "rejectedVotings", rejectedVotings);
        assertEquals(voterConfirmation.hasRejectedVotings(), expectsRejectedVotings);
    }

    @DataProvider
    public Object[][] hasRejectedVotings() {
        return new Object[][]{
                {singletonList(voting()), true},
                {emptyList(), false},
                {null, false}
        };
    }

    @Test(dataProvider = "cancelRejection")
    public void testCancelRejection_verifiesRejectVotingServiceCall(VoterConfirmationContext voterConfirmationViewModel, VotingDto votingDto)
            throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "contextViewModel", voterConfirmationViewModel);
        voterConfirmation.cancelRejection(votingDto);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .cancelRejection(getUserDataMock(), votingDto, toDto(mvArea().getMunicipality()));
    }

    @DataProvider
    public Object[][] cancelRejection() {
        VotingDto voting = voting();
        VoterConfirmationContext voterConfirmationViewModel = voterConfirmationViewModel(getUserDataMock(), electionGroup);
        return new Object[][]{
                {voterConfirmationViewModel, voting}
        };
    }

    @Test(dataProvider = "cancelRejectionVerifiesMessage")
    public void testCancelRejection_verifiesMessage(VoterConfirmationContext voterConfirmationViewModel, VotingDto votingDto, String expectedMessage)
            throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "contextViewModel", voterConfirmationViewModel);
        voterConfirmation.cancelRejection(votingDto);

        assertFacesMessage(FacesMessage.SEVERITY_INFO, expectedMessage);
    }

    @DataProvider
    public Object[][] cancelRejectionVerifiesMessage() {
        VotingDto voting = voting();
        VoterConfirmationContext voterConfirmationViewModel = voterConfirmationViewModel(getUserDataMock(), electionGroup);
        return new Object[][]{
                {voterConfirmationViewModel, voting,
                        format("[@voting.confirmation.voter.voting.rejection.cancelled, %s, %s]",
                        voting.getVotingNumberDisplay(), voterConfirmationViewModel.getVoterDto().getNameLine())}
        };
    }

    @Test
    public void testIsRenderCancelRejectionLink() {
        assertTrue(voterConfirmation.isRenderCancelRejectionLink());
    }

    @Test(dataProvider = "hasApprovedVotings")
    public void testHasApprovedVotings_givenVotings_verifiesHasVotings(List<VotingDto> votings, boolean expectedResult) throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "approvedVotings", votings);
        assertEquals(voterConfirmation.hasApprovedVotings(), expectedResult);
    }

    @DataProvider
    public Object[][] hasApprovedVotings() {
        return new Object[][]{
                {singletonList(voting()), true},
                {emptyList(), false},
                {null, false},
        };
    }

    @Test(dataProvider = "rowStyleClass")
    public void testRowStyleClass_givenVoting_verifiesRowStyleClass(VotingDto votingDto, String expectedRowStyleClass) {
        assertEquals(voterConfirmation.rowStyleClass(votingDto), expectedRowStyleClass);
    }

    @DataProvider
    public Object[][] rowStyleClass() {
        VotingDto voting = voting();
        return new Object[][]{
                {voting, voting.getVotingNumberDisplay()},
        };
    }

    @Test(dataProvider = "showElectoralRollHistoryDialog")
    public void testShowElectoralRollHistoryDialog_givenContextViewModel_verifiesInitComponent(VoterConfirmationContext voterConfirmationContext)
            throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationContext);

        VoterElectoralRollHistory.VoterElectoralRollHistoryContext historyContext = VoterElectoralRollHistory.VoterElectoralRollHistoryContext.builder()
                .userData(getUserDataMock())
                .voterDto(voterConfirmationContext.getVoterDto())
                .build();

        voterConfirmation.showElectoralRollHistoryDialog();

        verify(getInjectMock(VoterElectoralRollHistory.class), times(1)).initComponent(historyContext);
    }

    @Test(dataProvider = "showElectoralRollHistoryDialog")
    public void testShowElectoralRollHistoryDialog_givenContextViewModel_verifiesShowComponent(VoterConfirmationContext voterConfirmationViewModel)
            throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationViewModel);
        voterConfirmation.showElectoralRollHistoryDialog();

        verify(getInjectMock(VoterElectoralRollHistory.class), times(1)).show();
    }

    @DataProvider
    public Object[][] showElectoralRollHistoryDialog() {
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup)},
        };
    }

    @Test(dataProvider = "onRejectionReasonSelected")
    public void testOnRejectionReasonSelected_givenRejection_verifiesServiceCall(VoterConfirmationContext voterConfirmationViewModel,
                                                                                 VotingDto votingDto, VotingRejectionDto votingRejectionDto, String expectedMessage)
            throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationViewModel);

        setPrivateField(voterConfirmation, "selectedVoting", votingDto);

        voterConfirmation.onMoveVotingsToSuggestedRejected(votingRejectionDto);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .rejectVoting(getUserDataMock(), toDto(mvArea().getMunicipality()), votingDto, votingRejectionDto);
        assertFacesMessage(FacesMessage.SEVERITY_INFO, expectedMessage);
        verify(getInjectMock(VotingSuggestedRejectedDialog.class), times(1))
                .hide();
    }

    @DataProvider
    public Object[][] onRejectionReasonSelected() {
        VotingDto voting = voting();
        VotingRejectionDto votingRejectionDto = votingRejectionDto();
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup), voting, votingRejectionDto,
                        format("[@voting.confirmation.voter.voting.rejected, %s, %s, %s]",
                                voting.getVotingNumberDisplay(),
                                voting.getVoterDto().getNameLine(), votingRejectionDto.getName())},
        };
    }

    @Test(dataProvider = "onRejectionReasonSelectedHideComponent")
    public void testOnRejectionReasonSelected_givenRejection_verifiesHideComponent(VoterConfirmationContext voterConfirmationViewModel,
                                                                                   VotingDto votingDto,
                                                                                   VotingRejectionDto votingRejectionDto)
            throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationViewModel);

        setPrivateField(voterConfirmation, "selectedVoting", votingDto);

        voterConfirmation.onMoveVotingsToSuggestedRejected(votingRejectionDto);

        verify(getInjectMock(VotingSuggestedRejectedDialog.class), times(1))
                .hide();
    }

    @DataProvider
    public Object[][] onRejectionReasonSelectedHideComponent() {
        VotingDto voting = voting();
        VotingRejectionDto votingRejectionDto = votingRejectionDto();
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup), voting, votingRejectionDto},
        };
    }

    @Test
    public void testOnBackToVotingConfirmation() throws NoSuchFieldException, IllegalAccessException {
        VoterConfirmationHandler voterConfirmationHandler = createMock(VoterConfirmationHandler.class);
        VoterConfirmationContext voterConfirmationViewModel = voterConfirmationViewModel(getUserDataMock(), electionGroup, voterConfirmationHandler);
        setPrivateField(voterConfirmation, "contextViewModel", voterConfirmationViewModel);

        voterConfirmation.onBackToVotingConfirmation();

        verify(voterConfirmationHandler, times(1)).onVoterConfirmationDismiss();
    }

    @DataProvider
    public Object[][] onBackToVotingConfirmation() {
        return new Object[][]{
                {voterConfirmationViewModel(getUserDataMock(), electionGroup)},
        };
    }

    private VotingRejectionDto votingRejectionDto() {
        return VotingRejectionDto.builder()
                .name("rejectionName")
                .build();
    }
    
    @Test
    public void formatElectoralRollNumber_givenElectoralRollNumber_returnsProperlyFormattedString() throws NoSuchFieldException, IllegalAccessException {
        String expectedNumber = "1234567890 23";
        Manntallsnummer numberObject = new Manntallsnummer(expectedNumber.replace(" ", ""));
        mockContextViewModelField(voterConfirmationViewModel(getUserDataMock(), electionGroup));
        mockElectoralRollNumberField(numberObject);
        
        String actualNumber = voterConfirmation.formatElectoralRollNumber();
        
        assertEquals(actualNumber, expectedNumber);        
    }
    
    @Test
    public void formatElectoralRollNumber_withoutElectoralRollNumber_returnsEmptyString() throws NoSuchFieldException, IllegalAccessException {
        mockContextViewModelField(voterConfirmationViewModel(getUserDataMock(), electionGroup));
        
        String actualNumber = voterConfirmation.formatElectoralRollNumber();
        
        assertEquals(actualNumber, "");
    }


    private void mockContextViewModelField(VoterConfirmationContext voterConfirmationViewModel) throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation, "contextViewModel", voterConfirmationViewModel);
    }
    
    private void mockElectoralRollNumberField(Manntallsnummer number) throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(voterConfirmation.getContextViewModel(), "electoralRollNumber", number);
    }

    private VotingDto voting() {
        return voting(true, FI, false, false);
    }

    private VotingDto voting(boolean voterApprovedForVoting, no.valg.eva.admin.common.voting.VotingCategory votingCategoryEnum, boolean isEarlyVoting, boolean isLateValidation) {
        VotingCategory votingCategory = votingCategory(votingCategoryEnum);
        votingCategory.setEarlyVoting(isEarlyVoting);

        return VotingDto.builder()
                .votingCategory(votingCategory)
                .voterDto(voter(voterApprovedForVoting))
                .votingNumber(1234)
                .lateValidation(isLateValidation)
                .build();
    }

    private VotingCategory votingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategoryEnum) {
        VotingCategory votingCategory = new VotingCategory();
        votingCategory.setId(votingCategoryEnum.getId());
        return votingCategory;
    }

    private UpdatableComponentHandler componentHandlerMock() {
        return new ComponentHandlerMock();
    }

    private class ComponentHandlerMock implements UpdatableComponentHandler {


        @Override
        public void forceUpdate(UpdatableComponent component) {
        }
    }

    private class VoterConfirmationHandler implements VoterConfirmation.Handler {
        @Override
        public void onVoterConfirmationDismiss() {
        }
    }
}
