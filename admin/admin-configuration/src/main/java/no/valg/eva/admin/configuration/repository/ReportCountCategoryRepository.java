package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;

public class ReportCountCategoryRepository extends BaseRepository {

	public static final String QUERY_FIND_BY_MUNICIPALITY_ELECTION_GROUP_AND_VOTE_COUNT_CATEGORY = "SELECT rcc "
			+ "FROM ReportCountCategory rcc "
			+ "WHERE rcc.municipality.pk = :municipalityPk "
			+ "    AND rcc.electionGroup.pk = :electionGroupPk "
			+ "    AND rcc.voteCountCategory.id = :voteCountCategoryId";
	public static final String QUERY_FIND_BY_COUNTY_ELECTION_GROUP_AND_VOTE_COUNT_CATEGORY = "SELECT rcc.* "
			+ "FROM report_count_category rcc "
			+ "JOIN mv_area mva ON mva.municipality_pk = rcc.municipality_pk and area_level = 3 "
			+ "JOIN vote_count_category vcc USING (vote_count_category_pk) "
			+ "WHERE mva.area_path LIKE ?1 "
			+ "    AND rcc.election_group_pk = ?2 "
			+ "    AND vcc.vote_count_category_id = ?3 "
			+ "ORDER BY mva.municipality_id asc ";
	public static final String MUNICIPALITY_PK = "municipalityPk";
	public static final String ELECTION_GROUP_PK = "electionGroupPk";
	public static final String VOTE_COUNT_CATEGORY_ID = "voteCountCategoryId";

	public ReportCountCategoryRepository() {
	}

	ReportCountCategoryRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<ReportCountCategory> findByCountyElectionGroupAndCountCategory(AreaPath countyPath, long electionGroupPk, CountCategory countCategory) {
		return getEm().createNativeQuery(QUERY_FIND_BY_COUNTY_ELECTION_GROUP_AND_VOTE_COUNT_CATEGORY, ReportCountCategory.class)
				.setParameter(1, countyPath.path() + "%")
				.setParameter(2, electionGroupPk)
				
				.setParameter(3, countCategory.getId())
				
				.getResultList();
	}

	public ReportCountCategory findByMunicipalityElectionGroupAndVoteCountCategory(
			Municipality municipality, ElectionGroup electionGroup, CountCategory voteCountCategory) {
		TypedQuery<ReportCountCategory> query = getEm().createQuery(QUERY_FIND_BY_MUNICIPALITY_ELECTION_GROUP_AND_VOTE_COUNT_CATEGORY,
				ReportCountCategory.class);
		query.setHint("org.hibernate.cacheable", true);
		query.setParameter(MUNICIPALITY_PK, municipality.getPk());
		query.setParameter(ELECTION_GROUP_PK, electionGroup.getPk());
		query.setParameter(VOTE_COUNT_CATEGORY_ID, voteCountCategory.getId());

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Finds the CountingMode for a Municipality (by its MvArea) and an ElectionGroup (by its Contest).
	 * 
	 * @param mvAreaPk
	 *            an MvArea that <b>must be a Municipality</b>
	 * @param contestPk
	 *            the contest
	 * @return the counting mode for the municipality in the Contest
	 */
	public CountingMode findCountingModeByMvAreaForMunicipalityAndContest(final Long mvAreaPk, final Long contestPk) {
		Query query = getEm().createNamedQuery("ReportCountCategory.findCountingModeByMvAreaForMunicipalityAndContest");
		query.setParameter(1, mvAreaPk);
		query.setParameter(2, contestPk);

		Object[] resultMatrix = (Object[]) query.getSingleResult();
		return resultMatrixToCountingMode(resultMatrix);
	}

	private CountingMode resultMatrixToCountingMode(final Object[] resultMatrix) {
		boolean centralPreliminaryCount = (Boolean) resultMatrix[0];
		boolean pollingDistrictCount = (Boolean) resultMatrix[1];
		boolean technicalPollingDistrictCount = (Boolean) resultMatrix[2];

		return CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}

	public ReportCountCategory create(UserData userData, ReportCountCategory reportCountCategory) {
		return super.createEntity(userData, reportCountCategory);
	}

	public ReportCountCategory update(UserData userData, ReportCountCategory reportCountCategory) {
		return super.updateEntity(userData, reportCountCategory);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, ReportCountCategory.class, pk);
	}

	public List<ReportCountCategory> findReportCountCategories(Municipality municipality, ElectionGroup electionGroup) {
		TypedQuery<ReportCountCategory> query = getEm().createNamedQuery("ReportCountCategory.findReportCountCategories", ReportCountCategory.class);
		query.setParameter(MUNICIPALITY_PK, municipality.getPk());
		query.setParameter(ELECTION_GROUP_PK, electionGroup.getPk());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ReportCountCategory> findAllReportCountCategoriesForElectionEvent(Long electionEventPk, boolean detachEntities) {
		String sql = "select rcc.* from report_count_category rcc "
				+ "join election_group eg on eg.election_group_pk = rcc.election_group_pk "
				+ "join municipality m on m.municipality_pk = rcc.municipality_pk "
				+ "where eg.election_event_pk = ?1 "
				+ "order by eg.election_group_id, m.municipality_id;";
		Query query = getEm().createNativeQuery(sql, ReportCountCategory.class);
		query.setParameter(1, electionEventPk);
		if (detachEntities) {
			List resultList = query.getResultList();
			return super.detach(resultList);
		}
		return query.getResultList();
	}

	public ReportCountCategory findByMunAndEGAndVCC(Long mPk, Long egPk, Long vccPk) {
		try {
			TypedQuery<ReportCountCategory> query = getEm().createNamedQuery("ReportCountCategory.findByMunAndEGAndVCC", ReportCountCategory.class);
			query.setParameter("mPk", mPk);
			query.setParameter("egPk", egPk);
			query.setParameter("vccPk", vccPk);
			return query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}
	}

	public List<ReportCountCategory> findByMunicipalityAndElectionGroup(Long municipalityPk, Long electionGroupPk) {
		TypedQuery<ReportCountCategory> query = getEm().createNamedQuery("ReportCountCategory.findByMunAndEG", ReportCountCategory.class);
		query.setParameter(ELECTION_GROUP_PK, electionGroupPk);
		query.setParameter(MUNICIPALITY_PK, municipalityPk);
		return query.getResultList();
	}

	public ReportCountCategory findByContestAndMunicipalityAndCategory(Contest contest, Municipality municipality, CountCategory category) {
		return findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElection().getElectionGroup(), category);
	}

	public List<ReportCountCategory> findByContestAndMunicipality(Contest contest, Municipality municipality) {
		return findByMunicipalityAndElectionGroup(municipality.getPk(), contest.getElection().getElectionGroup().getPk());
	}
}
