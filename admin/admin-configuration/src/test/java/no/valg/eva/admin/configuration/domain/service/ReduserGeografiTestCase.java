package no.valg.eva.admin.configuration.domain.service;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;

public class ReduserGeografiTestCase extends MockUtilsTestCase {

	protected static final long FYLKE_1_PK = 1L;
	protected static final long FYLKE_2_PK = 2L;
	protected static final long FYLKE_3_PK = 3L;

	protected static final long KOMMUNE_1_PK = 11L;
	protected static final long KOMMUNE_2_PK = 12L;
	protected static final long KOMMUNE_3_PK = 13L;
	
	protected static final long KRETS_1_PK = 21L;
	protected static final long KRETS_2_PK = 22L;
	protected static final long KRETS_3_PK = 23L;

	protected static final long STEMMESTED_1_PK = 31L;
	protected static final long STEMMESTED_2_PK = 32L;
	protected static final long STEMMESTED_3_PK = 33L;

	protected GeografiSpesifikasjon geografiSpesifikasjon;
	
	@BeforeMethod
	protected void setup() throws Exception {
		geografiSpesifikasjon = geografiSpesifikasjon();
	}

	protected GeografiSpesifikasjon geografiSpesifikasjon() {
		List<String> beholdKommuner = asList("0101", "0102");
		List<String> beholdKretser = asList("0201.0001", "0201.0002");
		List<String> beholdStemmesteder = asList("0201.0004.0000", "0201.0004.0001");
		return new GeografiSpesifikasjon(beholdKommuner, beholdKretser, beholdStemmesteder);
	}

	protected List<MvArea> fylker() {
		List<MvArea> fylker = new ArrayList<>();
		fylker.add(fylke("123456.47.01", FYLKE_1_PK));
		fylker.add(fylke("123456.47.02", FYLKE_2_PK));
		fylker.add(fylke("123456.47.03", FYLKE_3_PK));
		return fylker;
	}

	protected MvArea fylke(String areaPath, Long countyPk) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);
		mvArea.setAreaLevel(COUNTY.getLevel());
		County fylke = new County();
		fylke.setPk(countyPk);
		mvArea.setCounty(fylke);
		mvArea.setCountyId(AreaPath.from(areaPath).getCountyId());
		return mvArea;
	}

	protected List<MvArea> kommuner() {
		List<MvArea> kommuner = new ArrayList<>();
		kommuner.add(kommune("123456.47.02.0201", KOMMUNE_1_PK));
		kommuner.add(kommune("123456.47.02.0202", KOMMUNE_2_PK));
		kommuner.add(kommune("123456.47.02.0203", KOMMUNE_3_PK));
		return kommuner;
	}

	protected MvArea kommune(String areaPath, Long municipalityPk) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);
		mvArea.setAreaLevel(MUNICIPALITY.getLevel());
		Municipality kommune = new Municipality();
		kommune.setPk(municipalityPk);
		mvArea.setMunicipality(kommune);
		mvArea.setMunicipalityId(AreaPath.from(areaPath).getMunicipalityId());
		return mvArea;
	}

	protected List<MvArea> kretser() {
		List<MvArea> kretser = new ArrayList<>();
		kretser.add(krets("123456.47.02.0201.020100.0001", KRETS_1_PK));
		kretser.add(krets("123456.47.02.0201.020100.0002", KRETS_2_PK));
		kretser.add(krets("123456.47.01.0201.020100.0003", KRETS_3_PK));
		return kretser;
	}
	
	protected MvArea krets(String areaPath, Long pollingDistrictId) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);
		mvArea.setAreaLevel(POLLING_DISTRICT.getLevel());
		PollingDistrict krets = new PollingDistrict();
		krets.setPk(pollingDistrictId);
		mvArea.setPollingDistrict(krets);
		mvArea.setPollingDistrictId(AreaPath.from(areaPath).getPollingDistrictId());
		mvArea.setMunicipalityId(AreaPath.from(areaPath).getMunicipalityId());
		return mvArea;
	}
	
	protected List<MvArea> stemmesteder() {
		List<MvArea> stemmesteder = new ArrayList<>();
		stemmesteder.add(stemmested("123456.47.02.0201.020100.0004.0000", STEMMESTED_1_PK));
		stemmesteder.add(stemmested("123456.47.02.0201.020100.0004.0001", STEMMESTED_2_PK));
		stemmesteder.add(stemmested("123456.47.01.0201.020100.0004.0002", STEMMESTED_3_PK));
		return stemmesteder;
	}

	protected MvArea stemmested(String areaPath, Long pollingPlaceId) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);
		mvArea.setAreaLevel(POLLING_PLACE.getLevel());
		PollingPlace stemmested = new PollingPlace();
		stemmested.setPk(pollingPlaceId);
		mvArea.setPollingPlace(stemmested);
		mvArea.setPollingPlaceId(AreaPath.from(areaPath).getPollingPlaceId());
		mvArea.setPollingDistrictId(AreaPath.from(areaPath).getPollingDistrictId());
		mvArea.setMunicipalityId(AreaPath.from(areaPath).getMunicipalityId());
		return mvArea;
	}

	protected ValghendelseSti valghendelseSti() {
		return new ValghendelseSti("123456");
	}


}
