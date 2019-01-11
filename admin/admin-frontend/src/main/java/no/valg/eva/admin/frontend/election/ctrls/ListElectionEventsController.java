package no.valg.eva.admin.frontend.election.ctrls;

import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.frontend.election.forms.EditElectionEventForm;

import org.apache.log4j.Logger;

@Named
@ViewScoped
public class ListElectionEventsController extends EditElectionEventController {

	private static final Logger LOGGER = Logger.getLogger(ListElectionEventsController.class);

	private Dialog createElectionEventDialog = Dialogs.CREATE_ELECTION_EVENT;

	private EditElectionEventForm createForm;
	private ElectionEvent electionEventToReOpen;

	@PostConstruct
	public void init() {
		reloadElectionEventList();
		loadElectionEventStatusList();
		loadLocaleList();
		findThemes();
	}

	public void doGetCreateElectionEvent() {
		createForm = new EditElectionEventForm(getUserDataController().getElectionEvent().getElectoralRollLinesPerPage());
		getAvailableLocalesSet().clear();
		getCreateElectionEventDialog().open();
	}

	public void reOpenElectionEvent() {
		execute(() -> {
			getElectionEventService().approveConfiguration(getUserDataController().getUserData(), electionEventToReOpen.getPk());
			String[] summaryParams = { "\"" + electionEventToReOpen.getName() + "\"" };
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@election.election_event.reopen.message", summaryParams, FacesMessage.SEVERITY_INFO);
			reloadElectionEventList();
		});
	}

	/**
	 * Creates election event.
	 */
	public void doCreateElectionEvent() {
		LOGGER.debug("doCreateElectionEvent");
		execute(() -> {
			ElectionEvent electionEvent = createForm.getElectionEvent();
			if (idExists(electionEvent)) {
				String[] summaryParams = { "@election_level[0].name", electionEvent.getId() };
				MessageUtil.buildFacesMessage(getFacesContext(), null, "@common.message.create.CHOOSE_UNIQUE_ELECTION_ID", summaryParams,
						FacesMessage.SEVERITY_ERROR);
				return;
			}

			electionEvent.setElectionEventStatus(getElectionEventStatus(createForm.getStatusId()));
			VotingHierarchy votingHierarchy = getVotingHierarchy();
			CountingHierarchy countingHierarchy = getCountingHierarchy();
			Long copyFromEventPk = createForm.isAllowCopying() ? createForm.getCopyFromEvent() : null;
			ElectionEvent copyFromEvent = getElectionEventService().findByPk(copyFromEventPk);
			boolean isCopyRoles = createForm.isAllowCopying() && createForm.isCopyRoles();
			if (createElectionEventSynchronously(votingHierarchy, countingHierarchy)) {
				electionEvent = getElectionEventService().create(getUserDataController().getUserData(), electionEvent, isCopyRoles, votingHierarchy,
						countingHierarchy, copyFromEvent, new HashSet<>(getAvailableLocalesSet()));
				String[] summaryParams = { "\"" + electionEvent.getName() + "\"" };
				MessageUtil.buildFacesMessage(getFacesContext(), null, "@election.election_event.election_event_created", summaryParams,
						FacesMessage.SEVERITY_INFO);
				reloadElectionEventList();
				getCreateElectionEventDialog().closeAndUpdate("form:electionEventTable", "form:msg");
			} else {
				getElectionEventService().createAsync(getUserDataController().getUserData(), electionEvent, isCopyRoles, votingHierarchy, countingHierarchy,
						
						copyFromEvent, new HashSet<>(getAvailableLocalesSet()));
				MessageUtil.buildFacesMessage(getFacesContext(), null, "@election.election_event.election_event_created_asynchronously",
						new String[] { electionEvent.getId() }, FacesMessage.SEVERITY_INFO);
				getCreateElectionEventDialog().closeAndUpdate("form:msg");
			}
		});
	}

	public boolean isStatusClosed(ElectionEvent electionEvent) {
		return electionEvent != null && ElectionEventStatusEnum.CLOSED.id() == electionEvent.getElectionEventStatus().getId();
	}

	public void setElectionEventToReOpen(ElectionEvent electionEventToReOpen) {
		this.electionEventToReOpen = electionEventToReOpen;
	}

	public ElectionEvent getElectionEventToReOpen() {
		return electionEventToReOpen;
	}

	public Dialog getCreateElectionEventDialog() {
		return createElectionEventDialog;
	}

	public EditElectionEventForm getCreateForm() {
		return createForm;
	}

	private boolean idExists(ElectionEvent electionevent) {
		return getElectionEventService().findById(getUserDataController().getUserData(), electionevent.getId()) != null;
	}

	private boolean createElectionEventSynchronously(VotingHierarchy votingHierarchy, CountingHierarchy countingHierarchy) {
		return VotingHierarchy.NONE.equals(votingHierarchy) && CountingHierarchy.NONE.equals(countingHierarchy);
	}

	private VotingHierarchy getVotingHierarchy() {
		if (createForm.isAllowCopying()) {
			return VotingHierarchy.getVotingHierarchy(
					createForm.isCopyAreas(), createForm.isCopyElections(),
					createForm.isCopyElectoralRoll(), createForm.isCopyVotings());
		}
		return VotingHierarchy.NONE;
	}

	private CountingHierarchy getCountingHierarchy() {
		if (createForm.isAllowCopying()) {
			return CountingHierarchy.getCountingHierarchy(createForm.isCopyAreas(), createForm.isCopyElections(),
					createForm.isCopyProposerList(),
					createForm.isCopyElectionReportCountCategories(), createForm.isCopyReportCountCategories(), createForm.isCopyReportingUnits(),
					createForm.isCopyCountings());
		}
		return CountingHierarchy.NONE;
	}

}
