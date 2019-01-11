package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.model.views.VoterAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.BaseController;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class LastNedHistorikkController extends BaseController {

	// Inject
	private UserData userData;
	private VoterAuditService voterAuditService;

	private List<VoterAudit> manntallshistorikk = new ArrayList<>();

	public LastNedHistorikkController() {
		// For CDI
	}

	@Inject
	public LastNedHistorikkController(UserData userData, VoterAuditService voterAuditService) {
		this.userData = userData;
		this.voterAuditService = voterAuditService;
	}

	public void lastManntallsHistorikk(Voter velger) {
		if (velger != null) {
			manntallshistorikk = voterAuditService.getHistoryForVoter(userData, velger.getPk());
		}
	}

	public List<VoterAudit> getManntallshistorikk() {
		return manntallshistorikk;
	}
}
