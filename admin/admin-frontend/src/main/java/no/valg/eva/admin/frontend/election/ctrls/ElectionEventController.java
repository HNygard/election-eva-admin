package no.valg.eva.admin.frontend.election.ctrls;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.frontend.election.forms.EditElectionEventForm;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.configuration.application.ElectionEventMapper.toDto;

@Named
@ViewScoped
public class ElectionEventController extends EditElectionEventController {

	private static final Logger LOGGER = Logger.getLogger(ElectionEventController.class);

	private EditElectionEventForm updateForm;
	private List<ElectionDay> electionDayList = new ArrayList<>();
	private ElectionDay deletedRow;
	private int totalNumberElectionDays;
	private ElectionDay electionDay = new ElectionDay();
	private Dialog addElectionDayDialog = Dialogs.ADD_ELECTION_DAY;

	@PostConstruct
	public void init() {
		loadElectionEvent();
		reloadElectionEventList();
		loadElectionEventStatusList();
		loadLocaleList();
		findThemes();
	}

	public void makeNewElectionDay() {
		electionDay = new ElectionDay();
		electionDay.setElectionEvent(toDto(updateForm.getElectionEvent()));
		getAddElectionDayDialog().open();
	}

	public void deleteElectionDay() {
		ElectionDay electionDayToDelete = deletedRow;
		if (totalNumberElectionDays == 1) {
			MessageUtil.buildDetailMessage(getFacesContext(),
					getMessageProvider().get(MessageUtil.FIELD_IS_REQUIRED, getMessageProvider().get("@election.election_event.election_day")),
					FacesMessage.SEVERITY_ERROR);
			return;
		}
		if (electionDayToDelete.getPk() != null && getElectionEventService().findElectionDayByPk(getUserDataController().getUserData(), electionDayToDelete.getPk()) != null) {
			try {
				getElectionEventService().deleteElectionDay(getUserDataController().getUserData(), electionDayToDelete);
				MessageUtil.buildDetailMessage(getMessageProvider().get("@election.election_event.election_day_deleted"), FacesMessage.SEVERITY_INFO);
			} catch (EvoteException e) {
				String[] electionDayParam = {MessageUtil.dateString(electionDayToDelete.getDate(), getUserDataController().getUserData().getJavaLocale())};
				MessageUtil.buildFacesMessage(getFacesContext(), null, "@election.election_event.election_day_cannot_be_deleted", electionDayParam,
						FacesMessage.SEVERITY_WARN);
			}
		}
		electionDayList.clear();
		electionDayList.addAll(getElectionEventService().findElectionDaysByElectionEvent(getUserDataController().getUserData(),	updateForm.getElectionEvent()));
		electionDayList.sort(Comparator.comparing(ElectionDay::getDate));
	}

	public void setDeletedRow(final ElectionDay electionDay) {
		deletedRow = electionDay;
		totalNumberElectionDays = electionDayList.size();
	}

	public void doAddElectionDay() {
		if (verifyThatEndTimeIsAfterStartTime() && verifyThatElectionDoesNotDayAlreadyExists()) {
			execute(() -> {
				ElectionDay createElectionDay = getElectionEventService().createElectionDay(getUserDataController().getUserData(), electionDay);
				electionDayList.add(createElectionDay);
				getAddElectionDayDialog().closeAndUpdate("form:electionDayDataTable", "form:messageBox");
			});
		}
	}

	public String doUpdateElectionEvent() {
		execute(() -> {
			updateForm.getElectionEvent().setElectionEventStatus(getElectionEventStatus(updateForm.getStatusId()));
			ElectionEvent event = getElectionEventService().update(getUserDataController().getUserData(), updateForm.getElectionEvent(),
					new HashSet<>(getAvailableLocalesSet()));
			updateForm.setElectionEvent(event);
			saveElectionDayList();
			getUserDataController().invalidateCachedElectionEvent();
			electionDayList = getElectionEventService().findElectionDaysByElectionEvent(getUserDataController().getUserData(), updateForm.getElectionEvent());
			MessageUtil.buildDetailMessage(MessageUtil.UPDATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);
		});
		return "";
	}

	@Override
	protected void loadLocaleList() {
		super.loadLocaleList();
		if (updateForm.getElectionEvent().getPk() != null) {
			List<ElectionEventLocale> electionEventLocaleList = getElectionEventService().getElectionEventLocalesForEvent(
					getUserDataController().getUserData(),
					updateForm.getElectionEvent());
			updateForm.setSelectedLocalePks(new ArrayList<>(electionEventLocaleList.size()));
			for (int i = 0; i < electionEventLocaleList.size(); i++) {
				getAvailableLocalesSet().add(electionEventLocaleList.get(i).getLocale());
				if (electionEventLocaleList.get(i).getLocale().getPk() != null) {
					updateForm.getSelectedLocalePks().add(i, electionEventLocaleList.get(i).getLocale().getPk().toString());
				}
			}
		}
	}

	public boolean isRenderDeleteElectionDayLink() {
		return updateForm.getElectionEvent().getElectionEventStatus().getId() == CENTRAL_CONFIGURATION.id();
	}

	public EditElectionEventForm getUpdateForm() {
		return updateForm;
	}

	public List<ElectionDay> getElectionDayList() {
		return electionDayList;
	}

	public void setElectionDayList(final List<ElectionDay> electionDayList) {
		this.electionDayList = electionDayList;
	}

	public ElectionDay getElectionDay() {
		return electionDay;
	}

	public void setElectionDay(final ElectionDay electionDay) {
		this.electionDay = electionDay;
	}

	public Dialog getAddElectionDayDialog() {
		return addElectionDayDialog;
	}

	private void loadElectionEvent() {
		// Cached ElectionEvent may contain stale data
		getUserDataController().invalidateCachedElectionEvent();
		updateForm = new EditElectionEventForm();
		updateForm.setElectionEvent(getUserDataController().getElectionEvent());
		electionDayList = getElectionEventService().findElectionDaysByElectionEvent(getUserDataController().getUserData(), updateForm.getElectionEvent());
		electionDayList.sort(Comparator.comparing(ElectionDay::getDate));
	}

	private void saveElectionDayList() {
		execute(() -> {
			List<ElectionDay> updatedElectionDays = new ArrayList<>();
			for (ElectionDay anElectionDay : electionDayList) {
				boolean doesElectionDayExists = doesElectionDayAlreadyExists(updatedElectionDays, anElectionDay);
				if (!doesElectionDayExists) {
					if (isStartTimeAfterEndTime(anElectionDay)) {

						String[] msgInvalid = {MessageUtil.timeString(anElectionDay.getStartTime(), getUserDataController().getUserData().getJavaLocale()),
								MessageUtil.timeString(anElectionDay.getEndTime(), getUserDataController().getUserData().getJavaLocale()),
								MessageUtil.dateString(anElectionDay.getDate(), getUserDataController().getUserData().getJavaLocale())};
						MessageUtil.buildFacesMessage(getFacesContext(), null, MessageUtil.EXCEPTION_END_DATE_BEFORE_START_DATE_AT_ELECTION_DAY, msgInvalid,
								FacesMessage.SEVERITY_WARN);

					}
					updatedElectionDays.add(anElectionDay);
					if (anElectionDay.getPk() == null) {
						anElectionDay = getElectionEventService().createElectionDay(getUserDataController().getUserData(), anElectionDay);
						LOGGER.debug(anElectionDay.getDate() + " created");
					} else {
						anElectionDay = getElectionEventService().updateElectionDay(getUserDataController().getUserData(), anElectionDay);
						LOGGER.debug(anElectionDay.getDate() + " updated");
					}
				}
			}
		});
	}

	private boolean doesElectionDayAlreadyExists(final List<ElectionDay> updatedElectionDays, final ElectionDay electionDay) {
		for (ElectionDay existingElectionDay : updatedElectionDays) {
			if (existingElectionDay.getDate().compareTo(electionDay.getDate()) == 0) {
				String[] summaryParams = { "\"" + updateForm.getElectionEvent().getName() + "\"" };
				MessageUtil.buildFacesMessage(getFacesContext(), null, "@election.election_event.election_day_already_exists",
						summaryParams, FacesMessage.SEVERITY_ERROR);
				return true;
			}
		}
		return false;
	}

	private boolean verifyThatElectionDoesNotDayAlreadyExists() {
		return !doesElectionDayAlreadyExists(electionDayList, electionDay);

	}

	private boolean isStartTimeAfterEndTime(final ElectionDay electionDay) {
		return electionDay.getStartTime().isAfter(electionDay.getEndTime());
	}

	private boolean verifyThatEndTimeIsAfterStartTime() {
		if (isStartTimeAfterEndTime(electionDay)) {
			String[] summaryParams = { "" };
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@common.message.evote_application_exception.START_TIME_NOT_BEFORE_END_TIME", summaryParams,
					FacesMessage.SEVERITY_ERROR);
			return false;
		}
		return true;
	}
}
