package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class ContestAreaRepository extends BaseRepository {
	public static final String FIELD_CONTEST_PK = "contestPk";
	public static final String FIELD_ELECTION_PK = "electionPk";

	public ContestAreaRepository() {
		// For testing
	}

	public ContestAreaRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public ContestArea create(UserData userData, ContestArea contestArea) {
		return super.createEntity(userData, contestArea);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, ContestArea.class, pk);
	}

	public ContestArea update(UserData userData, ContestArea contestArea) {
		return super.updateEntity(userData, contestArea);
	}

	public ContestArea findByPk(Long pk) {
		return findEntityByPk(ContestArea.class, pk);
	}

	public List<ContestArea> findContestAreasForContest(Long contestPk) {
		TypedQuery<ContestArea> query = getEm().createNamedQuery("ContestArea.findContestAreasForContest", ContestArea.class);
		query.setParameter(FIELD_CONTEST_PK, contestPk);
		return query.getResultList();
	}

	public List<ContestArea> findByElection(Election election) {
		TypedQuery<ContestArea> query = getEm().createNamedQuery("ContestArea.findByElection", ContestArea.class);
		query.setParameter(FIELD_ELECTION_PK, election.getPk());
		return query.getResultList();
	}

	public List<MvArea> findMvAreasForContest(Long contestPk) {
		TypedQuery<ContestArea> query = getEm().createNamedQuery("ContestArea.findContestAreasForContest", ContestArea.class);
		query.setParameter(FIELD_CONTEST_PK, contestPk);
		List<ContestArea> contestAreas = query.getResultList();
		return Lists.transform(contestAreas, new Function<ContestArea, MvArea>() {
			public MvArea apply(final ContestArea contestArea) {
				return contestArea.getMvArea();
			}
		});
	}

	public boolean isContestOnOrBelowArea(Long contestPk, String areaPath) {
		List<ContestArea> contestAreas = findContestAreasForContest(contestPk);
		for (ContestArea contestArea : contestAreas) {
			if (!contestArea.getMvArea().getPath().startsWith(areaPath)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public Multimap<Long, String> getContestMvAreaPaths(List<Long> contestPks) {
		Multimap<Long, String> contestMvAreaPathMap = HashMultimap.create();
		Query query = getEm().createNamedQuery("ContestArea.findAreaPathsForContests").setParameter("contestPks", contestPks);
		List<Object[]> resultList = query.getResultList();
		for (Object[] row : resultList) {
			contestMvAreaPathMap.put((Long) row[0], (String) row[1]);
		}
		return contestMvAreaPathMap;
	}

	@SuppressWarnings("unchecked")
	public List<ContestArea> findContestAreaForElectionAndMvArea(Long electionPk, Long mvAreaPk) {
		// @formatter:off
		String queryString = "SELECT ca.* FROM contest_area ca "
				+ "JOIN contest c ON (c.contest_pk = ca.contest_pk) "
				+ "JOIN election e ON (e.election_pk = c.election_pk) "
				+ "JOIN mv_area ma ON (ma.mv_area_pk = ca.mv_area_pk) "
				+ "WHERE ma.mv_area_pk=?1 "
				+ "AND c.election_pk=?2";
		// @formatter:on
		Query query = getEm().createNativeQuery(queryString, ContestArea.class);
		query.setParameter(1, mvAreaPk);
		query.setParameter(2, electionPk);
		return (List<ContestArea>) query.getResultList();
	}

	/**
	 * Find contest_area that is a municipality and child_area (if any) for an municipality in an election_group
	 */
	@SuppressWarnings("unchecked")
	public List<ContestArea> findContestAreaChildForElectionGroupAndMunicipality(Long electionGroupPk, Long mvAreaMunicipalityPk) {
		// @formatter:off
		String queryString = "SELECT ca.* FROM contest_area ca "
				+ "JOIN contest c ON (c.contest_pk = ca.contest_pk) "
				+ "JOIN election e ON (e.election_pk = c.election_pk) "
				+ "JOIN election_group eg ON (e.election_group_pk = eg.election_group_pk) "
				+ "JOIN mv_area ma ON (ma.mv_area_pk = ca.mv_area_pk and ma.area_level = 3) "
				+ "WHERE ma.mv_area_pk=?1 "
				+ "AND eg.election_group_pk=?2 AND ca.child_area;";
		// @formatter:on
		Query query = getEm().createNativeQuery(queryString, ContestArea.class);
		query.setParameter(1, mvAreaMunicipalityPk);
		query.setParameter(2, electionGroupPk);
		return (List<ContestArea>) query.getResultList();
	}

	public boolean existsContestAreaParentForElectionGroupAndMunicipality(Long electionGroupPk, Long mvAreaMunicipalityPk) {
		// @formatter:off
		String queryString = "SELECT ca.* FROM contest_area ca "
				+ "JOIN contest c ON (c.contest_pk = ca.contest_pk) "
				+ "JOIN election e ON (e.election_pk = c.election_pk) "
				+ "JOIN election_group eg ON (e.election_group_pk = eg.election_group_pk) "
				+ "JOIN mv_area ma ON (ma.mv_area_pk = ca.mv_area_pk and ma.area_level = 3) "
				+ "WHERE ma.mv_area_pk=?1 "
				+ "AND eg.election_group_pk=?2 AND ca.parent_area;";
		// @formatter:on
		Query query = getEm().createNativeQuery(queryString, ContestArea.class);
		query.setParameter(1, mvAreaMunicipalityPk);
		query.setParameter(2, electionGroupPk);
		return !query.getResultList().isEmpty();
	}

	public List<ContestArea> finnForValghendelseMedValgdistrikt(ElectionEvent valghendelse) {
		return getEm()
			.createNamedQuery("ContestArea.finnForValghendelseMedValgdistrikt", ContestArea.class)
			.setParameter("electionEventPk", valghendelse.getPk())
			.getResultList();
	}
}
