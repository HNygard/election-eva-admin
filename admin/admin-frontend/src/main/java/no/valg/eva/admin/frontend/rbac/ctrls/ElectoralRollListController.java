package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.Collection;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ViewScoped
public class ElectoralRollListController extends BaseController {

	@Inject
	private OperatorAdminController adminController;
	@Inject
	private OperatorListController listController;
	@Inject
	private OperatorEditController editController;
	@Inject
	private MessageProvider messageProvider;
	@Inject
	private UserData userData;
	@Inject
	private AdminOperatorService adminOperatorService;

	private Collection<Person> personList;

	public void init(Collection<Person> fromSearch) {
		personList = fromSearch;
		adminController.setView(RbacView.ELECTORAL_ROLL_LIST);
	}

	public void goToEditView(Person person) {
		if (listController.exists(person)) {
			editController.init(listController.getOperator(person), RbacView.EDIT);
		} else {
			Operator operator = adminOperatorService.operatorOrVoterById(userData, person.getPersonId());
			if (operator.getRoleAssociations().isEmpty()) {
				editController.init(operator, RbacView.NEW_FROM_ELECTORAL_ROLL);
			} else {
				editController.init(operator, RbacView.EDIT);
			}
		}
	}

	public void backToSearch() {
		adminController.setView(RbacView.SEARCH);
		resetState();
	}

	public String getOpenEditLabel(Person person) {
		if (listController.exists(person)) {
			return person.nameLine() + " (" + messageProvider.get("@rbac.operator.already.registered") + ")";
		} else {
			return person.nameLine();
		}
	}

	public Collection<Person> getPersonList() {
		return personList;
	}

	private void resetState() {
		personList = null;
	}
}
