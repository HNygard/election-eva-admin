package no.valg.eva.admin.configuration.repository;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;

@Default
@ApplicationScoped
public class VoteCountCategoryRepository extends BaseRepository {

	public VoteCountCategoryRepository() {
	}

	public VoteCountCategoryRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VoteCountCategory findById(String id) {
		return super.findEntityById(VoteCountCategory.class, id);
	}

	public VoteCountCategory findByEnum(CountCategory countCategory) {
		return super.findEntityById(VoteCountCategory.class, countCategory.getId());
	}

	public List<VoteCountCategory> findByContest(long cpk) {
		Contest contest = findEntityByPk(Contest.class, cpk);
		return categoriesForContest(contest);
	}

	public List<VoteCountCategory> categoriesForContest(Contest contest) {
		if (contest == null) {
			return emptyList();
		}
		
		if (contest.isOnBoroughLevel()) {
			return Arrays.asList(
					findById(VO.getId()), 
					findById(VS.getId()), 
					findById(VB.getId()), 
					findById(FO.getId()), 
					findById(FS.getId()), 
					findById(BF.getId())
			);
		}
		
		TypedQuery<VoteCountCategory> query = getEm().createNamedQuery("VoteCountCategory.findByContest", VoteCountCategory.class);
		query.setParameter(1, contest.getPk());
		return query.getResultList();
	}

	public List<VoteCountCategory> categoriesForContestAndMunicipality(Contest contest, Municipality municipality) {
		return findByMunicipality(municipality.getPk(), contest.getElection().getElectionGroup().getPk(), contest.isOnBoroughLevel());
	}

	public List<VoteCountCategory> findByMunicipality(long municipalityPk, long electionGroupPk, boolean includeBoroughCategory) {
		TypedQuery<VoteCountCategory> query = getEm().createNamedQuery("VoteCountCategory.findByMunicipality", VoteCountCategory.class);
		query.setParameter("municipalityPk", municipalityPk);
		query.setParameter("electionGroupPk", electionGroupPk);
		List<VoteCountCategory> resultList = query.getResultList();

		if (includeBoroughCategory(municipalityPk, includeBoroughCategory)) {
			// Bydelsvalg - legg til BF for Oslo
			resultList.add(findById(BF.getId()));
		}

		return resultList;
	}

	private boolean includeBoroughCategory(long municipalityPk, boolean includeBoroughCategory) {
		if (!includeBoroughCategory) {
			return false;
		}
		Municipality municipality = findEntityByPk(Municipality.class, municipalityPk);
		return municipality.getId().equals(AreaPath.OSLO_MUNICIPALITY_ID);
	}

	public List<VoteCountCategory> findByElectionAndAreaPath(Election election, AreaPath areaPath) {
		if (election == null) {
			return emptyList();
		}
		TypedQuery<VoteCountCategory> query = getEm().createNamedQuery("VoteCountCategory.findByElectionAndAreaPath", VoteCountCategory.class);
		List<VoteCountCategory> resultList = query
				.setParameter(1, election.getPk())
				.setParameter(2, areaPath.path())
				.getResultList();
		if (election.isOnBoroughLevel()) {
			resultList.add(findById(BF.getId()));
		}
		return resultList;
	}

	public List<VoteCountCategory> findByElectionEventAndAreaPath(ElectionEvent electionEvent, AreaPath areaPath) {
		if (electionEvent == null) {
			return emptyList();
		}
		TypedQuery<VoteCountCategory> query = getEm().createNamedQuery("VoteCountCategory.findByElectionEventAndAreaPath", VoteCountCategory.class);
		List<VoteCountCategory> resultList = query
				.setParameter(1, electionEvent.getPk())
				.setParameter(2, areaPath.path())
				.getResultList();
		if (electionEvent.hasElectionOnBoroughLevel()) {
			resultList.add(findById(BF.getId()));
		}
		return resultList;
	}

	public List<VoteCountCategory> findAll() {
		return super.findAllEntities(VoteCountCategory.class);
	}
}
