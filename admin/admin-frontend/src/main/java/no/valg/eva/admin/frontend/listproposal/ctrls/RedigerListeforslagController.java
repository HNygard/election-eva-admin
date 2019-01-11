package no.valg.eva.admin.frontend.listproposal.ctrls;

import static java.util.stream.Collectors.joining;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_APPROVE_LIST_PROPOSAL;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.SHOW_CANDIDATE_PROFESSION_RESIDENCE;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.evote.dto.ListProposalValidationData;
import no.evote.exception.ErrorCode;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.BallotService;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.configuration.LegacyListProposalService;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.TranslationProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;

@Named
@ViewScoped
public class RedigerListeforslagController extends ListeforslagBaseController implements ErrorCodeHandler {

	private static final int MAXIMUM_FEEDBACK_MESSAGES = 100;
	private final Set<MvArea> currentMvAreas = new HashSet<>();
	@Inject
	private ContestAreaService contestAreaService;
	@Inject
	private CandidateController candidateController;
	@Inject
	private ProposerController proposerController;
	@Inject
	private BallotService ballotService;
	@Inject
	private LegacyListProposalService listProposalService;
	@Inject
	private TranslationProvider translationProvider;
	private Affiliation affiliation;

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		RedirectInfo redirectInfo = getAndRemoveRedirectInfo();
		if (redirectInfo == null) {
			redirectToChoose();
			return;
		}

		setAffiliation((Affiliation) redirectInfo.getData());
		updateMvArea();
	}

	/**
	 * Deletes listProposal, that is Ballot, Affiliation and Proposer, if it has ballot status 'under construction' and no candidates.
	 *
	 * @return startpage for creating list proposal if deleted, else current page
	 */
	public void deleteListProposal() {
		Affiliation currentAffiliation = getAffiliation();
		if (isListProposalUnderConstruction(currentAffiliation)) {
			updateProposalPersons();
			if (candidateController.getCandidateList().isEmpty()) {
				ballotService.delete(getUserData(), currentAffiliation.getBallot());
				redirectToChoose();
				return;
			}
		}

		MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.canNotDeleteListProposal"), FacesMessage.SEVERITY_INFO);
	}

	@SuppressWarnings("unused")
	public void setStatusApprovedFromDialog() {
		if (updateAffiliationAndBallot(BallotStatus.BallotStatusValue.APPROVED)) {
			getConfirmApproveListProposal().closeAndUpdate("electionMeta", "editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
		}
	}

	public void updateProposalPersons() {
		candidateController.updateCandidateListFromDb();
		proposerController.updateProposerListFromDb();
	}

	private boolean isListProposalUnderConstruction(Affiliation currentAffiliation) {
		return currentAffiliation.getBallot().getBallotStatus().getBallotStatusValue().getId() == EvoteConstants.BALLOT_STATUS_UNDERCONSTRUCTION;
	}

	public boolean isElectoralRollSearchAvailable() {
		return isBallotStatus(BallotStatus.BallotStatusValue.PENDING.name());
	}

	public void revertStatusPending() {

		updateAffiliationAndBallot(BallotStatus.BallotStatusValue.PENDING);
	}

	public void setStatusPending() {
		updateAffiliationAndBallot(BallotStatus.BallotStatusValue.PENDING);
	}

	public String getMinimumProposerText() {
		return getContest().minNumberOfProposersFor(getAffiliation().getParty()).toString();
	}

	public void validateAndCheckAgainstElectoralRoll() {
		List<Candidate> candidateList = candidateController.getCandidateList();
		List<Proposer> proposerList = proposerController.getProposerList();
		execute(() -> {
			ListProposalValidationData validationData = listProposalService.validateListProposalAndCheckAgainstRoll(getUserData(), getAffiliation(),
					candidateList, proposerList, getUserDataController().getElectionEvent().getId(),
					getAreaRestrictionsForCandidateSearch(), getCurrentMvAreas());
			if (validationData.isApproved()) {
				MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.approve.succesess"), FacesMessage.SEVERITY_INFO);
			} else {
				showValidationMessage(validationData);
			}
			updateProposalPersons(validationData);
		}, this);
	}

	private boolean updateAffiliationAndBallot(final BallotStatus.BallotStatusValue ballotStatus) {
		return execute(() -> {
			Affiliation theAffiliation = getAffiliation();
			ballotService.updateBallotStatus(getUserData(), theAffiliation, ballotService.findBallotStatusById(getUserData(), ballotStatus.getId()));
			updateAffiliation();
			MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.status.new.updated"), FacesMessage.SEVERITY_INFO);
		}, this);
	}

	private void updateProposalPersons(final ListProposalValidationData validationData) {
		candidateController.setCandidateList(validationData.getCandidateList());
		proposerController.setProposerList(validationData.getProposerList());
	}

	private void showValidationMessage(final ListProposalValidationData validatedProposal) {
		int i = 0;
		String format = "%s %d# %s";

		for (Candidate candidate : validatedProposal.getCandidateList()) {
			if (candidate.isInvalid() && i < MAXIMUM_FEEDBACK_MESSAGES) {

				MessageUtil.buildDetailMessage(
						String.format(format, getMessageProvider().get("@common.candidate"), candidate.getDisplayOrder(),
								translate(candidate.getValidationMessageList())),
						FacesMessage.SEVERITY_ERROR);
				i++;
			}
		}

		for (Proposer proposer : validatedProposal.getProposerList()) {
			if (proposer.isInvalid() && i < MAXIMUM_FEEDBACK_MESSAGES) {
				MessageUtil.buildDetailMessage(
						String.format(format, getMessageProvider().get("@proposer"), proposer.getDisplayOrder(),
								translate(proposer.getValidationMessageList())),
						FacesMessage.SEVERITY_ERROR);
				i++;
			}
		}
		if (i > MAXIMUM_FEEDBACK_MESSAGES) {
			MessageUtil.buildDetailMessage("..." + getMessageProvider().get("@common.total") + ": " + i, FacesMessage.SEVERITY_ERROR);
		}
		for (String[] validateMsg : validatedProposal.getAffiliation().getValidationMessageList()) {
			MessageUtil.buildDetailMessage(validateMsg[0], validateMsg.length > 1 ? Arrays.copyOfRange(validateMsg, 1, validateMsg.length) : null,
					FacesMessage.SEVERITY_ERROR);
		}

	}

	/**
	 * If the election.isCandidatesInContestArea is set the electoral roll search should be restricted to the MvArea of the list proposal. If not there should
	 * be no restriction.
	 *
	 * @return The MvArea the search should be restricted to. Empty set if there is no restriction.
	 */
	Set<MvArea> getAreaRestrictionsForCandidateSearch() {
		if (getContest().getElection().isCandidatesInContestArea()) {
			return currentMvAreas;
		} else {
			return new HashSet<>();
		}
	}

	public Set<MvArea> getCurrentMvAreas() {
		return currentMvAreas;
	}

	public boolean isBallotStatus(String ballotStatus) {
		return affiliation != null
				&& affiliation.getBallot().getBallotStatus().getBallotStatusValue().toString().equals(ballotStatus);
	}

	public boolean isListLocked() {
		return isContestLocked() || affiliation == null || affiliation.getBallot().getBallotStatus().isListLocked();
	}

	public void setStatusWithdraw() {
		updateAffiliationAndBallot(BallotStatus.BallotStatusValue.WITHDRAWN);
	}

	/**
	 * Saves columns(residence/profession) shown on ballot.
	 */
	public void saveColumn() {
		affiliation = getAffiliationService().saveColumns(getUserData(), getAffiliation());
		getShowCandidateProfessionResidenceDialog().closeAndUpdate("editListProposalForm:msg",
				"editListProposalForm:tabs:candidatesDataTable");

	}

	public void setStatusReject() {
		updateAffiliationAndBallot(BallotStatus.BallotStatusValue.REJECTED);
	}

	public void setStatusApproved() {
		if (!getAffiliation().getParty().isApproved()) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.status.partyNotApproved"), FacesMessage.SEVERITY_ERROR);
			FacesUtil.updateDom("editListProposalForm:msg");
			return;
		}
		ListProposalValidationData validationData = getListProposalValidationData();
		if (validationData.isApproved()) {
			updateAffiliationAndBallot(BallotStatus.BallotStatusValue.APPROVED);
			FacesUtil.updateDom(Arrays.asList("electionMeta", "editListProposalForm"));
		} else if (validationData.isPartyReadyForApproval()) {
			getConfirmApproveListProposal().open();
		} else {
			createValidationMessages(validationData);
			FacesUtil.updateDom("editListProposalForm:msg");
		}
	}

	private ListProposalValidationData getListProposalValidationData() {
		final List<Proposer> proposerList = proposerController.getProposerList();
		ListProposalValidationData validationData = new ListProposalValidationData(candidateController.getCandidateList(), proposerList,
				getAffiliation());
		return listProposalService.validateCandidatesAndNumberOfCandidatesAndProposers(getUserData(), validationData);
	}

	private void createValidationMessages(ListProposalValidationData validationData) {
		if (!validationData.isApproved()) {
			int i = 0;
			String format = "%s %d# %s";
			for (Candidate candidate : validationData.getCandidateList()) {
				if (candidate.isInvalid() && i < MAXIMUM_FEEDBACK_MESSAGES) {
					MessageUtil.buildDetailMessage(
							String.format(format, getMessageProvider().get("@common.candidate"), candidate.getDisplayOrder(),
									translate(candidate.getValidationMessageList())),
							FacesMessage.SEVERITY_ERROR);
					i++;
				}
			}
			for (String[] validateMsg : validationData.getAffiliation().getValidationMessageList()) {
				MessageUtil.buildDetailMessage(validateMsg[0], validateMsg.length > 1 ? Arrays.copyOfRange(validateMsg, 1, validateMsg.length) : null,
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	private String translate(List<UserMessage> messages) {
		return messages.stream().map(message -> translationProvider.get(message.getMessage(), message.getArgs())).collect(joining(", "));
	}

	public String swapURL(String url) {
		return url.replace("editListProposal", "chooseEditListProposal");
	}

	public Dialog getConfirmApproveListProposal() {
		return CONFIRM_APPROVE_LIST_PROPOSAL;
	}

	public Dialog getShowCandidateProfessionResidenceDialog() {
		return SHOW_CANDIDATE_PROFESSION_RESIDENCE;
	}

	public void viewCandidateColumn() {
		getShowCandidateProfessionResidenceDialog().open();
	}

	private void redirectToChoose() {
		try {
			String redirectTil = getDenneSideURL().replace("editListProposal.xhtml", "chooseEditListProposal.xhtml");
			getFacesContext().getExternalContext().redirect(redirectTil);
		} catch (IOException e) {
			throw new RuntimeException("Failed to redirect " + e, e);
		}
	}

	private void updateMvArea() {
		currentMvAreas.clear();
		currentMvAreas.addAll(getMvAreasFromContest());
	}

	private Set<MvArea> getMvAreasFromContest() {
		List<ContestArea> contestAreaList = contestAreaService.findContestAreasForContest(getContest().getPk());
		Set<MvArea> mvareas = new HashSet<>();
		for (ContestArea contestArea : contestAreaList) {
			mvareas.add(contestArea.getMvArea());
		}

		if (mvareas.isEmpty()) {
			mvareas.add(null);
			return mvareas;
		}
		return mvareas;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	@Override
	public String onError(ErrorCode errorCode, String... params) {
		if (isOptimisticLockingException(errorCode)) {
			updateAffiliation();
			updateProposalPersons();
			return getMessageProvider().get("@listProposal.save.optimisticLockingException");
		}
		return null;

	}

	private void updateAffiliation() {
		affiliation = getAffiliationService().findByPk(getUserData(), getAffiliation().getPk());
	}
}
