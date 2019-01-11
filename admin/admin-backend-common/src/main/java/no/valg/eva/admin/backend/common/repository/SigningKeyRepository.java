package no.valg.eva.admin.backend.common.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import no.evote.model.KeyDomain;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.SigningKeyData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public class SigningKeyRepository extends BaseRepository {
	public SigningKeyRepository() {
	}

	public SigningKeyRepository(EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Returns a list of all election events with all key domains for it and a signing key if there is one
	 */
	@SuppressWarnings("unchecked")
	public List<SigningKeyData> findAllSigningKeys() {
		String sqlQuery = "SELECT ev.election_event_name, CAST(ev.election_event_id as text), kd.key_domain_name, kd.key_domain_id, sk.signing_key_pk "
				+ "FROM admin.key_domain kd "
				+ "CROSS JOIN admin.election_event ev " + "LEFT JOIN admin.signing_key sk "
				+ "	ON sk.election_event_pk = ev.election_event_pk "
				+ "	AND sk.key_domain_pk = kd.key_domain_pk "
				+ "WHERE kd.system_wide_key = CASE WHEN ev.election_event_id = '000000' THEN true ELSE false END "
				+ "ORDER BY ev.election_event_id, kd.key_domain_id";
		Query query = getEm().createNativeQuery(sqlQuery);
		List<Object[]> result = query.getResultList();

		List<SigningKeyData> rows = new ArrayList<>();

		
		for (Object[] array : result) {
			SigningKeyData row = new SigningKeyData((String) array[1], (String) array[0], (String) array[3], (String) array[2]);
			if (array[4] != null) {
				SigningKey signingKey = findByPk(Long.valueOf((Integer) array[4]));
				row.setSigningKeyPk(signingKey.getPk());
				if (signingKey.getBinaryData() != null) {
					row.setFileName(signingKey.getBinaryData().getFileName());
				}
			}
			rows.add(row);
		}
		
		return rows;
	}

	public SigningKey findByPk(Long pk) {
		return super.findEntityByPk(SigningKey.class, pk);
	}

	public SigningKey update(UserData userData, SigningKey signingKey) {
		return super.updateEntity(userData, signingKey);
	}

	public KeyDomain findKeyDomainById(String keyDomainId) {
		return super.findEntityById(KeyDomain.class, keyDomainId);
	}

	public SigningKey getSigningKeyForElectionEventSigning(Long electionEventPk) {
		try {
			return getEm()
					.createNamedQuery("SigningKey.getSigningKeyForElectionEventSigning", SigningKey.class)
					.setParameter("electionEventPk", electionEventPk)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<SigningKey> getAllSigningKeyForElectionEventSigning() {
		try {
			return getEm()
				.createNamedQuery("SigningKey.getAllSigningKeys", SigningKey.class)
				.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	public SigningKey getScanningCountVerificationSigningKey() {
		try {
			return getEm().createNamedQuery("SigningKey.getScanningCountVerificationSigningKey", SigningKey.class).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean isSigningKeySetForElectionEvent(ElectionEvent electionEvent) {
		Query q = getEm().createNativeQuery("select count(*) > 0 from signing_key where election_event_pk = ?1 and key_binary_data_pk is not null");
		q.setParameter(1, electionEvent.getPk());
		return (Boolean) q.getSingleResult();
	}
}
