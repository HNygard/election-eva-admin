package no.valg.eva.admin.frontend.electoralroll.ctrls;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VOTER_NUMBER;

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
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

@Named
@ViewScoped
public class VoterNumberController extends BaseController {

	// Injected
	private UserData userData;
	private UserDataController userDataController;
	private ManntallsnummerService manntallsnummerService;
	private BatchService batchService;

	private ManntallsnummergenereringStatus status;
	private List<Batch> batches;

	@SuppressWarnings("unused")
	public VoterNumberController() {
		// For CDI
	}
	
	@Inject
	public VoterNumberController(UserData userData, UserDataController userDataController,
								 ManntallsnummerService manntallsnummerService, BatchService batchService) {
		this.userData = userData;
		this.userDataController = userDataController;
		this.manntallsnummerService = manntallsnummerService;
		this.batchService = batchService;
	}

	@PostConstruct
	public void init() {
		loadData();
		checkStatus(status);
	}

	public void preRenderView() {
		// Get me started
	}

	public void generateVoterNumbers() {
		execute(() -> {
			manntallsnummerService.genererManntallsnumre(userData, userDataController.getElectionEvent().getPk());
			String[] params = { userDataController.getElectionEvent().getName() };
			MessageUtil.buildDetailMessage("@electoralRoll.generateVoterNumber.wasGenerated", params, FacesMessage.SEVERITY_INFO);
			loadData();
		});
	}

	public boolean isStatusOk() {
		return status == ManntallsnummergenereringStatus.OK;
	}

	public List<Batch> getBatches() {
		return batches;
	}

	private void checkStatus(ManntallsnummergenereringStatus status) {
		MessageUtil.clearMessages();
		switch (status) {
		case VALGHENDELSE_LAAST:
			MessageUtil.buildDetailMessage("@electoralRoll.generateVoterNumber.eventDisabled", FacesMessage.SEVERITY_WARN);
			break;
		case INGEN_VELGERE:
			MessageUtil.buildDetailMessage("@electoralRoll.generateVoterNumber.noVoters", FacesMessage.SEVERITY_WARN);
			break;
		case SKJARINGSDATO_I_FREMTIDEN:
			MessageUtil.buildDetailMessage("@electoralRoll.generateVoterNumber.cutOffAfterToday", FacesMessage.SEVERITY_WARN);
			break;
		case ALLEREDE_GENERERT:
			String[] params = { userDataController.getElectionEvent().getName() };
			MessageUtil.buildDetailMessage("@electoralRoll.generateVoterNumber.wasNotGenerated", params, FacesMessage.SEVERITY_WARN);
			break;
		default:
			break;
		}
	}

	private void loadData() {
		status = manntallsnummerService.hentManntallsnummergenereringStatus(userData, userDataController.getElectionEvent());
		batches = batchService.listBatchesByEventAndCategory(userData, VOTER_NUMBER, userDataController.getElectionEvent().getId());
	}
}
