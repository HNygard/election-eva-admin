package no.valg.eva.admin.frontend.picker.cfg;

import static no.evote.constants.AreaLevelEnum.ROOT;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;

@Deprecated
public class AreaCfg {

	private Set<AreaLevelEnum> areaLevels = new HashSet<>();
	private boolean hideLeadingSingles;

	public AreaCfg() {
		// convenience
	}

	public AreaCfg addLevel(AreaLevelEnum areaLevelEnum) {
		areaLevels.add(areaLevelEnum);
		return this;
	}

	public Set<AreaLevelEnum> getAreaLevels() {
		return areaLevels;
	}

	public AreaLevelEnum getAreaLevel() {
		if (areaLevels.isEmpty()) {
			return null;
		} else if (areaLevels.size() == 1) {
			return areaLevels.iterator().next();
		}
		AreaLevelEnum high = ROOT;
		for (Iterator<AreaLevelEnum> iter = areaLevels.iterator(); iter.hasNext();) {
			AreaLevelEnum level = iter.next();
			if (level.getLevel() > high.getLevel()) {
				high = level;
			}
		}
		return high;
	}

	public Integer getLevel() {
		return getAreaLevel() == null ? null : getAreaLevel().getLevel();
	}

	public boolean isMultiSelect() {
		return areaLevels.size() > 1;
	}

	public boolean isHideLeadingSingles() {
		return hideLeadingSingles;
	}

	public AreaCfg setHideLeadingSingles(boolean hideLeadingSingles) {
		this.hideLeadingSingles = hideLeadingSingles;
		return this;
	}
}
