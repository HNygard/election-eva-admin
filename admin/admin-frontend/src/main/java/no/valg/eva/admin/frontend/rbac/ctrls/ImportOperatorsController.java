package no.valg.eva.admin.frontend.rbac.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.common.rbac.ImportOperatorMessage;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class ImportOperatorsController extends BaseController {

	// Injected
	private MessageProvider messageProvider;
	private UserData userData;
	private OperatorRoleService operatorRoleService;

	public ImportOperatorsController() {
		// CDI
	}

	@Inject
	public ImportOperatorsController(UserData userData, MessageProvider messageProvider,
			OperatorRoleService operatorRoleService) {
		this.userData = userData;
		this.messageProvider = messageProvider;
		this.operatorRoleService = operatorRoleService;
	}

	public void fileUpload(FileUploadEvent fileUploadEvent) throws Exception {
		byte[] importFile = IOUtil.getBytes(fileUploadEvent.getFile().getInputstream());
		execute(() -> {
			List<ImportOperatorMessage> userMessages = operatorRoleService.importOperatorRoles(userData, userData.getElectionEventPk(), importFile);
			handleUserMessages(userMessages);
		});
	}

	private void handleUserMessages(List<ImportOperatorMessage> userMessages) {
		if (userMessages.isEmpty()) {
			MessageUtil.buildDetailMessage("@rbac.import_export.users_imported", SEVERITY_INFO);
		} else {
			MessageUtil.buildDetailMessage("@rbac.import_export.users_imported_msgs", SEVERITY_INFO);
			for (ImportOperatorMessage msg : userMessages) {
				String lineMessage = messageProvider.get("@rbac.import_operators.line_num", new Object[] { msg.line() });
				String message = messageProvider.get(msg.getMessage(), msg.getArgs());
				MessageUtil.buildDetailMessage(lineMessage + ": " + message, SEVERITY_INFO);
			}
		}
	}

}
