package no.valg.eva.admin.common.reporting.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;

public interface JasperReportService extends Serializable {
	ReportTemplate getReportTemplate(UserData userData, String uri);

	ReportTemplate getReportTemplate(UserData userData, ValghendelsesRapport rapport);

	ReportExecution executeReport(UserData userData, String reportUri, Map<String, String> parameters, String format);

	Collection<SelectableReportParameterValue> getSelectableValuesForParameter(UserData userData, ReportParameter reportParameter, String reportUri);

	ReportExecution pollReportExecution(UserData userData, ReportExecution reportInProgress);

	Map<String, String> getCanonicalReportParameterParentIdMap(UserData userData);
}
