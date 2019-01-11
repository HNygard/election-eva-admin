package no.valg.eva.admin.frontend.electoralroll.ctrls;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.AreaImportService;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.frontend.BaseController;

import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class ImportAreaHierarchyController extends BaseController {

	// Injected
	private UserData userData;
	private AreaImportService areaImportService;

	private byte[] importFile;

	public ImportAreaHierarchyController() {
	}

	@Inject
	public ImportAreaHierarchyController(UserData userData, AreaImportService areaImportService) {
		this.userData = userData;
		this.areaImportService = areaImportService;
	}

	public void fileUpload(FileUploadEvent fileUploadEvent) throws Exception {
		importFile = IOUtil.getBytes(fileUploadEvent.getFile().getInputstream());
		FacesUtil.executeJS("PF('confirmationWidget').show()");
	}

	public void importUploadedFile() throws IOException {
		if (importFile == null) {
			MessageUtil.buildDetailMessage("@config.area.import_error", FacesMessage.SEVERITY_ERROR);
			return;
		}
		execute(() -> {
			try {
				areaImportService.importAreaHierarchy(userData, importFile);
			} catch (IOException e) {
				throw new EvoteException(e.getMessage());
			}
			MessageUtil.buildDetailMessage("@config.area.import_success", FacesMessage.SEVERITY_INFO);
		});
	}

	@Override
	protected String oldConstraintViolationCheck(String constraintName) {
		if (constraintName.startsWith("fk_voting_x_")) {
			return "@config.area.import_error.voting";
		} else if (constraintName.startsWith("fk_vote_count_x_")) {
			return "@config.area.import_error.vote_count";
		} else if (constraintName.startsWith("fk_voter_x_")) {
			return "@config.area.import_error.voter";
		}
		return super.oldConstraintViolationCheck(constraintName);
	}
}
