package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class ValgstyreConfigurationController extends StyreConfigurationController {

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.VALGSTYRE;
	}

	@Override
	ReportingUnitTypeId getReportingUnitTypeId() {
		return VALGSTYRET;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.valgstyre.name";
	}

	@Override
	boolean hasAccess() {
		return super.hasAccess() && isMunicipalityLevel();
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setReportingUnitValgstyre(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isMunicipalityLevel() && getMunicipalityConfigStatus().isReportingUnitValgstyre();
	}
}
