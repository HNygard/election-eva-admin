package no.valg.eva.admin.common;

import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.PollingPlaceType;

import org.testng.annotations.Test;


public class PollingPlaceAreaTest {

	private static final String AREA_PATH_POLLING_DISTRICT = "201301.47.03.0301.030101.0101";
	private static final String AREA_PATH_TECHNICAL_POLLING_DISTRICT = "201301.47.03.0301.030100.0001";

	@Test
	public void getPollingPlaceType_withPathAndName_returnsNotApplicable() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT);

		assertThat(new PollingPlaceArea(path, "Navn").getPollingPlaceType()).isEqualTo(PollingPlaceType.NOT_APPLICABLE);
	}

	@Test
	public void getPollingPlaceType_withElectionDay_returnsElectionDay() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT);

		assertThat(new PollingPlaceArea(path, "Navn", PollingPlaceType.ELECTION_DAY_VOTING).getPollingPlaceType()).isEqualTo(PollingPlaceType.ELECTION_DAY_VOTING);
	}

	@Test
	public void equals_withDifferentPath_returnsFalse() throws Exception {
		PollingPlaceArea area1 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		PollingPlaceArea area2 = new PollingPlaceArea(AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(area1.equals(area2)).isFalse();
	}

	@Test
	public void equals_withDifferentName_returnsFalse() throws Exception {
		PollingPlaceArea area1 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name1", PollingPlaceType.ELECTION_DAY_VOTING);
		PollingPlaceArea area2 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name2", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(area1.equals(area2)).isFalse();
	}

	@Test
	public void equals_withRegularPollingDistrict_returnsFalse() throws Exception {
		PollingPlaceArea area1 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		PollingPlaceArea area2 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ADVANCE_VOTING);

		assertThat(area1.equals(area2)).isFalse();
	}

	@Test
	public void equals_withSameFields_returnsTrue() throws Exception {
		PollingPlaceArea area1 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		PollingPlaceArea area2 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(area1.equals(area2)).isTrue();
	}

	@Test
	public void equals_withSameObject_returnsTrue() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(area.equals(area)).isTrue();
	}

	@Test
	public void equals_withDifferentClass_returnsFalse() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat("".equals(area)).isFalse();
	}

	@Test
	public void compareTo_withSameInstance_returns0() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(area.compareTo(area)).isEqualTo(0);
	}

	@Test
	public void compareTo_withAreaPaths_returnsMinusOne() throws Exception {
		PollingPlaceArea area1 = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		PollingPlaceArea area2 = new PollingPlaceArea(AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT), "Name", PollingPlaceType.ADVANCE_VOTING);

		assertThat(area1.compareTo(area2)).isEqualTo(-1);
	}
	
	@Test
	public void getLeafIdForAreaLevels_forPollingDistrict_returnsPollingDistrictIdAndName() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		assertThat(area.getLeafIdForAreaLevels(POLLING_DISTRICT, POLLING_PLACE)).isEqualTo("0101");
	}

	@Test
	public void getLeafIdForAreaLevels_forElectionEvent_returnsElectionEventIdAndName() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from("950004"), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		assertThat(area.getLeafIdForAreaLevels(ROOT)).isEqualTo("950004");
	}

	@Test
	public void getLeafIdForAreaLevels_forNoParticular_returnsEmptyString() throws Exception {
		PollingPlaceArea area = new PollingPlaceArea(AreaPath.from(AREA_PATH_POLLING_DISTRICT), "Name", PollingPlaceType.ELECTION_DAY_VOTING);
		assertThat(area.getLeafIdForAreaLevels()).isEmpty();
	}

}
