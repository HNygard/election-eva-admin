package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;

@Default
@ApplicationScoped
public class BallotRejectionRepository extends BaseRepository {
	public BallotRejectionRepository() {
	}

	public BallotRejectionRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<BallotRejection> findBallotRejectionsByEarlyVoting(boolean earlyVoting) {
		TypedQuery<BallotRejection> query = getEm()
				.createNamedQuery("BallotRejection.BallotRejectionByEarlyVoting", BallotRejection.class)
				.setParameter("ev", earlyVoting);
		return query.getResultList();
	}

	public BallotRejection findBallotRejectionById(String id) {
		return super.findEntityById(BallotRejection.class, id);
	}

	public BallotRejection findBallotRejectionByPk(Long pk) {
		return super.findEntityByPk(BallotRejection.class, pk);
	}

	public List<BallotRejection> findAll() {
		return getEm().createNamedQuery("BallotRejection.findAll", BallotRejection.class).getResultList();
	}
}
