package no.valg.eva.admin.configuration.repository;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.model.views.ContestRelArea;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Default
@ApplicationScoped
public class ReportingUnitRepository extends BaseRepository {
    private static final String PARAM_MV_AREA_PK = "mvAreaPk";
    private static final String PARAM_MV_ELECTION_PK = "mvElectionPk";

	public ReportingUnitRepository() {
		// For testing
	}

	ReportingUnitRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public ReportingUnit findByAreaPathAndType(AreaPath areaPath, ReportingUnitTypeId typeId) {
		TypedQuery<ReportingUnit> query = reportingUnitTypedQuery(areaPath, typeId);
		return query.getSingleResult();
	}

	public boolean existsFor(AreaPath areaPath, ReportingUnitTypeId typeId) {
		TypedQuery<ReportingUnit> query = reportingUnitTypedQuery(areaPath, typeId);
		return !query.getResultList().isEmpty();
	}

	// Any reason the method is throwing exceptions?

	/**
	 * @deprecated Replaced by {@link #getReportingUnit(ElectionPath, AreaPath)}
	 */
	@Deprecated
	public ReportingUnit getReportingUnit(final ContestRelArea contestRelArea) {
		try {
			TypedQuery<ReportingUnit> query = getEm()
					.createNamedQuery("ReportingUnit.findReportingUnit", ReportingUnit.class)
					.setParameter(1, contestRelArea.getElectionPath())
					.setParameter(2, contestRelArea.getAreaPath());
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new EvoteNoRollbackException("@count.error.missing_reporting_unit", e);
		} catch (NonUniqueResultException e) {
			throw new EvoteNoRollbackException("@count.error.many_ReportingUnits", e);
		}
	}

	public ReportingUnit getReportingUnit(ElectionPath contestPath, AreaPath areaPath) {
		contestPath.assertContestLevel();
		if (areaPath.isBoroughLevel()) {
			return findByAreaPathAndType(areaPath.toMunicipalityPath(), ReportingUnitTypeId.VALGSTYRET);
		}
		try {
			TypedQuery<ReportingUnit> query = getEm()
					.createNamedQuery("ReportingUnit.findReportingUnit", ReportingUnit.class)
					.setParameter(1, contestPath.path())
					.setParameter(2, areaPath.path());
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new EvoteNoRollbackException("@count.error.missing_reporting_unit", e);
		} catch (NonUniqueResultException e) {
			throw new EvoteNoRollbackException("@count.error.many_ReportingUnits", e);
		}
	}

	public ReportingUnit getReportingUnit(UserData userData, AreaPath areaPath) {
		if (areaPath.isRootLevel()) {
			return getReportingUnit(
					ElectionPath.from(userData.getOperatorMvElection().getElectionPath()),
					areaPath);
		} else {
			return findReportingUnitByAreaLevel(areaPath);
		}
	}

	private TypedQuery<ReportingUnit> reportingUnitTypedQuery(AreaPath areaPath, ReportingUnitTypeId typeId) {
		TypedQuery<ReportingUnit> query = getEm().createNamedQuery("ReportingUnit.byAreaAndType", ReportingUnit.class);
		query.setParameter("areaPath", areaPath.path());
		query.setParameter("typeId", typeId.getId());
		return query;
	}

	public List<ReportingUnit> create(UserData userData, List<ReportingUnit> reportingUnits) {
		return super.createEntities(userData, reportingUnits);
	}

	public ReportingUnit create(UserData userData, ReportingUnit reportingUnit) {
		return super.createEntity(userData, reportingUnit);
	}

	public List<ReportingUnit> findAllForElectionEvent(long electionEventPk) {
		TypedQuery<ReportingUnit> query = getEm().createNamedQuery("ReportingUnit.findAllForElectionEvent", ReportingUnit.class);
		query.setParameter(1, electionEventPk);
		return query.getResultList();
	}

	public ReportingUnit findByMvElectionMvArea(Long mvElectionPk, Long mvAreaPk) {
		TypedQuery<ReportingUnit> query = getEm().createNamedQuery("ReportingUnit.findByMvElectionMvArea", ReportingUnit.class);
		query.setParameter(PARAM_MV_ELECTION_PK, mvElectionPk);
		query.setParameter(PARAM_MV_AREA_PK, mvAreaPk);
		return query.getSingleResult();
	}

	public ReportingUnit findCountElectoralBoardByContest(Contest contest) {
		return getEm()
				.createNamedQuery("ReportingUnit.findCountElectoralBoardByContest", ReportingUnit.class)
				.setParameter("contest", contest)
				.getSingleResult();
	}

	public List<ReportingUnit> findAlleValgstyrerIValghendelse(ElectionEvent valghendelse) {
		return getEm()
			.createNamedQuery("ReportingUnit.findAlleValgstyrerIValghendelse", ReportingUnit.class)
			.setParameter("electionEventPk", valghendelse.getPk())
				.getResultList();
	}
	
	public List<ReportingUnit> finnAlleFylkesvalgstyrerForValghendelse(ValghendelseSti valghendelseSti) {
		return getEm()
				.createNamedQuery("ReportingUnit.finnAlleFylkesvalgstyrerForValghendelse", ReportingUnit.class)
				.setParameter("electionEventId", valghendelseSti.valghendelseId())
				.getResultList();
	}
	
	public List<ReportingUnitType> findAllReportingUnitTypes() {
		return super.findAllEntities(ReportingUnitType.class);
	}

	public ReportingUnit findReportingUnitByAreaLevel(AreaPath areaPath) {
		List<ReportingUnitType> types = findReportingUnitTypesByAreaLevel(areaPath.getLevel());
		if (types.size() != 1) {
			throw new EvoteException("Unable to find single ReportingUnitType based on areaPath " + areaPath.path());
		}
		return findByAreaPathAndType(areaPath, types.get(0).reportingUnitTypeId());
	}

	private List<ReportingUnitType> findReportingUnitTypesByAreaLevel(AreaLevelEnum areaLevel) {
		return getEm()
				.createNamedQuery("ReportingUnitType.findByAreaLevel", ReportingUnitType.class)
				.setParameter("areaLevel", areaLevel.getLevel()).getResultList();
	}

	public ReportingUnitType findReportingUnitTypeByPk(long reportingUnitTypePk) {
		return super.findEntityByPk(ReportingUnitType.class, reportingUnitTypePk);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, ReportingUnit.class, pk);
	}

	public ReportingUnit byAreaPathElectionPathAndType(AreaPath areaPath, ElectionPath electionPath, ReportingUnitTypeId typeId) {
		TypedQuery<ReportingUnit> query = getEm().createNamedQuery("ReportingUnit.byAreaElectionAndType", ReportingUnit.class);
		query.setParameter("areaPath", areaPath.path());
		query.setParameter("electionPath", electionPath.path());
		query.setParameter("typeId", typeId.getId());
		return query.getSingleResult();
	}

	public List<ReportingUnit> finnOpptellingsvalgstyrer(ElectionEvent valghendelse) {
		 return getEm()
			.createNamedQuery("ReportingUnit.findOpptellingsvalgstyrer", ReportingUnit.class)
			.setParameter("electionEventPk", valghendelse.getPk()).getResultList();
	}
}
