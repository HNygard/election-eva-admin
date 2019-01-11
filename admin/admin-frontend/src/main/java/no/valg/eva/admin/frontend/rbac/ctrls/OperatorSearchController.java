package no.valg.eva.admin.frontend.rbac.ctrls;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Address;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class OperatorSearchController extends BaseController {

	public static final String FNR = "fnr";

	@Inject
	private OperatorAdminController adminController;
	@Inject
	private OperatorListController listController;
	@Inject
	private ExistingOperatorController existingController;
	@Inject
	private OperatorEditController editController;
	@Inject
	private ElectoralRollListController elListController;
	@Inject
	private AdminOperatorService adminOperatorService;
	@Inject
	private UserData userData;

	private String searchOperatorId;
	private String searchOperatorName;
	private String operatorSearchCriteria;

	public void init() {
		adminController.setView(RbacView.SEARCH);
		setSearchOperatorId(null);
		setSearchOperatorName(null);
		setOperatorSearchCriteria(FNR);
	}

	public void searchOperator() {
		if (FNR.equals(getOperatorSearchCriteria())) {
			Operator operator = adminOperatorService.operatorOrVoterById(userData, new PersonId(getSearchOperatorId()));
			if (operator == null) {
				Operator newOperator = new Operator(searchOperatorId, "", "", "", "", true);
				newOperator.setAddress(new Address("", "", "", "", "", ""));
				editController.init(newOperator, RbacView.NEW);
			} else {
				if (listController.exists(operator)) {
					existingController.init(operator);
				} else {
					operator.setActive(true);
					editController.init(operator, RbacView.NEW_FROM_EXISTING_VOTER);
				}
			}
		} else {
			elListController.init(adminOperatorService.operatorsByName(userData, searchOperatorName));
		}
	}

	public void backToList() {
		adminController.setView(RbacView.LIST);
		resetState();
	}

	public String getSearchOperatorId() {
		return searchOperatorId;
	}

	public void setSearchOperatorId(String searchOperatorId) {
		this.searchOperatorId = searchOperatorId;
	}

	public String getSearchOperatorName() {
		return searchOperatorName;
	}

	public void setSearchOperatorName(String searchOperatorName) {
		this.searchOperatorName = searchOperatorName;
	}

	public String getOperatorSearchCriteria() {
		return operatorSearchCriteria;
	}

	public void setOperatorSearchCriteria(String operatorSearchCriteria) {
		this.operatorSearchCriteria = operatorSearchCriteria;
	}

	private void resetState() {
		searchOperatorId = null;
		searchOperatorName = null;
		operatorSearchCriteria = null;
	}
}
