package no.evote.service;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Importer;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Se;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.common.rbac.Security;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "BinaryDataService")
@Remote(BinaryDataService.class)
public class BinaryDataServiceEjb implements BinaryDataService {
	@Inject
	private BinaryDataRepository binaryDataRepository;

	@Override
	@Security(accesses = {Opptelling_Importer, Opptelling_Rettelser_Se}, type = READ)
	public BinaryData findByPk(UserData userData, Long pk) {
		return binaryDataRepository.findBinaryDataByPk(pk);
	}
}
