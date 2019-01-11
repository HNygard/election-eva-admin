package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.evote.model.views.ContestRelArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;

public class ReportingUnitServiceBean {
	@Inject
	private ContestRelAreaRepository contestRelAreaRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;

	public List<ReportingUnit> getAccessibleReportingUnits(MvElection mvElection, MvArea mvArea) {
		List<ReportingUnit> reportingUnits = new ArrayList<>();
		for (ContestRelArea cra : contestRelAreaRepository.findAllAllowed(mvElection, mvArea)) {
			ReportingUnit ru = reportingUnitRepository.getReportingUnit(cra);
			if (ru != null && !reportingUnits.contains(ru)) {
				reportingUnits.add(ru);
			}
		}
		return reportingUnits;
	}
}
