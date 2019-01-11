package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class FylkesvalgstyreConfigurationController extends StyreConfigurationController {

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.FYLKESVALGSTYRE;
	}

	@Override
	ReportingUnitTypeId getReportingUnitTypeId() {
		return FYLKESVALGSTYRET;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.fylkesvalgstyre.name";
	}

	@Override
	boolean hasAccess() {
		return super.hasAccess() && isCountyLevel();
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isCountyLevel()) {
			getCountyConfigStatus().setReportingUnitFylkesvalgstyre(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isCountyLevel() && getCountyConfigStatus().isReportingUnitFylkesvalgstyre();
	}

}
