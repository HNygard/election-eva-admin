package no.valg.eva.admin.counting.domain.service.contestinfo;

import java.util.function.Predicate;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.model.ContestInfo;

/**
 * For filtrering av ContestInfo objekter basert på areaLevelFilter. Bydeler tas også med hvis det filtreres på kommune.
 */
class ContestInfoPredicate implements Predicate<ContestInfo> {

	private AreaLevelEnum areaLevelFilter;

	ContestInfoPredicate(AreaLevelEnum areaLevelFilter) {
		this.areaLevelFilter = areaLevelFilter;
	}

	@Override
	public boolean test(ContestInfo contestInfo) {
		if (areaLevelFilter == null || areaLevelFilter == AreaLevelEnum.ROOT) {
			return true;
		}
		return contestInfo.getAreaLevel() == areaLevelFilter || contestInfo.getAreaLevel() == AreaLevelEnum.BOROUGH
				&& areaLevelFilter == AreaLevelEnum.MUNICIPALITY;
	}
}
