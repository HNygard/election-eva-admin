package no.valg.eva.admin.frontend.picker.ctrls;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;

@Named
@ConversationScoped
public class ElectionPickerController extends AbstractPickerController {

	// Injected
	private MvElectionPickerController electionPicker;

	public ElectionPickerController() {
		// CDI
	}

	@Inject
	public ElectionPickerController(MvElectionPickerController electionPicker) {
		this.electionPicker = electionPicker;
	}
	
	@Override
	public String action() {
		StringBuilder result = new StringBuilder().append(cfg.getUri());
		if (electionPicker.getSelectionLevel() != null && electionPicker.getSelectedMvElection() != null) {
			appendRequestParam(result, "electionPath", electionPicker.getSelectedMvElection().getElectionPath());
		}

		if (getCfg().isKeepRequestParams()) {
			appendExistingRequestParams(result);
		}

		return result.toString();
	}
}
