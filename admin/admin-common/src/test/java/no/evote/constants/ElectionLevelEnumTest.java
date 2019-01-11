package no.evote.constants;

import static java.util.Arrays.asList;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.evote.constants.ElectionLevelEnum.NONE;
import static no.evote.constants.ElectionLevelEnum.comparator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ElectionLevelEnumTest {

	@Test(dataProvider = "getLevel")
    public void getLevel(int level, ElectionLevelEnum expected) {
		assertThat(ElectionLevelEnum.getLevel(level)).isSameAs(expected);
	}

	@DataProvider
	public Object[][] getLevel() {
		return new Object[][] {
				{ 0, ELECTION_EVENT },
				{ 1, ELECTION_GROUP },
				{ 2, ELECTION },
				{ 3, CONTEST },
				{ 100, NONE }
		};
	}

	@Test(dataProvider = "isLowerThan")
    public void isLowerThan(ElectionLevelEnum base, ElectionLevelEnum compare, boolean expected) {
		assertThat(base.isLowerThan(compare)).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] isLowerThan() {
		return new Object[][] {
				{ ELECTION_EVENT, ELECTION_EVENT, false },
				{ ELECTION_EVENT, CONTEST, true },
				{ CONTEST, ELECTION_EVENT, false }
		};
	}

	@Test(dataProvider = "getLevelDescription")
    public void getLevelDescription(ElectionLevelEnum electionLevel, String expected) {
		assertThat(electionLevel.getLevelDescription()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] getLevelDescription() {
		return new Object[][] {
				{ ELECTION_EVENT, "election event" },
				{ ELECTION_GROUP, "election group" },
				{ ELECTION, "election" },
				{ CONTEST, "contest" },
		};
	}

	@Test(dataProvider = "getLevelName")
    public void getLevelName(ElectionLevelEnum electionLevel, String expected) {
        assertThat(electionLevel.messageProperty()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] getLevelName() {
		return new Object[][] {
				{ ELECTION_EVENT, "@election_level[0].name" },
				{ ELECTION_GROUP, "@election_level[1].name" },
				{ ELECTION, "@election_level[2].name" },
				{ CONTEST, "@election_level[3].name" },
		};
	}

	@Test
    public void comparator_withUnsorted_verifySorting() {
		List<ElectionLevelEnum> unsorted = asList(ELECTION, ELECTION_EVENT, CONTEST, ELECTION_GROUP);

		List<ElectionLevelEnum> sorted = unsorted.stream().sorted(comparator()).collect(Collectors.toList());

		assertThat(sorted.get(0)).isSameAs(ELECTION_EVENT);
		assertThat(sorted.get(1)).isSameAs(ELECTION_GROUP);
		assertThat(sorted.get(2)).isSameAs(ELECTION);
		assertThat(sorted.get(3)).isSameAs(CONTEST);
	}

}

