package no.valg.eva.admin.frontend.rbac.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.rbac.RoleService;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.frontend.BaseController;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class ExportImportRolesController extends BaseController {

	private static final Logger LOGGER = Logger.getLogger(ExportImportRolesController.class);

	// Injected
	private RoleService roleService;
	private UserData userData;

	private boolean deleteExistingRoles = false;

	public ExportImportRolesController() {
		// CDI
	}

	@Inject
	public ExportImportRolesController(UserData userData, RoleService roleService) {
		this.userData = userData;
		this.roleService = roleService;
	}

	public void exportRoles() {
		execute(() -> {
			try {
				FacesUtil.sendFile(String.format("roles-%s.txt", userData.getOperatorRole().getMvElection().getElectionEventId()),
						roleService.exportRoles(userData, true).getBytes("UTF-8"));
			} catch (IOException e) {
				String md5 = ErrorPageRenderer.md5(e.getMessage());
				LOGGER.warn("Role export failed #" + md5, e);
				MessageUtil.buildDetailMessage("@rbac.import_export.export_operators.ioexception", new String[] { md5 }, FacesMessage.SEVERITY_ERROR);
			}
		});
	}

	public void fileUpload(FileUploadEvent fileUploadEvent) throws Exception {
		String fileContent = new String(IOUtil.getBytes(fileUploadEvent.getFile().getInputstream()), "UTF-8");
		execute(() -> {
			int numberOfRolesImported = roleService.importRoles(userData, fileContent, isDeleteExistingRoles());
			MessageUtil.buildDetailMessage("@rbac.roles.imported", new String[] { String.valueOf(numberOfRolesImported) }, SEVERITY_INFO);
		});
	}

	public boolean isDeleteExistingRoles() {
		return deleteExistingRoles;
	}

	public void setDeleteExistingRoles(final boolean deleteExistingRoles) {
		this.deleteExistingRoles = deleteExistingRoles;
	}
}
