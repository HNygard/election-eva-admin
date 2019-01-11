package no.valg.eva.admin.frontend.rbac.ctrls;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class OperatorCreatedController extends BaseController {

	@Inject
	private OperatorAdminController adminController;
	@Inject
	private OperatorEditController editController;
	@Inject
	private OperatorSearchController searchController;

	private Operator operator;

	public void init(Operator operator) {
		this.operator = operator;
		adminController.setView(RbacView.CREATED);
	}

	public void openEditMode() {
		editController.init(operator, RbacView.EDIT);
		resetState();
	}

	public void createAnother() {
		searchController.init();
		resetState();
	}

	public void goToList() {
		adminController.setView(RbacView.LIST);
		resetState();
	}

	public Operator getOperator() {
		return operator;
	}

	private void resetState() {
		operator = null;
	}
}
