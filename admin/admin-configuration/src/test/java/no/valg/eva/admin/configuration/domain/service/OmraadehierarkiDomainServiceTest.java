package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.factory.BoroughFilterFactory;
import no.valg.eva.admin.configuration.domain.model.factory.PollingDistrictFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum;
import no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_NAME_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_NAME_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_NAME_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_NAME_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_PATH_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_PATH_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_PATH_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTRY_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_NAME_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_NAME_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_NAME_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_NAME_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_PATH_0;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_PATH_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_PATH_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_PATH_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.COUNTY_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.CURRENT_MUNICIPALITY_NAME;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_NAME_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_NAME_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_NAME_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_COUNTY_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_COUNTY_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_COUNTY_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_POLLING_DISTRICT_NAME;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.PARENT_POLLING_DISTRICT_NAME;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_NAME_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_PATH_1;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_PATH_2;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_PATH_3;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_PATH_4;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.POLLING_DISTRICT_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.REGULAR_POLLING_DISTRICT_NAME;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.TECHNICAL_POLLING_DISTRICT_NAME;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.borough;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.contestArea;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.pollingDistrict;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_EVENT_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_NAME_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_1;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.mvElection;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.userData;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.userDataOpptellingsvalgstyret;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OmraadehierarkiDomainServiceTest extends MockUtilsTestCase {

	private MvArea borough1;
	private MvArea borough2;
	private MvArea borough3;

	@BeforeTest
	public void setup() {
		this.borough1 = mvArea(BOROUGH_PATH_1, BOROUGH_NAME_1, borough(true));
		this.borough2 = mvArea(BOROUGH_PATH_2, BOROUGH_NAME_2, borough(false));
		this.borough3 = mvArea(BOROUGH_PATH_3, BOROUGH_NAME_3, borough(false));
	}
	
	@Test
	public void getCountiesFor_givenUserDataElectionPathAndCountryPath_returnCountiesPicker() throws Exception {
		ElectionPath electionPath = ElectionPath.from(ELECTION_PATH_DEFAULT);
		AreaPath countryPath = AreaPath.from(COUNTRY_PATH_DEFAULT);

		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvElectionRepository stubMvElectionRepository = getInjectMock(MvElectionRepository.class);

        List<MvArea> expectedCountyList = prepareMvAreaRepository(countryPath, getInjectMock(MvAreaRepository.class));

		ValghierarkiSti valghierarkiSti = ValghierarkiSti.fra(electionPath);
		ValggeografiSti valggeografiSti1 = ValggeografiSti.fra(AreaPath.from(COUNTY_PATH_1));
		ValggeografiSti valggeografiSti2 = ValggeografiSti.fra(AreaPath.from(COUNTY_PATH_2));
		ValggeografiSti valggeografiSti3 = ValggeografiSti.fra(AreaPath.from(COUNTY_PATH_3));
		when(stubMvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, valggeografiSti1)).thenReturn(true);
		when(stubMvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, valggeografiSti2)).thenReturn(true);
		when(stubMvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, valggeografiSti3)).thenReturn(true);

        List<MvArea> actualCountyList = omraadehierarkiDomainService.getCountiesFor(userData(ELECTION_EVENT_PATH_DEFAULT), electionPath, countryPath, VO);

        assertThat(actualCountyList).isEqualTo(expectedCountyList);
	}

	@Test
	public void getCountiesFor_givenUserDataForOpptellingsvalgstyret_returnCountiesThatHaveMunicipalitiesWithNonChildContestAreasInContest() throws Exception {
		AreaPath countryPath = AreaPath.from(COUNTRY_PATH_DEFAULT);
		ElectionPath electionPath = ElectionPath.from(ELECTION_PATH_DEFAULT);
		MvElection election = mvElection(ELECTION_PATH_DEFAULT, ELECTION_NAME_DEFAULT);

		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvElectionRepository stubMvElectionRepository = getInjectMock(MvElectionRepository.class);
		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		ReportCountCategoryRepository stubReportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		ContestAreaDomainService stubContestAreaDomainService = getInjectMock(ContestAreaDomainService.class);

		MvArea countyWithOpptellingskommune			= mvArea(COUNTY_PATH_DEFAULT, COUNTY_NAME_DEFAULT);
		MvArea countyWith30PlussKommune				= mvArea(COUNTY_PATH_1, COUNTY_NAME_1);
		MvArea countyWith30MinusKommune				= mvArea(COUNTY_PATH_2, COUNTY_NAME_2);
		MvArea countyWithMunicipalityNotInContest 	= mvArea(COUNTY_PATH_3, COUNTY_NAME_3);

		MvArea municipalityOpptellingskommune 	= mvArea(MUNICIPALITY_PATH_DEFAULT, CURRENT_MUNICIPALITY_NAME);
		MvArea municipality30MinusKommune 		= mvArea(MUNICIPALITY_PATH_1, MUNICIPALITY_NAME_1);
		MvArea municipality30PlussKommune		= mvArea(MUNICIPALITY_PATH_2, MUNICIPALITY_NAME_2);
		MvArea municipalityIkkeIContest			= mvArea(MUNICIPALITY_PATH_3, MUNICIPALITY_NAME_3);

		ContestArea contestAreaOpptellingskommune 	= contestArea(municipalityOpptellingskommune, true, false);
		ContestArea contestArea30MinusKommune 		= contestArea(municipality30MinusKommune, false, true);
		ContestArea contestArea30PlussKommune		= contestArea(municipality30PlussKommune, false, false);
		UserData userData = userDataOpptellingsvalgstyret();

		// Lookup of all counties
		when(stubMvAreaRepository.findByPathAndLevel(countryPath, COUNTY))
				.thenReturn(asList(countyWithOpptellingskommune, countyWith30PlussKommune, countyWith30MinusKommune, countyWithMunicipalityNotInContest));

		// Lookup of municipalities in each county
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWithOpptellingskommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(singletonList(municipalityOpptellingskommune));
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWith30PlussKommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(singletonList(municipality30PlussKommune));
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWith30MinusKommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(singletonList(municipality30MinusKommune));
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath()), MUNICIPALITY))
				.thenReturn(singletonList(municipalityIkkeIContest));

		// Lookup of each municipality
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWithOpptellingskommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(municipalityOpptellingskommune);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWith30PlussKommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(municipality30PlussKommune);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWith30MinusKommune.getAreaPath()), MUNICIPALITY))
				.thenReturn(municipality30MinusKommune);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath()), MUNICIPALITY))
				.thenReturn(municipalityIkkeIContest);

		// Lookup of the connected election
		when(stubMvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(election);

		// Lookup of report count categories for each county
		when(stubReportCountCategoryRepository
				.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWithOpptellingskommune.getAreaPath())), anyLong(), eq(VO)))
				.thenReturn(singletonList(reportCountCategory(municipalityOpptellingskommune)));
		when(stubReportCountCategoryRepository
				.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWith30PlussKommune.getAreaPath())), anyLong(), eq(VO)))
				.thenReturn(singletonList(reportCountCategory(municipality30PlussKommune)));
		when(stubReportCountCategoryRepository
				.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWith30MinusKommune.getAreaPath())), anyLong(), eq(VO)))
				.thenReturn(singletonList(reportCountCategory(municipality30MinusKommune)));
		when(stubReportCountCategoryRepository
				.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath())), anyLong(), eq(VO)))
				.thenReturn(singletonList(reportCountCategory(municipalityIkkeIContest)));

		// Lookup of contest areas
		when(stubContestAreaDomainService.contestAreasFor(userData.getOperatorElectionPath()))
				.thenReturn(asList(contestAreaOpptellingskommune, contestArea30MinusKommune, contestArea30PlussKommune));

		List<MvArea> actual = omraadehierarkiDomainService.getCountiesFor(userData, electionPath, countryPath, VO);

		List<MvArea> expected = asList(countyWithOpptellingskommune, countyWith30PlussKommune);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getCountiesFor_givenUserDataForOpptellingsvalgstyretOgIngenMinus30Kommuner_returnCountiesThatHaveMunicipalitiesWithNonChildContestAreasInContest()
			throws Exception {
		AreaPath countryPath = AreaPath.from(COUNTRY_PATH_DEFAULT);
		ElectionPath electionPath = ElectionPath.from(ELECTION_PATH_DEFAULT);
		MvElection election = mvElection(ELECTION_PATH_DEFAULT, ELECTION_NAME_DEFAULT);

		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvElectionRepository stubMvElectionRepository = getInjectMock(MvElectionRepository.class);
		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		ReportCountCategoryRepository stubReportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		ContestAreaDomainService stubContestAreaDomainService = getInjectMock(ContestAreaDomainService.class);

		MvArea countyWithOpptellingskommune			= mvArea(COUNTY_PATH_0, COUNTY_NAME_DEFAULT);
		countyWithOpptellingskommune.setCountyId("00");
		MvArea countyWith30PlussKommune				= mvArea(COUNTY_PATH_1, COUNTY_NAME_1);
		MvArea countyWithMunicipalityNotInContest 	= mvArea(COUNTY_PATH_3, COUNTY_NAME_3);
		
		MvArea municipalityOpptellingskommune 	= mvArea(MUNICIPALITY_PATH_DEFAULT, CURRENT_MUNICIPALITY_NAME);
		MvArea municipality30PlussKommune		= mvArea(MUNICIPALITY_PATH_2, MUNICIPALITY_NAME_2);
		MvArea municipalityIkkeIContest			= mvArea(MUNICIPALITY_PATH_3, MUNICIPALITY_NAME_3);
		
		ContestArea contestAreaOpptellingskommune 	= contestArea(municipalityOpptellingskommune, true, false);
		ContestArea contestArea30PlussKommune		= contestArea(municipality30PlussKommune, false, false);
		UserData userData = userDataOpptellingsvalgstyret();
		
		// Lookup of all counties
		when(stubMvAreaRepository.findByPathAndLevel(countryPath, COUNTY))
			.thenReturn(asList(countyWithOpptellingskommune, countyWith30PlussKommune, countyWithMunicipalityNotInContest));

		// Lookup of municipalities in each county
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWithOpptellingskommune.getAreaPath()), MUNICIPALITY))
			.thenReturn(singletonList(municipalityOpptellingskommune));
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWith30PlussKommune.getAreaPath()), MUNICIPALITY))
			.thenReturn(singletonList(municipality30PlussKommune));
		when(stubMvAreaRepository.findByPathAndLevel(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath()), MUNICIPALITY))
			.thenReturn(singletonList(municipalityIkkeIContest));
		
		// Lookup of each municipality
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWithOpptellingskommune.getAreaPath()), MUNICIPALITY))
			.thenReturn(municipalityOpptellingskommune);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWith30PlussKommune.getAreaPath()), MUNICIPALITY))
			.thenReturn(municipality30PlussKommune);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath()), MUNICIPALITY))
			.thenReturn(municipalityIkkeIContest);
		
		// Lookup of the connected election
		when(stubMvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(election);
		
		// Lookup of report count categories for each county
		when(stubReportCountCategoryRepository
			.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWithOpptellingskommune.getAreaPath())), anyLong(), eq(FO)))
			.thenReturn(singletonList(reportCountCategory(municipalityOpptellingskommune)));
		when(stubReportCountCategoryRepository
			.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWith30PlussKommune.getAreaPath())), anyLong(), eq(FO)))
			.thenReturn(singletonList(reportCountCategory(municipality30PlussKommune)));
		when(stubReportCountCategoryRepository
			.findByCountyElectionGroupAndCountCategory(eq(AreaPath.from(countyWithMunicipalityNotInContest.getAreaPath())), anyLong(), eq(FO)))
			.thenReturn(singletonList(reportCountCategory(municipalityIkkeIContest)));
		
		// Lookup of contest areas
		when(stubContestAreaDomainService.contestAreasFor(userData.getOperatorElectionPath()))
			.thenReturn(asList(contestAreaOpptellingskommune, contestArea30PlussKommune));

		List<MvArea> actual = omraadehierarkiDomainService.getCountiesFor(userData, electionPath, countryPath, FO);

		List<MvArea> expected = singletonList(countyWith30PlussKommune);
		assertThat(actual).isEqualTo(expected);
	}

	private List<MvArea> prepareMvAreaRepository(AreaPath countryPath, MvAreaRepository stubMvAreaRepository) {
		MvArea county1 = mvArea(COUNTY_PATH_1, COUNTY_NAME_1);
		MvArea county2 = mvArea(COUNTY_PATH_2, COUNTY_NAME_2);
		MvArea county3 = mvArea(COUNTY_PATH_3, COUNTY_NAME_3);

		MvArea municipality1 = mvArea(MUNICIPALITY_PATH_COUNTY_1);
		MvArea municipality2 = mvArea(MUNICIPALITY_PATH_COUNTY_2);
		MvArea municipality3 = mvArea(MUNICIPALITY_PATH_COUNTY_3);

		when(stubMvAreaRepository.findByPathAndLevel(countryPath, COUNTY)).thenReturn(asList(county1, county2, county3));
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(COUNTY_PATH_1), MUNICIPALITY)).thenReturn(municipality1);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(COUNTY_PATH_2), MUNICIPALITY)).thenReturn(municipality2);
		when(stubMvAreaRepository.findFirstByPathAndLevel(AreaPath.from(COUNTY_PATH_3), MUNICIPALITY)).thenReturn(municipality3);
		
		return asList(county1, county2, county3);
	}

	@Test
	public void getMunicipalitiesFor_givenUserDataAndCountyPath_returnsMunicipalitiesInCounty() throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		ReportCountCategoryRepository stubReportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		AreaPath countyPath = new AreaPath(COUNTY_PATH_DEFAULT);
		MvArea municipality1 = mvArea(MUNICIPALITY_PATH_1, MUNICIPALITY_NAME_1);
		MvArea municipality2 = mvArea(MUNICIPALITY_PATH_2, MUNICIPALITY_NAME_2);

		when(stubMvAreaRepository.findByPathAndLevel(countyPath, MUNICIPALITY)).thenReturn(asList(municipality1, municipality2));
		when(stubReportCountCategoryRepository.findByCountyElectionGroupAndCountCategory(eq(countyPath), anyLong(), eq(VO)))
				.thenReturn(asList(reportCountCategory(municipality1), reportCountCategory(municipality2)));

		List<MvArea> actual = omraadehierarkiDomainService.getMunicipalitiesFor(userData(countyPath.path()), ElectionPath.from(ELECTION_PATH_1), countyPath, VO);

		List<MvArea> expected = asList(municipality1, municipality2);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getMunicipalitiesFor_givenUserDataForOpptellingsvalgstyret_returnsNonChildAreaMunicipalitiesInContestBasedOnOperatorRoleArea() throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		ReportCountCategoryRepository stubReportCountCategoryRepository = getInjectMock(ReportCountCategoryRepository.class);
		ContestAreaDomainService stubContestAreaDomainService = getInjectMock(ContestAreaDomainService.class);
		AreaPath countyPath = new AreaPath(COUNTY_PATH_DEFAULT);
		MvArea municipalityOpptellingskommune 	= mvArea(MUNICIPALITY_PATH_DEFAULT, CURRENT_MUNICIPALITY_NAME);
		MvArea municipality30MinusKommune 		= mvArea(MUNICIPALITY_PATH_1, MUNICIPALITY_NAME_1);
		MvArea municipality30PlussKommune		= mvArea(MUNICIPALITY_PATH_2, MUNICIPALITY_NAME_2);
		MvArea municipalityIkkeIContest			= mvArea(MUNICIPALITY_PATH_3, MUNICIPALITY_NAME_3);
		ContestArea contestAreaOpptellingskommune 	= contestArea(municipalityOpptellingskommune, true, false);
		ContestArea contestArea30MinusKommune 		= contestArea(municipality30MinusKommune, false, true);
		ContestArea contestArea30PlussKommune		= contestArea(municipality30PlussKommune, false, false);
		UserData userData = userDataOpptellingsvalgstyret();

		when(stubContestAreaDomainService.contestAreasFor(userData.getOperatorElectionPath()))
			.thenReturn(asList(contestAreaOpptellingskommune, contestArea30MinusKommune, contestArea30PlussKommune));
		when(stubReportCountCategoryRepository.findByCountyElectionGroupAndCountCategory(eq(countyPath), anyLong(), eq(VO)))
			.thenReturn(asList(
				reportCountCategory(municipalityOpptellingskommune),
				reportCountCategory(municipality30MinusKommune),
				reportCountCategory(municipality30PlussKommune),
				reportCountCategory(municipalityIkkeIContest)));
		when(stubMvAreaRepository.findByPathAndLevel(countyPath, MUNICIPALITY))
			.thenReturn(asList(municipalityOpptellingskommune, municipality30MinusKommune, municipality30PlussKommune, municipalityIkkeIContest));

		List<MvArea> actual = omraadehierarkiDomainService.getMunicipalitiesFor(userData, ElectionPath.from(ELECTION_EVENT_PATH_DEFAULT),
			countyPath, VO);

		List<MvArea> expected = asList(municipalityOpptellingskommune, municipality30PlussKommune);
		assertThat(actual).isEqualTo(expected);
	}

	private ReportCountCategory reportCountCategory(MvArea mvArea) {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setMunicipality(mvArea.getMunicipality());
		return reportCountCategory;
	}

	@Test(dataProvider = "municipalities")
	public void getMunicipalitiesFor_userOnHigherLevel_returnsCurrentMunicipality(String testCase,
																				  AreaPath operatorAreaPath) throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		AreaPath municipalityPath = operatorAreaPath.toMunicipalityPath();
		AreaPath countyPath = operatorAreaPath.toCountyPath();
		MvArea currentMunicipality = mvArea(municipalityPath.path(), CURRENT_MUNICIPALITY_NAME);

		when(stubMvAreaRepository.findSingleByPath(any(AreaPath.class))).thenReturn(currentMunicipality);

		List<MvArea> actual = omraadehierarkiDomainService.getMunicipalitiesFor(userData(operatorAreaPath.path()), ElectionPath.from(ELECTION_PATH_1), countyPath, VO);

		List<MvArea> expected = singletonList(currentMunicipality);
		assertThat(actual).isEqualTo(expected);
	}

	@DataProvider(name = "municipalities")
	public Object[][] municipalitiesTestData() {
		return new Object[][] {
			{ "userDataWithOperatorOnMunicipalityLevel", 	new AreaPath(MUNICIPALITY_PATH_DEFAULT) },
			{ "userDataWithOperatorOnPollingDistrictLevel",	new AreaPath(POLLING_DISTRICT_PATH_DEFAULT) }
		};
	}

	@Test(dataProvider = "boroughs")
	public void getBoroughsFor_givenUserDataAndMunicipalityPath_returnsAllBoroughsInMunicipality(@SuppressWarnings("UnusedParameters") String testCase,
			 Optional<BoroughFilterEnum> filter, List<MvArea> expectedList) throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		MvElectionRepository stubMvElectionRepository = getInjectMock(MvElectionRepository.class);
		BoroughFilterFactory stubBoroughFilterFactory = getInjectMock(BoroughFilterFactory.class);
		
		AreaPath municipalityPath = new AreaPath(MUNICIPALITY_PATH_DEFAULT);

		UserData userData = userData(MUNICIPALITY_PATH_DEFAULT);
		ElectionPath selectedElectionPath = ElectionPath.from(ELECTION_PATH_1);
		CountCategory selectedCountCategory = CountCategory.VO;

		when(stubMvAreaRepository.findByPathAndLevel(municipalityPath, BOROUGH)).thenReturn(asList(borough1, borough2, borough3));
		when(stubMvElectionRepository.hasContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class))).thenReturn(true);
		when(stubBoroughFilterFactory.build(selectedCountCategory, selectedElectionPath, municipalityPath)).thenReturn(filter);

        List<MvArea> actualList = omraadehierarkiDomainService.getBoroughsFor(userData, selectedCountCategory, selectedElectionPath, municipalityPath);

        assertThat(actualList).isEqualTo(expectedList);
	}

	@DataProvider(name = "boroughs")
	public Object[][] boroughsTestData() {
		return new Object[][] {
			{ "userDataAndMunicipalityPath", Optional.empty(), 									  asList(borough1, borough2, borough3) },
			{ "forBoroughElectionFilter",	 Optional.of(BoroughFilterEnum.FOR_BOROUGH_ELECTION), asList(borough2, borough3) }
		};
	}

	@Test
	public void getBoroughsFor_givenPollingDistrictOperator_returnsSingleBoroughsPicker() throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		MvElectionRepository stubMvElectionRepository = getInjectMock(MvElectionRepository.class);
		BoroughFilterFactory stubBoroughFilterFactory = getInjectMock(BoroughFilterFactory.class);
		
		AreaPath municipalityPath = new AreaPath(MUNICIPALITY_PATH_DEFAULT);
		MvArea borough = mvArea(BOROUGH_PATH_DEFAULT, BOROUGH_NAME_DEFAULT);

		UserData userData = userData(POLLING_DISTRICT_PATH_DEFAULT);
		ElectionPath selectedElectionPath = ElectionPath.from(ELECTION_PATH_1);
		CountCategory selectedCountCategory = CountCategory.VO;

		when(stubMvAreaRepository.findSingleByPath(any(AreaPath.class))).thenReturn(borough);
		when(stubMvElectionRepository.hasContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class))).thenReturn(true);
		when(stubBoroughFilterFactory.build(selectedCountCategory, selectedElectionPath, municipalityPath))
			.thenReturn(Optional.empty());
		
		List<MvArea> actual = omraadehierarkiDomainService.getBoroughsFor(userData, selectedCountCategory, selectedElectionPath, municipalityPath);

		List<MvArea> expected = singletonList(borough);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getPollingDistrictsFor_givenForCentralAndOperatorNotOnPollingDistrictFilter_returnsMunicipalityPollingDistrict() throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		PollingDistrictFilterFactory stubPollingDistrictFilterFactory = getInjectMock(PollingDistrictFilterFactory.class);
		
		AreaPath boroughPath = new AreaPath(BOROUGH_PATH_DEFAULT);
		MvArea pollingDistrict1 = mvArea(POLLING_DISTRICT_PATH_1, MUNICIPALITY_POLLING_DISTRICT_NAME, pollingDistrict(true, false, false));
		MvArea pollingDistrict2 = mvArea(POLLING_DISTRICT_PATH_2, PARENT_POLLING_DISTRICT_NAME, pollingDistrict(false, true, false));
		MvArea pollingDistrict3 = mvArea(POLLING_DISTRICT_PATH_3, TECHNICAL_POLLING_DISTRICT_NAME, pollingDistrict(false, false, true));
		MvArea pollingDistrict4 = mvArea(POLLING_DISTRICT_PATH_4, REGULAR_POLLING_DISTRICT_NAME, pollingDistrict(false, false, false));

		UserData userData = userData(MUNICIPALITY_PATH_DEFAULT);
		ElectionPath selectedElectionPath = ElectionPath.from(ELECTION_PATH_1);
		CountCategory selectedCountCategory = CountCategory.VO;

		when(stubPollingDistrictFilterFactory.build(userData, selectedCountCategory, selectedElectionPath, boroughPath))
			.thenReturn(PollingDistrictFilterEnum.FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT);
		when(stubMvAreaRepository.findByPathAndLevel(boroughPath, POLLING_DISTRICT))
			.thenReturn(asList(pollingDistrict1, pollingDistrict2, pollingDistrict3, pollingDistrict4));

		List<MvArea> actual = omraadehierarkiDomainService.getPollingDistrictsFor(userData, selectedCountCategory, selectedElectionPath, boroughPath);

		List<MvArea> expected = singletonList(pollingDistrict1);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getPollingDistrictsFor_givenPollingDistrictOperator_returnsSinglePollingDistrict() throws Exception {
		OmraadehierarkiDomainService omraadehierarkiDomainService = initializeMocks(OmraadehierarkiDomainService.class);

		MvAreaRepository stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		PollingDistrictFilterFactory stubPollingDistrictFilterFactory = getInjectMock(PollingDistrictFilterFactory.class);
		
		AreaPath boroughPath = new AreaPath(BOROUGH_PATH_DEFAULT);
		PollingDistrict pollingDistrict = new PollingDistrict();
		MvArea mvAreaPollingDistrict = mvArea(POLLING_DISTRICT_PATH_DEFAULT, POLLING_DISTRICT_NAME_DEFAULT, pollingDistrict);

		UserData userData = userData(POLLING_DISTRICT_PATH_DEFAULT);
		ElectionPath selectedElectionPath = ElectionPath.from(ELECTION_PATH_1);
		CountCategory selectedCountCategory = CountCategory.VO;

		when(stubPollingDistrictFilterFactory.build(userData, selectedCountCategory, selectedElectionPath, boroughPath)).thenReturn(PollingDistrictFilterEnum.DEFAULT);
		when(stubMvAreaRepository.findSingleByPath(AreaPath.from(POLLING_DISTRICT_PATH_DEFAULT))).thenReturn(mvAreaPollingDistrict);

		List<MvArea> actual = omraadehierarkiDomainService.getPollingDistrictsFor(userData, selectedCountCategory, selectedElectionPath, boroughPath);

		List<MvArea> expected =  singletonList(mvAreaPollingDistrict);
		assertThat(actual).isEqualTo(expected);
	}

}
