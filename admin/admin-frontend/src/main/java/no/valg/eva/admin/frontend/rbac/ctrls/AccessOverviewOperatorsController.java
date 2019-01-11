package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.evote.service.rbac.OperatorService;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

import org.primefaces.event.SelectEvent;

@Named
@ViewScoped
public class AccessOverviewOperatorsController extends BaseController {

	// Injected
	private UserData userData;
	private OperatorRoleService operatorRoleService;
	private OperatorService operatorService;
	private MessageProvider messageProvider;

	private Access access;
	private Operator selectedOperator;
	private ListDataModel<OperatorRole> operatorRoles;
	private ListDataModel<Operator> operators;
	private String accessPathName;

	public AccessOverviewOperatorsController() {
		// CDI
	}

	@Inject
	public AccessOverviewOperatorsController(UserData userData, OperatorRoleService operatorRoleService,
			OperatorService operatorService, MessageProvider messageProvider) {
		this.userData = userData;
		this.operatorRoleService = operatorRoleService;
		this.operatorService = operatorService;
		this.messageProvider = messageProvider;
	}

	@PostConstruct
	public void init() {
		access = new Access(getRequestParameter("access"));
		List<Operator> operatorList = operatorService.findOperatorsWithAccess(userData, userData.getOperatorRole().getMvArea(), access);
		operators = new ListDataModel<>(operatorList);
		accessPathName = buildAccessPathName(access);
	}
	
	private String buildAccessPathName(Access access) {
		String parentName = "";
		String delimiter = " > ";
		if (access.getParent() != null) {
			parentName += buildAccessPathName(access.getParent());
		} else {
			delimiter = "";
		}
		return parentName + delimiter + messageProvider.get(access.getName());
	}

	public void onOperatorSelect(SelectEvent event) {
		setSelectedOperator((Operator) event.getObject());
		if (getSelectedOperator() != null) {
			List<OperatorRole> operatorRoleList = operatorRoleService.findOperatorRolesGivingOperatorAccess(userData, userData.getOperatorRole().getMvArea(),
					getSelectedOperator(), access);

			Collections.sort(operatorRoleList, (operatorRole1, operatorRole2) -> messageProvider.get(operatorRole1.getRole().getName())
					.compareTo(messageProvider.get(operatorRole2.getRole().getName())));

			operatorRoles = new ListDataModel<>(operatorRoleList);
		}
	}

	public void setSelectedOperator(final Operator selectedOperator) {
		this.selectedOperator = selectedOperator;
	}

	public Operator getSelectedOperator() {
		return selectedOperator;
	}

	public ListDataModel<Operator> getOperators() {
		return operators;
	}

	public ListDataModel<OperatorRole> getOperatorRoles() {
		return operatorRoles;
	}
	
	public String getAccessPathName() {
		return accessPathName;
	}
}
