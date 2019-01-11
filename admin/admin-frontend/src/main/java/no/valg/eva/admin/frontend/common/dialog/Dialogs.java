package no.valg.eva.admin.frontend.common.dialog;

import static no.valg.eva.admin.frontend.common.dialog.Dialog.CLOSEABLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.DEFAULT_HEIGHT;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.DEFAULT_ON_HIDE;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.DEFAULT_ON_SHOW;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.DEFAULT_WIDTH;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.NOT_CLOSEABLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.NO_TITLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialog.STANDARD_WIDTH;

public final class Dialogs {

    public static final Dialog UPLOAD_BATCHES = countingDialog("uploadBatchesDialog", "@batch.title", CLOSEABLE, DEFAULT_ON_HIDE, STANDARD_WIDTH);

    public static final Dialog CREATE_MODIFIED_BALLOT_BATCH = countingDialog("createModifiedBallotBatchDialog", NO_TITLE, CLOSEABLE,
            "PF('poller').start();", DEFAULT_WIDTH);

    public static final Dialog CONFIRM_APPROVE_FINAL_COUNT = countingApproveDialog("confirmApproveFinalCountDialog");

    public static final Dialog CONFIRM_APPROVE_PRELIMINARY_COUNT = countingApproveDialog("confirmApprovePreliminaryCountDialog");

    public static final Dialog CONFIRM_APPROVE_PROTOCOL_AND_PRELIMINARY_COUNT = countingApproveDialog("confirmApproveProtocolAndPreliminaryCountDialog");

    public static final Dialog CONFIRM_APPROVE_PROTOCOL_COUNT = countingApproveDialog("confirmApproveProtocolCountDialog");

    public static final Dialog EDIT_CURRENT_USER = new Dialog("/secure/includes/editCurrentUserDialog", "@help.operator.edit.own.contact.info.header");

    public static final Dialog AUDIT_CANDIDATE = listProposalDialog("auditCandidateDialog", "@listProposal.edit.history.header", STANDARD_WIDTH);

    public static final Dialog SHOW_CANDIDATE_PROFESSION_RESIDENCE = listProposalDialog("showCandidateProfessionResidenceDialog",
            "@listProposal.showCandidate.columns", STANDARD_WIDTH);

    public static final Dialog CONFIRM_DELETE_ALL_CANDIDATES = listProposalDialog("confirmDeleteAllCandidatesDialog",
            "@listProposal.candidate.deleteAll.header", STANDARD_WIDTH);

    public static final Dialog CONFIRM_APPROVE_LIST_PROPOSAL = listProposalDialog("confirmApproveListProposal", "@listProposal.missing.signatures",
            STANDARD_WIDTH);

    public static final Dialog CONFIRM_DELETE_CANDIDATE = listProposalDialog("confirmDeleteCandidateDialog", "@listProposal.candidate.delete.header",
            STANDARD_WIDTH);

    public static final Dialog CONFIRM_DELETE_PROPOSER = listProposalDialog("confirmDeleteProposer", "@listProposal.proposer.delete.header", STANDARD_WIDTH);

    public static final Dialog EDIT_CANDIDATE = listProposalDialog("editCandidateDialog", "@common.candidate", STANDARD_WIDTH);

    public static final Dialog EDIT_PROPOSER = listProposalDialog("editProposerDialog", "@listProposal.edit.proposer.edit", DEFAULT_WIDTH);

    public static final Dialog UPLOAD_CANDIDATES = listProposalDialog("uploadCandidatesDialog", "@listProposal.edit.upload", STANDARD_WIDTH);

    public static final Dialog CONFIRM_LATE_VALIDATION_SEARCH = new Dialog("/secure/stemmegivning/dialogs/confirmLateValidationSearchDialog",
            "@voting.searchAdvanceLateValidation.dialog.title");

    public static final Dialog ADVANCE_VOTING_LATE_ARRIVAL_CONFIRM_SEARCH_DIALOG = new Dialog("/secure/voting/dialogs/confirmLateValidationSearchDialog",
            "@voting.searchAdvanceLateValidation.dialog.title");

    public static final Dialog REPORT_PARAMETER = new Dialog("/secure/reporting/reportParameterDialog", NO_TITLE);

    public static final Dialog CREATE_ELECTION_EVENT = electionDialog("createElectionEventDialog",
            "@election.election_event.create.header", STANDARD_WIDTH, "EVA.Application.getInstance().getView().createElectionEventDialog();");

    public static final Dialog ADD_ELECTION_DAY = electionDialog("addElectionDayDialog",
            "@election.election_event.election_day_add", "350", "");

    public static final Dialog CONFIRM_DELETE_ALL_PARENT_POLLING_DISTRICTS = new Dialog(
            "/secure/config/local/dialogs/confirmDeleteAllParentPollingDistrictsDialog",
            "@config.local.parent_polling_district.confirmDeleteAll");

    public static final Dialog CONFIRM_ELECTION_CARD_INFO_TEXT_OVERWRITE = configLocalDialog(
            "confirmElectionCardInfoTextOverwrite",
            "@config.local.election_card.dialog.confirmElectionCardInfoTextOverwrite.title",
            "900");

    public static final Dialog CONFIRM_ELECTION_DAY_OPENING_HOURS_OVERWRITE = configLocalDialog(
            "confirmElectionDayOpeningHoursOverwrite",
            "@config.local.election_day.overwriteDialogTitle",
            "900");

    public static final Dialog ELECTION_CARD_EDIT_ADDRESS = configLocalDialog(
            "electionCardEditAddressDialog",
            "@config.local.election_card.dialog.editAddress.title",
            "500");

    public static final Dialog CREATE_ROLE = rbacDialog("createRoleDialog", "createOrEditRoleDialog", "@rbac.role.createRole", STANDARD_WIDTH, "500");

    public static final Dialog EDIT_ROLE = rbacDialog("editRoleDialog", "createOrEditRoleDialog", "@common.redact", STANDARD_WIDTH, "500");

    public static final Dialog CONFIRM_REMOVE_INCLUDED_ROLE = rbacDialog("confirmRemoveIncludedRoleDialog", "@rbac.role.removeIncludedRole", "400");

    public static final Dialog CONFIRM_DELETE_ROLE = rbacDialog("confirmDeleteRoleDialog", "@rbac.role.deleteRole", "400");

    private Dialogs() {
    }

    private static Dialog countingApproveDialog(String templateId) {
        return countingDialog("approveDialog", templateId, "@count.dialog.header.confirmation.approve", NOT_CLOSEABLE,
                "EVA.Application.getInstance().getView().onConfirmApproveDialogShow();", DEFAULT_ON_HIDE, DEFAULT_WIDTH);
    }

    private static Dialog countingDialog(String templateId, String title, boolean closeable, String onHide, String width) {
        return countingDialog(null, templateId, title, closeable, DEFAULT_ON_SHOW, onHide, width);
    }

    private static Dialog countingDialog(String id, String templateId, String title, boolean closeable, String onShow, String onHide, String width) {
        Dialog dialog = new Dialog(id, "/secure/counting/dialogs/" + templateId, title);
        dialog.setCloseable(closeable);
        dialog.setOnShow(onShow);
        dialog.setOnHide(onHide);
        dialog.setWidth(width);
        return dialog;
    }

    private static Dialog rbacDialog(String templateId, String title, String width) {
        return rbacDialog(templateId, templateId, title, width, DEFAULT_HEIGHT);
    }

    private static Dialog rbacDialog(String id, String templateId, String title, String width, String height) {
        Dialog dialog = new Dialog(id, "/secure/rbac/dialogs/" + templateId, title);
        dialog.setWidth(width);
        dialog.setHeight(height);
        return dialog;
    }

    private static Dialog listProposalDialog(String templateId, String title, String width) {
        Dialog dialog = new Dialog("/secure/listProposal/dialogs/" + templateId, title);
        dialog.setWidth(width);
        return dialog;
    }

    private static Dialog configLocalDialog(String templateId, String title, String width) {
        Dialog dialog = new Dialog("/secure/config/local/dialogs/" + templateId, title);
        dialog.setWidth(width);
        return dialog;
    }

    private static Dialog electionDialog(String templateId, String title, String width, String onShow) {
        Dialog dialog = new Dialog("/secure/election/dialogs/" + templateId, title);
        dialog.setWidth(width);
        dialog.setOnShow(onShow);
        return dialog;
    }

}
