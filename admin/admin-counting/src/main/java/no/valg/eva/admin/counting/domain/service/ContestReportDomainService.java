package no.valg.eva.admin.counting.domain.service;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.repository.ContestReportRepository;

public class ContestReportDomainService {
	private ContestReportRepository contestReportRepository;

	@Inject
	public ContestReportDomainService(ContestReportRepository contestReportRepository) {
		this.contestReportRepository = contestReportRepository;
	}

	public List<ContestReport> findFinalContestReportsByContest(Contest contest) {
		List<ContestReport> result = new ArrayList<>();
		List<ContestReport> contestReports = contestReportRepository.findByContest(contest);
		ReportingUnitTypeId finalReportingUnitTypeId = finalReportingUnitTypeIdFor(contest);
		for (ContestReport contestReport : contestReports) {
			ReportingUnit reportingUnit = contestReport.getReportingUnit();
			if (isFinalReportingUnit(reportingUnit, finalReportingUnitTypeId)) {
				result.add(contestReport);
			}
		}
		return result;
	}

	private ReportingUnitTypeId finalReportingUnitTypeIdFor(Contest contest) {
		if (!contest.isSingleArea()) {
			return OPPTELLINGSVALGSTYRET;
		}
		if (contest.isOnCountyLevel()) {
			return FYLKESVALGSTYRET;
		}
		return VALGSTYRET;
	}

	private boolean isFinalReportingUnit(ReportingUnit reportingUnit, ReportingUnitTypeId finalReportingUnitTypeId) {
		ReportingUnitTypeId reportingUnitTypeId = reportingUnit.getReportingUnitType().reportingUnitTypeId();
		return reportingUnitTypeId == finalReportingUnitTypeId;
	}
}
