package no.valg.eva.admin.counting.repository;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Repo for finding info about contests that are accessible from a given area and election.
 */
@Default
@ApplicationScoped
public class ContestInfoRepository extends BaseRepository {

	private static final String CONTEST_INFO_QUERY_ON_ELECTION_PK =
			"SELECT c.election_path as election_path, c.election_name as election_name, c.contest_name as contest_name, "
					+ "ac.area_path as area_path "
					+ "FROM mv_election c "
					+ "JOIN contest_area ca ON ca.contest_pk = c.contest_pk "
					+ "JOIN mv_area ac ON ac.mv_area_pk = ca.mv_area_pk "
					+ "JOIN mv_area a ON text2ltree(a.area_path) <@ text2ltree(ac.area_path) "
					+ "OR (a.area_level = 3 AND ac.area_level = 4 AND text2ltree(a.area_path) @> text2ltree(ac.area_path))"
					+ "WHERE c.election_level = 3 and a.mv_area_pk = ?1 and c.election_pk = ?2";

	private static final String CONTEST_INFO_QUERY =
			"SELECT c.election_path as election_path, c.election_name as election_name, c.contest_name as contest_name, "
					+ "ac.area_path as area_path "
					+ "FROM mv_election c "
					+ "JOIN contest_area ca ON ca.contest_pk = c.contest_pk "
					+ "JOIN mv_area ac ON ac.mv_area_pk = ca.mv_area_pk "
					+ "JOIN mv_area a ON text2ltree(a.area_path) <@ text2ltree(ac.area_path) "
					+ "OR (a.area_level = 3 AND ac.area_level = 4 AND text2ltree(a.area_path) @> text2ltree(ac.area_path))"
					+ "WHERE c.election_level = 3 and a.mv_area_pk = ?1";

	public ContestInfoRepository() {
	}

	ContestInfoRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public ContestInfo contestForElectionAndArea(Election election, MvArea mvArea) {
		Query query = getEm().createNativeQuery(CONTEST_INFO_QUERY_ON_ELECTION_PK);
		query.setParameter(1, mvArea.getPk());
		query.setParameter(2, election.getPk());
		Object[] contestInfo = (Object[]) query.getSingleResult();
		
		return new ContestInfo((String) contestInfo[0], (String) contestInfo[1], (String) contestInfo[2], (String) contestInfo[3]);
		
	}

	public ContestInfo contestForSamiElection(Contest contest) {
		Query query = getEm().createNamedQuery("MvElection.contestInfoForSamiElection");
		query.setParameter(1, contest.getPk());
        return (ContestInfo) query.getSingleResult();
	}

	public boolean hasContestsForElectionAndArea(long electionPk, long mvAreaPk) {
		Query query = getEm().createNativeQuery(CONTEST_INFO_QUERY_ON_ELECTION_PK);
		query.setParameter(1, mvAreaPk);
		query.setParameter(2, electionPk);
		return !query.getResultList().isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<ContestInfo> contestsForArea(long mvAreaPk) {
		Query query = getEm().createNativeQuery(CONTEST_INFO_QUERY);
		query.setParameter(1, mvAreaPk);
		List<Object[]> rows = query.getResultList();
		List<ContestInfo> result = new ArrayList<>();
		for (Object[] row : rows) {
			
			result.add(new ContestInfo((String) row[0], (String) row[1], (String) row[2], (String) row[3]));
			
		}
		return result;
	}
}
