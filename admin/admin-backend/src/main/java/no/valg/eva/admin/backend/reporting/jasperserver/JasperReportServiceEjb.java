package no.valg.eva.admin.backend.reporting.jasperserver;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Rapport;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.Collection;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.ReportAuditEvent;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;
import no.valg.eva.admin.common.reporting.service.JasperReportService;

@Stateless(name = "JasperReportService")
@Remote(JasperReportService.class)
public class JasperReportServiceEjb implements JasperReportService {
	@Inject
	private JasperReportServiceBean jasperReportServiceBean;

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	public ReportTemplate getReportTemplate(UserData userData, String uri) {
		return jasperReportServiceBean.getReportTemplate(userData, uri);
	}

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	public ReportTemplate getReportTemplate(UserData userData, ValghendelsesRapport rapport) {
		return jasperReportServiceBean.getReportTemplate(userData, rapport);
	}

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	@AuditLog(eventClass = ReportAuditEvent.class, eventType = AuditEventTypes.GenerateReport, objectSource = AuditedObjectSource.ReturnValue)
	public ReportExecution executeReport(UserData userData, String reportUri, Map<String, String> parameters, String format) {
		return jasperReportServiceBean.executeReport(userData, reportUri, parameters, format);
	}

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	public ReportExecution pollReportExecution(UserData userData, ReportExecution reportInProgress) {
		return jasperReportServiceBean.pollReportExecution(userData, reportInProgress);
	}

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	public Collection<SelectableReportParameterValue> getSelectableValuesForParameter(UserData userData, ReportParameter reportParameter, String reportUri) {
		return jasperReportServiceBean.getSelectableValuesForParameter(userData, reportParameter, reportUri);
	}

	@Override
	@Security(accesses = Aggregert_Rapport, type = READ)
	public Map<String, String> getCanonicalReportParameterParentIdMap(UserData userData) {
		return jasperReportServiceBean.getCanonicalReportParameterParentIdMap();
	}
}
