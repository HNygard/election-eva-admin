package no.valg.eva.admin.rbac.application;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.OperatorMapper;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;

/**
 * Converts between domain model and view/API model.
 */
public final class OperatorViewToDomainMapper extends OperatorMapper {

	private MvAreaRepository mvAreaRepository;
	private RoleRepository roleRepository;

	private OperatorViewToDomainMapper(MvAreaRepository mvAreaRepository, RoleRepository roleRepository) {
		requireNonNull(mvAreaRepository);
		requireNonNull(roleRepository);

		this.mvAreaRepository = mvAreaRepository;
		this.roleRepository = roleRepository;
	}

	/**
	 * Mapping from view/API model to domain model requires repositories.
	 */
	public static OperatorViewToDomainMapper getInstance(MvAreaRepository mvAreaRepository, RoleRepository roleRepository) {
		return new OperatorViewToDomainMapper(mvAreaRepository, roleRepository);
	}

	static List<Operator> toOperatorWithRoleAssociations(List<OperatorRole> operatorsRoles) {
		Map<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> operatorToOperatorRoleMap = mapWithOperatorAsKey(operatorsRoles);
		List<Operator> operatorList = new ArrayList<>(operatorToOperatorRoleMap.keySet().size());

		for (Map.Entry<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> entry : operatorToOperatorRoleMap.entrySet()) {

			Operator operatorWithRoleAssociations = PersonMapper.toOperator(entry.getKey());
			for (OperatorRole operatorRole : entry.getValue()) {
				operatorWithRoleAssociations.addRoleAssociation(toRoleAssociation(operatorRole));
			}
			operatorList.add(operatorWithRoleAssociations);
		}
		return operatorList;
	}

	private static Map<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> mapWithOperatorAsKey(List<OperatorRole> operatorsRoles) {
		HashMap<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> operatorToOperatorRoleMap = new HashMap<>(operatorsRoles.size());
		for (OperatorRole operatorRole : operatorsRoles) {
			addOperatorRoleToOperator(operatorToOperatorRoleMap, operatorRole);
		}
		return operatorToOperatorRoleMap;
	}

	private static void addOperatorRoleToOperator(Map<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> operatorToOperatorRoleMap,
			OperatorRole operatorRole) {
		List<OperatorRole> operatorRoleList = getOperatorRoleListForOperator(operatorToOperatorRoleMap, operatorRole);
		operatorRoleList.add(operatorRole);
	}

	private static List<OperatorRole> getOperatorRoleListForOperator(
			Map<no.valg.eva.admin.rbac.domain.model.Operator, List<OperatorRole>> operatorToOperatorRoleMap,
			OperatorRole operatorRole) {
		no.valg.eva.admin.rbac.domain.model.Operator key = operatorRole.getOperator();
		requireNonNull(key, "OperatorRole association is missing Operator");

		List<OperatorRole> operatorRoleList = operatorToOperatorRoleMap.get(key);

		if (operatorRoleList == null) {
			operatorRoleList = new ArrayList<>();
			operatorToOperatorRoleMap.put(key, operatorRoleList);
		}

		return operatorRoleList;
	}

	public OperatorRole toOperatorRoleForOperator(RoleAssociation roleAssociation, no.valg.eva.admin.rbac.domain.model.Operator operator,
			MvElection mvElection) {
		MvArea mvArea = mvAreaRepository.findSingleByPath(roleAssociation.getArea().getAreaPath());
		ElectionEvent electionEvent = mvArea.getElectionEvent();
		Role role = roleRepository.findByElectionEventAndId(electionEvent, roleAssociation.getRole().getRoleId());

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(operator);
		operatorRole.setRole(role);
		operatorRole.setMvArea(mvArea);
		operatorRole.setMvElection(mvElection);

		return operatorRole;
	}
}
