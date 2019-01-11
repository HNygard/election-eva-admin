package no.valg.eva.admin.frontend.picker.ctrls;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;

@Named
@ConversationScoped
public class AreaPickerController extends AbstractPickerController {

	// Injected
	private MvAreaPickerController areaPicker;

	public AreaPickerController() {
		// CDI
	}

	@Inject
	public AreaPickerController(MvAreaPickerController areaPicker) {
		this.areaPicker = areaPicker;
	}

	@Override
	public String action() {
		StringBuilder result = new StringBuilder().append(cfg.getUri());
		if (areaPicker.getSelectionLevel() != null && areaPicker.getSelectedMvArea() != null) {
			appendRequestParam(result, "areaPath", areaPicker.getSelectedMvArea().getAreaPath());
		}

		if (getCfg().isKeepRequestParams()) {
			appendExistingRequestParams(result);
		}

		return result.toString();
	}
}
