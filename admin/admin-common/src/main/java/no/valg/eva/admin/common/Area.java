package no.valg.eva.admin.common;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.AreaLevelEnum;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Area implements Serializable, Comparable<Area> {
	private static final Function<String, AreaLevelEnum> AREA_LEVEL_ENUM_FUNCTION = new Function<String, AreaLevelEnum>() {
		@Override
		public AreaLevelEnum apply(String input) {
			return AreaLevelEnum.valueOf(input);
		}
	};
	protected static final String EMPTY = "";
	
    protected AreaPath areaPath;
	protected String name;

	public Area(AreaPath areaPath, String name) {
        this.areaPath = areaPath;
        this.name = name;
	}
    
	public AreaPath getAreaPath() {
		return areaPath;
	}
	
	public boolean contains(Area area) {
		return this.areaPath.contains(area.getAreaPath());
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Area area = (Area) o;

		if (!areaPath.equals(area.areaPath)) {
			return false;
		}
		if (!name.equals(area.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = areaPath.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

    @Override
    public String toString() {
        return "areaPath=Area{" + areaPath + ", name='" + name + '\'' + '}';
    }

    @Override
	public int compareTo(Area o) {
		return o.areaPath.toString().compareTo(this.areaPath.toString());
	}

	/**
	 * Returns id and name of area if this area's level matches any of the supplied area levels
	 */
	public String getLeafIdForAreaLevels(AreaLevelEnum... areaLevels) {
		if (areaLevels != null) {
			for (AreaLevelEnum level : areaLevels) {
				if (level == areaPath.getLevel()) {
					return areaPath.getLeafId();
				}
			}
		}
		return EMPTY;
	}

	/**
	 * JSF friendly facade to {@link #getLeafIdForAreaLevels(AreaLevelEnum... areaLevels)}
	 * @param areaLevels Only render id for are levels contained here
	 * @return last member of area path if area level is mentioned in areaLevels parameter, otherwise empty string
	 */
	public String getLeafIdForAreaLevels(List<String> areaLevels) {
		return getLeafIdForAreaLevels(Lists.transform(areaLevels, AREA_LEVEL_ENUM_FUNCTION).toArray(new AreaLevelEnum[]{}));
	}
}
