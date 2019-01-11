package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models;

import java.io.Serializable;

import org.joda.time.LocalDate;

public class ProvingSamletForm implements Serializable {

	private Long selectedPollingPlacePk;
	private String selectedVotingCategoryId;
	private LocalDate startDate;
	private LocalDate endDate;
	private boolean registeredToday;
	private String startVotingNumber;
	private String endVotingNumber;

	public Long getSelectedPollingPlacePk() {
		return selectedPollingPlacePk;
	}

	public void setSelectedPollingPlacePk(Long selectedPollingPlacePk) {
		this.selectedPollingPlacePk = selectedPollingPlacePk;
	}

	public String getSelectedVotingCategoryId() {
		return selectedVotingCategoryId;
	}

	public void setSelectedVotingCategoryId(String selectedVotingCategoryId) {
		this.selectedVotingCategoryId = selectedVotingCategoryId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isRegisteredToday() {
		return registeredToday;
	}

	public void setRegisteredToday(boolean registeredToday) {
		this.registeredToday = registeredToday;
	}

	public String getStartVotingNumber() {
		return startVotingNumber;
	}

	public void setStartVotingNumber(String startVotingNumber) {
		this.startVotingNumber = startVotingNumber;
	}

	public String getEndVotingNumber() {
		return endVotingNumber;
	}

	public void setEndVotingNumber(String endVotingNumber) {
		this.endVotingNumber = endVotingNumber;
	}
}
