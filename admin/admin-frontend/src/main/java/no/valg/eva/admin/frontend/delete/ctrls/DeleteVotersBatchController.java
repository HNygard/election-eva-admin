package no.valg.eva.admin.frontend.delete.ctrls;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.DELETE_VOTERS;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

@Named
@ViewScoped
public class DeleteVotersBatchController extends BaseController {

	@Inject
	private BatchService batchService;
	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;

	private List<Batch> batches;

	@PostConstruct
	public void init() {
		batches = batchService.listBatchesByEventAndCategory(userData, DELETE_VOTERS, userDataController.getElectionEvent().getId());
	}

	public List<Batch> getBatches() {
		return batches;
	}

}
