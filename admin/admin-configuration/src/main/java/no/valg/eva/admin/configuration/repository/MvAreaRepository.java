package no.valg.eva.admin.configuration.repository;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;

/**
 */
@Default
@ApplicationScoped
public class MvAreaRepository extends BaseRepository {
	public MvAreaRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public MvAreaRepository() {
		// empty constructor for hibernate
	}

	public MvArea findByPk(Long mvAreaPk) {
		return super.findEntityByPk(MvArea.class, mvAreaPk);
	}

	public MvArea findRoot(Long eepk) {
		Query query = getEm().createNamedQuery("MvArea.findRoot");
		query.setParameter("eepk", eepk);
		return (MvArea) query.getSingleResult();
	}

	public List<MvArea> findByPathAndChildLevel(MvArea mvArea) {
		if (mvArea != null && mvArea.getElectionEvent() != null) {
			return findByPathAndLevel(mvArea.getAreaPath(), mvArea.getAreaLevel() + 1);
		}
		return Collections.emptyList();
	}

	public List<MvArea> findByPathAndChildLevel(AreaPath areaPath) {
		return findByPathAndLevel(areaPath.path(), areaPath.getLevel().getLevel() + 1);
	}

	public List<MvArea> finnFor(ValggeografiSti valggeografiSti, AreaLevelEnum omraadenivaa) {
		return findByPathAndLevel(valggeografiSti.areaPath(), omraadenivaa);
	}
	
	public List<MvArea> findByPathAndLevel(AreaPath areaPath, AreaLevelEnum areaLevelEnum) {
		return findByPathAndLevel(areaPath.path(), areaLevelEnum.getLevel());
	}

	@Deprecated
	public List<MvArea> findByPathAndLevel(String path, int level) {
		return getEm()
				.createNamedQuery("MvArea.findByPathAndLevel", MvArea.class)
				.setParameter(1, path)
				.setParameter(2, level)
				.getResultList();
	}

	public List<MvAreaDigest> findDigestsByPathAndLevel(AreaPath areaPath, AreaLevelEnum areaLevelEnum) {
		return getEm()
				.createNamedQuery("MvAreaDigest.findDigestsByPathAndLevel", MvAreaDigest.class)
				.setParameter(1, areaPath.path())
				.setParameter(2, areaLevelEnum.getLevel())
				.getResultList();
	}

	public MvArea findFirstByPathAndLevel(AreaPath areaPath, AreaLevelEnum areaLevelEnum) {
		TypedQuery<MvArea> query = getEm().createNamedQuery("MvArea.findFirstByPathAndLevel", MvArea.class);
		query.setParameter(1, areaPath.toString());
		query.setParameter(2, areaLevelEnum.getLevel());
		return query.getSingleResult();
	}

	public MvAreaDigest findFirstDigestByPathAndLevel(AreaPath areaPath, AreaLevelEnum areaLevelEnum) {
		TypedQuery<MvAreaDigest> query = getEm().createNamedQuery("MvAreaDigest.findFirstDigestByPathAndLevel", MvAreaDigest.class);
		query.setParameter(1, areaPath.toString());
		query.setParameter(2, areaLevelEnum.getLevel());
		return query.getSingleResult();
	}

	public Boolean hasAccessToPkOnLevel(MvArea mvArea, long pk, int level) {
		Query query = getEm().createNamedQuery("MvArea.hasAccessToPkOnLevel");
		query.setParameter(1, level);
		query.setParameter(2, pk);
		
		query.setParameter(3, mvArea.getAreaPath());
		
		return query.getResultList().size() == 1;
	}

	public Long findByLevelAndId(int level, Long pk) {
		if (level == AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			Query query = getEm().createNamedQuery("MvArea.findByPollingDistrict");
			query.setParameter("pollingDistrictPk", pk);
			return (Long) query.getSingleResult();
		} else {
			return null;
		}
	}

	public List<AreaLevel> findAllAreaLevels() {
		return findAllEntities(AreaLevel.class);
	}

	public AreaLevel findAreaLevelById(String id) {
		return findEntityById(AreaLevel.class, id);
	}

	public AreaLevel findAreaLevelByPk(Long pk) {
		return findEntityByPk(AreaLevel.class, pk);
	}

	/**
	 * @deprecated use findSingleByPath(AreaPath)
	 */
	@Deprecated
	public MvArea findSingleByPath(String path) {
		try {
			return getEm()
					.createNamedQuery("MvArea.findSingleByPath", MvArea.class)
					.setParameter("path", path)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public MvAreaDigest findSingleDigestByPath(AreaPath areaPath) {
		try {
			return getEm()
					.createNamedQuery("MvAreaDigest.findSingleByPath", MvAreaDigest.class)
					.setParameter("path", areaPath.path())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public MvArea findSingleByPath(AreaPath areaPath) {
		return findSingleByPath(areaPath.path());
	}

	public MvArea findSingleByPath(String electionEventId, AreaPath areaPath) {
		try {
			return getEm()
					.createNamedQuery("MvArea.findSingleByElectionAndPath", MvArea.class)
					.setParameter("path", areaPath.path())
					.setParameter("electionEventId", electionEventId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public MvArea findSingleByPollingDistrictIdAndMunicipalityPk(String pollingDistrictId, Long municipalityPk) {
		TypedQuery<MvArea> query = getEm()
				.createNamedQuery("MvArea.findByPollingDistrictIdAndMunicipalityPk", MvArea.class)
				.setParameter("pollingDistrictId", pollingDistrictId)
				.setParameter("municipalityPk", municipalityPk);
		MvArea result;
		try {
			result = query.getSingleResult();
		} catch (NoResultException e) {
			result = null;
		}
		return result;
	}

	public MvArea findSingleByPollingPlaceIdAndMunicipalityPk(String pollingPlaceId, Long municipalityPk) {
		TypedQuery<MvArea> query = getEm()
				.createNamedQuery("MvArea.findByPollingPlaceIdAndMunicipalityPk", MvArea.class)
				.setParameter("pollingPlaceId", pollingPlaceId)
				.setParameter("municipalityPk", municipalityPk);
		MvArea result;
		try {
			result = query.getSingleResult();
		} catch (NoResultException e) {
			result = null;
		}
		return result;
	}

	public List<Contest> findContestsByElectionWhereAllBallotsAreProcessed(ElectionRef electionPk) {
		return getEm()
				.createNamedQuery("MvArea.findContestsByElectionWhereAllBallotsAreProcessed", Contest.class)
				.setParameter("electionPk", electionPk.getPk())
				.getResultList();
	}
	
	public MvArea findParentAreaByPk(Long areaPk) {
		return getEm()
				.createNamedQuery("MvArea.findParentAreaByPk", MvArea.class)
				.setParameter("areaPk", areaPk)
				.getSingleResult();
	}

	public MvArea finnEnkeltMedSti(ValggeografiSti sti) {
		return findSingleByPath(sti.areaPath());
	}

	public List<MvArea> finnKommunerForFylkeskommune(FylkeskommuneSti fylkeskommuneSti) {
		return findByPathAndLevel(fylkeskommuneSti.areaPath(), MUNICIPALITY);
	}
}
