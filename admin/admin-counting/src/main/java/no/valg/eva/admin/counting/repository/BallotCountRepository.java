package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class BallotCountRepository extends BaseRepository {
	public BallotCountRepository() {
	}

	public BallotCountRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public BallotCount update(UserData userData, BallotCount ballotCount) {
		return super.updateEntity(userData, ballotCount);
	}

	public List<BallotCount> populateBallotCounts(VoteCount voteCount, ContestReport contestReport, boolean approved) {
		try {
			
			TypedQuery<BallotCount> query = getEm()
					.createNamedQuery("BallotCount.findBallotCountByVCBallot", BallotCount.class)
					.setParameter(1, voteCount.getPk().intValue())
					.setParameter(2, contestReport.getContest().getPk())
					.setParameter(3, approved);
			return query.getResultList();
			
		} catch (NoResultException e) {
			return null;
		}
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, BallotCount.class, pk);
	}

	public BallotCount create(UserData userData, BallotCount ballotCount) {
		return super.createEntity(userData, ballotCount);
	}

	public List<BallotCount> findUnapprovedBallotCounts(VoteCount voteCount, ContestReport contestReport) {
		return populateBallotCounts(voteCount, contestReport, false);
	}

	public List<BallotCount> getBallotCounts(Long vcpk) {
		return getEm()
				.createNamedQuery("BallotCount.getByVoteCount", BallotCount.class)
				.setParameter("vcpk", vcpk)
				.getResultList();
	}

	public BallotCount findByPk(Long pk) {
		return super.findEntityByPk(BallotCount.class, pk);
	}
}
