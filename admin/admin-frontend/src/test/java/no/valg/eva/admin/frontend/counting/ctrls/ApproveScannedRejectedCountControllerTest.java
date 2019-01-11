package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedBallot;
import no.valg.eva.admin.common.counting.model.ApprovedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.counting.model.RejectedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.service.CastBallotBinaryDataService;
import no.valg.eva.admin.common.counting.service.CastBallotService;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ValgdistriktOpptellingskategoriOgValggeografiHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Arrays;
import java.util.List;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.renderedAndEnabled;
import static no.valg.eva.admin.frontend.common.ButtonType.APPROVE_TO_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ApproveScannedRejectedCountControllerTest extends BaseFrontendTest {

    private static final ElectionPath DEFAULT_ELECTION_PATH = ElectionPath.from("111111.22.33.444444");

    private ApproveScannedRejectedCountController ctrl;

    @BeforeMethod
    public void setUp() throws Exception {
        ctrl = initializeMocks(ApproveScannedRejectedCountController.class);
    }

    @Test
    public void doInit_withNoFinalCount_returnsNoCountsError() {
        mockPicker(createContestInfo(MUNICIPALITY, DEFAULT_ELECTION_PATH), VS);
        mockFinalCount(null);

        ctrl.doInit();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.noCounts");
    }

    @Test
    public void doInit_withManualFinalCount_returnsNoScannedError() {
        mockPicker(createContestInfo(MUNICIPALITY, DEFAULT_ELECTION_PATH), VS);
        mockFinalCount(createFinalCount(true, false, false));

        ctrl.doInit();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.ballot.approve.rejected.noScannedCounts");
    }

    @Test
    public void doInit_withModifiedNotProcessedAndNotApproved_returnsRegCorrectedNotDoneError() {
        mockPicker(createContestInfo(MUNICIPALITY, DEFAULT_ELECTION_PATH), VS);
        mockFinalCount(createFinalCount(false, false, false));

        ctrl.doInit();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.ballot.approve.warning.regCorrectedNotDone");
    }

    @Test
    public void doInit_withValidStatus_verifyState() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();

        assertThat(ctrl.getFinalCount()).isNotNull();
        assertThat(ctrl.getBallotRejections()).hasSize(3);
        assertThat(ctrl.getBallots()).hasSize(3);
        assertThat(ctrl.getRejectedBallots()).hasSize(6);
        assertThat(ctrl.getStateSelectItems()).hasSize(3);
        assertThat(ctrl.getNewBallotCounts()).hasSize(4);
    }

    @Test
    public void downloadRejectedBallot_withNoBinaryData_returnsDownloadNoResultInfoMessage() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        mockCastBallotBinaryData(null);

        ctrl.downloadRejectedBallot(ctrl.getRejectedBallots().get(0));

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.download.noResult");
    }

    @Test
    public void downloadRejectedBallot_withBinaryData_verifyResponseWrite() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        CastBallotBinaryData castBallotBinaryData = mockCastBallotBinaryData(createCastBallotBinaryData("test"));

        ctrl.downloadRejectedBallot(ctrl.getRejectedBallots().get(0));

        verify(getServletContainer().getResponseMock()).setContentType(castBallotBinaryData.mimeType());
        verify(getServletContainer().getResponseMock()).addHeader("Content-Disposition", "attachment; filename=\"file.txt\"");
        verify(getServletContainer().getResponseMock()).setContentLength(4);
        verify(getServletContainer().getResponseMock().getOutputStream()).write(castBallotBinaryData.bytes());
    }

    @Test
    public void downloadMunicipalityRejectedBallotFor_withNoBinaryData_returnsDownloadNoResultInfoMessage() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid(COUNTY, false, null);
        mockCastBallotBinaryData(null);

        ctrl.downloadMunicipalityRejectedBallotFor(ctrl.getRejectedBallots().get(0));

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.download.noResult");
    }

    @Test
    public void downloadMunicipalityRejectedBallotFor_withBinaryData_verifyResponseWrite() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid(COUNTY, false, null);
        CastBallotBinaryData castBallotBinaryData = mockCastBallotBinaryData(createCastBallotBinaryData("test"));

        ctrl.downloadMunicipalityRejectedBallotFor(ctrl.getRejectedBallots().get(0));

        verify(getServletContainer().getResponseMock()).setContentType(castBallotBinaryData.mimeType());
        verify(getServletContainer().getResponseMock()).addHeader("Content-Disposition", "attachment; filename=\"file.txt\"");
        verify(getServletContainer().getResponseMock()).setContentLength(4);
        verify(getServletContainer().getResponseMock().getOutputStream()).write(castBallotBinaryData.bytes());
    }

    @Test
    public void downloadMunicipalityApprovedBallotFor_withNoBinaryData_returnsDownloadNoResultInfoMessage() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid(COUNTY, false, null);
        mockCastBallotBinaryData(null);

        ctrl.downloadMunicipalityApprovedBallotFor(new RejectedBallot("DD-1", "DD"));

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.ballot.approve.rejected.download.noResult");
    }

    @Test
    public void downloadMunicipalityApprovedBallotFor_withBinaryData_verifyResponseWrite() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid(COUNTY, false, null);
        CastBallotBinaryData castBallotBinaryData = mockCastBallotBinaryData(createCastBallotBinaryData("test"));

        ctrl.downloadMunicipalityApprovedBallotFor(new RejectedBallot("DD-1", "DD"));

        verify(getServletContainer().getResponseMock()).setContentType(castBallotBinaryData.mimeType());
        verify(getServletContainer().getResponseMock()).addHeader("Content-Disposition", "attachment; filename=\"file.txt\"");
        verify(getServletContainer().getResponseMock()).setContentLength(4);
        verify(getServletContainer().getResponseMock().getOutputStream()).write(castBallotBinaryData.bytes());
    }

    @Test
    public void getRejectedBallotName_withRejectedAABallot_returnsAABallotName() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        RejectedBallot ballot = ctrl.getRejectedBallots().get(0);

        String result = ctrl.getRejectedBallotName(ballot);

        assertThat(result).isEqualTo("@ballot_rejection[AA].name");
    }

    @Test
    public void getRejectedBallotName_withModifedHBallot_returnsHBallotName() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        RejectedBallot ballot = ctrl.getRejectedBallots().get(0);
        ballot.setState(RejectedBallot.State.MODIFIED);
        ballot.setSelectedBallotId("H");

        String result = ctrl.getRejectedBallotName(ballot);

        assertThat(result).isEqualTo("@count.modified @party[H].name");
    }

    @Test
    public void isEdited_verifyCorrectItemReturnsTrue() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        List<RejectedBallot> ballots = ctrl.getRejectedBallots();
        ctrl.edit(ballots.get(0));

        assertThat(ctrl.isCurrent()).isTrue();
        assertThat(ctrl.isEdited(ballots.get(0))).isTrue();
        for (int i = 1; i < ballots.size(); i++) {
            assertThat(ctrl.isEdited(ballots.get(i))).isFalse();
        }
    }

    @Test
    public void edit_withRejectedBallot_verifyState() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        RejectedBallot ballot = ctrl.getRejectedBallots().get(0);

        ctrl.edit(ballot);

        RejectedBallot current = getPrivateField("current", RejectedBallot.class);
        RejectedBallot previous = getPrivateField("previous", RejectedBallot.class);
        assertThat(current).isEqualToComparingFieldByField(ballot);
        assertThat(previous).isEqualToComparingFieldByField(ballot);
    }

    @Test
    public void done_fromRejectedToModified_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);

        // Edit from REJECTED to MODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "H");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 1, 0);
    }

    @Test
    public void done_fromRejectedToUnModified_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);

        // Edit from REJECTED to UNMODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "H");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 1);
    }

    @Test
    public void done_fromModifiedToUnModified_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 1, 0);

        // Edit from MODIFIED to UNMODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "H");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 1);
    }

    @Test
    public void done_fromModifiedToRejected_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 1, 0);

        // Edit from MODIFIED to UNMODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.REJECTED, "AA");

        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
    }

    @Test
    public void done_fromUnModifiedToModified_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 1);

        // Edit from UNMODIFIED to MODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "H");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 1, 0);
    }

    @Test
    public void done_fromUnModifiedToRejected_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 1);

        // Edit from UNMODIFIED to MODIFIED
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.REJECTED, "AA");

        assertRejectedBallotCount(ctrl, "AA", 2);
        assertBallotCount(ctrl, "H", 0, 0);
    }

    @Test
    public void done_withRejectedValueChange_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        assertRejectedBallotCount(ctrl, "AA", 2);
        assertRejectedBallotCount(ctrl, "BB", 2);

        // Edit REJECTED from AA to BB
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.REJECTED, "BB");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertRejectedBallotCount(ctrl, "BB", 3);
    }

    @Test
    public void done_withModifiedValueChange_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 1, 0);

        // Edit MODIFIED from H to V
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.MODIFIED, "V");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 0);
        assertBallotCount(ctrl, "V", 1, 0);
    }

    @Test
    public void done_withUnModifiedValueChange_verifyCounts() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "H");
        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 1);

        // Edit MODIFIED from H to V
        editRejectedBallot(ctrl, "AA-1", RejectedBallot.State.UNMODIFIED, "V");

        assertRejectedBallotCount(ctrl, "AA", 1);
        assertBallotCount(ctrl, "H", 0, 0);
        assertBallotCount(ctrl, "V", 0, 1);
    }

    @Test
    public void doRenderSaveLink_withNoEdit_returnsFalse() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();

        assertThat(ctrl.doRenderSaveLink(ctrl.getRejectedBallots().get(0))).isFalse();
    }

    @Test
    public void doRenderSaveLink_withEditAndOtherRejectedBallot_returnsFalse() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        ctrl.edit(ctrl.getRejectedBallots().get(0));

        assertThat(ctrl.doRenderSaveLink(ctrl.getRejectedBallots().get(1))).isFalse();
    }

    @Test
    public void doRenderSaveLink_withEditAndEmptyModified_returnsFalse() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        RejectedBallot ballot = ctrl.getRejectedBallots().get(0);
        ctrl.edit(ballot);
        ballot.setState(RejectedBallot.State.MODIFIED);
        ballot.setSelectedBallotId(null);

        assertThat(ctrl.doRenderSaveLink(ballot)).isFalse();
    }

    @Test
    public void doRenderSaveLink_withEditAndModifiedWithValue_returnsTrue() throws Exception {
        ApproveScannedRejectedCountController ctrl = setupValid();
        RejectedBallot ballot = ctrl.getRejectedBallots().get(0);
        ctrl.edit(ballot);
        ballot.setState(RejectedBallot.State.MODIFIED);
        ballot.setSelectedBallotId("H");

        assertThat(ctrl.doRenderSaveLink(ballot)).isTrue();
    }

    @Test
    public void buildFromUrlPart_returnsConstant() {
        ApproveScannedRejectedCountController ctrl = new ApproveScannedRejectedCountController();

        assertThat(ctrl.buildFromUrlPart()).isEqualTo("fromApproveScannedRejectedCount=true");
    }

    @Test
    public void isEditMode_returnsFalse() {
        ApproveScannedRejectedCountController ctrl = new ApproveScannedRejectedCountController();

        assertThat(ctrl.isEditMode()).isFalse();
    }

    private void editRejectedBallot(ApproveScannedRejectedCountController ctrl, String id, RejectedBallot.State state, String ballotId) {
        RejectedBallot rejectedBallot = rejectedBallot(ctrl, id);
        ctrl.edit(rejectedBallot);
        rejectedBallot.setState(state);
        if (state == RejectedBallot.State.REJECTED) {
            rejectedBallot.setSelectedBallotRejectionId(ballotId);
        } else {
            rejectedBallot.setSelectedBallotId(ballotId);
        }
        ctrl.done();
    }

    private RejectedBallot rejectedBallot(ApproveScannedRejectedCountController ctrl, String id) {
        for (RejectedBallot rb : ctrl.getRejectedBallots()) {
            if (rb.getId().equals(id)) {
                return rb;
            }
        }
        throw new RuntimeException("unknown rejected ballot id: " + id);
    }

    private void assertBallotCount(ApproveScannedRejectedCountController ctrl, String id, int modified, int unmodified) {
        for (BallotCount bc : ctrl.getNewBallotCounts()) {
            if (bc.getId().equals(id)) {
                assertThat(bc.getModifiedCount()).isEqualTo(modified);
                assertThat(bc.getUnmodifiedCount()).isEqualTo(unmodified);
                return;
            }
        }
        fail("Could not find BallotCount with id=" + id);
    }

    private void assertRejectedBallotCount(ApproveScannedRejectedCountController ctrl, String id, int count) {
        for (RejectedBallotCount rbc : ctrl.getFinalCount().getRejectedBallotCounts()) {
            if (rbc.getId().equals(id)) {
                assertThat(rbc.getCount()).isEqualTo(count);
                return;
            }
        }
        fail("Could not find RejectedBallotCount with id=" + id);
    }

    private ApproveScannedRejectedCountController setupValid() throws Exception {
        return setupValid(MUNICIPALITY, false, null);
    }

    private ApproveScannedRejectedCountController setupValid(
            AreaLevelEnum areaLevel, boolean electionEventAdmin, ReportingUnitTypeId reportingUnitTypeId) throws Exception {
        ApproveScannedRejectedCountController ctrl = initializeMocks(ApproveScannedRejectedCountController.class);
        when(getUserDataMock().getOperatorAreaLevel()).thenReturn(areaLevel);
        when(getUserDataMock().isElectionEventAdminUser()).thenReturn(electionEventAdmin);
        if (reportingUnitTypeId != null) {
            getServletContainer().setRequestParameter("reportingUnitType", reportingUnitTypeId.name());
        }
        mockPicker(createContestInfo(areaLevel, DEFAULT_ELECTION_PATH), VS);
        mockFinalCount(createFinalCount(false, true, true));
        mockRejectedBallots();
        if (areaLevel == COUNTY) {
            mockApprovedBallots();
        }
        ctrl.doInit();
        return ctrl;
    }

    private List<RejectedBallot> mockRejectedBallots() {
        List<RejectedBallot> result = Arrays.asList(
                new RejectedBallot("AA-1", "AA"),
                new RejectedBallot("AA-2", "AA"),
                new RejectedBallot("BB-1", "BB"),
                new RejectedBallot("BB-2", "BB"),
                new RejectedBallot("CC-1", "CC"),
                new RejectedBallot("CC-2", "CC"));
        when(getInjectMock(CastBallotService.class)
                .rejectedBallots(any(UserData.class), any(ApprovedFinalCountRef.class)))
                .thenReturn(result);
        return result;
    }

    private List<ApprovedBallot> mockApprovedBallots() {
        List<ApprovedBallot> result = Arrays.asList(
                new ApprovedBallot("DD-1", "DD", false),
                new ApprovedBallot("DD-2", "DD", true));
        when(getInjectMock(CastBallotService.class)
                .approvedBallots(any(UserData.class), any(ApprovedFinalCountRef.class)))
                .thenReturn(result);
        return result;
    }

    private void mockFinalCount(FinalCount finalCount) {
        when(getInjectMock(CountingService.class).findApprovedFinalCount(eq(getUserDataMock()), any(ApprovedFinalCountRef.class))).thenReturn(finalCount);
    }

    private void mockPicker(ContestInfo contestInfo, CountCategory countCategory) {
        when(getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class).getSelectedContestInfo()).thenReturn(contestInfo);
        when(getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class).getSelectedCountCategory()).thenReturn(countCategory);
    }

    private ContestInfo createContestInfo(AreaLevelEnum areaLevel, ElectionPath electionPath) {
        ContestInfo contestInfo = createMock(ContestInfo.class);
        when(contestInfo.getAreaLevel()).thenReturn(areaLevel);
        when(contestInfo.getElectionPath()).thenReturn(electionPath);
        return contestInfo;
    }

    private FinalCount createFinalCount(boolean isManualCount, boolean isModifiedBallotsProcessed, boolean isApproved) {
        FinalCount finalCount = createMock(FinalCount.class);
        when(finalCount.isManualCount()).thenReturn(isManualCount);
        when(finalCount.isModifiedBallotsProcessed()).thenReturn(isModifiedBallotsProcessed);
        when(finalCount.isApproved()).thenReturn(isApproved);
        if (isApproved) {
            when(finalCount.getStatus()).thenReturn(APPROVED);
        }
        List<BallotCount> ballotCounts = getBallotCounts();
        when(finalCount.getBallotCounts()).thenReturn(ballotCounts);
        List<RejectedBallotCount> rejectedBallotCounts = getRejectedBallotCounts();
        when(finalCount.getRejectedBallotCounts()).thenReturn(rejectedBallotCounts);
        return finalCount;
    }

    private List<BallotCount> getBallotCounts() {
        return Arrays.asList(mockBallotCount("H"), mockBallotCount("V"), mockBallotCount("A"));
    }

    private BallotCount mockBallotCount(String id) {
        BallotCount result = createMock(BallotCount.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn("name[" + id + "]");
        return result;
    }

    private List<RejectedBallotCount> getRejectedBallotCounts() {
        return Arrays.asList(mockRejectedBallotCount("AA"), mockRejectedBallotCount("BB"), mockRejectedBallotCount("CC"));
    }

    private RejectedBallotCount mockRejectedBallotCount(String id) {
        RejectedBallotCount result = new RejectedBallotCount();
        result.setId(id);
        result.setCount(2);
        return result;
    }

    private CastBallotBinaryData mockCastBallotBinaryData(CastBallotBinaryData castBallotBinaryData) {
        when(getInjectMock(CastBallotBinaryDataService.class)
                .rejectedCastBallotBinaryData(eq(getUserDataMock()), any(RejectedCastBallotRefForApprovedFinalCount.class)))
                .thenReturn(castBallotBinaryData);
        when(getInjectMock(CastBallotBinaryDataService.class)
                .approvedCastBallotBinaryData(eq(getUserDataMock()), any(ApprovedCastBallotRefForApprovedFinalCount.class)))
                .thenReturn(castBallotBinaryData);
        return castBallotBinaryData;
    }

    private CastBallotBinaryData createCastBallotBinaryData(String s) {
        return new CastBallotBinaryData("file.txt", "html/text", s.getBytes());
    }

    @Test
    public void hasMunicipalityRejectedBallots__givenMunicipalityUser_returnsFalse() throws Exception {
        ctrl = setupValid(MUNICIPALITY, false, null);
        assertThat(ctrl.hasMunicipalityRejectedBallots()).isFalse();
    }

    @Test
    public void hasMunicipalityRejectedBallots__givenCountyUser_returnsTrue() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        assertThat(ctrl.hasMunicipalityRejectedBallots()).isTrue();
    }

    @Test
    public void hasMunicipalityRejectedBallots__givenElectionEventUserAndFylkesvalgstyret_returnsTrue() throws Exception {
        ctrl = setupValid(ROOT, true, FYLKESVALGSTYRET);
        assertThat(ctrl.hasMunicipalityRejectedBallots()).isTrue();
    }

    @Test
    public void hasMunicipalityRejectedBallots__givenElectionEventUser_returnsFalse() throws Exception {
        ctrl = setupValid(ROOT, true, null);
        assertThat(ctrl.hasMunicipalityRejectedBallots()).isFalse();
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "expected <reportingUnitType> to be <FYLKESVALGSTYRET>, but was <VALGSTYRET>")
    public void doInit_givenValgstyret_throwsException() throws Exception {
        setupValid(MUNICIPALITY, false, VALGSTYRET);
    }

    @Test
    public void hasMunicipalityRejectedBallotFor_givenRejectedBallotWithMunicipalityRejectedBallot_returnsTrue() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("AA-1", "AA");
        assertThat(ctrl.hasMunicipalityRejectedBallotFor(rejectedBallot)).isTrue();
    }

    @Test
    public void hasMunicipalityRejectedBallotFor_givenRejectedBallotWithoutMunicipalityRejectedBallot_returnsFalse() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("DD-1", "DD");
        assertThat(ctrl.hasMunicipalityRejectedBallotFor(rejectedBallot)).isFalse();
    }

    @Test
    public void hasMunicipalityApprovedBallotFor_givenRejectedBallotWithoutMunicipalityApprovedBallot_returnsFalse() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("AA-1", "AA");
        assertThat(ctrl.hasMunicipalityApprovedBallotFor(rejectedBallot)).isFalse();
    }

    @Test
    public void hasMunicipalityApprovedBallotFor_givenRejectedBallotWithMunicipalityApprovedBallot_returnsTrue() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("DD-1", "DD");
        assertThat(ctrl.hasMunicipalityApprovedBallotFor(rejectedBallot)).isTrue();
    }

    @Test
    public void getMunicipalityRejectedBallotNameFor_givenRejectedBallotWithMunicipalityRejectedBallot_returnsRejectedBallotName() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("AA-1", "AA");
        assertThat(ctrl.getMunicipalityRejectedBallotNameFor(rejectedBallot)).isEqualTo("@ballot_rejection[AA].name");
    }

    @Test
    public void getMunicipalityRejectedBallotNameFor_givenRejectedBallotWithMunicipalityApprovedBallot_returnsApprovedBallotName() throws Exception {
        ctrl = setupValid(COUNTY, false, null);
        RejectedBallot rejectedBallot = new RejectedBallot("DD-1", "DD");
        assertThat(ctrl.getMunicipalityApprovedBallotNameFor(rejectedBallot)).isEqualTo("@count.unmodified @party[DD].name");
    }

    @Test
    public void button_givenApproveToSettlementHasCurrent_returnRenderedAndDisabled() throws Exception {
        ctrl = setupValid();
        RejectedBallot rejectedBallot = new RejectedBallot("AA-1", "AA");
        ctrl.edit(rejectedBallot);

        Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

        assertThat(button).isEqualTo(enabled(false));
    }

    @Test
    public void button_givenApproveToSettlementHasNoCurrent_returnRenderedAndEnabled() throws Exception {
        ctrl = setupValid();

        Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

        assertThat(button).isEqualTo(renderedAndEnabled());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void goToRegisterCorrectedBallotsRejectionMode_callsProcessRejectedBallots() throws Exception {
        ctrl = setupValid();

        ctrl.goToRegisterCorrectedBallotsRejectionMode();

        verify(getInjectMock(CastBallotService.class)).processRejectedBallots(any(UserData.class), any(ApprovedFinalCountRef.class), anyList());
    }
}

