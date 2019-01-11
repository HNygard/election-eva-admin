package no.valg.eva.admin.counting.domain.builder;

import java.util.function.Predicate;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class VoteCountFilterBuilder {
	public Predicate<VoteCount> voteCountFilterFor(CountCategory countCategory, AreaPath areaPath, StatusType statusType) {
		Predicate<VoteCount> predicate = voteCount -> hasCategoryAndAreaPath(countCategory, areaPath, voteCount);
		return predicate.and(voteCountFilterFor(statusType));
	}

	private boolean hasCategoryAndAreaPath(CountCategory countCategory, AreaPath areaPath, VoteCount voteCount) {
		return voteCount.getCountCategory() == countCategory && voteCount.getMvArea().getAreaPath().equals(areaPath.path());
	}

	private Predicate<VoteCount> voteCountFilterFor(StatusType statusType) {
		return voteCount -> {
			CountQualifier qualifier = CountQualifier.fromId(voteCount.getCountQualifier().getId());
			AreaLevelEnum reportingUnitAreaLevel = voteCount.getContestReport().getReportingUnit().getActualAreaLevel();
			return statusType.filter(qualifier, reportingUnitAreaLevel);
		};
	}
}
