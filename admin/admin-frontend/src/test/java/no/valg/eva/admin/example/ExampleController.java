package no.valg.eva.admin.example;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.evote.exception.ErrorCode;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

public class ExampleController extends ConversationScopedController implements ErrorCodeHandler {

	@Inject
	private MessageProvider messageProvider;
	@Inject
	private UserData userData;
	@Inject
	private AdminOperatorService operatorService;

	private List<Operator> operatorList;
	private boolean addToOperatorList;

	@Override
	protected void doInit() {
		addToOperatorList = "true".equals(FacesUtil.resolveExpression("#{cc.attrs.addToOperatorList}"));
		loadData();
		if (getFacesContext().isPostback()) {
			return;
		}
		if (getRequestContext().isAjaxRequest()) {
			return;
		}
	}

	@Override
	public String onError(ErrorCode errorCode, String... params) {
		if (isOptimisticLockingException(errorCode)) {
			// FEATURE: If you call process(RuntimeException,..) in a catch statement and the exception contains an ErrorCode,
			// this interface method will be called to make it possible
			// to react on the error situation. The return value should be a resolved text to be displayed in a FacesMessage.
			// Or return null if not handled!
			loadData();
			return messageProvider.get("@error.optimisticLockingException");
		}
		return null;

	}

	private void loadData() {
		operatorList = operatorService.operatorsInArea(userData, userData.getOperatorMvArea().areaPath());
	}

	public void saveOperator(final Operator operator) {
		if (execute(() -> {
			Operator updatedOperator = operatorService.updateOperator(userData, operator, userData.getOperatorMvArea().areaPath(),
					new ArrayList<>(), new ArrayList<>());
			if (addToOperatorList) {
				operatorList.add(updatedOperator);
			}
		}, this)) {
			MessageUtil.buildDetailMessage(messageProvider.get("@operator.saved"), FacesMessage.SEVERITY_INFO);
		}
	}

	public void back() {
		ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) getFacesContext().getApplication()
				.getNavigationHandler();
		configurableNavigationHandler.performNavigation("/secure/index.xhtml");
	}

	public List<Operator> getOperatorList() {
		return operatorList;
	}

	public boolean isAddToOperatorList() {
		return addToOperatorList;
	}
}
