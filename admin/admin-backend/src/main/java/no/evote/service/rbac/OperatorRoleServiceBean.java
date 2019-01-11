package no.evote.service.rbac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.AreaAndElectionLevelVerifier;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class OperatorRoleServiceBean {
	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private OperatorRoleRepository operatorRoleRepository;

	public List<OperatorRole> getOperatorRoles(Operator operator) {
		return operatorRoleRepository.getOperatorRoles(operator);
	}

	public OperatorRole create(UserData userData, OperatorRole operatorRole) {
		verifyAreaAndElectionLevels(userData, operatorRole);
		return operatorRoleRepository.create(userData, operatorRole);
	}

	/**
	 * Verify that the role that is created or modified is at a level equal to, or below the users own level.
	 */
	protected void verifyAreaAndElectionLevels(UserData userData, OperatorRole operatorRole) {
		AreaAndElectionLevelVerifier.getInstance().verifyAreaAndElectionLevels(userData, operatorRole);
	}

	public Map<ElectionEvent, List<OperatorRole>> getOperatorRolesPerElectionEvent(UserData userData) {
		Map<ElectionEvent, List<OperatorRole>> operatorRolePerElectionEvent = new HashMap<>();
		for (Operator operator : operatorRepository.findOperatorsById(userData.getUid())) {
			operatorRolePerElectionEvent.put(operator.getElectionEvent(), getOperatorRoles(operator));
		}

		return operatorRolePerElectionEvent;
	}

	public void setOperatorRepository(OperatorRepository operatorRepository) {
		this.operatorRepository = operatorRepository;
	}
}
