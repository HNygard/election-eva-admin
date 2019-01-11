package no.valg.eva.admin.counting.builder;

import lombok.Getter;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class CountBuilder<C extends AbstractCount, B extends CountBuilder<C, B>> {
    @Getter
    private C count;
    @Getter
    private B self;

    CountBuilder(CountCategory category, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
        init(category, areaPath, areaName, reportingUnitAreaName, manualCount);
    }

    private void init(CountCategory category, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
        this.count = initCount(category, areaPath, areaName, reportingUnitAreaName, manualCount);
        this.self = initSelf();
    }

	protected abstract C initCount(CountCategory category, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount);

	protected abstract B initSelf();
	
	public C build() {
		return count;
	}

    B applyVotings(Collection<Voting> votings, DailyMarkOffCounts dailyMarkOffCounts) {
        Map<LocalDate, DailyMarkOffCount> dailyMarkOffCountMap = new HashMap<>();
        for (DailyMarkOffCount dailyMarkOffCount : dailyMarkOffCounts) {
            dailyMarkOffCountMap.put(dailyMarkOffCount.getDate(), dailyMarkOffCount);
        }
        // included to support testing on non-election days where VO votings may occur on non-election days
        int extraMarkOffCount = 0;
        for (Voting voting : votings) {
            LocalDate electionDate = voting.getCastTimestamp().toLocalDate();
            DailyMarkOffCount dailyMarkOffCount = dailyMarkOffCountMap.get(electionDate);
            if (dailyMarkOffCount != null) {
                dailyMarkOffCount.incrementMarkOffCount();
            } else {
                extraMarkOffCount++;
            }
        }
        dailyMarkOffCounts.setExtraMarkOffCount(extraMarkOffCount);
        return self;
    }
}
