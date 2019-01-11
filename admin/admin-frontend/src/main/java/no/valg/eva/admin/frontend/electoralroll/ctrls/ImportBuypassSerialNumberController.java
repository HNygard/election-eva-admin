package no.valg.eva.admin.frontend.electoralroll.ctrls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class ImportBuypassSerialNumberController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(ImportBuypassSerialNumberController.class);

	// Injected
	private UserData userData;
	private AdminOperatorService adminOperatorService;

	private byte[] importFile;
	private List<BuypassOperator> operators = new ArrayList<>();

	public ImportBuypassSerialNumberController() {
	}

	@Inject
	public ImportBuypassSerialNumberController(UserData userData, AdminOperatorService adminOperatorService) {
		this.userData = userData;
		this.adminOperatorService = adminOperatorService;
	}

	public void fileUpload(FileUploadEvent fileUploadEvent) throws Exception {
		importFile = IOUtil.getBytes(fileUploadEvent.getFile().getInputstream());
		FacesUtil.executeJS("PF('confirmationWidget').show()");
	}

	public void importUploadedFile() throws IOException {
		if (importFile == null) {
			MessageUtil.buildDetailMessage("@config.operator.import_buypass_number_missing_file", FacesMessage.SEVERITY_ERROR);
			return;
		}
		try {
			operators = adminOperatorService.updateBuypassKeySerialNumbers(userData, importFile);
			MessageUtil.buildDetailMessage("@config.operator.import_buypass_number_success", FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			MessageUtil.buildDetailMessage("@config.operator.import_buypass_number_error", FacesMessage.SEVERITY_FATAL);
			LOGGER.error("Feil i import av buypassserienummer", e);
		}
	}

	public List<BuypassOperator> getOperators() {
		return operators;
	}
}
