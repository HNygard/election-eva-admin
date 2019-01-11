package no.valg.eva.admin.common;

import static no.valg.eva.admin.common.AreaPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import no.evote.constants.AreaLevelEnum;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class AreaPathTest {

	public static final String AREA_PATH_COUNTY = "201301.47.01";
	private static final String AREA_PATH_POLLING_DISTRICT = "201301.47.03.0301.030101.0101";
	private static final String AREA_PATH_TECHNICAL_POLLING_DISTRICT = "201301.47.03.0301.030100.0001";
	private static final String AREA_PATH_MUNICIPALITY_POLLING_DISTRICT = "201301.47.03.0301.030100.0000";
	private static final String AREA_PATH_MUNICIPALITY_POLLING_STATION = "201301.47.03.0301.030100.0000.0001.02";

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void from_withInvalidPath_throwsIllegalArgumentException() {
		AreaPath.from("invalid");
	}

	@Test
	public void from_withelectionEventCountryCountyAndMuniciplity_returnsValidAreaPath() {
		AreaPath path = AreaPath.from("201301", "47", "03", AreaPath.OSLO_MUNICIPALITY_ID);
		assertThat(path.getElectionEventId()).isEqualTo("201301");
		assertThat(path.getCountryId()).isEqualTo("47");
		assertThat(path.getCountyId()).isEqualTo("03");
		assertThat(path.getMunicipalityId()).isEqualTo(AreaPath.OSLO_MUNICIPALITY_ID);
	}

	@Test
	public void anAreaPathContainsALeafNode() {
		AreaPath county = new AreaPath(AREA_PATH_COUNTY);
		AreaPath municipality = new AreaPath("201301.47.01.0101");
		assertThat(county.contains(municipality)).isTrue();
	}

	@Test
	public void anAreaPathIsPartOfAParentNode() {
		AreaPath county = new AreaPath(AREA_PATH_COUNTY);
		AreaPath municipality = new AreaPath("201301.47.01.0101");
		assertThat(municipality.isSubpathOf(county)).isTrue();
	}

	@Test
	public void anAreaPathDoesNotContainALeafNode() {
		AreaPath county = new AreaPath(AREA_PATH_COUNTY);
		AreaPath municipality = new AreaPath("201301.47.03.0301");
		assertThat(county.contains(municipality)).isFalse();
	}

	@Test
	public void anAreaPathIsNotPartOfAParentNode() {
		AreaPath county = new AreaPath(AREA_PATH_COUNTY);
		AreaPath municipality = new AreaPath("201301.47.03.0301");
		assertThat(municipality.isSubpathOf(county)).isFalse();
	}

	@Test
	public void pathToMunicipality() {
		AreaPath municipality = new AreaPath("201301.47.03.0301");
		AreaPath pollingDistrict = new AreaPath("201301.47.03.0301.030101.0101");
		assertThat(pollingDistrict.toMunicipalityPath()).isEqualTo(municipality);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void pathToMunicipalityWhenAbove() {
		AreaPath county = new AreaPath("201301.47.03");
		county.toMunicipalityPath();
	}

	@Test
	public void addingSubPathReturnsNewPath() {
		AreaPath county = new AreaPath("201301.47.03");
		AreaPath municipality = new AreaPath("201301.47.03.0301");
		assertThat(county.add(AreaPath.OSLO_MUNICIPALITY_ID)).isEqualTo(municipality);
	}

	@Test
	public void getParent_returnsParentPath() {
		AreaPath pollingDistrict = new AreaPath("201301.47.03.0301.030101.1234");
		assertEquals(pollingDistrict.getParentPath().path(), "201301.47.03.0301.030101");
	}

	@Test
	public void isMunicipalityPollingDistrict_whenPollingDistrictIdIs0000_returnsTrue() {
		String pollingDistrictId = "0000";
		AreaPath path = new AreaPath("123456.78.90.1234.567890." + pollingDistrictId);

		boolean isMunicipalityPollingDistrict = path.isMunicipalityPollingDistrict();

		assertThat(isMunicipalityPollingDistrict).isTrue();
	}

	@Test
	public void isMunicipalityPollingDistrict_whenPollingDistrictIdIsNot0000_returnsFalse() {
		String pollingDistrictId = "1234";
		AreaPath path = new AreaPath("123456.78.90.1234.567890." + pollingDistrictId);

		boolean isMunicipalityPollingDistrict = path.isMunicipalityPollingDistrict();

		assertThat(isMunicipalityPollingDistrict).isFalse();
	}

	@Test
	public void isMunicipalityPollingDistrict_whenPollingDistrictIdIsNull_returnsFalse() {
		AreaPath path = new AreaPath("123456.78.90.1234.567890");

		boolean isMunicipalityPollingDistrict = path.isMunicipalityPollingDistrict();

		assertThat(isMunicipalityPollingDistrict).isFalse();
	}

	@Test
	public void isCentralEnvelopeRegistrationPollingPlace_whenPollingPlaceIdIs9999_returnsTrue() {
		String pollingPlaceId = "9999";
		AreaPath path = new AreaPath("123456.78.90.1234.567890.0000." + pollingPlaceId);

		boolean centralEnvelopeRegistrationPollingPlace = path.isCentralEnvelopeRegistrationPollingPlace();

		assertThat(centralEnvelopeRegistrationPollingPlace).isTrue();
	}

	@Test
	public void isCentralEnvelopeRegistrationPollingPlace_whenPollingPlaceIdIs0001_returnsFalse() {
		String pollingPlaceId = "0001";
		AreaPath path = new AreaPath("123456.78.90.1234.567890.0000." + pollingPlaceId);

		boolean centralEnvelopeRegistrationPollingPlace = path.isCentralEnvelopeRegistrationPollingPlace();

		assertThat(centralEnvelopeRegistrationPollingPlace).isFalse();
	}

	@Test
	public void toMunicipalityPollingDistrictPath_givenMunicipalityPath_returnsMunicipalityPollingDistrictPath() throws Exception {
		AreaPath path = new AreaPath("123456.78.90.1234");
		AreaPath areaPath = path.toMunicipalityPollingDistrictPath();
		assertThat(areaPath.toString()).isEqualTo("123456.78.90.1234.123400.0000");
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "path to municipality cannot be found for <" + AREA_PATH_COUNTY + ">")
	public void toMunicipalityPollingDistrictPath_givenCountyPath_throwsException() throws Exception {
		AreaPath path = new AreaPath(AREA_PATH_COUNTY);
		path.toMunicipalityPollingDistrictPath();
	}

	@Test
	public void hashCode_givenAreaPath_returnsPathHashCode() throws Exception {
		String pathString = AREA_PATH_COUNTY;
		AreaPath path = new AreaPath(pathString);
		int hashCode = path.hashCode();
		assertThat(hashCode).isEqualTo(pathString.hashCode());
	}

	@Test
	public void equals_givenSameObject_returnsTrue() throws Exception {
		AreaPath path = new AreaPath(AREA_PATH_COUNTY);
		boolean equals = path.equals(path);
		assertThat(equals).isTrue();
	}

	@Test
	public void equals_givenDifferentObjectType_returnsFalse() throws Exception {
		AreaPath path = new AreaPath(AREA_PATH_COUNTY);
		boolean equals = path.equals(new Object());
		assertThat(equals).isFalse();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "path to borough cannot be found for <123456.78.90.1234>")
	public void toPollingDistrictSubPath_givenMunicipalityPath_throwsException() throws Exception {
		AreaPath path = new AreaPath("123456.78.90.1234");
		path.toPollingDistrictSubPath(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "polling district id cannot be null")
	public void toPollingDistrictSubPath_givenNullPollingDistrictId_throwsException() throws Exception {
		AreaPath path = new AreaPath("123456.78.90.1234.567890");
		path.toPollingDistrictSubPath(null);
	}

	@Test
	public void validateAreaPath_givenValidLevel_doesNotThrowException() throws Exception {
		AreaPath path = new AreaPath("123456.78.90.1234");
		path.validateAreaPath(AreaLevelEnum.MUNICIPALITY, AreaLevelEnum.BOROUGH, AreaLevelEnum.POLLING_DISTRICT);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
		  expectedExceptionsMessageRegExp = "expected area path to be one of <\\[MUNICIPALITY, BOROUGH, POLLING_DISTRICT\\]>, but was <COUNTY>")
	public void validateAreaPath_givenInvalidLevel_throwsException() throws Exception {
		AreaPath path = new AreaPath(AREA_PATH_COUNTY);
		path.validateAreaPath(AreaLevelEnum.MUNICIPALITY, AreaLevelEnum.BOROUGH, AreaLevelEnum.POLLING_DISTRICT);
	}

	@Test
	public void isTechnicalPollingDistrict_whenNotPathToPollingDistrict_isFalse() {
		AreaPath path = new AreaPath(AREA_PATH_COUNTY);
		assertThat(path.isTechnicalPollingDistrict()).isFalse();
	}

	@Test
	public void isTechnicalPollingDistrict_whenPathToPollingDistrictAndBoroughIdDoesNotEqualMunicipalityIdPlus00_isFalse() {
		AreaPath path = new AreaPath(AREA_PATH_POLLING_DISTRICT);
		assertThat(path.isTechnicalPollingDistrict()).isFalse();
	}

	@Test
	public void isTechnicalPollingDistrict_whenPathToMunicipalityPollingDistrict_isFalse() {
		AreaPath path = new AreaPath(AREA_PATH_MUNICIPALITY_POLLING_DISTRICT);
		assertThat(path.isTechnicalPollingDistrict()).isFalse();
	}

	@Test
	public void isTechnicalPollingDistrict_whenPathToTechnicalPollingDistrict_isTrue() {
		AreaPath path = new AreaPath(AREA_PATH_TECHNICAL_POLLING_DISTRICT);
		assertThat(path.isTechnicalPollingDistrict()).isTrue();
	}

	@Test
	public void isTechnicalPollingDistrict_whenPathToPollingPlace_isFalse() {
		AreaPath path = new AreaPath(AREA_PATH_TECHNICAL_POLLING_DISTRICT + ".0001");
		assertThat(path.isTechnicalPollingDistrict()).isFalse();
	}

	@Test
	public void getRootPathReturnsRoot() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.ROOT), from("201301"));
	}

	@Test
	public void getCountryPathReturnsCountry() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.COUNTRY), from("201301.47"));
	}

	@Test
	public void getCountyPathReturnsCounty() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.COUNTY), from("201301.47.03"));
	}

	@Test
	public void getMunicipalityPathReturnsMunicipality() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.MUNICIPALITY), from("201301.47.03.0301"));
	}

	@Test
	public void getBoroughPathReturnsBorough() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.BOROUGH), from("201301.47.03.0301.030100"));
	}

	@Test
	public void getPollingDistrictPathReturnsPollingDistrict() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.POLLING_DISTRICT), from("201301.47.03.0301.030100.0000"));
	}

	@Test
	public void getPollingStationPathReturnsPollingStation() {
		assertEquals(from(AREA_PATH_MUNICIPALITY_POLLING_STATION).toAreaLevelPath(AreaLevelEnum.POLLING_STATION), from(AREA_PATH_MUNICIPALITY_POLLING_STATION));
	}

	@Test
	public void toAreaLevelPath_withPollingPlaceLevelOnPollingStationLevel_returnsPollingPlacePath() throws Exception {
		AreaPath areaPath = AreaPath.from(AREA_PATH_MUNICIPALITY_POLLING_STATION);

		areaPath = areaPath.toAreaLevelPath(AreaLevelEnum.POLLING_PLACE);

		assertThat(areaPath.getPollingStationId()).isNull();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toCountyPath_withNoCounty_throwsIllegalStateException() {
		AreaPath path = AreaPath.from("201301.47");

		path.toCountyPath();
	}

	@Test
	public void toCountyPath_withPollingDistrict_returnsCountyPath() {
		AreaPath path = AreaPath.from(AREA_PATH_POLLING_DISTRICT);

		path = path.toCountyPath();

		assertThat(path.path()).isEqualTo("201301.47.03");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toBoroughPath_withNoBorough_throwsIllegalStateException() {
		AreaPath path = AreaPath.from("201301.47");

		path.toBoroughPath();
	}

	@Test
	public void toBoroughPath_withPollingDistrict_returnsBoroughPath() {
		AreaPath path = AreaPath.from(AREA_PATH_POLLING_DISTRICT);

		path = path.toBoroughPath();

		assertThat(path.path()).isEqualTo("201301.47.03.0301.030101");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toBoroughSubPath_withNoMunicipality_throwsIllegalStateException() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_COUNTY);

		path.toBoroughSubPath("030101");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void toBoroughSubPath_withPollingDistrictAndNullForBoroughId_throwsIllegalArgumentException() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_POLLING_DISTRICT);

		path.toBoroughSubPath(null);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toPollingDistrictPath_withNoPollingDistrictId_throwsIllegalStateException() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_COUNTY);

		path.toPollingDistrictPath();
	}

	@Test
	public void isAdvanceVotingPollingPlace_withCountyLevel_returnsFalse() throws Exception {
		AreaPath areaPath = AreaPath.from(AREA_PATH_COUNTY);

		assertThat(areaPath.isAdvanceVotingPollingPlace()).isFalse();
	}

	@Test
	public void isAdvanceVotingPollingPlace_withPollingPlaceLevelButNotMunicipalityPollingDistrict_returnsFalse() throws Exception {
		AreaPath areaPath = AreaPath.from(AREA_PATH_MUNICIPALITY_POLLING_DISTRICT);

		assertThat(areaPath.isAdvanceVotingPollingPlace()).isFalse();
	}

	@Test
	public void isAdvanceVotingPollingPlace_withPollingPlaceLevelAndMunicipalityPollingDistrict_returnsTrue() throws Exception {
		AreaPath areaPath = AreaPath.from("201301.47.03.0301.030100.0000.0001");

		assertThat(areaPath.isAdvanceVotingPollingPlace()).isTrue();
	}

	@Test(dataProvider = "isLevel")
	public void isLevel_withDataProvider_verifyExpected(String path, boolean isRoot, boolean isCountry, boolean isCounty, boolean isMunicipality, boolean isBorough,
			boolean isPollingDistrict, boolean isPollingPlace, boolean isPollingStation) throws Exception {

		AreaPath areaPath = AreaPath.from(path);

		assertThat(areaPath.isRootLevel()).isEqualTo(isRoot);
		assertThat(areaPath.isCountryLevel()).isEqualTo(isCountry);
		assertThat(areaPath.isCountyLevel()).isEqualTo(isCounty);
		assertThat(areaPath.isMunicipalityLevel()).isEqualTo(isMunicipality);
		assertThat(areaPath.isBoroughLevel()).isEqualTo(isBorough);
		assertThat(areaPath.isPollingDistrictLevel()).isEqualTo(isPollingDistrict);
		assertThat(areaPath.isPollingPlaceLevel()).isEqualTo(isPollingPlace);
		assertThat(areaPath.isPollingStationLevel()).isEqualTo(isPollingStation);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "illegal borough path: 111111.11.11.1111")
	public void assertLevel_withMunicipalityLevelAndBoroughLevel_throwsIllegalArgumentException() throws Exception {
		AreaPath areaPath = AreaPath.from("111111.11.11.1111");

		areaPath.assertLevel(AreaLevelEnum.BOROUGH);
	}

	@Test
	public void assertLevel_withMunicipalityLevels_noExceptionThrown() throws Exception {
		AreaPath areaPath = AreaPath.from("111111.11.11.1111");

		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
	}

	@DataProvider(name = "isLevel")
	public Object[][] isLevel() {
		return new Object[][] {
				{ "111111", true, false, false, false, false, false, false, false },
				{ "111111.11", false, true, false, false, false, false, false, false },
				{ "111111.11.11", false, false, true, false, false, false, false, false },
				{ "111111.11.11.1111", false, false, false, true, false, false, false, false },
				{ "111111.11.11.1111.111111", false, false, false, false, true, false, false, false },
				{ "111111.11.11.1111.111111.1111", false, false, false, false, false, true, false, false },
				{ "111111.11.11.1111.111111.1111.1111", false, false, false, false, false, false, true, false },
				{ "111111.11.11.1111.111111.1111.1111.11", false, false, false, false, false, false, false, true }
		};
	}

	@Test(dataProvider = "contains")
	public void contains_withDataProvider_verifyExpected(AreaPath path1, AreaPath path2, boolean expected) throws Exception {
		assertThat(path1.contains(path2)).isEqualTo(expected);
	}

	@DataProvider(name = "contains")
	public Object[][] contains() {
		return new Object[][] {
				{ AreaPath.from("111111"), AreaPath.from("111111"), true },
				{ AreaPath.from("111111"), AreaPath.from("111111.22"), true },
				{ AreaPath.from("111111.22"), AreaPath.from("111111"), false },
				{ AreaPath.from("111111.22"), AreaPath.from("111111.22"), true },
				{ AreaPath.from("111111"), AreaPath.from("111111.22.33"), true },
				{ AreaPath.from("111111.22.33"), AreaPath.from("111111"), false }
		};
	}

	@Test(dataProvider = "isSubpathOf")
	public void isSubpathOf_withDataProvider_verifyExpected(AreaPath path1, AreaPath path2, boolean expected) throws Exception {
		assertThat(path1.isSubpathOf(path2)).isEqualTo(expected);
	}

	@DataProvider(name = "isSubpathOf")
	public Object[][] isSubpathOf() {
		return new Object[][] {
				{ AreaPath.from("111111"), AreaPath.from("111111"), true },
				{ AreaPath.from("111111"), AreaPath.from("111111.22"), false },
				{ AreaPath.from("111111.22"), AreaPath.from("111111"), true },
				{ AreaPath.from("111111.22"), AreaPath.from("111111.22"), true },
				{ AreaPath.from("111111"), AreaPath.from("111111.22.33"), false },
				{ AreaPath.from("111111.22.33"), AreaPath.from("111111"), true }
		};
	}
	
	@Test
	public void isSamiElectionPath_countyIdIs00_isTrue() {
		assertThat(AreaPath.from("730001.47.00.0001").isSamiValgkretsPath()).isTrue();
	}

	@Test
	public void isSamiElectionPath_countyIdIs01_isTrue() {
		assertThat(AreaPath.from("730001.47.01.0001").isSamiValgkretsPath()).isFalse();
	}
	
}
