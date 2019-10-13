package no.valg.eva.admin.configuration.repository;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

@Default
@ApplicationScoped
public class MvElectionRepository extends BaseRepository {

	public MvElectionRepository() {
		// Needed for wiring
	}

	public MvElectionRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public MvElection update(UserData userData, MvElection mvElection) {
		return super.updateEntity(userData, mvElection);
	}

	public MvElectionDigest findSingleDigestByPath(ElectionPath electionPath) {
		try {
			return getEm()
					.createNamedQuery("MvElectionDigest.findSingleDigestByPath", MvElectionDigest.class)
					.setParameter("path", electionPath.path())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<MvElection> findByPathAndLevelAndAreaLevel(ElectionPath electionPath, ElectionLevelEnum electionLevelEnum, AreaLevelEnum areaLevelEnum) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findByPathAndLevelAndAreaLevel", MvElection.class);
		query.setParameter(1, electionPath.path());
		query.setParameter(2, electionLevelEnum.getLevel());
		
		query.setParameter(3, areaLevelEnum.getLevel());
		
		return query.getResultList();
	}

	public List<MvElection> findContestsByElectionEventAndAreas(ElectionEvent electionEvent, List<AreaPath> areaPaths) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findContestsByElectionEventAndAreas", MvElection.class);
		List<String> paths = areaPaths.stream().map(areaPath -> areaPath.path()).collect(Collectors.toList());
		query.setParameter("electionEventId", electionEvent.getId());
		query.setParameter("areaPaths", paths);
		return query.getResultList();
	}

	public List<MvElection> findByPathAndLevel(ElectionPath electionPath, ElectionLevelEnum electionLevelEnum) {
		return findByPathAndLevel(electionPath.toString(), electionLevelEnum.getLevel());
	}

	public List<MvElection> findByPathAndLevel(String pathString, int level) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findByPathAndLevel", MvElection.class);
		query.setParameter(1, pathString);
		query.setParameter(2, level);
		return query.getResultList();
	}

	public List<MvElectionDigest> findDigestsByPathAndLevel(ElectionPath electionPath, ElectionLevelEnum electionLevelEnum) {
		return getEm()
				.createNamedQuery("MvElectionDigest.findDigestsByPathAndLevel", MvElectionDigest.class)
				.setParameter(1, electionPath.path())
				.setParameter(2, electionLevelEnum.getLevel())
				.getResultList();
	}

	public List<MvElection> findByPathAndChildLevel(MvElection mvElection) {
		if (mvElection == null || mvElection.getElectionEvent() == null) {
			return null;
		}
		return findByPathAndLevel(mvElection.getElectionPath(), mvElection.getElectionLevel() + 1);
	}

	public MvElection findFirstByPathAndLevel(ElectionPath electionPath, ElectionLevelEnum electionLevelEnum) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findFirstByPathAndLevel", MvElection.class);
		query.setParameter(1, electionPath.toString());
		query.setParameter(2, electionLevelEnum.getLevel());
		return query.getSingleResult();
	}

	public boolean hasContestsForElectionAndArea(ElectionPath electionPath, AreaPath areaPath) {
		Query query = getEm().createNamedQuery("MvElection.findFirstPkByElectionPathAndOperatorAreaPath");
		query.setParameter(1, electionPath.path() + "%");
		query.setParameter(2, areaPath.path());
		try {
			return query.getSingleResult() != null;
		} catch (NoResultException e) {
			return false;
		}
	}

	public List<MvElection> findContestsForElectionAndArea(ElectionPath electionPath, AreaPath areaPath) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findByElectionPathAndOperatorAreaPath", MvElection.class);
		query.setParameter(1, electionPath.path() + "%");
		query.setParameter(2, areaPath.path());
		return query.getResultList();
	}

	public List<MvElectionDigest> findDigestByElectionPathAndAreaPath(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti) {
		TypedQuery<MvElectionDigest> query = getEm().createNamedQuery("MvElectionDigest.findDigestByElectionPathAndAreaPath", MvElectionDigest.class);
		query.setParameter(1, valghierarkiSti.toString() + "%");
		query.setParameter(2, valggeografiSti.toString());
		return query.getResultList();
	}

	public ElectionLevel findElectionLevelByPk(Long pk) {
		return super.findEntityByPk(ElectionLevel.class, pk);
	}

	public MvElection findByPk(Long pk) {
		return super.findEntityByPk(MvElection.class, pk);
	}

	public MvElection findRoot(long electionEvent) {
		TypedQuery<MvElection> query = getEm().createNamedQuery("MvElection.findRoot", MvElection.class);
		query.setParameter("eepk", electionEvent);
		MvElection result = null;
		List<MvElection> resultList = query.getResultList();
		if (resultList != null && !resultList.isEmpty()) {
			result = resultList.get(0);
		}
		return result;
	}

	public MvElection findByContest(Contest contest) {
		return finnEnkeltMedSti(contest.valgdistriktSti());
	}

	public boolean hasElectionsWithElectionType(MvElection mvElection, ElectionType electionType) {
		if (mvElection.getElectionLevel() > 2) {
			throw new IllegalArgumentException("MvElection passed as parameter needs to have election level < 2");
		}
		String sql = "SELECT count(*) > 0 FROM mv_election mve LEFT JOIN admin.election e "
				+ "on mve.election_pk = e.election_pk WHERE text2ltree(mve.election_path) <@ text2ltree(?1) "
				+ "AND e.election_type_pk = ?2 AND mve.election_level = 2";
		Query query = getEm().createNativeQuery(sql);
		query.setParameter(1, mvElection.getElectionPath());
		query.setParameter(2, electionType.getPk());
		return (Boolean) query.getSingleResult();
	}

	public Boolean hasAccessToPkOnLevel(MvElection mvElection, long pk, int level) {
		Query query = getEm().createNamedQuery(MvElection.HAS_ACCESS_TO_PK_ON_LEVEL);
		query.setParameter(1, level);
		query.setParameter(2, pk);
		
		query.setParameter(3, mvElection.getElectionPath());
		
		return query.getResultList().size() == 1;
	}

	public MvElection findByPkAndLevel(long pk, int level) {
		TypedQuery<MvElection> query = getEm().createNamedQuery(MvElection.FIND_BY_PK_AND_LEVEL, MvElection.class);
		query.setParameter(1, level);
		query.setParameter(2, pk);
		return query.getSingleResult();
	}

	public boolean matcherValghierarkiStiOgValggeografiSti(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti) {
		Query query = getEm().createNamedQuery("MvElection.matchElectionPathAndAreaPath");
		query.setParameter(1, valghierarkiSti.toString() + "%");
		query.setParameter(2, valggeografiSti.toString());
		return ((Number) query.getSingleResult()).intValue() > 0;
	}

	public MvElection finnEnkeltMedSti(ValghierarkiSti sti) {
		MvElection result;
		try {
			result = getEm()
					.createNamedQuery("MvElection.findByPath", MvElection.class)
					.setParameter("path", sti.toString())
					.getSingleResult();
		} catch (NoResultException e) {
			result = null;
		}
		return result;
	}
}
