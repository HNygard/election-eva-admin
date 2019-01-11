package no.valg.eva.admin.counting.domain.builder;

import java.util.function.Predicate;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;

public class VoteCountDigestFilterBuilder {
	public Predicate<VoteCountDigest> voteCountDigestFilterFor(CountCategory countCategory, AreaPath areaPath, StatusType statusType) {
		Predicate<VoteCountDigest> predicate = voteCountDigest -> hasCategoryAndAreaPath(countCategory, areaPath, voteCountDigest);
		return predicate.and(voteCountDigestFilterFor(statusType));
	}

	private boolean hasCategoryAndAreaPath(CountCategory countCategory, AreaPath areaPath, VoteCountDigest voteCountDigest) {
		return voteCountDigest.getCountCategory() == countCategory && voteCountDigest.getAreaPath().equals(areaPath);
	}

	private Predicate<VoteCountDigest> voteCountDigestFilterFor(StatusType statusType) {
		return voteCountDigest -> {
			CountQualifier qualifier = voteCountDigest.getCountQualifier();
			AreaLevelEnum reportingUnitAreaLevel = voteCountDigest.getReportingUnitAreaLevel();
			return statusType.filter(qualifier, reportingUnitAreaLevel);
		};
	}
}
