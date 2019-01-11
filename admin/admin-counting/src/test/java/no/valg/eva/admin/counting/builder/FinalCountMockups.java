package no.valg.eva.admin.counting.builder;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.counting.builder.BallotCountMockups.dtoNewBallotCounts;
import static no.valg.eva.admin.counting.builder.RejectedBallotCountMockups.rejectedBallotCountList;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;

public final class FinalCountMockups {

	private FinalCountMockups() {
	}

	public static FinalCount finalCount(
			CountStatus status,
			int blankBallotCount,
			Boolean manualCount,
			String comment,
			AreaPath areaPath,
			String areaName,
			String reportingUnitAreaName,
			String id,
			ReportingUnitTypeId reportingUnitTypeId) {

		FinalCount finalCount = new FinalCount(
				id,
				areaPath,
				VO,
				areaName,
				reportingUnitTypeId,
				reportingUnitAreaName, manualCount);
		finalCount.setStatus(status);
		finalCount.setBlankBallotCount(blankBallotCount);
		finalCount.setBallotCounts(dtoNewBallotCounts());
		finalCount.setRejectedBallotCounts(rejectedBallotCountList());
		finalCount.setComment(comment);
		return finalCount;
	}

	public static FinalCount finalCount(CountStatus countStatus, AreaPath areaPath) {
		return finalCount(countStatus, 0, true, "", areaPath, "", "", "FVO1", ReportingUnitTypeId.FYLKESVALGSTYRET);
	}
}
