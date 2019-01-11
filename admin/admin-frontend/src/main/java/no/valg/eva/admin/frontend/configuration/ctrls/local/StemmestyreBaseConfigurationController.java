package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.application.ResponsibilityValidationService;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.frontend.common.RoleConflictHandler;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverter;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverterSource;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import javax.inject.Inject;
import java.util.List;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class StemmestyreBaseConfigurationController extends StyreConfigurationController implements PlaceConverterSource<PollingDistrict>, RoleConflictHandler {

	@Inject
	private PollingDistrictService pollingDistrictService;
    @Inject
    private MessageProvider messageProvider;
    @Inject
    private ResponsibilityValidationService responsibilityValidationService;

    private List<ResponsibilityConflict> boardMemberConflicts;
	private PlaceConverter placeConverter = new PlaceConverter(this);
	private List<PollingDistrict> pollingDistricts;
	private PollingDistrict pollingDistrict;

	@Override
	public void init() {
		pollingDistricts = pollingDistrictService.findRegularPollingDistrictsByArea(getUserData(), getAreaPath(), true);
		if (!pollingDistricts.isEmpty()) {
			setPollingDistrict(pollingDistricts.get(0));
		}
		super.init();
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.STEMMESTYRE;
	}

	@Override
	ReportingUnitTypeId getReportingUnitTypeId() {
		return STEMMESTYRET;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.stemmestyre.name";
	}

	@Override
	boolean hasAccess() {
		return super.hasAccess() && isMunicipalityLevel();
	}

	@Override
	Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
		return new Class[] { ElectionDayPollingPlacesConfigurationController.class };
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setReportingUnitStemmestyre(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isMunicipalityLevel() && getMunicipalityConfigStatus().isReportingUnitStemmestyre();
	}

	@Override
	boolean canBeSetToDone() {
		if (pollingDistricts == null || pollingDistricts.isEmpty()) {
			return false;
		}
		for (PollingDistrict district : pollingDistricts) {
			if (!district.isValid() || !district.isHasResponsibleOffiers()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public PlaceConverter getPlaceConverter() {
		return placeConverter;
	}

	@Override
	public List<PollingDistrict> getPlaces() {
		return pollingDistricts;
	}

	@Override
	AreaPath getStyreAreaPath() {
		return pollingDistrict == null ? null : pollingDistrict.getPath();
	}

	@Override
	void loadResponsibleOfficers() {
		super.loadResponsibleOfficers();
		pollingDistrict.setHasResponsibleOffiers(!getResponsibleOfficers().isEmpty());
	}

	@Override
	public void saveResponsibleOfficer() {
        ResponsibleOfficer selectedResponsibleOfficer = getSelectedResponsibleOfficer();

        boardMemberConflicts = responsibilityValidationService.checkIfBoardMemberHasCandidateConflict(
                getUserData(),
                selectedResponsibleOfficer.getFirstName(),
                selectedResponsibleOfficer.getMiddleName(),
                selectedResponsibleOfficer.getLastName(),
                getStyreAreaPath());

        if (boardMemberConflicts.isEmpty()) {
            addBoardMemberAndDismissRoleWidget();
        } else {
            showConflictWidget();
        }
    }

    private void showConflictWidget() {
        FacesUtil.updateDom(getConflictDialogWidgetId());
        FacesUtil.executeJS("PF('roleConflictWidget').show()");
    }

    private String getConflictDialogWidgetId() {
        return getBoardMemberFormIdPath() + ":conflictWidget:roleConflictDialog";
    }

    private void addBoardMemberAndDismissRoleWidget() {
        super.saveResponsibleOfficer();
        pollingDistrict.setHasResponsibleOffiers(!getResponsibleOfficers().isEmpty());
        FacesUtil.updateDom(asList(getBoardMemberFormIdPath(), "main-messages"));
    }

    abstract String getBoardMemberFormIdPath();

	@Override
	public void confirmDelete() {
		super.confirmDelete();
		pollingDistrict.setHasResponsibleOffiers(!getResponsibleOfficers().isEmpty());

	}

	void districtSelected(PollingDistrict district) {
		if (district != null) {
			setMode(ConfigurationMode.READ);
			pollingDistrict = district;
			loadResponsibleOfficers();
		}
	}

	public PollingDistrict getPollingDistrict() {
		return pollingDistrict;
	}

	public void setPollingDistrict(PollingDistrict pollingDistrict) {
		this.pollingDistrict = pollingDistrict;
	}

    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public List<ResponsibilityConflict> getResponsibilityConflicts() {
        return boardMemberConflicts;
    }

    @Override
    public void onAcceptRoleConflict() {
        addBoardMemberAndDismissRoleWidget();
    }

    @Override
    public String getLocalizedRoleConflictMessage() {
        if (getSelectedResponsibleOfficer() == null) {
            return null;
        }

        final String boardMemberFullName = getSelectedResponsibleOfficer().getFullName();
        if (isNotEmpty(boardMemberFullName)) {
            return getMessageProvider().get("@config.local.responsible.officer.conflictMessage", boardMemberFullName);
        }

        return null;
    }

    @Override
    public String getLocalizedRoleConflictExplanation() {
        return getMessageProvider().get("@config.local.responsible.officer.conflictExplanation");
    }
}
