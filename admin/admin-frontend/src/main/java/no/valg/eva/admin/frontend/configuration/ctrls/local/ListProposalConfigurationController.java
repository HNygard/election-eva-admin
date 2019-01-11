package no.valg.eva.admin.frontend.configuration.ctrls.local;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class ListProposalConfigurationController extends ListProposalBaseConfigurationController {

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.LIST_PROPOSAL;
	}

	@Override
	boolean hasAccess() {
		ListProposalConfig data = getListProposal();
		if (data == null) {
			try {
				data = loadData();
			} catch (RuntimeException e) {
				return false;
			}

		}
		return data != null && data.getChildren().isEmpty() && data.isSingleArea();
	}

	@Override
	boolean canBeSetToDone() {
		return true;
	}

	@Override
	public void saveDone() {
		saveListProposal();
	}

	@Override
	public void saveListProposal() {
		if (isEditable() && getListProposal().isValid() && saveDone(true)) {
			execute(() -> {
				setListProposal(getListProposalService().save(getUserData(), getListProposal(), false));
				MessageUtil.buildSavedMessage(getListProposal());
			});
		}
	}

	@Override
	public void prepareForSave() {
		// Not implemented
	}
}
