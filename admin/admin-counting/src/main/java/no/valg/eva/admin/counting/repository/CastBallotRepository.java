package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;

@Default
@ApplicationScoped
public class CastBallotRepository extends BaseRepository {
	public CastBallotRepository() {
	}

	public CastBallotRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public CastBallot create(UserData userData, CastBallot castBallot) {
		return super.createEntity(userData, castBallot);
	}

	public CastBallot update(UserData userData, CastBallot castBallot) {
		return super.updateEntity(userData, castBallot);
	}

	public List<CastBallot> findCastBallotsByBallotCount(Long pk) {
		return getEm()
				.createNamedQuery("CastBallot.getByBallotCount", CastBallot.class)
				.setParameter("bcpk", pk)
				.getResultList();
	}

	public CastBallot findCastBallotByBallotCountAndId(Long pk, String ballotId) {
		try {
			return getEm()
					.createNamedQuery("CastBallot.getByBallotCountAndId", CastBallot.class)
					.setParameter("bcpk", pk)
					.setParameter("id", ballotId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<CandidateVote> findCandidateVoteByCastBallot(Long pk) {
		return getEm()
				.createNamedQuery("CandidateVote.getByCastBallot", CandidateVote.class)
				.setParameter("cvpk", pk)
				.getResultList();
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, CastBallot.class, pk);
	}
}
