package no.valg.eva.admin.frontend.counting.ctrls;

import lombok.extern.log4j.Log4j;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedBallot;
import no.valg.eva.admin.common.counting.model.ApprovedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotId;
import no.valg.eva.admin.common.counting.model.BallotRejectionId;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.counting.model.RejectedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.service.CastBallotBinaryDataService;
import no.valg.eva.admin.common.counting.service.CastBallotService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.RejectedBallot.State.MODIFIED;
import static no.valg.eva.admin.common.counting.model.RejectedBallot.State.REJECTED;
import static no.valg.eva.admin.common.counting.model.RejectedBallot.State.UNMODIFIED;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Named
@ConversationScoped
@Log4j
public class ApproveScannedRejectedCountController extends BaseApproveRejectedCountController {

    private static final String UNSUPPORTED_STATE = "Unsupported state: ";
    private static final long serialVersionUID = -8376583549957310797L;

    @Inject
    private CastBallotBinaryDataService castBallotBinaryDataService;
    @Inject
    private CastBallotService castBallotService;

    private Map<String, RejectedBallot> municipalityRejectedBallotMap;
    private Map<String, ApprovedBallot> municipalityApprovedBallotMap;
    private List<RejectedBallot> rejectedBallots;
    private List<SelectItem> stateSelectItems;
    private List<SelectItem> ballotSelectItems;
    private List<SelectItem> ballotRejectionSelectItems;
    private RejectedBallot current;
    private RejectedBallot previous;

    @Override
    protected void doInit() {
        super.doInit();
        initRejectedBallots();
    }

    public void downloadMunicipalityRejectedBallotFor(RejectedBallot rejectedBallot) {
        downloadRejectedBallot(VALGSTYRET, municipalityRejectedBallotFor(rejectedBallot));
    }

    public void downloadRejectedBallot(RejectedBallot rejectedBallot) {
        downloadRejectedBallot(reportingUnitTypeIdForRead(), rejectedBallot);
    }


    private void downloadRejectedBallot(ReportingUnitTypeId reportingUnitTypeId, RejectedBallot rejectedBallot) {
        CastBallotId castBallotId = new CastBallotId(rejectedBallot.getId());
        BallotRejectionId ballotRejectionId = new BallotRejectionId(rejectedBallot.getBallotRejectionId());
        RejectedCastBallotRefForApprovedFinalCount ref = new RejectedCastBallotRefForApprovedFinalCount(
                reportingUnitTypeId, buildCountContext(), finalCount.getAreaPath(), castBallotId, ballotRejectionId);
        downloadBinaryData(castBallotBinaryDataService.rejectedCastBallotBinaryData(userData, ref));
    }

    public void downloadMunicipalityApprovedBallotFor(RejectedBallot rejectedBallot) {
        ApprovedBallot approvedBallot = municipalityApprovedBallotFor(rejectedBallot);
        CastBallotId castBallotId = new CastBallotId(approvedBallot.getId());
        BallotId ballotId = new BallotId(approvedBallot.getBallotId());
        ApprovedCastBallotRefForApprovedFinalCount ref = new ApprovedCastBallotRefForApprovedFinalCount(
                VALGSTYRET, buildCountContext(), finalCount.getAreaPath(), castBallotId, ballotId);
        downloadBinaryData(castBallotBinaryDataService.approvedCastBallotBinaryData(userData, ref));
    }

    private void downloadBinaryData(CastBallotBinaryData castBallotBinaryData) {
        if (castBallotBinaryData != null) {
            try {
                FacesUtil.sendFile(castBallotBinaryData);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.download.noResult"), FacesMessage.SEVERITY_INFO);
        }
    }

    public String getRejectedBallotName(RejectedBallot rejectedBallot) {
        if (rejectedBallot.isRejected()) {
            return getRejectedBallotNameTranslated(rejectedBallot.getSelectedBallotRejectionId());
        }
        return messageProvider.get("@count." + rejectedBallot.getState().name().toLowerCase()) + " "
                + getPartyNameTranslated(rejectedBallot.getSelectedBallotId());
    }

    public String getMunicipalityRejectedBallotNameFor(RejectedBallot rejectedBallot) {
        return getRejectedBallotName(municipalityRejectedBallotFor(rejectedBallot));
    }

    public String getMunicipalityApprovedBallotNameFor(RejectedBallot rejectedBallot) {
        ApprovedBallot approvedBallot = municipalityApprovedBallotFor(rejectedBallot);
        String modifiedOrUnModified = approvedBallot.isModified() ? "modified" : "unmodified";
        return messageProvider.get("@count." + modifiedOrUnModified) + " " + getPartyNameTranslated(approvedBallot.getBallotId());
    }

    public boolean isEdited(RejectedBallot rejectedBallot) {
        return current != null && current.getId().equals(rejectedBallot.getId());
    }

    public void edit(RejectedBallot rejectedBallot) {
        current = rejectedBallot;
        previous = new RejectedBallot(current);
    }

    public void done() {
        if (current.getState() != previous.getState()) {
            stateChanged();
        } else {
            boolean valueChange;
            if (current.isRejected()) {
                valueChange = notEqual(current.getSelectedBallotRejectionId(), previous.getSelectedBallotRejectionId());
            } else {
                valueChange = notEqual(current.getSelectedBallotId(), previous.getSelectedBallotId());
            }

            if (valueChange) {
                valueChanged();
            }
        }
        current = null;
        previous = null;
    }

    public boolean doRenderSaveLink(RejectedBallot rejectedBallot) {
        return isEqual(current, rejectedBallot) &&
                rejectedBallot != null &&
                (rejectedBallot.isRejected() ||
                        !isBlank(rejectedBallot.getSelectedBallotId()));
    }

    @Override
    protected boolean doRegisterRejectedCounts() {
        ApprovedFinalCountRef approvedFinalCountRef = new ApprovedFinalCountRef(reportingUnitTypeIdForWrite(), getCountContext(), finalCount.getAreaPath());
        castBallotService.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);
        doInit(); // Re-initialize data.
        return true;
    }

    private ReportingUnitTypeId reportingUnitTypeIdForWrite() {
        if (userData.isElectionEventAdminUser() && reportingUnitTypeId != null) {
            return reportingUnitTypeId;
        }
        return null;
    }

    public List<SelectItem> getStateSelectItems() {
        return stateSelectItems;
    }

    public boolean isCurrent() {
        return current != null;
    }

    private String getRejectedBallotNameTranslated(String id) {
        return messageProvider.get("@ballot_rejection[" + id + "].name");
    }

    private String getPartyNameTranslated(String id) {
        return messageProvider.get("@party[" + id + "].name");
    }

    @Override
    protected boolean isScanned() {
        return true;
    }

    @Override
    protected String buildFromUrlPart() {
        return "fromApproveScannedRejectedCount=true";
    }

    @Override
    protected boolean resolveErrorStateAndMessages(FinalCount finalCount) {
        if (finalCount == null) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.noCounts"), FacesMessage.SEVERITY_INFO);
            return true;
        }
        if (finalCount.isManualCount()) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.noScannedCounts"), FacesMessage.SEVERITY_ERROR);
            return true;
        }
        if (!finalCount.isModifiedBallotsProcessed() && !finalCount.isApproved() && isReportingUnitOnContestLevel()) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.warning.regCorrectedNotDone"), FacesMessage.SEVERITY_ERROR);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEditMode() {
        return false;
    }

    public List<RejectedBallot> getRejectedBallots() {
        return rejectedBallots;
    }

    public boolean hasMunicipalityRejectedBallots() {
        return municipalityRejectedBallotMap != null;
    }

    public boolean hasMunicipalityRejectedBallotFor(RejectedBallot rejectedBallot) {
        return municipalityRejectedBallotFor(rejectedBallot) != null;
    }

    public RejectedBallot municipalityRejectedBallotFor(RejectedBallot rejectedBallot) {
        return municipalityRejectedBallotMap != null ? municipalityRejectedBallotMap.get(rejectedBallot.getId()) : null;
    }

    public boolean hasMunicipalityApprovedBallotFor(RejectedBallot rejectedBallot) {
        return municipalityApprovedBallotFor(rejectedBallot) != null;
    }

    public ApprovedBallot municipalityApprovedBallotFor(RejectedBallot rejectedBallot) {
        return municipalityApprovedBallotMap != null ? municipalityApprovedBallotMap.get(rejectedBallot.getId()) : null;
    }

    public List<SelectItem> getBallotRejections() {
        return ballotRejectionSelectItems;
    }

    public List<SelectItem> getBallots() {
        return ballotSelectItems;
    }

    private void initRejectedBallots() {
        if (finalCount != null) {
            CountContext countContext = buildCountContext();
            AreaPath countingAreaPath = finalCount.getAreaPath();
            ApprovedFinalCountRef approvedFinalCountRef = new ApprovedFinalCountRef(reportingUnitTypeIdForRead(), countContext, countingAreaPath);
            rejectedBallots = castBallotService.rejectedBallots(userData, approvedFinalCountRef);
            loadStateSelectItems();
            loadBallotSelectItems();
            loadBallotRejectionSelectItems();
            if (userData.getOperatorAreaLevel() == COUNTY || userData.isElectionEventAdminUser() && reportingUnitTypeId == FYLKESVALGSTYRET) {
                initMunicipalityRejectedBallotMap(countContext, countingAreaPath);
                initMunicipalityApprovedBallotMap(countContext, countingAreaPath);
            }
        }
    }

    private ReportingUnitTypeId reportingUnitTypeIdForRead() {
        if (userData.isElectionEventAdminUser() && reportingUnitTypeId != null) {
            return reportingUnitTypeId;
        }
        if (userData.getOperatorAreaLevel() == COUNTY) {
            return FYLKESVALGSTYRET;
        }
        return VALGSTYRET;
    }

    private void loadStateSelectItems() {
        stateSelectItems = new ArrayList<>();
        stateSelectItems.add(new SelectItem(REJECTED.name(), messageProvider.get("@count.rejected")));
        stateSelectItems.add(new SelectItem(MODIFIED.name(), messageProvider.get("@count.modified")));
        stateSelectItems.add(new SelectItem(UNMODIFIED.name(), messageProvider.get("@count.unmodified")));
    }

    private void loadBallotSelectItems() {
        ballotSelectItems = new ArrayList<>();
        for (BallotCount ballot : newBallotCounts) {
            if (!EvoteConstants.BALLOT_BLANK.equals(ballot.getId())) {
                ballotSelectItems.add(new SelectItem(ballot.getId(), getPartyNameTranslated(ballot.getId())));
            }
        }
    }

    private void loadBallotRejectionSelectItems() {
        ballotRejectionSelectItems = new ArrayList<>();
        for (RejectedBallotCount ballotRejection : finalCount.getRejectedBallotCounts()) {
            ballotRejectionSelectItems.add(new SelectItem(ballotRejection.getId(), getRejectedBallotNameTranslated(ballotRejection.getId())));
        }
    }

    private void initMunicipalityRejectedBallotMap(CountContext countContext, AreaPath countingAreaPath) {
        ApprovedFinalCountRef approvedFinalCountRef = new ApprovedFinalCountRef(VALGSTYRET, countContext, countingAreaPath);
        municipalityRejectedBallotMap = new HashMap<>();
        List<RejectedBallot> municipalityRejectedBallots = castBallotService.rejectedBallots(userData, approvedFinalCountRef);
        for (RejectedBallot municipalityRejectedBallot : municipalityRejectedBallots) {
            municipalityRejectedBallotMap.put(municipalityRejectedBallot.getId(), municipalityRejectedBallot);
        }
    }

    private void initMunicipalityApprovedBallotMap(CountContext countContext, AreaPath countingAreaPath) {
        ApprovedFinalCountRef approvedFinalCountRef = new ApprovedFinalCountRef(VALGSTYRET, countContext, countingAreaPath);
        municipalityApprovedBallotMap = new HashMap<>();
        List<ApprovedBallot> municipalityApprovedBallots = castBallotService.approvedBallots(userData, approvedFinalCountRef);
        for (ApprovedBallot municipalityApprovedBallot : municipalityApprovedBallots) {
            municipalityApprovedBallotMap.put(municipalityApprovedBallot.getId(), municipalityApprovedBallot);
        }
    }

    private void stateChanged() {
        switch (previous.getState()) {
            case REJECTED:
                moveFromRejected();
                break;
            case MODIFIED:
                moveFromModified();
                break;
            case UNMODIFIED:
                moveFromUnmodified();
                break;
            default:
                throw new EvoteException(UNSUPPORTED_STATE + previous.getState());
        }
    }

    private void moveFromRejected() {
        RejectedBallotCount fromRejected = findRejectedBallotCount(previous);
        BallotCount toBallot = findBallotCount(current);
        fromRejected.setCount(fromRejected.getCount() - 1);
        if (current.getState() == MODIFIED) {
            // Move from rejected to modified
            toBallot.setModifiedCount(toBallot.getModifiedCount() + 1);
        } else if (current.getState() == UNMODIFIED) {
            // Move from rejected to unmodified
            toBallot.setUnmodifiedCount(toBallot.getUnmodifiedCount() + 1);
        } else {
            throw new EvoteException(UNSUPPORTED_STATE + current.getState());
        }
    }

    private void moveFromModified() {
        BallotCount fromModified = findBallotCount(previous);
        fromModified.setModifiedCount(fromModified.getModifiedCount() - 1);
        if (current.getState() == UNMODIFIED) {
            // Move from modified to unmodified
            BallotCount toUnmodified = findBallotCount(current);
            toUnmodified.setUnmodifiedCount(toUnmodified.getUnmodifiedCount() + 1);
        } else if (current.getState() == REJECTED) {
            // Move modified to rejected
            RejectedBallotCount toRejected = findRejectedBallotCount(current);
            toRejected.setCount(toRejected.getCount() + 1);
        } else {
            throw new EvoteException(UNSUPPORTED_STATE + current.getState());
        }
    }

    private void moveFromUnmodified() {
        BallotCount fromUnmodified = findBallotCount(previous);
        fromUnmodified.setUnmodifiedCount(fromUnmodified.getUnmodifiedCount() - 1);
        if (current.getState() == MODIFIED) {
            // Move from unmodified to modified
            BallotCount toModified = findBallotCount(current);
            toModified.setModifiedCount(toModified.getModifiedCount() + 1);
        } else if (current.getState() == REJECTED) {
            // Move unmodified to rejected
            RejectedBallotCount toRejected = findRejectedBallotCount(current);
            toRejected.setCount(toRejected.getCount() + 1);
        } else {
            throw new EvoteException(UNSUPPORTED_STATE + current.getState());
        }
    }

    private void valueChanged() {
        if (current.isRejected()) {
            // Move count from one reject reason to the other
            RejectedBallotCount from = findRejectedBallotCount(previous);
            RejectedBallotCount to = findRejectedBallotCount(current);
            from.setCount(from.getCount() - 1);
            to.setCount(to.getCount() + 1);
        } else {
            BallotCount from = findBallotCount(previous);
            BallotCount to = findBallotCount(current);
            if (current.getState() == MODIFIED) {
                // Move modified count from one party to the other
                from.setModifiedCount(from.getModifiedCount() - 1);
                to.setModifiedCount(to.getModifiedCount() + 1);
            } else if (current.getState() == UNMODIFIED) {
                // Move unmodified count from one party to the other
                from.setUnmodifiedCount(from.getUnmodifiedCount() - 1);
                to.setUnmodifiedCount(to.getUnmodifiedCount() + 1);
            }
        }
    }

    private RejectedBallotCount findRejectedBallotCount(RejectedBallot rejectedBallot) {
        for (RejectedBallotCount count : finalCount.getRejectedBallotCounts()) {
            if (count.getId().equals(rejectedBallot.getSelectedBallotRejectionId())) {
                return count;
            }
        }
        throw new EvoteException("Could not find RejectedBallotCount with id=" + rejectedBallot.getSelectedBallotRejectionId() + " in FinalCount");
    }

    private BallotCount findBallotCount(RejectedBallot rejectedBallot) {
        for (BallotCount count : newBallotCounts) {
            if (count.getId().equals(rejectedBallot.getSelectedBallotId())) {
                return count;
            }
        }
        throw new EvoteException("Could not find BallotCount with id=" + rejectedBallot.getSelectedBallotId() + " newBallotCounts");
    }

    private boolean isEqual(Object o1, Object o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

    private boolean notEqual(String string1, String string2) {
        return (string1 == null ? string2 != null : !string1.equals(string2));
    }

    @Override
    public Button button(ButtonType type) {
        Button button = super.button(type);
        if (button.isRendered() && current != null) {
            return enabled(false);
        }
        return button;
    }
}
