package no.valg.eva.admin.common;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.ElectionLevelEnum;

import org.testng.annotations.Test;


public class ElectionPathTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void from_withInvalidPath_throwsIllegalArgumentException() {
		ElectionPath.from("234324.24324.2342343");
	}

	@Test
	public void from_withValidPath_returnsSamePath() {
		ElectionPath election = ElectionPath.from("201301.01.01");
		assertThat(election.path()).isEqualTo("201301.01.01");
	}

	@Test
	public void from_withValidContestLevel_returnsValidState() {
		ElectionPath path = ElectionPath.from("201301.01.01.010101");
		assertThat(path.getLevel()).isEqualTo(ElectionLevelEnum.CONTEST);
		assertThat(path.getElectionEventId()).isEqualTo("201301");
		assertThat(path.getElectionGroupId()).isEqualTo("01");
		assertThat(path.getElectionId()).isEqualTo("01");
		assertThat(path.getContestId()).isEqualTo("010101");
	}

	@Test
	public void from_withValidElectionIdLevel_returnsValidState() {
		ElectionPath path = ElectionPath.from("201301.01.01");
		assertThat(path.getLevel()).isEqualTo(ElectionLevelEnum.ELECTION);
		assertThat(path.getElectionEventId()).isEqualTo("201301");
		assertThat(path.getElectionGroupId()).isEqualTo("01");
		assertThat(path.getElectionId()).isEqualTo("01");
		assertThat(path.getContestId()).isNull();
	}

	@Test
	public void from_withValidElectionGroupLevel_returnsValidState() {
		ElectionPath path = ElectionPath.from("201301.01");
		assertThat(path.getLevel()).isEqualTo(ElectionLevelEnum.ELECTION_GROUP);
		assertThat(path.getElectionEventId()).isEqualTo("201301");
		assertThat(path.getElectionGroupId()).isEqualTo("01");
		assertThat(path.getElectionId()).isNull();
		assertThat(path.getContestId()).isNull();
	}

	@Test
	public void from_withValidElectionEventLevel_returnsValidState() {
		ElectionPath path = ElectionPath.from("201301");
		assertThat(path.getLevel()).isEqualTo(ElectionLevelEnum.ELECTION_EVENT);
		assertThat(path.getElectionEventId()).isEqualTo("201301");
		assertThat(path.getElectionGroupId()).isNull();
		assertThat(path.getElectionId()).isNull();
		assertThat(path.getContestId()).isNull();
	}

	@Test
	public void contains_withElectionContainsContest_returnTrue() {
		ElectionPath election = ElectionPath.from("201301.01.01");
		ElectionPath contest = ElectionPath.from("201301.01.01.010101");
		assertThat(election.contains(contest)).isTrue();
	}

	@Test
	public void isSubpathOf_withElectionIsSubpathOfContest_returnTrue() {
		ElectionPath election = ElectionPath.from("201301.01.01");
		ElectionPath contest = ElectionPath.from("201301.01.01.010101");
		assertThat(contest.isSubpathOf(election)).isTrue();
	}

	@Test
	public void contains_withElectionContainsOtherContest_returnFalse() {
		ElectionPath election = ElectionPath.from("201301.01.01");
		ElectionPath contest = ElectionPath.from("201301.01.03.030101");
		assertThat(election.contains(contest)).isFalse();
	}

	@Test
	public void isSubpathOf_withElectionIsSubpathOfAnotherContest_returnFalse() {
		ElectionPath election = ElectionPath.from("201301.01.01");
		ElectionPath contest = ElectionPath.from("201301.01.03.030101");
		assertThat(contest.isSubpathOf(election)).isFalse();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toElectionGroupPath_withElectionEventLevel_throwsIllegalStateException() {
		ElectionPath.from("201301").toElectionGroupPath();
	}

	@Test
	public void toElectionGroupPath_withElectionGroupLevel_returnsSamePath() {
		assertThat(ElectionPath.from("201301.01").toElectionGroupPath().path()).isEqualTo("201301.01");
	}

	@Test
	public void toElectionGroupPath_withElectionIdLevel_returnsSamePath() {
		assertThat(ElectionPath.from("201301.01.01").toElectionGroupPath().path()).isEqualTo("201301.01");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void toElectionPath_withElectionGroupLevel_throwsIllegalStateException() {
		ElectionPath.from("201301.01").toElectionPath();
	}

	@Test
	public void toElectionPath_withElectionIdLevel_returnsSamePath() {
		assertThat(ElectionPath.from("201301.01.01").toElectionPath().path()).isEqualTo("201301.01.01");
	}

	@Test
	public void toElectionPath_withContestIdLevel_returnsElectionPathLevel() {
		assertThat(ElectionPath.from("201301.01.01.030101").toElectionPath().path()).isEqualTo("201301.01.01");
	}

	@Test
	public void equals_withSamePath_returnsTrue() {
		ElectionPath path1 = ElectionPath.from("201301.01.01.030101");
		ElectionPath path2 = ElectionPath.from("201301.01.01.030101");
		assertThat(path1.equals(path1)).isTrue();
		assertThat(path1.equals(path2)).isTrue();
	}

	@Test
	public void equals_withDifferentPath_returnsFalse() {
		ElectionPath path1 = ElectionPath.from("201301.01.01.030101");
		ElectionPath path2 = ElectionPath.from("201301.01.01.030102");
		assertThat(path1.equals(path2)).isFalse();
		assertThat("string".equals(path1)).isFalse();
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected <CONTEST> election path, but got <ELECTION>")
	public void assertContestLevel_withElectionPath_throwsIllegalArgumentException() throws Exception {
		ElectionPath path = ElectionPath.from("201301.01.01");

		path.assertContestLevel();
	}

	@Test
	public void assertContestLevel_withContestPath_noExceptionThrown() throws Exception {
		ElectionPath path = ElectionPath.from("201301.01.01.030101");

		path.assertContestLevel();
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "illegal election path: 201301.01.01.030101")
	public void assertLevel_withContestAndElectionLevels_throwsIllegalArgumentException() throws Exception {
		ElectionPath path = ElectionPath.from("201301.01.01.030101");

		path.assertLevel(ElectionLevelEnum.ELECTION);
	}

	@Test
	public void assertLevel_withElectionLevels_noExceptionThrown() throws Exception {
		ElectionPath path = ElectionPath.from("201301.01.01.030101");

		path.assertLevel(ElectionLevelEnum.CONTEST);
	}

	@Test
	public void toElectionEventPath_givenElectionPath_returnsElectionEventPath() throws Exception {
		ElectionPath path = ElectionPath.from("201301.01.01.030101");
		ElectionPath electionEventPath = path.toElectionEventPath();
		assertThat(electionEventPath).isEqualTo(ElectionPath.from("201301"));
	}
}
