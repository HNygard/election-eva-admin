package no.valg.eva.admin.frontend.electoralroll.ctrls;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.model.Batch;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.evote.service.configuration.ImportElectoralRollService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

@Named
@ViewScoped
public class ImportElectoralRollController extends BaseController {

	private UserData userData;
	private UserDataController userDataController;
	private BatchService batchService;
	private ImportElectoralRollService importElectoralRollService;

	private String filePath = "/";
	private Boolean finalImport;
	private List<Batch> batches;

	public ImportElectoralRollController() {
	}

	@Inject
	public ImportElectoralRollController(UserData userData, UserDataController userDataController, BatchService batchService,
			ImportElectoralRollService importElectoralRollService) {
		this.userData = userData;
		this.userDataController = userDataController;
		this.batchService = batchService;
		this.importElectoralRollService = importElectoralRollService;
	}

	@PostConstruct
	public void init() {
		batches = batchService.listBatchesByEventAndCategory(userData, ELECTORAL_ROLL, userDataController.getElectionEvent().getId());
		filePath = "/";
		finalImport = null;
	}

	public void importElectoralRoll() {
		if (finalImport == null) {
			throw new IllegalArgumentException("Final import is 'null'");
		}
		execute(() -> {
			importElectoralRollService.validateImportFile(userData, userDataController.getElectionEvent(), filePath);
			if (finalImport) {
				importElectoralRollService.finalFullImportElectoralRoll(userData, userDataController.getElectionEvent(), filePath);
			} else {
				importElectoralRollService.preliminaryFullImportElectoralRoll(userData, userDataController.getElectionEvent(), filePath);
			}
			MessageUtil.buildDetailMessage(getFacesContext(), "@electoralRoll.importElectoralRoll.finished", FacesMessage.SEVERITY_INFO);
			init();
		});
	}

	public List<Batch> getBatches() {
		return batches;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	public Boolean getFinalImport() {
		return finalImport;
	}

	public void setFinalImport(Boolean isFinalImport) {
		this.finalImport = isFinalImport;
	}

}
