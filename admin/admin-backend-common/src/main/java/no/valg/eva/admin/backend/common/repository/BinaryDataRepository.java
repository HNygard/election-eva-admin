package no.valg.eva.admin.backend.common.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.evote.model.BinaryData;
import no.evote.security.UserData;

@Default
@ApplicationScoped
public class BinaryDataRepository extends BaseRepository {
	public BinaryDataRepository() {
	}

	public BinaryDataRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public BinaryData createBinaryData(UserData userData, BinaryData binaryData) {
		return createEntity(userData, binaryData);
	}
	
	public BinaryData createBinaryData(BinaryData binaryData) {
		getEm().persist(binaryData);
		getEm().flush();
		getEm().refresh(binaryData);
		return binaryData;
	}

	public BinaryData findBinaryDataByPk(Long pk) {
		return findEntityByPk(BinaryData.class, pk);
	}

	public void deleteBinaryData(UserData userData, Long pk) {
		deleteEntity(userData, BinaryData.class, pk);
	}
}
