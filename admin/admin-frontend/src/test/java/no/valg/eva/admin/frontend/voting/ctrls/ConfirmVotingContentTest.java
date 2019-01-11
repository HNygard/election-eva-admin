package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingContentViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingPeriodViewModel;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.APPROVED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.REJECTED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.TO_BE_CONFIRMED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class ConfirmVotingContentTest extends BaseFrontendTest {

    private ConfirmVotingContent confirmVotingContent;

    private ConfirmVotingViewModel contextViewModel;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        this.confirmVotingContent = initializeMocks(ConfirmVotingContent.class);
        this.contextViewModel = contextViewModel();
    }

    private ConfirmVotingViewModel contextViewModel() {
        return contextViewModel(true);
    }

    private ConfirmVotingViewModel contextViewModel(boolean categoryOpen) {

        no.valg.eva.admin.configuration.domain.model.Municipality municipality = new no.valg.eva.admin.configuration.domain.model.Municipality();
        municipality.setPk(1L);

        MvArea mockedMvArea = mock(MvArea.class);
        when(mockedMvArea.getMunicipality()).thenReturn(municipality);

        return ConfirmVotingViewModel.builder()
                .userData(mock(UserData.class))
                .electionGroup(mock(ElectionGroup.class))
                .mvArea(mockedMvArea)
                .votingCategory(VotingCategory.FU)
                .votingPhase(VotingPhase.ADVANCE)
                .categoryOpen(categoryOpen)
                .startDate(LocalDateTime.now())
                .endDateIncluding(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    public void testOnInitComponent_givenContext_verifyVotingConfirmationReportServiceCall() {

        confirmVotingContent.initComponent(contextViewModel, null);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .votingConfirmationReport(eq(contextViewModel.getUserData()), any(MvArea.class), any(ElectionGroup.class), eq(contextViewModel.getVotingCategory()),
                        eq(contextViewModel.getVotingPhase()), any(), any());
    }

    @Test(dataProvider = "onInitComponentVerifyViewModelTestData")
    public void testOnInitComponent_givenContext_verifyConfirmVotingContentViewModel(VotingConfirmationReportDto votingConfirmationReportDto,
                                                                                     VotingViewModel votingViewModel) {
        when(getInjectMock(VotingInEnvelopeService.class)
                .votingConfirmationReport(eq(contextViewModel.getUserData()), any(MvArea.class), any(ElectionGroup.class), eq(contextViewModel.getVotingCategory()),
                        eq(contextViewModel.getVotingPhase()), any(), any()))
                .thenReturn(votingConfirmationReportDto);

        confirmVotingContent.initComponent(contextViewModel, null);

        ConfirmVotingContentViewModel viewModel = confirmVotingContent.getViewModel();

        assertEquals(viewModel.getNumberOfApprovedVotings(), votingConfirmationReportDto.getNumberOfApprovedVotings());
        assertEquals(viewModel.getNumberOfRejectedVotings(), votingConfirmationReportDto.getNumberOfRejectedVotings());
        assertEquals(viewModel.getNumberOfVotingsToConfirm(), votingConfirmationReportDto.getVotingDtoListToConfirm().size());
        assertEquals(viewModel.getVotingList().get(0), votingViewModel);
    }

    @DataProvider
    public Object[][] onInitComponentVerifyViewModelTestData() {

        VotingConfirmationReportDto votingConfirmationReportDto = VotingConfirmationReportDto.builder()
                .numberOfApprovedVotings(10)
                .numberOfRejectedVotings(11)
                .build();

        VoterDto voterDto = VoterDto.builder()
                .id("123")
                .nameLine("Per Fjasefjord Gåsland")
                .mvArea(mock(MvArea.class))
                .build();

        VotingDto votingDto = VotingDto.builder()
                .voterDto(voterDto)
                .votingNumber(2)
                .castTimestamp(LocalDateTime.now())
                .build();

        votingConfirmationReportDto.addVotingToVerify(votingDto);

        VotingViewModel votingViewModel = VotingViewModel.builder()
                .personId(voterDto.getId())
                .nameLine(voterDto.getNameLine())
                .voter(voterDto)
                .votingCategory(votingCategory(VotingCategory.FI))
                .build();

        return new Object[][]{
                {votingConfirmationReportDto, votingViewModel}
        };
    }

    private no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory(VotingCategory votingCategoryEnum) {
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();
        votingCategory.setId(votingCategoryEnum.getId());

        return votingCategory;
    }


    @Test
    public void testOnInitComponent_doesNotKeepSelectedVotingPeriod() {

        confirmVotingContent.initComponent(contextViewModel, null);
        final ConfirmVotingContentViewModel viewModel = confirmVotingContent.getViewModel();
        modifySelectedVotingPeriod(viewModel);

        confirmVotingContent.initComponent(contextViewModel, null);
        final ConfirmVotingContentViewModel newViewModel = confirmVotingContent.getViewModel();

        assertNotEquals(viewModel.getSelectedVotingPeriod().getFromDate().toLocalDate(),
                newViewModel.getSelectedVotingPeriod().getFromDate().toLocalDate(), "Period start date should not match");
        assertNotEquals(viewModel.getSelectedVotingPeriod().getToDateIncluding().toLocalDate(),
                newViewModel.getSelectedVotingPeriod().getToDateIncluding().toLocalDate(), "Period end date should not match");
    }

    private void modifySelectedVotingPeriod(ConfirmVotingContentViewModel viewModel) {
        viewModel.getSelectedVotingPeriod().setFromDate(LocalDateTime.now().minusDays(123));
        viewModel.getSelectedVotingPeriod().setToDateIncluding(LocalDateTime.now().plusDays(123));
    }

    @Test
    public void testComponentDidUpdate_doesKeepSelectedVotingPeriod() {

        confirmVotingContent.initComponent(contextViewModel, null);
        final ConfirmVotingContentViewModel viewModel = confirmVotingContent.getViewModel();
        modifySelectedVotingPeriod(viewModel);

        confirmVotingContent.componentDidUpdate(contextViewModel);
        final ConfirmVotingContentViewModel newViewModel = confirmVotingContent.getViewModel();

        assertEquals(viewModel.getSelectedVotingPeriod().getFromDate().toLocalDate(),
                newViewModel.getSelectedVotingPeriod().getFromDate().toLocalDate(), "Period start date should match");
        assertEquals(viewModel.getSelectedVotingPeriod().getToDateIncluding().toLocalDate(),
                newViewModel.getSelectedVotingPeriod().getToDateIncluding().toLocalDate(), "Period end date should match");
    }

    @Test(dataProvider = "getVotingsToBeConfirmedOverviewURLTestData")
    public void testGetVotingsToBeConfirmedOverviewURL_VerifiesURL(ConfirmVotingContentViewModel contentViewModel,
                                                                   ConfirmVotingViewModel contextViewModel,
                                                                   String expectedUrl) {
        confirmVotingContent.setViewModel(contentViewModel);
        confirmVotingContent.setContextViewModel(contextViewModel);
        assertEquals(confirmVotingContent.getVotingsToBeConfirmedOverviewURL(),
                expectedUrl);
    }

    @DataProvider
    public Object[][] getVotingsToBeConfirmedOverviewURLTestData() {
        ConfirmVotingViewModel contextViewModel = contextViewModel();
        return new Object[][]{
                {contentViewModel(LocalDateTime.now().withYear(2019).withDayOfMonth(8).withMonth(10), LocalDateTime.now().withYear(2019).withDayOfMonth(11).withMonth(10)),
                        contextViewModel,
                        overviewURL(contextViewModel.getVotingCategory(),
                                contextViewModel.getVotingPhase(),
                                TO_BE_CONFIRMED, "08102019",
                                "11102019",
                                false)}
        };
    }

    @Test(dataProvider = "getApprovedVotingsOverviewURLTestData")
    public void testGetApprovedVotingsOverviewURL_VerifiesURL(ConfirmVotingContentViewModel viewModel,
                                                              ConfirmVotingViewModel contextViewModel,
                                                              String expectedUrl) {
        confirmVotingContent.setViewModel(viewModel);
        confirmVotingContent.setContextViewModel(contextViewModel);
        assertEquals(confirmVotingContent.getApprovedVotingsOverviewURL(),
                expectedUrl);
    }

    @DataProvider
    public Object[][] getApprovedVotingsOverviewURLTestData() {
        ConfirmVotingViewModel contextViewModel = contextViewModel();
        return new Object[][]{
                {contentViewModel(LocalDateTime.now().withYear(2019).withDayOfMonth(8).withMonth(10), LocalDateTime.now().withYear(2019).withDayOfMonth(11).withMonth(10)),
                        contextViewModel,
                        overviewURL(contextViewModel.getVotingCategory(),
                                contextViewModel.getVotingPhase(),
                                APPROVED, "08102019",
                                "11102019",
                                true)}
        };
    }

    @Test(dataProvider = "getRejectedVotingsOverviewURLTestData")
    public void testGetRejectedVotingsOverviewURL_VerifiesURL(ConfirmVotingContentViewModel contentViewModel,
                                                              ConfirmVotingViewModel contextViewModel,
                                                              String expectedUrl) {
        confirmVotingContent.setViewModel(contentViewModel);
        confirmVotingContent.setContextViewModel(contextViewModel);
        assertEquals(confirmVotingContent.getRejectedVotingsOverviewURL(),
                expectedUrl);
    }

    @DataProvider
    public Object[][] getRejectedVotingsOverviewURLTestData() {
        ConfirmVotingViewModel contextViewModel = contextViewModel();
        return new Object[][]{
                {contentViewModel(LocalDateTime.now().withYear(2019).withDayOfMonth(8).withMonth(10), LocalDateTime.now().withYear(2019).withDayOfMonth(11).withMonth(10)),
                        contextViewModel,
                        overviewURL(contextViewModel.getVotingCategory(),
                                contextViewModel.getVotingPhase(),
                                REJECTED, "08102019",
                                "11102019",
                                true)}
        };
    }

    private ConfirmVotingContentViewModel contentViewModel(LocalDateTime fromDate, LocalDateTime toDateIncluding) {
        return ConfirmVotingContentViewModel.builder()
                .selectedVotingPeriod(votingPeriodViewModel(fromDate, toDateIncluding))
                .build();
    }

    private VotingPeriodViewModel votingPeriodViewModel(LocalDateTime fromDate, LocalDateTime toDateIncluding) {
        return VotingPeriodViewModel.builder()
                .fromDate(fromDate)
                .toDateIncluding(toDateIncluding)
                .build();
    }

    private String overviewURL(VotingCategory votingCategory, VotingPhase votingPhase, VotingConfirmationStatus votingConfirmationStatus, String fromDate,
                               String toDate, boolean validated) {
        return "null&votingCategory=" + votingCategory.getId() + "&phase=" + votingPhase.name() + "&status=" + votingConfirmationStatus.name() + "&fromDate=" +
                fromDate + "&toDate=" + toDate + "&validated=" +
                validated + "";
    }

    @Test(dataProvider = "isRenderContentTestData")
    public void testIsRenderContent(ConfirmVotingViewModel contextViewModel, boolean expectedResult) {
        confirmVotingContent.setContextViewModel(contextViewModel);
        assertEquals(confirmVotingContent.isRenderContent(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderContentTestData() {
        return new Object[][]{
                {contextViewModel(true), true},
                {contextViewModel(false), false}
        };
    }

    @Test
    public void testVotingPeriodUpdated_doesReloadWithinPeriod_andInitTabsComponent() {

        confirmVotingContent.initComponent(contextViewModel, null);
        verify(getInjectMock(ConfirmVotingTabs.class)).initComponent(any(ConfirmVotingTabs.ContextViewModel.class), eq(confirmVotingContent));

        LocalDateTime selectedFromDate = LocalDateTime.now().minusDays(15);
        LocalDateTime selectedToDate = LocalDateTime.now().plusDays(15);
        confirmVotingContent.getViewModel().getSelectedVotingPeriod().setFromDate(selectedFromDate);
        confirmVotingContent.getViewModel().getSelectedVotingPeriod().setToDateIncluding(selectedToDate);

        confirmVotingContent.onVotingPeriodUpdated();

        verify(getInjectMock(VotingInEnvelopeService.class))
                .votingConfirmationReport(any(), any(), any(), any(), any(), eq(selectedFromDate), eq(selectedToDate));
        
        verify(getInjectMock(ConfirmVotingTabs.class), times(2))
                .initComponent(any(ConfirmVotingTabs.ContextViewModel.class), eq(confirmVotingContent));
    }

    @Test
    public void testSearchCallback_initTabsComponentWithFilteredVotings() {

        confirmVotingContent.initComponent(contextViewModel, null);

        List<VotingViewModel> filteredVotings = Arrays.asList(
                VotingViewModel.builder()
                        .personId("123")
                        .build(),
                VotingViewModel.builder()
                        .personId("321")
                        .build(),
                VotingViewModel.builder()
                        .personId("213")
                        .build()
        );

        confirmVotingContent.onSearchCallback(filteredVotings);

        ArgumentCaptor<ConfirmVotingTabs.ContextViewModel> context = ArgumentCaptor.forClass(ConfirmVotingTabs.ContextViewModel.class);

        verify(getInjectMock(ConfirmVotingTabs.class), times(2))
                .initComponent(context.capture(), eq(confirmVotingContent));

        assertEquals(filteredVotings, context.getValue().getVotingList());
    }

    @Test
    public void testForceUpdatePropagatesUpwards() {
        UpdatableComponentHandler componentHandler = mock(UpdatableComponentHandler.class);
        confirmVotingContent.initComponent(contextViewModel, componentHandler);

        confirmVotingContent.forceUpdate(null);

        verify(componentHandler).forceUpdate(confirmVotingContent);
    }
}
