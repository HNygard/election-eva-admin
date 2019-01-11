package no.valg.eva.admin.frontend.election.forms;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public class EditElectionEventForm {

	private ElectionEvent electionEvent;
	private Integer statusId;
	private List<String> selectedLocalePks = new ArrayList<>();
	private boolean allowCopying;
	private Long copyFromEvent;
	private boolean copyRoles;
	private boolean copyAreas;
	private boolean copyElections;
	private boolean copyProposerList;
	private boolean copyElectoralRoll;
	private boolean copyElectionReportCountCategories;
	private boolean copyReportCountCategories;
	private boolean copyVotings;
	private boolean copyCountings;
	private boolean copyReportingUnits;

	public EditElectionEventForm() {
	}

	public EditElectionEventForm(Integer electoralRollLinesPerPage) {
		electionEvent = new ElectionEvent();
		electionEvent.setElectoralRollLinesPerPage(electoralRollLinesPerPage);
		statusId = CENTRAL_CONFIGURATION.id();
	}

	public void setElectionEvent(ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
		statusId = electionEvent.getElectionEventStatus().getId();
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public List<String> getSelectedLocalePks() {
		return selectedLocalePks;
	}

	public void setSelectedLocalePks(List<String> selectedLocalePks) {
		this.selectedLocalePks = selectedLocalePks;
	}

	public boolean isAllowCopying() {
		return allowCopying;
	}

	public void setAllowCopying(boolean allowCopying) {
		this.allowCopying = allowCopying;
	}

	public boolean isCopyRoles() {
		return copyRoles;
	}

	public void setCopyRoles(boolean copyRoles) {
		this.copyRoles = copyRoles;
	}

	public boolean isCopyAreas() {
		return copyAreas;
	}

	public void setCopyAreas(boolean copyAreas) {
		this.copyAreas = copyAreas;
	}

	public boolean isCopyElections() {
		return copyElections;
	}

	public void setCopyElections(boolean copyElections) {
		this.copyElections = copyElections;
	}

	public boolean isCopyProposerList() {
		return copyProposerList;
	}

	public void setCopyProposerList(boolean copyProposerList) {
		this.copyProposerList = copyProposerList;
	}

	public boolean isCopyElectoralRoll() {
		return copyElectoralRoll;
	}

	public void setCopyElectoralRoll(boolean copyElectoralRoll) {
		this.copyElectoralRoll = copyElectoralRoll;
	}

	public boolean isCopyElectionReportCountCategories() {
		return copyElectionReportCountCategories;
	}

	public void setCopyElectionReportCountCategories(boolean copyElectionReportCountCategories) {
		this.copyElectionReportCountCategories = copyElectionReportCountCategories;
	}

	public boolean isCopyReportCountCategories() {
		return copyReportCountCategories;
	}

	public void setCopyReportCountCategories(boolean copyReportCountCategories) {
		this.copyReportCountCategories = copyReportCountCategories;
	}

	public boolean isCopyVotings() {
		return copyVotings;
	}

	public void setCopyVotings(boolean copyVotings) {
		this.copyVotings = copyVotings;
	}

	public boolean isCopyCountings() {
		return copyCountings;
	}

	public void setCopyCountings(boolean copyCountings) {
		this.copyCountings = copyCountings;
	}

	public boolean isCopyReportingUnits() {
		return copyReportingUnits;
	}

	public void setCopyReportingUnits(boolean copyReportingUnits) {
		this.copyReportingUnits = copyReportingUnits;
	}

	public Long getCopyFromEvent() {
		return copyFromEvent;
	}

	public void setCopyFromEvent(Long copyFromEvent) {
		this.copyFromEvent = copyFromEvent;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}
}
