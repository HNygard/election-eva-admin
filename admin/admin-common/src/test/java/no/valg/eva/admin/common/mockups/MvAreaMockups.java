package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.MV_AREA_PK_SERIES;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public final class MvAreaMockups {

	public static final long CONTEST_MV_AREA_PK = MV_AREA_PK_SERIES + 1;
	public static final long POLLING_DISTRICT_MV_AREA_PK = MV_AREA_PK_SERIES + 2;
	
	public static final long MUNICIPALITY_MV_AREA_PK = MV_AREA_PK_SERIES + 3;
	public static final String MV_AREA_PATH_POLLING_DISTRICT = "730001.47.01.0101.010100.0001";
	public static final String MV_AREA_PATH_POLLING_DISTRICT_OTHER = "340001.47.01.0101.010100.0001";
	public static final String MV_AREA_PATH_MUNICIPALITY = "730001.47.01.0101";
	public static final String MV_AREA_PATH_COUNTRY = "730001.47";
	public static final String MV_AREA_PATH_OSLO_MUNICIPALITY = "730001.47.03.0301";
	public static final String MV_AREA_PATH_BOROUGH = "730001.47.03.0301.030101";
	public static final String MV_AREA_MUNICIPALITY_NAME = "SomeMunicipalityName";
	private static final long BOROUGH_MV_AREA_PK = MV_AREA_PK_SERIES + 4;
	private static final Boolean PARENT_POLLING_DISTRICT_FALSE = false;

	

	private MvAreaMockups() {
		// no instances allowed
	}

	public static MvArea mvArea(final Long mvAreaPk, final Municipality municipality, final int areaLevel, final String areaPath) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(mvAreaPk);
		mvArea.setMunicipality(municipality);
		mvArea.setAreaLevel(areaLevel);
		mvArea.setAreaPath(areaPath);
		mvArea.setParentPollingDistrict(PARENT_POLLING_DISTRICT_FALSE);
		mvArea.setPollingDistrict(PollingDistrictMockups.pollingDistrict());
		mvArea.setMunicipalityName(MV_AREA_MUNICIPALITY_NAME);
		return mvArea;
	}

	public static MvArea mvBoroughArea(final Long mvAreaPk, final Borough borough, final int areaLevel, final String areaPath) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(mvAreaPk);
		mvArea.setMunicipality(borough.getMunicipality());
		mvArea.setBorough(borough);
		mvArea.setAreaLevel(areaLevel);
		mvArea.setAreaPath(areaPath);
		return mvArea;
	}

	public static MvArea boroughMvArea(final Borough borough) {
		return mvBoroughArea(BOROUGH_MV_AREA_PK, borough, AreaLevelEnum.BOROUGH.getLevel(), MV_AREA_PATH_BOROUGH);
	}

	public static MvArea pollingDistrictMvArea() {
		return pollingDistrictMvArea(MunicipalityMockups.municipality(true));
	}

	public static MvArea pollingDistrictMvArea(final Municipality municipality) {
		return mvArea(POLLING_DISTRICT_MV_AREA_PK, municipality, AreaLevelEnum.POLLING_DISTRICT.getLevel(), MV_AREA_PATH_POLLING_DISTRICT);
	}

	public static MvArea municipalityMvArea() {
		return municipalityMvArea(MunicipalityMockups.municipality(true));
	}

	public static MvArea municipalityMvArea(final Municipality municipality) {
		return mvArea(MUNICIPALITY_MV_AREA_PK, municipality, AreaLevelEnum.MUNICIPALITY.getLevel(), MV_AREA_PATH_MUNICIPALITY);
	}
}
