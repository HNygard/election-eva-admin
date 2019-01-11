package no.valg.eva.admin.frontend.rbac.ctrls;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class ExistingOperatorController extends BaseController {

	@Inject
	private OperatorAdminController adminController;
	@Inject
	private OperatorEditController editController;

	private Operator operator;

	public void init(Operator operator) {
		this.operator = operator;
		adminController.setView(RbacView.EXISTING);
	}

	public Operator getOperator() {
		return operator;
	}

	public void goToEditView() {
		editController.init(operator, RbacView.EDIT);
	}

	public void backToSearch() {
		adminController.setView(RbacView.SEARCH);
		resetState();
	}

	private void resetState() {
		operator = null;
	}
}
