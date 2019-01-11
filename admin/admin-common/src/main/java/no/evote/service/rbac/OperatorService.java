package no.evote.service.rbac;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface OperatorService extends Serializable {

	List<Operator> findOperatorsById(String id);

	List<Operator> findOperatorsWithAccess(UserData userData, MvArea mvArea, Access data);

	/**
	 * @return true if operator for uid exists
	 */
	boolean hasOperator(String uid);
}
