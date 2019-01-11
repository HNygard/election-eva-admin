package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates;

import no.valg.eva.admin.common.auditlog.auditevents.valgnatt.ValgnattAuditable;

public class CandidatesReport implements ValgnattAuditable {
	
	private final String jsonReport;

	public CandidatesReport(String jsonReport) {
		this.jsonReport = jsonReport;
	}

	@Override
	public String toJson() {
		return jsonReport;
	}
}
