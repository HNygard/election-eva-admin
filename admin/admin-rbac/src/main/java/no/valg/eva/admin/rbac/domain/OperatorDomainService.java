package no.valg.eva.admin.rbac.domain;

import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;

/**
 * Domain service for handling domain logic related to relationship between {@link Operator} and {@link OperatorRole}.
 */
@Default
@ApplicationScoped
public class OperatorDomainService {

	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private OperatorRoleRepository operatorRoleRepository;

	public OperatorDomainService() {
	}

	public Operator operatorByElectionEventAndId(ElectionEvent electionEvent, PersonId operatorId) {
		Operator operator = operatorRepository.findByElectionEventsAndId(electionEvent.getPk(), operatorId.getId());
		if (operator == null) {
			throw new EvoteException("Operator is unknown: " + operatorId);
		}
		return operator;
	}

	/**
	 * Finds all role assignments for an Operator in a selected area.
	 * <p/>
	 * If the user invoking this operation can see below his/hers area level, then all role associations at and below the area is returned. If the user does not
	 * have this access, only the operators at the specified area is returned. This may be used to restrict view for county users, while allowing municipality
	 * users to see users at area levels below the municipality.
	 */
	public List<OperatorRole> findAllOperatorRolesForOperatorInArea(Operator operator, MvArea mvArea, UserData userData) {
		if (isTilgangBrukereAdministrereBrukereUnderliggendeNivå(userData)) {
			return operatorRoleRepository.operatorRolesForOperatorAtOrBelowMvArea(operator, mvArea);
		} else {
			return operatorRoleRepository.operatorRolesForOperatorAtOwnLevel(operator, mvArea);
		}
	}

	/**
	 * Finds all role assignments for any Operators in a selected area.
	 * <p/>
	 * If the user invoking this operation can see below his/hers area level, then all role associations at and below the area is returned. If the user does not
	 * have this access, only the operators at the specified area is returned. This may be used to restrict view for county users, while allowing municipality
	 * users to see users at area levels below the municipality.
	 * 
	 * Brukerstøtterollen skal bare med hvis området er på rot-nivå
	 */
	public List<OperatorRole> operatorRolesInArea(MvArea mvArea, UserData userData) {
		List<OperatorRole> operatorRoles = isTilgangBrukereAdministrereBrukereUnderliggendeNivå(userData)
				? operatorRoleRepository.findDescOperatorsRoles(mvArea)
				: operatorRoleRepository.operatorRolesAtArea(mvArea);

		boolean includeUserSupportOperator = AreaPath.from(mvArea.getAreaPath()).isRootLevel();
		if (!includeUserSupportOperator) {
			operatorRoles = removeUserSupportRole(operatorRoles);
		}

		return operatorRoles;
	}

	private List<OperatorRole> removeUserSupportRole(List<OperatorRole> operatorRoles) {
		List<OperatorRole> filteredOperatorRoles = new ArrayList<>();
		for (OperatorRole operatorRole : operatorRoles) {
			if (!operatorRole.getRole().isUserSupport()) {
				filteredOperatorRoles.add(operatorRole);
			}
		}
		return filteredOperatorRoles;
	}

	private boolean isTilgangBrukereAdministrereBrukereUnderliggendeNivå(UserData userData) {
		return userData.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå);
	}

	public void deleteOperatorInArea(UserData userData, Operator operator, MvArea mvArea) {
		List<OperatorRole> allOperatorRolesForOperatorInArea = findAllOperatorRolesForOperatorInArea(operator, mvArea, userData);
		operatorRoleRepository.delete(userData, allOperatorRolesForOperatorInArea);

		deleteOperatorIfNoMoreOperatorRoles(userData, operator);
	}

	public void deleteOperatorIfNoMoreOperatorRoles(UserData userData, Operator operator) {
		List<OperatorRole> remainingOperatorRoles = operatorRoleRepository.getOperatorRoles(operator);
		if (remainingOperatorRoles.isEmpty()) {
			operatorRepository.delete(userData, operator);
		}
	}

}
