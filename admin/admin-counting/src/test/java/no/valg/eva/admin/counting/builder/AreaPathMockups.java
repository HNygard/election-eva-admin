package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.AreaPath;

public final class AreaPathMockups {
	public static final String AREA_PATH_COUNTY_OSLO = "730001.47.03";
	public static final String AREA_PATH_MUNICIPALITY_OSLO = "730001.47.03.0301";
	public static final String AREA_PATH_BOROUGH_VESTRE_AKER = "730001.47.03.0301.030107";
	public static final String AREA_PATH_BOROUGH_GAMLE_OSLO = "730001.47.03.0301.030101";

	private AreaPathMockups() {
	}

	public static AreaPath areaPath(String areaPath) {
		return new AreaPath(areaPath);
	}

}
