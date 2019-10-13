package no.evote.service.configuration;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent.addAuditEvent;
import static no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent.from;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Styrer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.dto.ReportingUnitTypeDto;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.repository.MvElectionReportingUnitsRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ReportingUnitTypeService")



@Default
@Remote(ReportingUnitTypeService.class)
public class ReportingUnitTypeServiceEjb implements ReportingUnitTypeService {
	@Inject
	private MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private ReportingUnitTypeServiceBean reportingUnitTypeService;

	/**
	 * Used to display information in GUI.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Styrer, type = READ)
	public List<ReportingUnitTypeDto> populateReportingUnitTypeDto(UserData userData, String electionEventId) {
		return reportingUnitTypeService.populateReportingUnitTypeDto(electionEventId);
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Opptelling, type = READ)
	public List<ReportingUnitType> findAll(UserData userData) {
		return reportingUnitRepository.findAllReportingUnitTypes();
	}

	/**
	 * The table mv_election_reporting_units has triggers that inserts/removes rows in to reporting_unit
	 */
	@Override
	@Security(accesses = Konfigurasjon_Styrer, type = WRITE)
	@AuditLog(eventClass = CompositeAuditEvent.class, eventType = AuditEventTypes.Update, objectSource = AuditedObjectSource.Collected)
	public void updateMvElectionReportingUnits(
			UserData userData, @SecureEntity(electionLevelDynamic = true) List<MvElection> mvElectionList, long reportingUnitTypePk) {
		ReportingUnitType reportingUnitType = reportingUnitRepository.findReportingUnitTypeByPk(reportingUnitTypePk);
		updateMvElectionReportingUnits(userData, mvElectionList, reportingUnitType);
	}

	private void updateMvElectionReportingUnits(UserData userData, List<MvElection> mvElectionList, ReportingUnitType reportingUnitType) {
		for (MvElection mvElection : mvElectionList) {
			if (shouldDeleteMvElectionReportingUnit(reportingUnitType.getPk(), mvElection)) {
				mvElectionReportingUnitsRepository.deleteMvElectionReportingUnit(userData, reportingUnitType.getPk(), mvElection);
				addAuditEvent(electionAndReportingTypeAuditEvent(userData, reportingUnitType, mvElection).ofType(Delete).build());
			}

			if (shouldCreateMvElectionReportingUnit(reportingUnitType.getPk(), mvElection)) {
				mvElectionReportingUnitsRepository.createMvElectionReportingUnit(userData, reportingUnitType, mvElection);
				addAuditEvent(electionAndReportingTypeAuditEvent(userData, reportingUnitType, mvElection).ofType(Create).build());
			}
		}
	}

	private SimpleAuditEvent.Builder electionAndReportingTypeAuditEvent(UserData userData, ReportingUnitType reportingUnitType, MvElection mvElection) {
		return from(userData)
				.withObjectType(ReportingUnitType.class)
				.withOutcome(Success)
				.withProcess(CENTRAL_CONFIGURATION)
				.withAuditObjectProperty("electionPath", mvElection.getElectionPath())
				.withAuditObjectProperty("name", mvElection.getNamedPath())
				.withAuditObjectProperty("reportingUnitType",  reportingUnitType.reportingUnitTypeId().name());
	}

	private boolean shouldDeleteMvElectionReportingUnit(long reportingUnitTypePk, MvElection mvElection) {
		return !mvElection.isReportingUnit()
				&& !mvElectionReportingUnitsRepository.findMvElectionReportingUnitByElectionAndType(reportingUnitTypePk, mvElection.getPk()).isEmpty();
	}

	private boolean shouldCreateMvElectionReportingUnit(long reportingUnitTypePk, MvElection mvElection) {
		return mvElection.isReportingUnit()
				&& mvElectionReportingUnitsRepository.findMvElectionReportingUnitByElectionAndType(reportingUnitTypePk, mvElection.getPk()).isEmpty();
	}

}
