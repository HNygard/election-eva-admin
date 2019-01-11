package no.evote.service.rbac;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.ImportOperatorMessage;
import no.valg.eva.admin.common.rbac.OperatorExportFormat;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface OperatorRoleService extends Serializable {

	OperatorRole create(UserData userData, OperatorRole operatorRole);

	/**
	 * Returns the number of Role objects that the Operator has.
	 */
	Long findUserCountForRole(final Long rolePk);

	List<OperatorRole> findOperatorRolesGivingOperatorAccess(UserData userData, MvArea mvArea, Operator operator, Access data);

	byte[] exportOperatorRoles(UserData userData, Long electionEventPk, OperatorExportFormat format);

	List<ImportOperatorMessage> importOperatorRoles(UserData userData, Long electionEventPk, byte[] data);

	/**
	 * Returns a map with every operatorRole bellonging to the operatorId, the roles are mapped to the election event on where theyare assigned.
	 * @param userData from where the operator id is found
	 */
	Map<ElectionEvent, List<OperatorRole>> getOperatorRolesPerElectionEvent(final UserData userData);
}
