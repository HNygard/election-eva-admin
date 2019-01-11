package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ReportingUnit;

public final class ReportingUnitMapper {

	private ReportingUnitMapper() {
	}

	public static ReportingUnit toReportingUnit(no.valg.eva.admin.configuration.domain.model.ReportingUnit dbReportingUnit) {
		ReportingUnit result = new ReportingUnit(AreaPath.from(dbReportingUnit.getMvArea().getAreaPath()), dbReportingUnit.reportingUnitTypeId(),
				dbReportingUnit.getAuditOplock());
		result.setPk(dbReportingUnit.getPk());
		result.setNameLine(dbReportingUnit.getNameLine());
		result.setAreaName(dbReportingUnit.getMvArea().getAreaName());
		result.setAddress(dbReportingUnit.getAddressLine1());
		result.setPostalCode(dbReportingUnit.getPostalCode());
		result.setPostTown(dbReportingUnit.getPostTown());
		return result;
	}
}
