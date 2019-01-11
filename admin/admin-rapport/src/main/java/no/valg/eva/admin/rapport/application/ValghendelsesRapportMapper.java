package no.valg.eva.admin.rapport.application;

import java.util.List;

import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;

public class ValghendelsesRapportMapper {

	public ValghendelsesRapport fromReport(Report report, List<ElectionEventReport> electionEventReports) {
		ValghendelsesRapport result = new ValghendelsesRapport(report.getId(), report.getCategory(), report.getAccess().toViewObject());
		ElectionEventReport electionEventReport = getElectionEventReport(report, electionEventReports);
		if (electionEventReport != null) {
			result.setSynlig(true);
			result.setTilgjengelig(true);
		}
		return result;
	}

	private ElectionEventReport getElectionEventReport(Report report, List<ElectionEventReport> electionEventReports) {
		return electionEventReports.stream().filter(eventReport -> eventReport.getReport().equals(report)).findFirst().orElse(null);
	}

}
