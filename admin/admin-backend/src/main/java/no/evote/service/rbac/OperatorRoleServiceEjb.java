package no.evote.service.rbac;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.Collected;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent.addAuditEvent;
import static no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent.from;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Importer_Sentralt;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Tilganger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.evote.service.util.ExportImportOperatorsServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.ImportOperatorMessage;
import no.valg.eva.admin.common.rbac.OperatorExportFormat;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "OperatorRoleService")


@Default
@Remote(OperatorRoleService.class)
public class OperatorRoleServiceEjb implements OperatorRoleService {
	@Inject
	private OperatorRoleServiceBean operatorRoleService;
	@Inject
	private OperatorRoleRepository operatorRoleRepository;
	@Inject
	private ExportImportOperatorsServiceBean exportImportOperatorsService;
	@Inject
	private LocaleTextRepository localeTextRepository;
	@Inject
	private AccessRepository accessRepository;

	@Override
	@Security(accesses = Tilgang_Brukere_Administrere, type = READ)
	@AuditLog(eventClass = CompositeAuditEvent.class, eventType = Create, objectSource = Collected)
	public OperatorRole create(UserData userData, OperatorRole operatorRole) {
		OperatorRole createdOperatorRole = operatorRoleService.create(userData, operatorRole);
		addAuditEvent(operatorRoleCrudAuditEvent(userData, createdOperatorRole, Create));
		return createdOperatorRole;
	}

	@Override
	@SecurityNone
	public Long findUserCountForRole(Long rolePk) {
		return operatorRoleRepository.findUserCountForRole(rolePk);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Tilganger, type = READ)
	public List<OperatorRole> findOperatorRolesGivingOperatorAccess(UserData userData, MvArea mvArea, Operator operator, Access access) {
		no.valg.eva.admin.rbac.domain.model.Access entity = accessRepository.findAccessByPath(access.getPath());
		return operatorRoleRepository.findOperatorRolesGivingOperatorAccess(userData.getElectionEventPk(), mvArea, operator, entity);
	}

	@Override
	@Security(accesses = Tilgang_Brukere_Importer_Sentralt, type = READ)
	public byte[] exportOperatorRoles(UserData userData, Long electionEventPk, OperatorExportFormat format) {
		return exportImportOperatorsService.exportOperatorRoles(userData, electionEventPk, format);
	}

	@Override
	@Security(accesses = Tilgang_Brukere_Importer_Sentralt, type = WRITE)
	public List<ImportOperatorMessage> importOperatorRoles(UserData userData, Long electionEventPk, byte[] data) {
		return exportImportOperatorsService.importOperatorRoles(userData, electionEventPk, data);
	}

	@Override
	@SecurityNone
	public Map<ElectionEvent, List<OperatorRole>> getOperatorRolesPerElectionEvent(UserData userData) {
		return operatorRoleService.getOperatorRolesPerElectionEvent(userData);
	}

	private SimpleAuditEvent operatorRoleCrudAuditEvent(UserData userData, OperatorRole operatorRole, AuditEventTypes type) {
		MvArea mvArea = operatorRole.getMvArea();
		Locale locale = userData.getLocale();
		Long electionEventPk = operatorRole.getMvElection().getElectionEvent().getPk();
		return from(userData)
				.withProcess(CENTRAL_CONFIGURATION)
				.ofType(type)
				.withObjectType(OperatorRole.class)
				.withOutcome(Success)
				.withAuditObjectProperty("personId", operatorRole.getOperator().getId())
				.withAuditObjectProperty("roleName",
						localeTextRepository.findByElectionEventLocaleAndTextId(electionEventPk, locale.getPk(), operatorRole.getRole().getName())
								.getLocaleText())
				.withAuditObjectProperty("roleAreaName", mvArea.getAreaName())
				.withAuditObjectProperty("roleAreaPath", mvArea.getAreaPath())
				.build();
	}
}
