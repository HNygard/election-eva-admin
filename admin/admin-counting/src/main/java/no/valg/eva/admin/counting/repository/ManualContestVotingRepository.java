package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;

@Default
@ApplicationScoped
public class ManualContestVotingRepository extends BaseRepository {

	public static final String QUERY_FIND_FOR_VO_BY_CONTEST_AND_AREA =
			"SELECT mcv "
					+ "FROM ManualContestVoting mcv "
					+ "WHERE mcv.contest.pk = :contestPk "
					+ "AND mcv.mvArea.pk = :mvAreaPk "
					+ "AND mcv.votingCategory.id = 'VO' "
					+ "ORDER BY mcv.electionDay.date ASC";

	public ManualContestVotingRepository() {
	}

	ManualContestVotingRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public List<ManualContestVoting> findForVoByContestAndArea(final long contestPk, final long mvAreaPk) {
		TypedQuery<ManualContestVoting> query = getEm().createQuery(QUERY_FIND_FOR_VO_BY_CONTEST_AND_AREA, ManualContestVoting.class);
		query.setParameter("contestPk", contestPk);
		query.setParameter("mvAreaPk", mvAreaPk);

		return query.getResultList();
	}

	public List<ManualContestVoting> createMany(UserData userData, List<ManualContestVoting> manualContestVotings) {

		return super.createEntities(userData, manualContestVotings);
	}

	public List<ManualContestVoting> updateMany(UserData userData, List<ManualContestVoting> manualContestVotings) {

		return super.updateEntities(userData, manualContestVotings);
	}
}
