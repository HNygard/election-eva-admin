package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.MvElectionReportingUnits;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;

public class MvElectionReportingUnitsRepository extends BaseRepository {
	public MvElectionReportingUnitsRepository() {
	}

	public MvElectionReportingUnitsRepository(EntityManager entityManager) {
		super(entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<MvElectionReportingUnits> findMvElectionReportingUnitByElectionAndType(Long reportingUnitTypePk, Long mvElectionPk) {
		Query query = getEm().createNativeQuery(
				"select * from mv_election_reporting_units where reporting_unit_type_pk = :reportingUnitTypePk and mv_election_pk = :mvElectionPk",
				MvElectionReportingUnits.class);
		query.setParameter("reportingUnitTypePk", reportingUnitTypePk);
		query.setParameter("mvElectionPk", mvElectionPk);
		return query.getResultList();
	}

	public MvElectionReportingUnits create(UserData userData, MvElectionReportingUnits mvElectionReportingUnits) {
		return super.createEntity(userData, mvElectionReportingUnits);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, MvElectionReportingUnits.class, pk);
	}

	public void updateMvElectionReportingUnits(UserData userData, List<MvElection> mvElectionList, ReportingUnitType reportingUnitType) {
		for (MvElection mvElection : mvElectionList) {
			if (shouldDeleteMvElectionReportingUnit(reportingUnitType.getPk(), mvElection)) {
				deleteMvElectionReportingUnit(userData, reportingUnitType.getPk(), mvElection);
			}

			if (shouldCreateMvElectionReportingUnit(reportingUnitType.getPk(), mvElection)) {
				createMvElectionReportingUnit(userData, reportingUnitType, mvElection);
			}
		}
	}

	private boolean shouldDeleteMvElectionReportingUnit(long reportingUnitTypePk, MvElection mvElection) {
		return !mvElection.isReportingUnit() && !this.findMvElectionReportingUnitByElectionAndType(reportingUnitTypePk, mvElection.getPk()).isEmpty();
	}

	private boolean shouldCreateMvElectionReportingUnit(long reportingUnitTypePk, MvElection mvElection) {
		return mvElection.isReportingUnit() && this.findMvElectionReportingUnitByElectionAndType(reportingUnitTypePk, mvElection.getPk()).isEmpty();
	}

	public void createMvElectionReportingUnit(UserData userData, ReportingUnitType reportingUnitType, MvElection mvElection) {
		MvElectionReportingUnits electionReportingUnits = new MvElectionReportingUnits();
		electionReportingUnits.setMvElection(mvElection);
		electionReportingUnits.setReportingUnitType(reportingUnitType);
		create(userData, electionReportingUnits);
	}

	public void deleteMvElectionReportingUnit(UserData userData, long reportingUnitTypePk, MvElection mvElection) {
		// delete
		MvElectionReportingUnits electionReportingUnits = this.findMvElectionReportingUnitByElectionAndType(reportingUnitTypePk, mvElection.getPk()).get(0);
		delete(userData, electionReportingUnits.getPk());
	}

	public boolean hasReportingUnitTypeConfigured(UserData userData, ReportingUnitTypeId reportingUnitTypeId) {
		Query query = getEm().createNamedQuery("MvElectionReportingUnits.countByReportingUnitTypeId");
		query.setParameter("electionEventPk", userData.getElectionEventPk());
		query.setParameter("reportingUnitTypeId", reportingUnitTypeId.getId());
		return ((Long) query.getSingleResult()) != 0;
	}
}
