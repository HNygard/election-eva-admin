package no.valg.eva.admin.frontend.reportingunit.ctrls;

import no.evote.dto.ReportingUnitTypeDto;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.ReportingUnitTypeService;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ViewScoped
public class ReportingUnitTypeController extends BaseController {

	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private ReportingUnitTypeService reportingUnitTypeService;
	@Inject
	private transient MessageProvider mms;

	private List<ReportingUnitTypeDto> reportingUnitTypeDtoList;
	private ReportingUnitTypeDto selectedReportingUnitTypeDto;
	private List<MvElection> electionList;

	@PostConstruct
	public void init() {
		reportingUnitTypeDtoList = reportingUnitTypeService.populateReportingUnitTypeDto(userData, userDataController.getElectionEvent().getId());
		if (!userDataController.isCentralConfigurationStatus()) {
			showMessage(mms.get("@reporting_unit_type.election_event_not_central"), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void setReportingUnitTypeRow(ReportingUnitTypeDto reportingUnitType) {
		selectedReportingUnitTypeDto = reportingUnitType;
		electionList = selectedReportingUnitTypeDto.getElections();

		int i;
		for (i = 0; i < electionList.size(); i++) {
			electionList.get(i).setReportingUnit(false);
			for (MvElection selectedMvElection : selectedReportingUnitTypeDto.getSelectedElections()) {
				if (electionList.get(i).getPk().longValue() == selectedMvElection.getPk().longValue()) {
					electionList.get(i).setReportingUnit(true);
					break;
				} else {
					electionList.get(i).setReportingUnit(false);
				}
			}
		}
	}

	public void updateElectionReportingUnits(ReportingUnitTypeDto dto) {
		try {
			selectedReportingUnitTypeDto = dto;
			reportingUnitTypeService.updateMvElectionReportingUnits(userData, electionList, selectedReportingUnitTypeDto.getReportingUnitTypePk());
		} catch (EvoteException e) {
			if (e.getMessage().contains("@reporting_unit.duplicate")) {
				// The exception from PSQL contains this text in the message
				showMessage(mms.get("@reporting_unit.duplicate"), FacesMessage.SEVERITY_ERROR);
			} else {
				showMessage(mms.get("@reporting_unit_type.update_election_reporting_unit_fail", mms.get(selectedReportingUnitTypeDto.getName())),
						FacesMessage.SEVERITY_ERROR);
			}
			reportingUnitTypeDtoList = reportingUnitTypeService.populateReportingUnitTypeDto(userData, userDataController.getElectionEvent().getId());
			return;
		}
		showMessage(mms.get("@reporting_unit_type.update_election_reporting_unit_success", mms.get(selectedReportingUnitTypeDto.getName())),
				FacesMessage.SEVERITY_INFO);
		reportingUnitTypeDtoList = reportingUnitTypeService.populateReportingUnitTypeDto(userData, userDataController.getElectionEvent().getId());
	}

	private void showMessage(final String message, final Severity severityInfo) {
		getFacesContext().addMessage("form:", new FacesMessage(severityInfo, message, message));
	}

	public List<ReportingUnitTypeDto> getReportingUnitTypeDtoList() {
		return reportingUnitTypeDtoList;
	}

	public void setReportingUnitTypeDtoList(final List<ReportingUnitTypeDto> reportingUnitTypeDtoList) {
		this.reportingUnitTypeDtoList = reportingUnitTypeDtoList;
	}

	public ReportingUnitTypeDto getSelectedReportingUnitTypeDto() {
		return selectedReportingUnitTypeDto;
	}

	public void setSelectedReportingUnitTypeDto(final ReportingUnitTypeDto selectedReportingUnitTypeDto) {
		this.selectedReportingUnitTypeDto = selectedReportingUnitTypeDto;
	}

	public List<MvElection> getElectionList() {
		return electionList;
	}

	public void setElectionList(final List<MvElection> electionList) {
		this.electionList = electionList;
	}

}
