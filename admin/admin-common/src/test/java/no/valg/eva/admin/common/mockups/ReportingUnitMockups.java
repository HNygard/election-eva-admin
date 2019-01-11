package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.REPORTING_UNIT_PK_SERIES;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;

public final class ReportingUnitMockups {

	public static final long REPORTING_UNIT_PK_1 = REPORTING_UNIT_PK_SERIES + 1;
	public static final long REPORTING_UNIT_PK_2 = REPORTING_UNIT_PK_SERIES + 2;

	private ReportingUnitMockups() {
		// no instances allowed
	}

	public static ReportingUnit reportingUnit() {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setPk(REPORTING_UNIT_PK_1);
		ReportingUnitType reportingUnitType = new ReportingUnitType();
		reportingUnitType.setId(ReportingUnitTypeId.VALGSTYRET.getId());
		reportingUnit.setReportingUnitType(reportingUnitType);
		reportingUnit.setMvArea(new MvArea());
		reportingUnit.setMvElection(new MvElection());
		return reportingUnit;
	}

	public static ReportingUnit reportingUnit(final MvArea mvArea) {
		ReportingUnit reportingUnit = reportingUnit();
		reportingUnit.setMvArea(mvArea);
		reportingUnit.setPk(REPORTING_UNIT_PK_2);
		return reportingUnit;
	}
}
