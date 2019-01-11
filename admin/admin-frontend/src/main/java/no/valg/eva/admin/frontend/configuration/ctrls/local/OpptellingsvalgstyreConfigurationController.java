package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class OpptellingsvalgstyreConfigurationController extends StyreConfigurationController {

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.OPPTELLINGSVALGSTYRE;
	}

	@Override
	ReportingUnitTypeId getReportingUnitTypeId() {
		return OPPTELLINGSVALGSTYRET;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.opptellingsvalgstyre.name";
	}

	@Override
	boolean hasAccess() {
		return super.hasAccess() && getUserData().isOpptellingsvalgstyret();
	}

	@Override
	void setDoneStatus(boolean value) {
		// Do nothing
	}

	@Override
	public boolean isDoneStatus() {
		return false;
	}
}
