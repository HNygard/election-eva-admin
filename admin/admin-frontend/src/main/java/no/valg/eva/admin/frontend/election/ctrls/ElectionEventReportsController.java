package no.valg.eva.admin.frontend.election.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rapport.service.RapportService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.reports.ReportMenuBuilder;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

@Named
@ViewScoped
public class ElectionEventReportsController extends BaseController {

	// Injected
	private UserDataController userDataController;
	private RapportService rapportService;

	private List<ValghendelsesRapport> reports;
	private ReportMenuBuilder builder;

	public ElectionEventReportsController() {
		// CDI
	}

	@Inject
	public ElectionEventReportsController(UserDataController userDataController, RapportService rapportService) {
		this.userDataController = userDataController;
		this.rapportService = rapportService;
	}

	@PostConstruct
	public void init() {
		reports = rapportService.rapporterForValghendelse(getUserData(), getUserData().getOperatorMvElection().getElectionEvent().electionPath());
		builder = new ReportMenuBuilder(userDataController, reports);
	}

	public void saveReports() {
		if (execute(() -> rapportService.lagre(getUserData(), getUserData().getOperatorMvElection().getElectionEvent().electionPath(), reports))) {
			MessageUtil.buildDetailMessage("@election.event.reports.saved", SEVERITY_INFO);
		}
	}

	public List<Menu> getMenus() {
		return builder.getMenus();
	}

	private UserData getUserData() {
		return userDataController.getUserData();
	}
}
