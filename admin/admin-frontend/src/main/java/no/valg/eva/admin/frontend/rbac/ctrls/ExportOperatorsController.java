package no.valg.eva.admin.frontend.rbac.ctrls;

import static no.valg.eva.admin.common.rbac.OperatorExportFormat.ALLE_ROLLER;
import static no.valg.eva.admin.common.rbac.OperatorExportFormat.VALGANSVARLIG_CIM;
import static no.valg.eva.admin.common.rbac.OperatorExportFormat.VALGANSVARLIG_EVA;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.valg.eva.admin.common.rbac.OperatorExportFormat;
import no.valg.eva.admin.frontend.BaseController;

import org.apache.log4j.Logger;

@Named
@ViewScoped
public class ExportOperatorsController extends BaseController {

	private static final Logger LOGGER = Logger.getLogger(ExportOperatorsController.class);

	private OperatorExportFormat format = ALLE_ROLLER;
	
	// Injected
	private UserData userData;
	private OperatorRoleService operatorRoleService;

	public ExportOperatorsController() {
		// CDI
	}

	@Inject
	public ExportOperatorsController(UserData userData, OperatorRoleService operatorRoleService) {
		this.userData = userData;
		this.operatorRoleService = operatorRoleService;
	}

	public String getFormat() {
		return format.getId();
	}
	
	public void setFormat(String format) {
		this.format = OperatorExportFormat.fromId(format);
	}

	public void export(boolean valgansvarlige) {
		execute(() -> {
			try {
				if (valgansvarlige && format == VALGANSVARLIG_EVA) {
					FacesUtil.sendFile("valgansvarlige.xlsx", operatorRoleService.exportOperatorRoles(userData, userData.getElectionEventPk(), VALGANSVARLIG_EVA));
				} else if (valgansvarlige && format == VALGANSVARLIG_CIM) {
					FacesUtil.sendFile("valgansvarlige.csv", operatorRoleService.exportOperatorRoles(userData, userData.getElectionEventPk(), VALGANSVARLIG_CIM));
				} else {
					FacesUtil.sendFile("operators.xlsx", operatorRoleService.exportOperatorRoles(userData, userData.getElectionEventPk(), ALLE_ROLLER));
				}
			} catch (IOException e) {
				String md5 = ErrorPageRenderer.md5(e.getMessage());
				LOGGER.warn("Operator export failed #" + md5, e);
				MessageUtil.buildDetailMessage("@rbac.import_export.export_operators.ioexception", new String[] { md5 }, FacesMessage.SEVERITY_ERROR);
			}
		});
	}
}
