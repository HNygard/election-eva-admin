package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;

public class ContestReportRepository extends BaseRepository {

	private static final String PARAM_CPK = "cpk";
	private static final String PARAM_RUPK = "rupk";

	public ContestReportRepository() {
		// brukes av rammeverk
	}

	ContestReportRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<ContestReport> findByContest(Contest contest) {
		return getEm()
				.createNamedQuery("ContestReport.findByContest", ContestReport.class)
				.setParameter("cpk", contest.getPk())
				.getResultList();
	}

	public List<ContestReport> findByContestAndMunicipality(Contest contest, Municipality municipality) {
		return getEm()
				.createNamedQuery("ContestReport.findByContestAndMunicipality", ContestReport.class)
				.setParameter("contestPk", contest.getPk())
				.setParameter("municipalityPk", municipality.getPk())
				.getResultList();
	}

	public List<ContestReport> findByContestAndMvArea(Contest contest, MvArea mvArea) {
		return getEm()
				.createNamedQuery("ContestReport.findByContestAndMvArea", ContestReport.class)
				.setParameter("contestPk", contest.getPk())
				.setParameter("mvAreaPk", mvArea.getPk())
				.getResultList();
	}

	public List<ContestReport> byContestInArea(Contest contest, AreaPath areaPath) {
		return getEm()
				.createNamedQuery("ContestReport.byContestInArea", ContestReport.class)
				.setParameter("contestPk", contest.getPk())
				.setParameter("areaPath", areaPath.path())
				.getResultList();
	}

	public ContestReport findByPk(long pk) {
		return super.findEntityByPk(ContestReport.class, pk);
	}

	public ContestReport findByBallotCount(BallotCountRef ballotCountRef) {
		return (ContestReport) getEm().createNamedQuery("ContestReport.findByBallotCount").setParameter("ballotCountPk", ballotCountRef.getPk())
				.getSingleResult();
	}

	public ContestReport findByFinalCount(Long countPk) {
		return (ContestReport) getEm().createNamedQuery("ContestReport.findByCountPk").setParameter(PARAM_CPK, countPk).getSingleResult();
	}

	public boolean hasContestReport(Long contestPk) {
		return getEm()
				.createNamedQuery("ContestReport.countByContest", Long.class)
				.setParameter(PARAM_CPK, contestPk)
				.getSingleResult()
				.intValue() > 0;
	}

	public boolean hasContestReport(Contest contest, ReportingUnit reportingUnit) {
		Query query = getEm().createNamedQuery("ContestReport.findByReportingUnitContest").setParameter(PARAM_RUPK, reportingUnit.getPk())
				.setParameter(PARAM_CPK,
						contest.getPk());
		return !query.getResultList().isEmpty();
	}

	public ContestReport findByReportingUnitContest(Long rupk, Long cpk) {
		try {
			TypedQuery<ContestReport> query = getEm().createNamedQuery("ContestReport.findByReportingUnitContest", ContestReport.class)
					.setParameter(PARAM_RUPK, rupk).setParameter(PARAM_CPK, cpk);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public ContestReport update(UserData userData, ContestReport contestReport) {
		return super.updateEntity(userData, contestReport);
	}

	public void delete(UserData userData, Long contestReportPk) {
		super.deleteEntity(userData, ContestReport.class, contestReportPk);
	}

	public ContestReport create(UserData userData, ContestReport contestReport) {
		return super.createEntity(userData, contestReport);
	}

	public ContestReport create(ContestReport contestReport) {
		getEm().persist(contestReport);
		getEm().flush();
		getEm().refresh(contestReport);
		return contestReport;
	}

	public List<ContestReport> findByElectionGroupAndMunicipality(ElectionGroup electionGroup, Municipality municipality) {
		return getEm()
				.createNamedQuery("ContestReport.findByElectionGroupAndMunicipality", ContestReport.class)
				.setParameter("electionGroupPk", electionGroup.getPk())
				.setParameter("municipalityPk", municipality.getPk())
				.getResultList();
	}

	public List<ContestReport> finnForValghendelseStiOgStyretype(ValghendelseSti valghendelseSti, Styretype styretype) {
		return getEm().createNamedQuery("ContestReport.finnForValghendelseIdOgStyretype", ContestReport.class)
				.setParameter("election_event_id", valghendelseSti.valghendelseId())
				.setParameter("reporting_unit_type_id", styretype.id())
				.getResultList();
	}
}
