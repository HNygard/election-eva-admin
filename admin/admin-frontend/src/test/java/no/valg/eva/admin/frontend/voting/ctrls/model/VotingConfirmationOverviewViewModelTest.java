package no.valg.eva.admin.frontend.voting.ctrls.model;

import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingRejection;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.frontend.voting.ctrls.VotingViewModel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.EnumSet.allOf;
import static no.valg.eva.admin.frontend.voting.ctrls.model.VotingOverviewType.CONFIRMED;
import static no.valg.eva.admin.frontend.voting.ctrls.model.VotingOverviewType.TO_BE_CONFIRMED;
import static org.testng.Assert.assertEquals;

public class VotingConfirmationOverviewViewModelTest {

    @Test(dataProvider = "gettersAndSettersTestData")
    public void testGettersAndSetters(VotingConfirmationOverviewViewModel viewModel) {
        VotingConfirmationOverviewViewModel result = VotingConfirmationOverviewViewModel.builder()
                .rejectionReasons(viewModel.getRejectionReasons())
                .allVotingCategories(viewModel.getAllVotingCategories())
                .fromDate(viewModel.getFromDate())
                .toDateIncluding(viewModel.getToDateIncluding())
                .build();

        result.setVotingOverviewType(viewModel.getVotingOverviewType());
        result.setSelectedVoting(viewModel.getSelectedVoting());
        result.setSelectedVotingCategories(viewModel.getSelectedVotingCategories());
        result.setVotings(viewModel.getVotings());
        result.setSelectedConfirmationStatus(viewModel.getSelectedConfirmationStatus());
        result.setSelectedVotingPeriod(viewModel.getSelectedVotingPeriod());
        result.setSelectedVotingList(viewModel.getSelectedVotingList());
        result.setSelectedProcessingType(viewModel.getSelectedProcessingType());
        result.setSuggestedProcessingList(viewModel.getSuggestedProcessingList());
        result.setSelectedSuggestedProcessingList(viewModel.getSuggestedProcessingList().stream()
                .map(SuggestedProcessingDto::getId)
                .collect(Collectors.toList()));

        assertEquals(result.getId(), viewModel.getId());
        assertEquals(result.getVotingOverviewType(), viewModel.getVotingOverviewType());
        assertEquals(result.isRenderProcessingTypeColumn(), viewModel.isRenderProcessingTypeColumn());
        assertEquals(result.isRenderVotingStatusColumn(), viewModel.isRenderVotingStatusColumn());
        assertEquals(result.isRenderSuggestedRejectionReason(), viewModel.isRenderSuggestedRejectionReason());
        assertEquals(result.getSuggestedProcessingList(), viewModel.getSuggestedProcessingList());
        assertEquals(result.getVotingStatusColumnHeader(), viewModel.getVotingStatusColumnHeader());
        assertEquals(result.getSuggestedProcessingList(), viewModel.getSuggestedProcessingList());
        assertEquals(result.getAllVotingCategories(), viewModel.getAllVotingCategories());
        assertEquals(result.getRejectionReasons(), viewModel.getRejectionReasons());
        assertEquals(result.getSelectedVoting(), viewModel.getSelectedVoting());
        assertEquals(result.getSelectedVotingCategories(), viewModel.getSelectedVotingCategories());
        assertEquals(result.getSelectedVotingList(), viewModel.getSelectedVotingList());
        assertEquals(result.getSelectedProcessingType(), viewModel.getSelectedProcessingType());
        assertEquals(result.getFromDate(), viewModel.getFromDate());
        assertEquals(result.getToDateIncluding(), viewModel.getToDateIncluding());


    }

    @DataProvider
    public Object[][] gettersAndSettersTestData() {

        VotingConfirmationOverviewViewModel viewModel = VotingConfirmationOverviewViewModel.builder()
                .votingOverviewType(CONFIRMED)
                .toDateIncluding(LocalDateTime.now())
                .fromDate(LocalDateTime.now())
                .votings(Collections.emptyList())
                .allVotingCategories(Collections.emptyList())
                .selectedVotingCategories(singletonList(VotingCategory.FI.getId()))
                .selectedVoting(VotingViewModel.builder().build())
                .votings(Collections.emptyList())
                .selectedConfirmationStatus(VotingConfirmationStatus.APPROVED)
                .selectedVotingPeriod(VotingPeriodViewModel.builder().build())
                .suggestedProcessingList(singletonList(SuggestedProcessingDto.builder()
                        .id("TG")
                        .processingType(ProcessingType.SUGGESTED_APPROVED)
                        .textProperty("textProperty")
                        .build()))
                .rejectionReasons(Collections.emptyList())
                .build();

        return new Object[][]{
                {viewModel}
        };
    }

    @Test(dataProvider = "showValidatedVotingsTestData")
    public void testShowValidatedVotings_givenViewModel_verifiesShowValidatedVotingsField(VotingConfirmationOverviewViewModel votingConfirmationOverviewViewModel, boolean expectedResult) {
        assertEquals(votingConfirmationOverviewViewModel.isShowConfirmedVotings(), expectedResult);
    }

    @DataProvider
    public Object[][] showValidatedVotingsTestData() {
        return new Object[][]{
                {VotingConfirmationOverviewViewModel.builder().build(), false},
                {VotingConfirmationOverviewViewModel.builder()
                        .votingOverviewType(CONFIRMED)
                        .build(), true}
        };
    }

    @Test(dataProvider = "setShowValidatedVotingsTestData")
    public void testSetShowValidatedVotings_givenViewModel_verifiesShowValidatedVotingsField(VotingOverviewType expectedResult) {
        VotingConfirmationOverviewViewModel viewModel = VotingConfirmationOverviewViewModel.builder().build();
        viewModel.setVotingOverviewType(expectedResult);

        assertEquals(viewModel.getVotingOverviewType(), expectedResult);
    }

    @DataProvider
    public Object[][] setShowValidatedVotingsTestData() {
        return new Object[][]{
                {CONFIRMED},
                {TO_BE_CONFIRMED}
        };
    }

    @Test
    public void testVotingRejectionFromId_givenRejectionId_verifiesRejection() {
        List<VotingRejectionDto> rejectionReasons = votingRejections();
        VotingConfirmationOverviewViewModel votingConfirmationOverviewViewModel = VotingConfirmationOverviewViewModel.builder()
                .rejectionReasons(rejectionReasons)
                .build();

        rejectionReasons.forEach(votingRejectionDto -> {
            assertEquals(votingConfirmationOverviewViewModel.votingRejectionFromId(votingRejectionDto.getId()), votingRejectionDto);
        });
    }

    @DataProvider
    public Object[][] votingRejectionFromIdTestData() {
        return allOf(VotingRejection.class).stream()
                .map(votingRejection ->
                        new Object[]{rejectionDtos(votingRejection.getId(), votingRejection.name())})
                .toArray(Object[][]::new);
    }

    @Test
    public void testGetRejectionReasons_givenNull_verifiesEmptyList() {
        assertEquals(VotingConfirmationOverviewViewModel.builder().build().getRejectionReasons(), Collections.emptyList());
    }

    @Test
    public void testGetSuggestedProcessingList_givenNull_verifiesEmptyList() {
        assertEquals(VotingConfirmationOverviewViewModel.builder().build().getSuggestedProcessingList(), Collections.emptyList());
    }

    @Test
    public void testGetSuggestedRejectionReasons_givenSortedRejectionReasons_verifiesList() {
        List<SuggestedProcessingDto> suggestedProcessingList = singletonList(SuggestedProcessingDto.builder()
        .textProperty("textProperty")
        .processingType(ProcessingType.SUGGESTED_APPROVED)
        .id("id")
        .build());

        VotingConfirmationOverviewViewModel votingConfirmationOverviewViewModel = VotingConfirmationOverviewViewModel.builder()
                .suggestedProcessingList(suggestedProcessingList)
                .build();

        assertEquals(votingConfirmationOverviewViewModel.getSuggestedProcessingList(), suggestedProcessingList);
    }

    @Test(dataProvider = "getVotingStatusColumnHeaderTestData")
    public void testGetVotingStatusColumnHeader_givenShowValidatedVotings_verifiesHeaderString(boolean showValidatedVotings, String expectedHeader) {
        VotingConfirmationOverviewViewModel viewModel = viewModel(showValidatedVotings);

        assertEquals(viewModel.getVotingStatusColumnHeader(), expectedHeader);
    }

    @DataProvider
    public Object[][] getVotingStatusColumnHeaderTestData() {
        return new Object[][]{
                {false, "@voting.envelope.overview.heading.suggestedProcessing"},
                {true, "@voting.envelope.overview.heading.processedStatus"},
        };
    }

    @Test(dataProvider = "isRenderProcessingTypeColumnTestData")
    public void testIsRenderProcessingTypeColumn_givenShowValidatedVotings_verifiesRender(boolean showValidatedVotings, boolean expectedResult) {
        VotingConfirmationOverviewViewModel viewModel = viewModel(showValidatedVotings);

        assertEquals(viewModel.isRenderProcessingTypeColumn(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderProcessingTypeColumnTestData() {
        return new Object[][]{
                {true, false},
                {false, true}
        };
    }

    @Test(dataProvider = "isRenderSuggestedRejectionReasonTestData")
    public void testIsRenderSuggestedRejectionReason_givenShowValidatedVotings_verifiesRender(boolean showValidatedVotings, boolean expectedResult) {
        VotingConfirmationOverviewViewModel viewModel = viewModel(showValidatedVotings);

        assertEquals(viewModel.isRenderSuggestedRejectionReason(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderSuggestedRejectionReasonTestData() {
        return new Object[][]{
                {true, false},
                {false, true}
        };
    }

    @Test(dataProvider = "isRenderVotingStatusColumnTestData")
    public void testIsRenderVotingStatusColumn_givenShowValidatedVotings_verifiesRender(boolean showValidatedVotings, boolean expectedResult) {
        VotingConfirmationOverviewViewModel viewModel = viewModel(showValidatedVotings);

        assertEquals(viewModel.isRenderVotingStatusColumn(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderVotingStatusColumnTestData() {
        return new Object[][]{
                {true, true},
                {false, false}
        };
    }

    private VotingConfirmationOverviewViewModel viewModel(boolean showValidatedVotings) {
        return VotingConfirmationOverviewViewModel.builder()
                .votingOverviewType(showValidatedVotings ? CONFIRMED : TO_BE_CONFIRMED)
                .build();
    }

    private static List<VotingRejectionDto> votingRejections() {
        return EnumSet.allOf(VotingRejection.class).stream()
                .map(votingRejection -> rejectionDto(votingRejection.getId(), votingRejection.name()))
                .collect(Collectors.toList());
    }

    private static List<VotingRejectionDto> rejectionDtos(String rejectionId, String name) {
        return singletonList(VotingRejectionDto.builder()
                .name(name)
                .id(rejectionId)
                .suggestedRejectionName(name)
                .build());
    }

    private static VotingRejectionDto rejectionDto(String rejectionId, String name) {
        return VotingRejectionDto.builder()
                .name(name)
                .id(rejectionId)
                .suggestedRejectionName(name)
                .build();
    }
}