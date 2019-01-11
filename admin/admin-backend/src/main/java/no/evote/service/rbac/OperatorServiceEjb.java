package no.evote.service.rbac;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.OperatorRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Tilganger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "OperatorService")
@Remote(OperatorService.class)
public class OperatorServiceEjb implements OperatorService {
	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private AccessRepository accessRepository;

	@Override
	@SecurityNone
	public List<Operator> findOperatorsById(String id) {
		return operatorRepository.findOperatorsById(id);
	}

	@Override
	@SecurityNone
	public boolean hasOperator(String uid) {
		return operatorRepository.hasOperator(uid);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Tilganger, type = READ)
	public List<Operator> findOperatorsWithAccess(UserData userData, MvArea mvArea, Access access) {
		no.valg.eva.admin.rbac.domain.model.Access accessEntity = accessRepository.findAccessByPath(access.getPath());
		return operatorRepository.findOperatorsWithAccess(userData, mvArea, accessEntity);
	}

}
