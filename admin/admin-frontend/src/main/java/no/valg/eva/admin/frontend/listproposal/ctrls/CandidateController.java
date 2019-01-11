package no.valg.eva.admin.frontend.listproposal.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.evote.exception.ErrorCode;
import no.evote.model.views.CandidateAudit;
import no.evote.security.UserData;
import no.evote.service.configuration.CandidateService;
import no.valg.eva.admin.configuration.application.ResponsibilityValidationService;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Candidate.Gender;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.common.RoleConflictHandler;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.MathUtil;
import org.primefaces.event.ReorderEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.AUDIT_CANDIDATE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_DELETE_ALL_CANDIDATES;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_DELETE_CANDIDATE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.EDIT_CANDIDATE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.UPLOAD_CANDIDATES;

@Named
@ViewScoped
public class CandidateController extends BaseController implements ErrorCodeHandler, RoleConflictHandler {

    private static final int MAX_NO_OF_RESULTS_TO_DISPLAY = 10;
    private static final int ELEVEN_MEMBERS = 11;
    private static final int TWENTY_THREE_MEMBERS = 23;
    private static final int MAX_BASELINE_VOTES_FOUR = 4;
    private static final int TWENTY_FIVE_MEMBERS = 25;
    private static final int FIFTY_THREE_MEMBERS = 53;
    private static final int MAX_BASELINE_VOTES_SIX = 6;
    private static final int FIFTY_FIVE_MEMBERS = 55;
    private static final int MAX_BASELINE_VOTES_TEN = 10;
    private static final int ZERO_BASELINE_VOTES = 0;

    @Inject
    private MessageProvider messageProvider;
    @Inject
    private UserData userData;
    @Inject
    private CandidateService candidateService;
    @Inject
    private RedigerListeforslagController redigerListeforslagController;
    @Inject
    private ResponsibilityValidationService responsibilityValidationService;

    @Setter
    private List<Candidate> candidateList;
    @Getter
    @Setter
    private List<Voter> voterResult;
    @Getter
    @Setter
    private Voter selectedVoterResult;
    @Getter
    @Setter
    private Candidate candidateForEdit;
    @Getter
    private boolean searchMode;
    @Getter
    private List<ResponsibilityConflict> conflicts;

    private Long currentAffiliationPk;
    private int maximumBaselineVotes = -1;
    private int currentBaseLineCount = 0;
    private List<CandidateAudit> candidateAuditList;
    private boolean moreHitsThanDisplayed;

    @PostConstruct
    public void init() {
        candidateList = new ArrayList<>();
    }


    public void validateRoleConflicts() {
        List<ResponsibilityConflict> responsibilityConflicts = responsibilityValidationService.checkIfCandidateHasBoardMemberOrRoleConflict(userData, candidateForEdit,
                getAffiliation());
        if (!responsibilityConflicts.isEmpty() && !candidateHasFieldsMissing()) {
            conflicts = new ArrayList<>();
            conflicts.addAll(responsibilityConflicts);
            FacesUtil.updateDom("editListProposalForm:roleConflictWidget:roleConflictDialog");
            FacesUtil.executeJS("PF('roleConflictWidget').show()");
        } else {
            saveCandidate();
        }
    }

    private boolean candidateHasFieldsMissing() {
        return candidateForEdit.getFirstName().isEmpty() || candidateForEdit.getLastName().isEmpty() || candidateForEdit.getFormattedDateOfBirth().isEmpty();
    }

    public void saveCandidate() {
        execute(() -> {
            candidateForEdit.setNameLine();
            Candidate candidate;
            if (candidateForEdit.isCreated()) {
                candidate = candidateService.update(userData, candidateForEdit);
                candidateList.set(candidateForEdit.getDisplayOrder() - 1, candidate);
                MessageUtil.buildDetailMessage(messageProvider.get("@listProposal.candidate.updated", candidateForEdit.toString()), FacesMessage.SEVERITY_INFO);
            } else {
                if (!candidateForEdit.isIdSet()) {
                    candidateForEdit = candidateService.setMockIdForEmptyId(userData, candidateForEdit, getBallotPk(), null);
                }
                if (candidateForEdit.getDisplayOrder() <= currentBaseLineCount) {
                    candidateForEdit.setBaselineVotes(true);
                }
                candidate = candidateService.create(userData, candidateForEdit, getBallotPk());
                candidateList.add(candidate);
                MessageUtil.buildDetailMessage(messageProvider.get("@listProposal.candidate.created", candidateForEdit.toString()), FacesMessage.SEVERITY_INFO);
            }
            getEditCandidateDialog().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
        }, this);
        FacesUtil.updateDom("editCandidateDialog:dialogForm");
        FacesUtil.executeJS("window.scrollTo(0,0)");
    }

    public void deleteCandidate() {
        execute(() -> {
            candidateService.deleteAndReorder(userData, candidateForEdit, getBallotPk());
            updateCandidateListFromDb();
            MessageUtil.buildDetailMessage(messageProvider.get("@common.list.delete", candidateForEdit.getDisplayOrder(), candidateForEdit.toString()),
                    FacesMessage.SEVERITY_INFO);
            getConfirmDeleteCandidateDialog().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
        }, this);
    }

    public void deleteAllCandidates() {
        execute(() -> {
            candidateService.deleteAll(userData, candidateList);
            updateCandidateListFromDb();
            getConfirmDeleteAllCandidatesDialog().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
        }, this);
    }

    public void updateCandidateListFromDb() {
        candidateList = candidateService.findByAffiliation(userData, getAffiliation().getPk());

        maximumBaselineVotes = findMaximumBaselineVotes(getAffiliation().getBallot().getContest().getNumberOfPositions());
        updateCurrentBaseLineCount();
    }

    public boolean isElectoralRollSearchAvailable() {
        return redigerListeforslagController.isElectoralRollSearchAvailable();
    }

    public List<CandidateAudit> getCandidateAuditList() {
        return candidateAuditList;
    }

    public void onRowReorder(ReorderEvent event) {
        execute(() -> {
            List<Candidate> list = candidateService.changeDisplayOrder(
                    userData, candidateList.get(event.getToIndex()), event.getFromIndex() + 1, event.getToIndex() + 1);
            // Swap in sublist
            // Swap in sublist
            if (list.size() == candidateList.size()) {
                candidateList = list;
            } else {
                int start = event.getFromIndex() < event.getToIndex() ? event.getFromIndex() : event.getToIndex();
                int counter = 0;
                for (int i = start; i < start + list.size(); i++) {
                    candidateList.remove(i);
                    candidateList.add(i, list.get(counter++));
                }
            }
        }, this);
    }

    public List<Candidate> getCandidateList() {
        needsUpdating();
        return candidateList;
    }

    public void searchForCandidateInElectoralRoll(Candidate candidate) {

        candidateForEdit = candidate;
        Set<MvArea> areaRestrictions = redigerListeforslagController.getAreaRestrictionsForCandidateSearch();

        voterResult = candidateService.searchVoter(userData, candidateForEdit, getElectionId(), areaRestrictions);

        if (!voterResult.isEmpty()) {
            selectedVoterResult = voterResult.get(0);
        }


        if (voterResult.size() > MAX_NO_OF_RESULTS_TO_DISPLAY) {
            moreHitsThanDisplayed = true;
            voterResult.remove(MAX_NO_OF_RESULTS_TO_DISPLAY);
        } else {
            moreHitsThanDisplayed = false;
        }

        searchMode = true;
    }

    public void cancelSearchForCandidateInElectoralRoll() {
        searchMode = false;
    }

    /**
     * Sets information from selected voter to the candidate
     */
    public void createCandidateFromVoter() {
        if (selectedVoterResult != null) {
            candidateForEdit = candidateService.convertVoterToCandidate(userData, candidateForEdit, selectedVoterResult);
            selectedVoterResult = null;
            updateCandidateListFromDb();
            searchMode = false;
        }
    }

    public void viewCandidateAudit() {
        candidateAuditList = candidateService.getCandidateAuditByBallot(userData, getBallotPk());
        getAuditCandidateDialog().open();
    }

    public void editCandidate(Candidate candidate) {
        candidateForEdit = candidate;
        searchMode = false;
        getEditCandidateDialog().open();
    }

    public void promptDeleteCandidate(Candidate candidate) {
        candidateForEdit = candidate;
        getConfirmDeleteCandidateDialog().open();
    }

    public void showCreateCandidateDialog() {
        Affiliation affiliation = getAffiliation();
        candidateForEdit = candidateService.createNewCandidate(userData, affiliation);
        candidateForEdit.setDisplayOrder(getCandidateList().size() + 1);
        searchMode = false;
        getEditCandidateDialog().open();
    }

    public void promptDeleteAllCandidate() {
        getConfirmDeleteAllCandidatesDialog().open();
    }

    public void showUploadCandidatesDialog() {
        getUploadCandidatesDialog().open();
    }

    public Integer getMaxCandidateNameLength() {
        return getElection().getMaxCandidateNameLength();
    }

    public Integer getMaxCandidateResidenceProfessionLength() {
        return getElection().getMaxCandidateResidenceProfessionLength();
    }

    @Override
    public String onError(ErrorCode errorCode, String... params) {
        if (isOptimisticLockingException(errorCode)) {
            updateCandidateListFromDb();
            updateCurrentBaseLineCount();
            return messageProvider.get("@listProposal.save.optimisticLockingException");
        }
        return null;
    }

    private void updateCurrentBaseLineCount() {
        if (candidateList != null) {
            Iterator<Candidate> iterator = candidateList.iterator();
            currentBaseLineCount = 0;

            if (!candidateList.isEmpty()) {
                while (iterator.hasNext() && (iterator.next()).isBaselineVotes()) {
                    currentBaseLineCount++;
                }
            }
        }
    }

    /**
     * Dette bestemmes av Valgloven § 6-2. Antall kandidatnavn på listeforslaget
     * Antall kommunestyremedlemmer alltid skal være et oddetall etter kommuneloven § 7. Derfor er det "hull" i rekken.
     *
     * @param members Antall kommunestyremedlemmer som skal velges.
     * @return Maksimum antall kandidater som kan få stemmetillegg på listeforslaget.
     */
    private int findMaximumBaselineVotes(final int members) {

        if (members >= ELEVEN_MEMBERS && members <= TWENTY_THREE_MEMBERS) {
            return MAX_BASELINE_VOTES_FOUR;
        }
        if (members >= TWENTY_FIVE_MEMBERS && members <= FIFTY_THREE_MEMBERS) {
            return MAX_BASELINE_VOTES_SIX;
        }
        if (members >= FIFTY_FIVE_MEMBERS) {
            return MAX_BASELINE_VOTES_TEN;
        }

        return ZERO_BASELINE_VOTES;
    }

    /**
     * Sets candidates when new list proposal/affiliation is selected
     */
    private void needsUpdating() {
        if ((getAffiliation() != null) && !getAffiliation().getPk().equals(currentAffiliationPk)) {
            currentAffiliationPk = getAffiliation().getPk();
            updateCandidateListFromDb();
            updateCurrentBaseLineCount();
        }
    }

    public int getCurrentBaseLineCount() {
        needsUpdating();
        return currentBaseLineCount;
    }

    public void setCurrentBaseLineCount(final int affiliationCount) {
        if (currentBaseLineCount != affiliationCount && affiliationCount <= candidateList.size()) {
            execute(() -> {
                if (affiliationCount < currentBaseLineCount && affiliationCount < candidateList.size()) {
                    decrease(affiliationCount);
                } else if (affiliationCount > currentBaseLineCount && affiliationCount <= candidateList.size() && affiliationCount > 0) {
                    increase(affiliationCount);
                }
                currentBaseLineCount = affiliationCount;
            }, this);
        } else {
            currentBaseLineCount = affiliationCount;
        }
    }

    private void increase(int affiliationCount) {
        Candidate candidate;
        int indexToChange = affiliationCount - 1;
        // need to handle input from keyboard with more than 1 in increment
        while (indexToChange >= currentBaseLineCount) {
            candidate = candidateList.get(indexToChange);
            candidate.setBaselineVotes(true);
            candidateList.set(indexToChange, candidateService.update(userData, candidate));
            indexToChange--;
        }
    }

    private void decrease(int affiliationCount) {
        Candidate candidate;
        int indexToChange = affiliationCount;
        // need to handle input from keyboard with more than 1 in decrement
        while (indexToChange < currentBaseLineCount) {
            candidate = candidateList.get(indexToChange);
            candidate.setBaselineVotes(false);
            candidateList.set(indexToChange, candidateService.update(userData, candidate));
            indexToChange++;
        }
    }

    public int getMaximumBaselineVotes() {
        needsUpdating();
        return maximumBaselineVotes;
    }

    public boolean isMoreHitsThanDisplayed() {
        return moreHitsThanDisplayed;
    }

    public void setMoreHitsThanDisplayed(final boolean moreHitsThanDisplayed) {
        this.moreHitsThanDisplayed = moreHitsThanDisplayed;
    }

    public int getFemalePercentage() {
        return getGenderPercentage(Gender.FEMALE);
    }

    public int getMalePercentage() {
        return getGenderPercentage(Gender.MALE);
    }

    private int getGenderPercentage(final Gender gender) {
        needsUpdating();
        int total = 0;
        int genderSpecific = 0;
        for (Candidate c : candidateList) {

            total++;

            // Skip candidate if it isn't in the electoral roll, since we need the full ssn to determine gender
            if (!c.isIdSet()) {
                continue;
            }

            switch (gender) {
                case FEMALE:
                    if (c.isFemale()) {
                        genderSpecific++;
                    }
                    break;
                case MALE:
                    if (c.isMale()) {
                        genderSpecific++;
                    }
                    break;
                default:
                    break;
            }
        }

        if (total == 0) {
            return 0;
        }

        return MathUtil.calculatePercentage(genderSpecific, total);
    }

    public boolean isShowCandidateProfession() {
        return getAffiliation().isShowCandidateProfession();
    }

    public boolean isShowCandidateResidence() {
        return getAffiliation().isShowCandidateResidence();
    }

    public Dialog getEditCandidateDialog() {
        return EDIT_CANDIDATE;
    }

    public Dialog getConfirmDeleteCandidateDialog() {
        return CONFIRM_DELETE_CANDIDATE;
    }

    public Dialog getConfirmDeleteAllCandidatesDialog() {
        return CONFIRM_DELETE_ALL_CANDIDATES;
    }

    public Dialog getAuditCandidateDialog() {
        return AUDIT_CANDIDATE;
    }

    public Dialog getUploadCandidatesDialog() {
        return UPLOAD_CANDIDATES;
    }

    private Affiliation getAffiliation() {
        return redigerListeforslagController.getAffiliation();
    }

    private Long getBallotPk() {
        return getAffiliation().getBallot().getPk();
    }

    private Election getElection() {
        return redigerListeforslagController.getContest().getElection();
    }

    private String getElectionId() {
        return getElection().getElectionGroup().getElectionEvent().getId();
    }

    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public List<ResponsibilityConflict> getResponsibilityConflicts() {
        return conflicts;
    }

    @Override
    public void onAcceptRoleConflict() {
        saveCandidate();
    }

    @Override
    public String getLocalizedRoleConflictMessage() {
        return messageProvider.get("@listProposal.role.conflict.message", candidateForEdit);
    }

    @Override
    public String getLocalizedRoleConflictExplanation() {
        return messageProvider.get("@listProposal.role.conflict.explanation");
    }
}
