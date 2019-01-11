package no.valg.eva.admin.frontend.configuration.ctrls.local;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.common.configuration.service.ReportCountCategoryService;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.frontend.common.DeleteAction;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class TechnicalPollingDistrictConfigurationController extends PlacesConfigurationController<TechnicalPollingDistrict> implements DeleteAction {

	private ReportCountCategoryService reportCountCategoryService;

	public TechnicalPollingDistrictConfigurationController() {
		// CDI
	}

	@Inject
	public TechnicalPollingDistrictConfigurationController(ReportCountCategoryService reportCountCategoryService) {
		this.reportCountCategoryService = reportCountCategoryService;
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.TECHNICAL_POLLING_DISTRICT;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.technical_polling_district.name";
	}

	@Override
	boolean hasAccess() {
		return isMunicipalityLevel() && isConfiguredWithTechnicalPollingDistricts();
	}
	
	boolean isConfiguredWithTechnicalPollingDistricts() {
		return reportCountCategoryService
				.findFirstByAreaAndCountCategory(getUserData(), getAreaPath(), getMainController().getElectionGroup().getElectionGroupPath(), CountCategory.FO)
				.getCountingMode()
				.isTechnicalPollingDistrictCount();
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setTechPollingDistricts(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isMunicipalityLevel() && getMunicipalityConfigStatus().isTechPollingDistricts();
	}

	@Override
	boolean canBeSetToDone() {
		if (getPlaces() == null || getPlaces().isEmpty()) {
			return true;
		}
		for (TechnicalPollingDistrict district : getPlaces()) {
			if (!district.isValid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	List<TechnicalPollingDistrict> collectPollingPlaces() {
		return getPollingDistrictService().findTechnicalPollingDistrictsByArea(getUserData(), getAreaPath());
	}

	@Override
	TechnicalPollingDistrict collectPollingPlace(String id) {
		return getPollingDistrictService().findTechnicalPollingDistrictByAreaAndId(getUserData(), getAreaPath(), id);
	}

	@Override
	TechnicalPollingDistrict save(TechnicalPollingDistrict place) {
		return getPollingDistrictService().saveTechnicalPollingDistrict(getUserData(), place);
	}

	@Override
	public void confirmDelete() {
		if (getPlace() == null) {
			return;
		}
		execute(() -> {
			saveDone(false);
			getPollingDistrictService().deleteTechnicalPollingDistrict(getUserData(), getPlace());
			MessageUtil.buildDeletedMessage(getPlace());
			setPlace(null);
			collectData();
		});
	}

	/**
	 * Prepare for new polling place.
	 */
	public void initCreate() {
		setPlace(new TechnicalPollingDistrict(getAreaPath()));
		setMode(ConfigurationMode.CREATE);
	}

}
