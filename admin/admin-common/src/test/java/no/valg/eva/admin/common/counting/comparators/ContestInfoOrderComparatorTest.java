package no.valg.eva.admin.common.counting.comparators;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.counting.model.ContestInfo;

import org.testng.annotations.Test;


public class ContestInfoOrderComparatorTest {

	@Test
	public void compare_withSameAreaLevelAndSameElectionPath_returns0() throws Exception {
		ContestInfo info1 = new ContestInfo("752900.01.03", "", "", "752900.47.03.0301.030107");
		ContestInfo info2 = new ContestInfo("752900.01.03", "", "", "752900.47.03.0301.030107");

		assertThat(new ContestInfoOrderComparator().compare(info1, info2)).isEqualTo(0);
	}

	@Test
	public void compare_withSameAreaLevelAndDifferentElectionPath_returns1() throws Exception {
		ContestInfo info1 = new ContestInfo("752900.01.03", "", "", "752900.47.03.0301.030107");
		ContestInfo info2 = new ContestInfo("752900.01.02", "", "", "752900.47.03.0301.030107");

		assertThat(new ContestInfoOrderComparator().compare(info1, info2)).isEqualTo(1);
	}

	@Test
	public void compare_withDifferentAreaLevelAndSameElectionPath_returnsMinus1() throws Exception {
		ContestInfo info1 = new ContestInfo("752900.01.03", "", "", "752900.47.03.0301.030107");
		ContestInfo info2 = new ContestInfo("752900.01.03", "", "", "752900.47.03");

		assertThat(new ContestInfoOrderComparator().compare(info1, info2)).isEqualTo(-1);
	}

}
