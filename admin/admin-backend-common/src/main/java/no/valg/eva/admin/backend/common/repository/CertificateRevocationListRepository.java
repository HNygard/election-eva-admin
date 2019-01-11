package no.valg.eva.admin.backend.common.repository;

import javax.persistence.EntityManager;
import javax.security.auth.x500.X500Principal;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;

/**
 * Collection-oriented repository for CertificateRevocationList. NOTE! This is prototyping the idea of implementing repositories as collections, among other
 * things, this implies that object creation is not a responsibility of the repository thus no create method is exposed.
 */
public class CertificateRevocationListRepository extends BaseRepository {

	@SuppressWarnings("unused")
	public CertificateRevocationListRepository() {
	}

	public CertificateRevocationListRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public void add(CertificateRevocationList certificateRevocationList) {
		CertificateRevocationList existingCrl = findEntityById(CertificateRevocationList.class, certificateRevocationList.getId());
		if (existingCrl != null) {
			certificateRevocationList.setPk(existingCrl.getPk());
		}
		getSession().merge(certificateRevocationList);
	}

	public CertificateRevocationList certificateRevocationListByIssuer(X500Principal issuer) {
		return findEntityById(CertificateRevocationList.class, issuer.toString());
	}

}
