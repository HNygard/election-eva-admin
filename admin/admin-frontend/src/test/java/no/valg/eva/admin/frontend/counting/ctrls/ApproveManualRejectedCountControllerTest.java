package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.frontend.common.ValgdistriktOpptellingskategoriOgValggeografiHolder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ApproveManualRejectedCountControllerTest extends BaseFrontendTest {

    private static final String CONTEST_PATH = "111111.22.33.444444";
    private static final String AREA_PATH = "111111.22.33.4444";

    @DataProvider(name = "doInit")
    public static Object[][] doInit() {
        return new Object[][]{
                {false, false, false, false, false, FacesMessage.SEVERITY_ERROR, "@count.ballot.approve.rejected.noManualCounts"},
                {true, false, false, false, false, FacesMessage.SEVERITY_ERROR, "@count.ballot.approve.warning.regCorrectedNotDone"},
                {true, false, true, false, false, FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.registerNumbers"},
                {true, true, true, true, false, FacesMessage.SEVERITY_WARN, "@count.ballot.approve.rejected.rejectedBallotsProcessed"},
                {true, true, true, false, true, FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.toSettlement.done"}
        };
    }

    @Test
    public void doInit_withReportingUnitNotOnContestLevelAndNoFinalCount_addsFacesMessage() throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.BOROUGH);
        initFinalCount();
        getServletContainer().setRequestParameter("reportingUnitType", null);
        ArrayList<FacesMessage> expectedMessages = new ArrayList<>();
        expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "@count.info.operator_not_contest_level.manual", null));
        expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.noCounts", null));

        ctrl.doInit();

        assertThat(ctrl.getFinalCount()).isNull();
        assertFacesMessages(expectedMessages);
    }

    @Test(dataProvider = "doInit")
    public void doInit_withNoManualCount_addsFacesMessage(boolean isManualCount, boolean isModifiedBallotsProcessed, boolean isApproved,
                                                          boolean isRejectedBallotsProcessed, boolean isReadyForSettlement, FacesMessage.Severity severity, String message) throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.COUNTY);
        initFinalCount(isManualCount, isModifiedBallotsProcessed, isApproved, isRejectedBallotsProcessed, isReadyForSettlement);
        getServletContainer().setRequestParameter("reportingUnitType", null);

        ctrl.doInit();

        assertFacesMessage(severity, message);
    }

    @Test
    public void calculateTotalNewBallotCount_withBallotCounts_returns10() throws Exception {
        ApproveManualRejectedCountController ctrl = initializeMocks(ApproveManualRejectedCountController.class);
        BallotCount ballotCount1 = new BallotCount("id1", "name1", 2, 3);
        BallotCount ballotCount2 = new BallotCount("id2", "name2", 1, 4);
        mockFieldValue("newBallotCounts", Arrays.asList(ballotCount1, ballotCount2));
        getServletContainer().setRequestParameter("reportingUnitType", null);

        int sum = ctrl.calculateTotalNewBallotCount();

        assertThat(sum).isEqualTo(10);
    }

    @Test
    public void approveToSettlement_withFinalCount_verifyCountingServiceCall() throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.COUNTY);
        initFinalCount(true, true, true, false, true);
        FinalCount updateFinalCountMock = createMock(FinalCount.class);
        when(getInjectMock(CountingService.class).updateFinalCountStatusToSettlement(eq(getUserDataMock()), any(ApprovedFinalCountRef.class)))
                .thenReturn(updateFinalCountMock);
        getServletContainer().setRequestParameter("reportingUnitType", null);
        ctrl.doInit();

        ctrl.approveToSettlement();

        assertThat(ctrl.finalCount).isSameAs(updateFinalCountMock);
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.toSettlement.done");
    }

    @Test
    public void registerRejectedCounts_withInputValuesLowerThanOriginal_addsFacesMessage() throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.COUNTY);
        mockFieldValue("newBallotCounts", singletonList(new BallotCount("id", "name", 2, 3)));
        FinalCount originalFinalCount = initFinalCount(true, true, true, false, true);
        ctrl.finalCount = createMock(FinalCount.class);
        when(originalFinalCount.getTotalRejectedBallotCount()).thenReturn(10);

        ctrl.registerRejectedCounts();

        assertFacesMessage(FacesMessage.SEVERITY_WARN, "@count.ballot.approve.rejected.validate.toFewBallotsRegistred 10");
    }

    @Test
    public void registerRejectedCounts_withInputValuesHigherThanOriginal_addsFacesMessage() throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.COUNTY);
        mockFieldValue("newBallotCounts", singletonList(new BallotCount("id", "name", 2, 3)));
        FinalCount originalFinalCount = initFinalCount(true, true, true, false, true);
        ctrl.finalCount = createMock(FinalCount.class);
        when(originalFinalCount.getTotalRejectedBallotCount()).thenReturn(4);

        ctrl.registerRejectedCounts();

        assertFacesMessage(FacesMessage.SEVERITY_WARN, "@count.ballot.approve.rejected.validate.toManyBallotsRegistred 4");
    }

    @Test
    public void goToRegisterCorrectedBallotsRejectionMode_withSelected_returnsCorrectURL() throws Exception {
        ApproveManualRejectedCountController ctrl = create(AreaLevelEnum.COUNTY);
        mockFieldValue("newBallotCounts", singletonList(new BallotCount("id", "name", 2, 2)));
        FinalCount originalFinalCount = initFinalCount(true, true, true, false, true);
        when(originalFinalCount.getTotalRejectedBallotCount()).thenReturn(4);
        ctrl.finalCount = createMock(FinalCount.class);
        when(ctrl.finalCount.getTotalRejectedBallotCount()).thenReturn(0);
        when(ctrl.finalCount.getBallotCounts()).thenReturn(singletonList(new BallotCount("id", "name", 2, 2)));
        ValgdistriktOpptellingskategoriOgValggeografiHolder holder = getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class);
        when(holder.getSelectedAreaPath()).thenReturn(AreaPath.from(AREA_PATH));

        String url = ctrl.goToRegisterCorrectedBallotsRejectionMode();

        assertThat(url).isEqualTo("/secure/counting/modifiedBallotsStatus.xhtml?category=FO&contestPath=" + CONTEST_PATH + "&areaPath=" + AREA_PATH
                + "&fromApproveManualRejectedCount=true&faces-redirect=true");
    }

    @Test
    public void buildFromUrlPart_returnsfromApproveManualRejectedCountTrue() throws Exception {
        ApproveManualRejectedCountController ctrl = initializeMocks(ApproveManualRejectedCountController.class);

        assertThat(ctrl.buildFromUrlPart()).isEqualTo("fromApproveManualRejectedCount=true");
    }

    @Test
    public void isEditMode_returnsTrue() throws Exception {
        ApproveManualRejectedCountController ctrl = initializeMocks(ApproveManualRejectedCountController.class);

        assertThat(ctrl.isEditMode()).isTrue();
    }

    private void initFinalCount() {
        when(getInjectMock(CountingService.class).findApprovedFinalCount(eq(getUserDataMock()), any(ApprovedFinalCountRef.class))).thenReturn(null);
    }

    private FinalCount initFinalCount(boolean isManualCount, boolean isModifiedBallotsProcessed, boolean isApproved,
                                      boolean isRejectedBallotsProcessed, boolean isReadyForSettlement) {
        FinalCount result = createMock(FinalCount.class);
        when(result.isManualCount()).thenReturn(isManualCount);
        when(result.isModifiedBallotsProcessed()).thenReturn(isModifiedBallotsProcessed);
        when(result.isApproved()).thenReturn(isApproved);
        when(result.isRejectedBallotsProcessed()).thenReturn(isRejectedBallotsProcessed);
        when(result.isReadyForSettlement()).thenReturn(isReadyForSettlement);
        when(getInjectMock(CountingService.class).findApprovedFinalCount(eq(getUserDataMock()), any(ApprovedFinalCountRef.class))).thenReturn(result);
        BallotCount ballotCount = new BallotCount("id", "name", 10, 20);
        when(result.getBallotCounts()).thenReturn(singletonList(ballotCount));
        when(getInjectMock(ModifiedBallotBatchService.class).buildModifiedBallotStatuses(getUserDataMock(), result, MODIFIED_BALLOTS_PROCESS))
                .thenReturn(singletonList(new ModifiedBallotsStatus(ballotCount, null)));
        return result;
    }

    @Test
    public void testBuildFromUrlPart_verifyCorrectString() throws Exception {
        ApproveManualRejectedCountController controller = create(AreaLevelEnum.COUNTY);
        assertEquals(controller.buildFromUrlPart(), "fromApproveManualRejectedCount=true");
    }

    private ApproveManualRejectedCountController create(AreaLevelEnum contestInfoAreaLevel)
            throws Exception {
        ApproveManualRejectedCountController ctrl = initializeMocks(ApproveManualRejectedCountController.class);
        ValgdistriktOpptellingskategoriOgValggeografiHolder holder = getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class);
        ContestInfo contestInfoMock = createMock(ContestInfo.class);
        when(holder.getSelectedContestInfo()).thenReturn(contestInfoMock);
        when(holder.getSelectedCountCategory()).thenReturn(CountCategory.FO);
        when(contestInfoMock.getElectionPath()).thenReturn(ElectionPath.from(CONTEST_PATH));
        when(contestInfoMock.getAreaLevel()).thenReturn(contestInfoAreaLevel);
        when(getUserDataMock().getOperatorAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);

        return ctrl;
    }
}

