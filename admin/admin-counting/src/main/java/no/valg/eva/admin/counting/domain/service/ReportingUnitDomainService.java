package no.valg.eva.admin.counting.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;

/**
 * Contains domain logic related to reporting units for areas and count configurations.
 */
public class ReportingUnitDomainService {

	private ReportingUnitRepository reportingUnitRepository;

	@Inject
	public ReportingUnitDomainService(ReportingUnitRepository reportingUnitRepository) {
		this.reportingUnitRepository = reportingUnitRepository;
	}

	public ReportingUnit reportingUnitForCountyFinalCount(AreaPath operatorAreaPath, AreaPath countingAreaPath, MvElection mvElectionContest) {
		return reportingUnitForFinalCount(reportingUnitTypeForCountyFinalCount(mvElectionContest), operatorAreaPath, countingAreaPath, mvElectionContest);
	}

	public ReportingUnit reportingUnitForFinalCount(ReportingUnitTypeId reportingUnitTypeId, AreaPath operatorAreaPath, AreaPath countingAreaPath,
			MvElection mvElectionContest) {

		if (operatorAreaPath.getLevel().getLevel() > MUNICIPALITY.getLevel()) {
			throw new IllegalStateException("Operator must be at municipality level or higher for counting final.");
		}
		if (reportingUnitTypeId == STEMMESTYRET) {
			throw new IllegalStateException("ReportingUnitId must be VALGSTYRET, FYLKESVALGSTYRET or OPPTELLINGSVALGSTYRET.");
		}

		if (reportingUnitTypeId == OPPTELLINGSVALGSTYRET) {
			return reportingUnitRepository.byAreaPathElectionPathAndType(
					AreaPath.from(operatorAreaPath.getElectionEventId()), ElectionPath.from(mvElectionContest.getElectionPath()), reportingUnitTypeId);
		}

		AreaPath areaPathForReportingUnit;
		if (reportingUnitTypeId == VALGSTYRET) {
			areaPathForReportingUnit = countingAreaPath.toMunicipalityPath();
		} else if (operatorAreaPath.getLevel() == ROOT && reportingUnitTypeId == FYLKESVALGSTYRET) {
			areaPathForReportingUnit = countingAreaPath.toCountyPath();
		} else {
			areaPathForReportingUnit = operatorAreaPath;
		}
		return reportingUnitRepository.findByAreaPathAndType(areaPathForReportingUnit, reportingUnitTypeId);
	}

	public ReportingUnit getReportingUnit(UserData userData, AreaPath areaPath) {
		if (areaPath.isRootLevel()) {
			return reportingUnitRepository.getReportingUnit(ElectionPath.from(userData.getOperatorMvElection().getElectionPath()), areaPath);
		} else {
			return reportingUnitRepository.findReportingUnitByAreaLevel(areaPath);
		}
	}

	/**
	 * @return operator area path or counting area path
	 */
	public AreaPath areaPathForFindingReportingUnit(ReportingUnitTypeId typeId, AreaPath operatorAreaPath, MvArea countingMvArea) {
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());
		if (typeId == OPPTELLINGSVALGSTYRET) {
			return AreaPath.from(operatorAreaPath.getElectionEventId());
		}
		if (typeId == STEMMESTYRET && POLLING_DISTRICT.lowerThan(operatorAreaPath.getLevel()) && countingAreaPath.isSubpathOf(operatorAreaPath)) {
			return countingAreaPath;
		}
		if (typeId == VALGSTYRET && MUNICIPALITY.lowerThan(operatorAreaPath.getLevel()) && countingAreaPath.isSubpathOf(operatorAreaPath)) {
			return countingAreaPath.toMunicipalityPath();
		}
		if (typeId == FYLKESVALGSTYRET && COUNTY.lowerThan(operatorAreaPath.getLevel()) && countingAreaPath.isSubpathOf(operatorAreaPath)) {
			return countingAreaPath.toCountyPath();
		}
		return operatorAreaPath;
	}

	private ReportingUnitTypeId reportingUnitTypeForCountyFinalCount(MvElection mvElectionContest) {
		return mvElectionContest.getContest().isSingleArea() ? FYLKESVALGSTYRET : OPPTELLINGSVALGSTYRET;
	}

}
