package no.valg.eva.admin.configuration.domain;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class OmraadehierarkiObjectMother {

	public static final String COUNTRY_PATH_DEFAULT = "123456.12";
	public static final String COUNTY_PATH_DEFAULT = "123456.12.12";
	public static final String COUNTY_PATH_0 = "123456.12.00";
	public static final String COUNTY_PATH_1 = "123456.12.01";
	public static final String COUNTY_PATH_2 = "123456.12.02";
	public static final String COUNTY_PATH_3 = "123456.12.03";
	public static final String MUNICIPALITY_PATH_DEFAULT = "123456.12.12.1234";
	public static final String MUNICIPALITY_PATH_COUNTY_1 = "123456.12.01.1234";
	public static final String MUNICIPALITY_PATH_COUNTY_2 = "123456.12.02.1234";
	public static final String MUNICIPALITY_PATH_COUNTY_3 = "123456.12.03.1234";
	public static final String MUNICIPALITY_PATH_1 = "123456.12.12.1201";
	public static final String MUNICIPALITY_PATH_2 = "123456.12.12.1202";
	public static final String MUNICIPALITY_PATH_3 = "123456.12.12.1203";
	public static final String BOROUGH_PATH_1 = "123456.12.12.1234.123401";
	public static final String BOROUGH_PATH_2 = "123456.12.12.1234.123402";
	public static final String BOROUGH_PATH_3 = "123456.12.12.1234.123403";
	public static final String BOROUGH_PATH_DEFAULT = "123456.12.12.1234.123456";
	public static final String POLLING_DISTRICT_PATH_DEFAULT = "123456.12.12.1234.123456.5678";
	public static final String POLLING_DISTRICT_PATH_1 = "123456.12.12.1234.123456.5601";
	public static final String POLLING_DISTRICT_PATH_2 = "123456.12.12.1234.123456.5602";
	public static final String POLLING_DISTRICT_PATH_3 = "123456.12.12.1234.123456.5603";
	public static final String POLLING_DISTRICT_PATH_4 = "123456.12.12.1234.123456.5604";
	public static final String POLLING_DISTRICT_PATH_5 = "123456.12.12.1234.123456.5605";
	public static final String POLLING_DISTRICT_PATH_6 = "123456.12.12.1234.123456.5606";

	public static final String COUNTY_NAME_DEFAULT = "countyNameDefault";
	public static final String COUNTY_NAME_1 = "countyName1";
	public static final String COUNTY_NAME_2 = "countyName2";
	public static final String COUNTY_NAME_3 = "countyName3";
	public static final String MUNICIPALITY_NAME_1 = "municipalityName1";
	public static final String MUNICIPALITY_NAME_2 = "municipalityName2";
	public static final String MUNICIPALITY_NAME_3 = "municipalityName3";
	public static final String CURRENT_MUNICIPALITY_NAME = "currentMunicipalityName";
	public static final String BOROUGH_NAME_1 = "boroughName1";
	public static final String BOROUGH_NAME_2 = "boroughName2";
	public static final String BOROUGH_NAME_3 = "boroughName3";
	public static final String BOROUGH_NAME_DEFAULT = "boroughName";
	public static final String POLLING_DISTRICT_NAME_1 = "pollingDistrictName1";
	public static final String POLLING_DISTRICT_NAME_2 = "pollingDistrictName2";
	public static final String POLLING_DISTRICT_NAME_3 = "pollingDistrictName3";
	public static final String MUNICIPALITY_POLLING_DISTRICT_NAME = "municipalityPollingDistrictName";
	public static final String PARENT_POLLING_DISTRICT_NAME = "parentPollingDistrictName";
	public static final String TECHNICAL_POLLING_DISTRICT_NAME = "technicalPollingDistrictName";
	public static final String REGULAR_POLLING_DISTRICT_NAME = "regularPollingDistrictName";
	public static final String POLLING_DISTRICT_NAME_DEFAULT = "pollingDistrictName";
	public static final String CHILD_POLLING_DISTRICT_NAME_1 = "childPollingDistrictName1";
	public static final String CHILD_POLLING_DISTRICT_NAME_2 = "childPollingDistrictName2";

	private OmraadehierarkiObjectMother() { /* Not possible to instantiate */ }
	
	public static MvArea mvArea(String areaPath) {
		return mvArea(areaPath, null, null, null);
	}

	public static MvArea mvArea(String areaPath, String name) {
		return mvArea(areaPath, name, null, null);
	}

    public static MvArea mvArea(String areaPath, String name, Municipality municipality) {
        return mvArea(areaPath, name, municipality, null, null);
    }

	public static MvArea mvArea(String areaPath, String name, Borough borough) {
		return mvArea(areaPath, name, borough, null);
	}

	public static MvArea mvArea(String areaPath, String name, PollingDistrict pollingDistrict) {
		return mvArea(areaPath, name, null, pollingDistrict);
	}

    public static MvArea mvArea(String path, String name, Borough borough, PollingDistrict pollingDistrict) {
        return mvArea(path, name, null, borough, pollingDistrict);
    }

    public static MvArea mvArea(String path, String name, Municipality municipality, Borough borough, PollingDistrict pollingDistrict) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(randomPk());
		mvArea.setAreaPath(path);
		AreaPath areaPath = AreaPath.from(path);
		AreaLevelEnum areaLevelEnum = areaPath.getLevel();
		mvArea.setAreaLevel(areaLevelEnum.getLevel());
		switch (areaLevelEnum) {
			case COUNTY:
				mvArea.setCountyName(name);
				break;
			case MUNICIPALITY:
				String municipalityId = areaPath.getMunicipalityId();
				mvArea.setMunicipality(municipality(municipalityId));
				mvArea.setMunicipalityId(municipalityId);
				mvArea.setMunicipalityName(name);
				break;
			case BOROUGH:
				mvArea.setBoroughName(name);
				break;
			case POLLING_DISTRICT:
				mvArea.setPollingDistrictName(name);
				break;
            case POLLING_PLACE:
                mvArea.setPollingPlaceName(name);
                break;
			default:
		}
        if (municipality != null) {
            mvArea.setMunicipality(municipality);
            mvArea.setMunicipalityId(municipality.getId());
            mvArea.setMunicipalityName(municipality.getName());
        }
		mvArea.setBorough(borough);
		mvArea.setPollingDistrict(pollingDistrict);
		return mvArea;
	}

    public static Municipality municipality(String municipalityId) {
        return municipality(municipalityId, null);
    }

    public static Municipality municipality(String municipalityId, String municipalityName) {
		Municipality municipality = new Municipality();
		municipality.setPk(randomPk());
		municipality.setId(municipalityId);
        municipality.setName(municipalityName);
		return municipality;
	}

	public static Borough borough(boolean municipality, PollingDistrict... pollingDistricts) {
		Borough borough = new Borough();
		borough.setMunicipality1(municipality);
		Set<PollingDistrict> pollingDistrictSet = new HashSet<>();
		Collections.addAll(pollingDistrictSet, pollingDistricts);
		borough.setPollingDistricts(pollingDistrictSet);
		return borough;
	}

	public static PollingDistrict pollingDistrict(PollingDistrict parent, boolean municipality, boolean parentPollingDistrict, boolean technicalPollingDistrict) {
		PollingDistrict pollingDistrict = pollingDistrict(municipality, parentPollingDistrict, technicalPollingDistrict);
		pollingDistrict.setPollingDistrict(parent);
		return pollingDistrict;
	}

	public static PollingDistrict pollingDistrict(boolean municipality, boolean parentPollingDistrict, boolean technicalPollingDistrict) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setPk(randomPk());
		pollingDistrict.setMunicipality(municipality);
		pollingDistrict.setParentPollingDistrict(parentPollingDistrict);
		pollingDistrict.setTechnicalPollingDistrict(technicalPollingDistrict);
		return pollingDistrict;
	}

	public static ContestArea contestArea(MvArea mvArea) {
		ContestArea contestArea = new ContestArea();
		contestArea.setPk(randomPk());
		contestArea.setMvArea(mvArea);
		return contestArea;
	}

	public static ContestArea contestArea(MvArea mvArea, boolean parentArea, boolean childArea) {
		ContestArea contestArea = new ContestArea();
		contestArea.setPk(randomPk());
		contestArea.setMvArea(mvArea);
		contestArea.setParentArea(parentArea);
		contestArea.setChildArea(childArea);
		return contestArea;
	}

	public static long randomPk() {
		return new Random().nextLong();
	}
}
