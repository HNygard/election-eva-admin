package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.common.mockups.ContestMockups.defaultContest;
import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.CONTEST_REPORT_PK_SERIES;
import static no.valg.eva.admin.common.mockups.ReportingUnitMockups.reportingUnit;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.ContestReport;

public final class ContestReportMockups {

	public static final long CONTEST_REPORT_PK = CONTEST_REPORT_PK_SERIES + 1;

	public static ContestReport contestReport(final Contest contest, final ReportingUnit reportingUnit) {
		ContestReport contestReport = new ContestReport();
		contestReport.setPk(CONTEST_REPORT_PK);
		contestReport.setContest(contest);
		contestReport.setReportingUnit(reportingUnit);
		return contestReport;
	}

	public static ContestReport defaultContestReport() {
		return contestReport(defaultContest(electionEvent()), reportingUnit());
	}

	private ContestReportMockups() {
		// no instances allowed
	}
}
