package no.valg.eva.admin.frontend.electoralroll.ctrls;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VALGKORTUNDERLAG;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.evote.model.Batch;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.valg.eva.admin.common.configuration.service.ValgkortgrunnlagService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

@Named
@ViewScoped
public class GenererValgkortgrunnlagController extends BaseController {

	// Injected
	private UserData userData;
	private UserDataController userDataController;
	private ValgkortgrunnlagService valgkortgrunnlagService;
	private BatchService batchService;

	private GenererValgkortgrunnlagStatus status;
	private boolean tillatVelgereIkkeTilknyttetValgdistrikt = false;
	private List<Batch> batches;

	public GenererValgkortgrunnlagController() {
		// For CDI(?)
	}

	@Inject
	public GenererValgkortgrunnlagController(UserData userData, UserDataController userDataController, BatchService batchService,
											 ValgkortgrunnlagService valgkortgrunnlagService) {
		this.userData = userData;
		this.userDataController = userDataController;
		this.valgkortgrunnlagService = valgkortgrunnlagService;
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

	public void genererValgkortgrunnlag() {
		execute(() -> {
			valgkortgrunnlagService.genererValgkortgrunnlag(userData, tillatVelgereIkkeTilknyttetValgdistrikt);
			String[] params = { userDataController.getElectionEvent().getName() };
			MessageUtil.buildDetailMessage("@electoralRoll.genererValgkortgrunnlag.startet", params, FacesMessage.SEVERITY_INFO);
			loadData();
		});
	}

	public boolean isStatusOk() {
		return status == GenererValgkortgrunnlagStatus.OK;
	}

	public List<Batch> getListeOverGenereringsjobber() {
		return batches;
	}

	private void checkStatus(GenererValgkortgrunnlagStatus status) {
		MessageUtil.clearMessages();
		switch (status) {
			case TOMT_MANNTALL:
				MessageUtil.buildDetailMessage("@electoralRoll.genererValgkortgrunnlag.tomtManntall", FacesMessage.SEVERITY_WARN);
				break;
			case MANNTALLSNUMRE_MANGLER:
				MessageUtil.buildDetailMessage("@electoralRoll.genererValgkortgrunnlag.manglerManntallsnumre", FacesMessage.SEVERITY_WARN);
				break;
			default:
				break;
		}
	}

	private void loadData() {
		status = valgkortgrunnlagService.sjekkForutsetningerForGenerering(userData);
		batches = batchService.listBatchesByEventAndCategory(userData, VALGKORTUNDERLAG, userDataController.getElectionEvent().getId());
	}

	public boolean isTillatVelgereIkkeTilknyttetValgdistrikt() {
		return tillatVelgereIkkeTilknyttetValgdistrikt;
	}

	public void setTillatVelgereIkkeTilknyttetValgdistrikt(boolean tillatVelgereIkkeTilknyttetValgdistrikt) {
		this.tillatVelgereIkkeTilknyttetValgdistrikt = tillatVelgereIkkeTilknyttetValgdistrikt;
	}
}
