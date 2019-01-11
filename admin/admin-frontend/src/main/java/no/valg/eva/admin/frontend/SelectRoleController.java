package no.valg.eva.admin.frontend;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.evote.service.rbac.RoleService;
import no.evote.service.security.SigningKeyService;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.common.rbac.service.UserDataService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;

@Named
@ViewScoped
public class SelectRoleController extends BaseController {
	private static final String MENU = "index.xhtml";
	private static final long serialVersionUID = 5547722002660819324L;
	private final Map<Long, Boolean> electionEventSigningKeySetMap = new HashMap<>();
	@Inject
	private OperatorRoleService operatorRoleService;
	@Inject
	private SigningKeyService keyService;
	@Inject
	private ElectionEventService electionEventService;
	@Inject
	private RoleService roleService;
	@Inject
	private UserDataService userDataService;
	private List<ElectionEvent> electionEvents = null;
	private Map<Long, Integer> accumulatedSecLevelMap;
	private Map<ElectionEvent, List<OperatorRole>> operatorRolesPerElectionEvent = null;
	@Inject
	private UserData userData;
	@Inject
	private AuditLogService auditLogService;

	private boolean dataLoaded = false;

	@PostConstruct
	public void init() {
		loadData();
	}

	private void loadData() {
		if (!dataLoaded) {
			dataLoaded = true;

			operatorRolesPerElectionEvent = operatorRoleService.getOperatorRolesPerElectionEvent(userData);

			electionEvents = new ArrayList<>(operatorRolesPerElectionEvent.keySet());
			Collections.sort(electionEvents, new Comparator<ElectionEvent>() {

				@Override
				public int compare(final ElectionEvent o1, final ElectionEvent o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});

			accumulatedSecLevelMap = new HashMap<>();
			for (ElectionEvent electionEvent : electionEvents) {
				for (OperatorRole or : getOperatorRoles(electionEvent)) {
					accumulatedSecLevelMap.put(or.getRole().getPk(), roleService.getAccumulatedSecLevelFor(or.getRole()));
				}
			}
		}
	}

	public Collection<ElectionEvent> getElectionEvents() {
		return electionEvents;
	}

	public String selectRole(final OperatorRole operatorRole) {
		ElectionEvent electionEvent = electionEventService.findByPk(operatorRole.getOperator().getElectionEvent().getPk());
		if (!isSigningKeySetForElectionEventInCache(electionEvent) && (!electionEvent.getId().equals(ROOT_ELECTION_EVENT_ID))) {
			throw new EvoteSecurityException("P12 is not availible for this event");
		}

		if (userData.getSecurityLevel() != null && accumulatedSecLevelMap.get(operatorRole.getRole().getPk()) > userData.getSecurityLevel()) {
			throw new EvoteSecurityException("Your current security level is lower than the requirement for this role");
		}

		if (!operatorRole.getRole().isActive()) {
			throw new EvoteSecurityException("Your role is deactivated for this election event");
		}

		if (!operatorRole.getOperator().isActive()) {
			throw new EvoteSecurityException("Your operator is deactivated for this election event");
		}

		UserData userDataFromBackend = userDataService.setAccessCacheOnUserData(userData, operatorRole);
		userData.setOperatorRole(userDataFromBackend.getOperatorRole());
		userData.setUid(userDataFromBackend.getUid());
		userData.setAccessCache(userDataFromBackend.getAccessCache());
		userData.setClientAddress(userDataFromBackend.getClientAddress());
		userData.setLocale(userDataFromBackend.getLocale());
		userData.setSecurityLevel(userDataFromBackend.getSecurityLevel());

		auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(SimpleAuditEventType.OperatorSelectedRole).build());

		// conversation.end();
		//
		return addRedirect(MENU);
	}

	/*
	 * Instead of looking up the signing key for every operatorRole, we cache it pr. election event
	 */
	private boolean isSigningKeySetForElectionEventInCache(ElectionEvent electionEvent) {
		if (electionEventSigningKeySetMap.containsKey(electionEvent.getPk())) {
			return electionEventSigningKeySetMap.get(electionEvent.getPk());
		}
		return isSigningKeySetForElectionEvent(electionEvent);
	}

	private boolean isSigningKeySetForElectionEvent(final ElectionEvent electionEvent) {
		if (electionEvent.getId().equals(ROOT_ELECTION_EVENT_ID)) {
			electionEventSigningKeySetMap.put(electionEvent.getPk(), true);
			return true;
		}

		if (electionEventSigningKeySetMap.containsKey(electionEvent.getPk())) {
			return electionEventSigningKeySetMap.get(electionEvent.getPk());
		} else {
			boolean isSigningKeySet = keyService.isSigningKeySetForElectionEvent(userData, electionEvent);
			electionEventSigningKeySetMap.put(electionEvent.getPk(), isSigningKeySet);
			return isSigningKeySet;
		}
	}

	/*
	 * Checks that the OperatorRole can be choosen
	 * Criterias:
	 * * Signing p12 in place for election event
	 * * Security level for role lower or equal to the users
	 * * Role active
	 * * Operator active
	 */
	public boolean isOperatorRoleEnabled(final OperatorRole operatorRole) {
		if (!isSigningKeySetForElectionEvent(operatorRole.getOperator().getElectionEvent())) {
			return false;
		}

		if (userData.getSecurityLevel() != null && accumulatedSecLevelMap.get(operatorRole.getRole().getPk()) > userData.getSecurityLevel()) {
			return false;
		}

		if (!operatorRole.getRole().isActive()) {
			return false;
		}

        return operatorRole.getOperator().isActive();
    }

	public Integer getAccumulatedSecLevel(final Role r) {
		return accumulatedSecLevelMap.get(r.getPk());
	}

	public List<OperatorRole> getOperatorRoles(final ElectionEvent electionEvent) {
		return operatorRolesPerElectionEvent.get(electionEvent);
	}

	public String getAreaName(OperatorRole role) {
		MvArea area = role.getMvArea();
		if (area.getAreaLevel() <= AreaLevelEnum.COUNTY.getLevel()) {
			return getBaseAreaName(role);
		}
		if (area.getAreaLevel() <= AreaLevelEnum.BOROUGH.getLevel()) {
			return area.getAreaName();
		}
		if (area.getAreaLevel() == AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			return area.getPollingDistrictId() + " " + area.getPollingDistrictName();
		}
		return area.getPollingPlaceName();
	}

	public String getBaseAreaName(OperatorRole role) {
		MvArea area = role.getMvArea();
		if (area.getAreaLevel() == AreaLevelEnum.ROOT.getLevel()) {
			return "System";
		}
		if (area.getAreaLevel() <= AreaLevelEnum.COUNTY.getLevel()) {
			return area.getAreaName();
		}
		return area.getMunicipalityName();
	}

	public String getContestName(OperatorRole role) {
		return role.getMvElection().getContest() == null ? "" : role.getMvElection().getContest().getName();
	}

}
