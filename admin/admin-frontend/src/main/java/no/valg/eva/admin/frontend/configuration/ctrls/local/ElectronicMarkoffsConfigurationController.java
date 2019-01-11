package no.valg.eva.admin.frontend.configuration.ctrls.local;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class ElectronicMarkoffsConfigurationController extends ConfigurationController {

	private boolean electronicMarkoffs;

	@Override
	public void init() {
		this.electronicMarkoffs = getMunicipalityConfigStatus().isUseElectronicMarkoffs();
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.ELECTRONIC_MARKOFFS;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.electronic_markoffs.name";
	}

	@Override
	boolean hasAccess() {
		return isMunicipalityLevel() && getMainController().getElectionGroup().isElectronicMarkoffs();
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setElectronicMarkoffs(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isMunicipalityLevel() && getMunicipalityConfigStatus().isElectronicMarkoffs();
	}

	@Override
	boolean canBeSetToDone() {
		return true;
	}

	@Override
	public void saveDone() {
		saveState();
	}

	private boolean saveState() {
		return isEditable() && execute(() -> {
			getMunicipalityConfigStatus().setUseElectronicMarkoffs(electronicMarkoffs);
			if (super.saveDone(true)) {
				MessageUtil.buildDetailMessage("@config.local.electronic_markoffs.saved." + electronicMarkoffs, new String[] { getMvArea().getMunicipalityName() },
					FacesMessage.SEVERITY_INFO);
			}
		});
	}

	public boolean isElectronicMarkoffs() {
		return electronicMarkoffs;
	}

	public void setElectronicMarkoffs(boolean electronicMarkoffs) {
		this.electronicMarkoffs = electronicMarkoffs;
	}
}
