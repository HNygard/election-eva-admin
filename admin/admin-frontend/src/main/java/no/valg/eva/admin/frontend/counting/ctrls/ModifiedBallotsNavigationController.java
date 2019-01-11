package no.valg.eva.admin.frontend.counting.ctrls;

import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.lang.StrictMath.max;
import static no.valg.eva.admin.frontend.counting.ctrls.CreateModifiedBallotBatchController.BALLOT_COUNT_REF;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.model.BinaryData;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.BinaryDataService;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallots;
import no.valg.eva.admin.common.counting.service.ModifiedBallotService;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.counting.view.PersonVotes;
import no.valg.eva.admin.frontend.counting.view.WriteInAutoComplete;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class ModifiedBallotsNavigationController extends ConversationScopedController {
	public static final String MODIFIED_BALLOT_STATUS_PATH = "./modifiedBallotsStatus.xhtml";
	private static final Logger LOGGER = Logger.getLogger(ModifiedBallotsNavigationController.class);

	@Inject
	protected ModifiedBallotsStatusController modifiedBallotsStatusController;
	@Inject
	protected MessageProvider messageProvider;
	@Inject
	protected ModifiedBallotService modifiedBallotService;
	@Inject
	protected UserData userData;
	@Inject
	protected WriteInAutoComplete writeInAutoComplete;
	@Inject
	private BinaryDataService binaryDataService;

	protected ModifiedBallot currentModifiedBallot;
	protected ModifiedBallots modifiedBallots;
	private PersonVotes personVotes;
	private int currentModifiedBallotNumber;

	@Override
	protected void doInit() {
	}

	public void onPageUpdate(ComponentSystemEvent event) {
		if (!getFacesContext().isPostback()) {
			loadBallots();
			loadInitialModifiedBallot();
			updateCurrentPersonalVotes();
			updateCurrentWriteIns();
		}
	}

	private void loadInitialModifiedBallot() {
		currentModifiedBallotNumber = 0;
		String ballotNumber = getRequestParameter("ballotNumber");
		if (isNotBlank(ballotNumber)) {
			try {
				currentModifiedBallotNumber = max(min(parseInt(ballotNumber) - 1, modifiedBallots.getModifiedBallots().size() - 1), 0);
			} catch (NumberFormatException e) {
				// ignore parameter if unparseable
			}
		}
		do {
			currentModifiedBallot = load(modifiedBallots.getModifiedBallots().get(currentModifiedBallotNumber));
			if (!currentModifiedBallot.isDone()) {
				currentModifiedBallotNumber--;
			}
		} while (currentModifiedBallotNumber > 0 && !currentModifiedBallot.isDone());
		currentModifiedBallotNumber = max(0, currentModifiedBallotNumber);
	}

	protected void loadBallots() {
		modifiedBallots = modifiedBallotService.modifiedBallotsFor(userData, readBallotCountRefFromRequest(), modifiedBallotsStatusController.getProcess());
	}

	private BallotCountRef readBallotCountRefFromRequest() {
		return new BallotCountRef(getRequestParameter(BALLOT_COUNT_REF));
	}

	public void gotoNextBallot() {
		if (currentModifiedBallotNumber < modifiedBallots.getModifiedBallots().size() - 1) {
			currentModifiedBallot = load(modifiedBallots.getModifiedBallots().get(++currentModifiedBallotNumber));
			modifiedBallotChanged();
		}
	}

	public void gotoPreviousBallot() {
		if (currentModifiedBallotNumber > 0) {
			currentModifiedBallot = load(modifiedBallots.getModifiedBallots().get(--currentModifiedBallotNumber));
			modifiedBallotChanged();
		}
	}

	protected void modifiedBallotChanged() {
		updateCurrentPersonalVotes();
		updateCurrentWriteIns();
	}

	protected void updateCurrentPersonalVotes() {
		personVotes = new PersonVotes(modifiedBallots.getBallot().personalVoteCandidates(), currentModifiedBallot.getPersonVotes());
	}

	protected ModifiedBallot load(ModifiedBallot modifiedBallot) {
		return modifiedBallotService.load(userData, modifiedBallot);
	}

	protected void updateCurrentWriteIns() {
		int maxWriteIns = modifiedBallots.getBallot().getMaxWriteIns();
		writeInAutoComplete.fillWriteInAutoComplete(maxWriteIns, modifiedBallots.getWriteInCandidates(), currentModifiedBallot.getWriteIns(), messageProvider);
	}

	public ModifiedBallot getCurrentModifiedBallot() {
		return currentModifiedBallot;
	}

	public void setCurrentModifiedBallot(ModifiedBallot currentModifiedBallot) {
		this.currentModifiedBallot = currentModifiedBallot;
	}

	public int getCurrentModifiedBallotNumber() {
		return currentModifiedBallotNumber;
	}

	public int maxIndexForNoOfModifiedBallots() {
		return modifiedBallots.getModifiedBallots().size() - 1;
	}

	public PersonVotes getPersonVotes() {
		return personVotes;
	}

	public ModifiedBallots getModifiedBallots() {
		return modifiedBallots;
	}

	public WriteInAutoComplete getWriteInAutoComplete() {
		return writeInAutoComplete;
	}

	public void finished() {
		FacesUtil.redirect(MODIFIED_BALLOT_STATUS_PATH + "?cid=" + getCid(), false);
	}

	public boolean isNextButtonDisabled() {
		return modifiedBallots != null
				&& (modifiedBallots.getModifiedBallots() == null
				|| currentModifiedBallotNumber > modifiedBallots.getModifiedBallots().size() - 2);
	}

	public boolean isPreviousButtonDisabled() {
		return currentModifiedBallotNumber == 0;
	}

	public boolean isFinishedButtonDisabled() {
		return modifiedBallots != null
				&& (modifiedBallots.getModifiedBallots() == null
				|| (!modifiedBallots.isDone() && currentModifiedBallotNumber < modifiedBallots.getModifiedBallots().size() - 1));
	}

	public void downloadBallot(ModifiedBallot modifiedBallot) {
		long binaryDataPk = modifiedBallot.getBinaryDataPk();
		BinaryData binaryData = binaryDataService.findByPk(userData, binaryDataPk);
		downloadBinaryData(binaryData);
	}

	private void downloadBinaryData(BinaryData binaryData) {
		if (binaryData != null) {
			try {
				FacesUtil.sendFile(binaryData);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.download.noResult"), FacesMessage.SEVERITY_INFO);
		}
	}

	public boolean usePersonalVotes() {
		return modifiedBallots.getBallot().usePersonalVotes();
	}

	public boolean useWriteIns() {
		return modifiedBallots != null && modifiedBallots.getBallot().useWriteIns();
	}

	public boolean useRenumbering() {
		return modifiedBallots.getBallot().useRenumbering();
	}

	public boolean useStrikeOuts() {
		return modifiedBallots.getBallot().useStrikeOuts();
	}

	public boolean isRenumberLimitExceeded(Candidate candidate) {
		Integer maxRenumber = modifiedBallots.getBallot().getMaxRenumber();
		return maxRenumber != null && candidate.getDisplayOrder() > maxRenumber;
	}

	public List<String> getRenumberPositions() {
		Integer maxRenumber = modifiedBallots.getBallot().getMaxRenumber();
		List<String> list = new ArrayList<>();
		for (Integer i = 1; i <= getNoOfRenumberPositions() && (maxRenumber == null || i <= maxRenumber); i += 1) {
			list.add(i.toString());
		}
		return list;
	}

	public int getNoOfRenumberPositions() {
		return modifiedBallots.getBallot().personalVoteCandidates().size();
	}

	public boolean disableRenumberButton(String position, Candidate candidate) {
		return (candidate != null && position != null) && String.valueOf(candidate.getDisplayOrder()).equals(position);
	}

	void setModifiedBallots(ModifiedBallots modifiedBallots) {
		this.modifiedBallots = modifiedBallots;
	}
}
