package no.valg.eva.admin.frontend.electoralroll.ctrls;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.common.configuration.service.AreaImportChangesService;
import no.valg.eva.admin.frontend.BaseController;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;

@Named
@ViewScoped
public class ImportAreaHierarchyChangesController extends BaseController {

	// Injected
	private AreaImportChangesService areaImportChangesService;
	private UserData userData;

	private byte[] importFile;
	private byte[] response;

	public ImportAreaHierarchyChangesController() {
	}

	@Inject
	public ImportAreaHierarchyChangesController(UserData userData, AreaImportChangesService areaImportChangesService) {
		this.userData = userData;
		this.areaImportChangesService = areaImportChangesService;
	}

	public void fileUpload(FileUploadEvent fileUploadEvent) throws Exception {
		importFile = IOUtil.getBytes(fileUploadEvent.getFile().getInputstream());
		FacesUtil.executeJS("PF('confirmationWidget').show()");
	}

	public void importUploadedFile() {
		response = null;
		if (importFile == null) {
			MessageUtil.buildDetailMessage("@config.area.import_error", FacesMessage.SEVERITY_ERROR);
			return;
		}
		execute(() -> {
			try {
				response = areaImportChangesService.importAreaHierarchyChanges(userData, importFile);
			} catch (IOException e) {
				throw new EvoteException(e.getMessage());
			}
			importFile = null;
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@config.area.import_success", null, FacesMessage.SEVERITY_INFO);
		});

	}

	public DefaultStreamedContent getUploadResponse() {
		if (response == null) {
			return null;
		}
		String contentType = "text/plain";
		return new DefaultStreamedContent(new ByteArrayInputStream(response), contentType, "importEndringer.txt");
	}
}
