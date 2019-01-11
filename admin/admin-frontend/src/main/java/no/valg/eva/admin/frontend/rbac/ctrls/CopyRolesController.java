package no.valg.eva.admin.frontend.rbac.ctrls;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.frontend.BaseController;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;

@Named
@ViewScoped
public class CopyRolesController extends BaseController {

	/// Injected
	private UserData userData;
	private ElectionEventService electionEventService;

	private List<ElectionEvent> electionEvents;
	private long fromElectionEventPk;
	private long toElectionEventPk;

	public CopyRolesController() {
		// CDI
	}

	@Inject
	public CopyRolesController(UserData userData, ElectionEventService electionEventService) {
		this.userData = userData;
		this.electionEventService = electionEventService;
	}

	@PostConstruct
	public void init() {
		electionEvents = electionEventService.findAll(userData);
		electionEvents.sort(Comparator.comparing(ElectionEvent::getId));
	}

	public void copyRoles() {
		if (fromElectionEventPk == toElectionEventPk) {
			MessageUtil.buildDetailMessage("@config.copy_roles.error", FacesMessage.SEVERITY_ERROR);
		} else {
			execute(() -> {
				ElectionEvent fromElectionEvent = electionEventService.findByPk(fromElectionEventPk);
				ElectionEvent toElectionEvent = electionEventService.findByPk(toElectionEventPk);
				electionEventService.copyRoles(userData, fromElectionEvent, toElectionEvent);
				MessageUtil.buildDetailMessage("@config.copy_roles.ok", FacesMessage.SEVERITY_INFO);
			});
		}
	}

	public void setElectionEvents(List<ElectionEvent> electionEvents) {
		this.electionEvents = electionEvents;
	}

	public List<ElectionEvent> getElectionEvents() {
		return electionEvents;
	}

	public void setFromElectionEventPk(long fromElectionEventPk) {
		this.fromElectionEventPk = fromElectionEventPk;
	}

	public long getFromElectionEventPk() {
		return fromElectionEventPk;
	}

	public void setToElectionEventPk(long toElectionEventPk) {
		this.toElectionEventPk = toElectionEventPk;
	}

	public long getToElectionEventPk() {
		return toElectionEventPk;
	}
}
