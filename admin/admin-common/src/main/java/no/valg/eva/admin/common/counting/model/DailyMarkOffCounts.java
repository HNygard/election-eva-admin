package no.valg.eva.admin.common.counting.model;

import java.util.ArrayList;
import java.util.List;

public class DailyMarkOffCounts extends ArrayList<DailyMarkOffCount> {
	// included to support testing on non-election days where VO votings may occur on non-election days
	private int extraMarkOffCount;

	public DailyMarkOffCounts() {
		super();
	}

	public DailyMarkOffCounts(List<DailyMarkOffCount> dailyMarkOffCountList) {
		super();
		for (DailyMarkOffCount dailyMarkOffCount : dailyMarkOffCountList) {
			add(dailyMarkOffCount);
		}
	}

	public int getMarkOffCount() {
		int markOffCount = 0;
		for (DailyMarkOffCount dailyMarkOffCount : this) {
			markOffCount += dailyMarkOffCount.getMarkOffCount();
		}
		return markOffCount + extraMarkOffCount;
	}

	public void setExtraMarkOffCount(int extraMarkOffCount) {
		this.extraMarkOffCount = extraMarkOffCount;
	}
}
