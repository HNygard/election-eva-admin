package no.valg.eva.admin.frontend.listproposal.ctrls;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_DELETE_PROPOSER;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.EDIT_PROPOSER;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.ErrorCode;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.ProposerService;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.primefaces.event.ReorderEvent;

@Named
@ViewScoped
public class ProposerController extends BaseController implements ErrorCodeHandler {

	@Inject
	private UserData userData;
	@Inject
	private MessageProvider mms;
	@Inject
	private ProposerService proposerService;
	@Inject
	private RedigerListeforslagController redigerListeforslagController;

	private List<Proposer> proposerList;
	private List<Voter> voterResult;
	private Voter selectedVoterResult;
	private Proposer proposerForEdit;
	private Long currentAffiliationPk;
	private boolean moreHitsThanDisplayed;
	private List<ProposerRole> proposerRoleList;
	private boolean searchMode;

	@PostConstruct
	public void init() {
		proposerList = new ArrayList<>();
		proposerRoleList = proposerService.findSelectiveProposerRoles(userData);
	}

	public void saveProposer() {
		if (!isProposerValid(proposerForEdit, getBallotPk())) {
			return;
		}
		execute(() -> {
			if (proposerForEdit.isCreated()) {
				proposerList.set(proposerForEdit.getDisplayOrder() - 1, proposerService.update(userData, proposerForEdit));
				MessageUtil.buildDetailMessage(mms.get("@listProposal.candidate.updated", proposerForEdit.toString()), FacesMessage.SEVERITY_INFO);
			} else {
				if (!proposerForEdit.isIdSet()) {
					proposerForEdit = proposerService.setMockIdForEmptyId(userData, proposerForEdit, getBallotPk(), null);
				}
				proposerList.add(proposerService.create(userData, proposerForEdit));
				MessageUtil.buildDetailMessage(mms.get("@listProposal.candidate.created", proposerForEdit.toString()), FacesMessage.SEVERITY_INFO);
			}
			getEditProposerDialog().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:proposerDataTable");
		}, this);
	}

	public void deleteProposer() {
		execute(() -> {
			proposerService.deleteAndReorder(userData, proposerForEdit, getBallotPk());
			updateProposerListFromDb();
			MessageUtil.buildDetailMessage(mms.get("@common.list.delete", proposerForEdit.getDisplayOrder(), proposerForEdit.toString()),
					FacesMessage.SEVERITY_INFO);
			getConfirmDeleteProposer().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:proposerDataTable");
		}, this);
	}

	void updateProposerListFromDb() {
		proposerList = proposerService.findByBallot(userData, getBallotPk());
	}

	/**
	 * Sets proposers when new list proposal/affiliation is selected
	 */
	public List<Proposer> getProposerList() {
		if ((getAffiliation() != null) && !getAffiliation().getPk().equals(currentAffiliationPk)) {
			currentAffiliationPk = getAffiliation().getPk();
			updateProposerListFromDb();
			createSignaturesForProposer();
		}
		return proposerList;
	}

	public void setProposerList(final List<Proposer> proposerListModel) {
		proposerList = proposerListModel;
	}

	/* Dialog */

	public void onRowReorder(ReorderEvent event) {
		execute(() -> {
			List<Proposer> list = proposerService.changeDisplayOrder(userData, proposerList.get(event.getToIndex()), event.getFromIndex() + 1,
					event.getToIndex() + 1);
			// Swap in sublist
			if (list.size() == proposerList.size()) {
				proposerList = list;
			} else {
				int start = event.getFromIndex() < event.getToIndex() ? event.getFromIndex() : event.getToIndex();
				int counter = 0;
				for (int i = start; i < start + list.size(); i++) {
					proposerList.remove(i);
					proposerList.add(i, list.get(counter++));
				}
			}
		}, this);
	}

	public void editProposer(final Proposer proposer) {
		proposerForEdit = proposer;
		searchMode = false;
		getEditProposerDialog().open();
	}

	public void promptDeleteProposer(final Proposer proposer) {
		proposerForEdit = proposer;
		getConfirmDeleteProposer().open();
	}

	public void createNewProposer() {
		proposerForEdit = proposerService.createNewProposer(userData, getAffiliation().getBallot());
		proposerForEdit.setDisplayOrder(proposerList.size() + 1);
		getEditProposerDialog().open();
	}

	/**
	 * Sets information from selected voter to the proposer
	 */
	public void createProposerFromVoter() {
		if (selectedVoterResult != null) {
			proposerForEdit = proposerService.convertVoterToProposer(userData, proposerForEdit, selectedVoterResult);
			selectedVoterResult = null;
			searchMode = false;
		}
	}

	public void searchForProposerInElectoralRoll(Proposer proposer) {
		proposerForEdit = proposer;
		Set<MvArea> areaRestrictions = redigerListeforslagController.getCurrentMvAreas();
		voterResult = proposerService.searchVoter(userData, proposerForEdit, getElectionId(), areaRestrictions);

		if (!voterResult.isEmpty()) {
			selectedVoterResult = voterResult.get(0);
		}

		
		if (voterResult.size() > 10) {
			moreHitsThanDisplayed = true;
			voterResult.remove(10);
		} else {
			moreHitsThanDisplayed = false;
		}
		

		searchMode = true;
	}

	public void cancelSearchForProposerInElectoralRoll() {
		searchMode = false;
	}

	private boolean isProposerValid(final Proposer proposer, final Long ballotPk) {
		Proposer validatedProposer = proposerService.validate(userData, proposer, ballotPk);
		boolean valid = !validatedProposer.isInvalid();
		if (!valid) {
			MessageUtil.buildDetailMessageFromValidationResults(validatedProposer.getValidationMessageList());
		}
		return valid;
	}

	/**
	 * All list proposals have minimum two proposers ('Tillitsvalgt' and 'Varatillitsvalgt').
	 */
	private void createSignaturesForProposer() {
		if (proposerList.isEmpty()) {
			Ballot ballot = getAffiliation().getBallot();
			proposerService.createDefaultProposers(userData, ballot);

			proposerList.add(proposerService.findByBallotAndOrder(userData, ballot.getPk(), 1));
			proposerList.add(proposerService.findByBallotAndOrder(userData, ballot.getPk(), 2));
		}
	}

	@Override
	public String onError(ErrorCode errorCode, String... params) {
		if (isOptimisticLockingException(errorCode)) {
			updateProposerListFromDb();
			return mms.get("@listProposal.save.optimisticLockingException");
		}
		return null;

	}

	public Voter getSelectedVoterResult() {
		return selectedVoterResult;
	}

	public void setSelectedVoterResult(final Voter selectedVoterResult) {
		this.selectedVoterResult = selectedVoterResult;
	}

	public Proposer getProposerForEdit() {
		return proposerForEdit;
	}

	public void setProposerForEdit(final Proposer selectedProposerForEdit) {
		proposerForEdit = selectedProposerForEdit;
	}

	public boolean isMoreHitsThanDisplayed() {
		return moreHitsThanDisplayed;
	}

	public void setMoreHitsThanDisplayed(final boolean moreHitsThanDisplayed) {
		this.moreHitsThanDisplayed = moreHitsThanDisplayed;
	}

	public String getEditProposerLabel(int rowIndex, Proposer proposer) {

		String editProposerEditLabel = this.mms.get("@common.redact");

		if (proposer != null) {

			String editProposerChooseLabel = this.mms.get("@common.choose");
			ProposerRole proposerRole = proposer.getProposerRole();

			if (proposerRole != null && rowIndex <= 1 && proposer.getLastName().length() == 0) {
				return editProposerChooseLabel + " " + this.mms.get(proposer.getProposerRole().getName()).toLowerCase();
			}
		}

		return editProposerEditLabel;
	}

	public List<Voter> getVoterResult() {
		return voterResult;
	}

	public void setVoterResult(final List<Voter> voterResult) {
		this.voterResult = voterResult;
	}

	public boolean isElectoralRollSearchAvailable() {
		return redigerListeforslagController.isElectoralRollSearchAvailable();
	}

	public List<ProposerRole> getProposerRoleList() {
		return proposerRoleList;
	}

	public boolean isSearchMode() {
		return searchMode;
	}

	private Affiliation getAffiliation() {
		return redigerListeforslagController.getAffiliation();
	}

	private Long getBallotPk() {
		return getAffiliation().getBallot().getPk();
	}

	public Election getElection() {
		return redigerListeforslagController.getContest().getElection();
	}

	public String getElectionId() {
		return getElection().getElectionGroup().getElectionEvent().getId();
	}

	public Dialog getEditProposerDialog() {
		return EDIT_PROPOSER;
	}

	public Dialog getConfirmDeleteProposer() {
		return CONFIRM_DELETE_PROPOSER;
	}
}
