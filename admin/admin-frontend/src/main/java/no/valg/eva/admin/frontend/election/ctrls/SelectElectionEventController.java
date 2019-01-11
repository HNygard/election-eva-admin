package no.valg.eva.admin.frontend.election.ctrls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.valg.eva.admin.common.rbac.service.AccessService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

@Named
@ViewScoped
public class SelectElectionEventController extends BaseController {
	@Inject
	private UserData userData;
	@Inject
	private OperatorRoleService operatorRoleService;
	@Inject
	private AccessService accessService;

	private List<ElectionEvent> electionEvents;
	private ElectionEvent selectedElectionEvent;
	private Map<ElectionEvent, List<OperatorRole>> operatorRolesPerElectionEvent;

	@PostConstruct
	public void init() {
		operatorRolesPerElectionEvent = operatorRoleService.getOperatorRolesPerElectionEvent(userData);
		electionEvents = new ArrayList<>(operatorRolesPerElectionEvent.keySet());

		if (electionEvents.size() == 1) {
			setOperatorRoleOnUserData(electionEvents.get(0));

			FacesUtil.redirect(nextPage(), false);
		}
	}

	public void select() {
		if (selectedElectionEvent == null) {
			throw new IllegalStateException("Action 'select' invoked before selecting an election event");
		}

		setOperatorRoleOnUserData(selectedElectionEvent);

		FacesUtil.redirect(nextPage(), false);
	}

	public List<ElectionEvent> getElectionEvents() {
		return electionEvents;
	}

	public ElectionEvent getSelectedElectionEvent() {
		return selectedElectionEvent;
	}

	public void setSelectedElectionEvent(ElectionEvent electionEvent) {
		selectedElectionEvent = electionEvent;
	}

	private void setOperatorRoleOnUserData(ElectionEvent electionEvent) {
		List<OperatorRole> operatorRolesForSelectedElectionEvent = operatorRolesPerElectionEvent.get(electionEvent);
		userData.setOperatorRole(operatorRolesForSelectedElectionEvent.get(0)); // randomly selects the first
		userData.setAccessCache(accessService.findAccessCacheFor(userData));
	}

	private String nextPage() {
		return "/secure/" + FacesUtil.resolveExpression("#{cc.attrs.nextPage}");
	}
}
