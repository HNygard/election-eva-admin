package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.BallotCount;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



public class CompareBallotCountsTest {

	@BeforeMethod
	public void setUp() throws Exception {

	}

	@Test
	public void testWithSize_checkState() throws Exception {
		CompareBallotCounts counts = new CompareBallotCounts(5);

		assertThat(counts.size()).isEqualTo(5);
	}

	@Test
	public void testWithCounts_checkState() throws Exception {
		List<BallotCount> ballotCounts = getCounts(5, 100);
		CompareBallotCounts counts = new CompareBallotCounts(ballotCounts);

		assertThat(counts.size()).isEqualTo(5);
	}

	@Test
	public void testWithCountsAndBase_checkState() throws Exception {
		List<BallotCount> ballotCounts = getCounts(5, 100);
		List<BallotCount> ballotCountsBase = getCounts(5, 200);
		CompareBallotCounts counts = new CompareBallotCounts(ballotCounts, ballotCountsBase);

		assertThat(counts.size()).isEqualTo(5);
		assertThat(counts.get(0).getDiff()).isEqualTo(-200);
		assertThat(counts.get(0).getStyleClassForDiff()).isEqualTo("diff-neg");
		assertThat(counts.get(1).getDiff()).isEqualTo(-200);
		assertThat(counts.get(1).getStyleClassForDiff()).isEqualTo("diff-neg");
		assertThat(counts.get(2).getDiff()).isEqualTo(-200);
		assertThat(counts.get(2).getStyleClassForDiff()).isEqualTo("diff-neg");
		assertThat(counts.get(3).getDiff()).isEqualTo(-200);
		assertThat(counts.get(3).getStyleClassForDiff()).isEqualTo("diff-neg");
		assertThat(counts.get(4).getDiff()).isEqualTo(-200);
		assertThat(counts.get(4).getStyleClassForDiff()).isEqualTo("diff-neg");
	}

	private List<BallotCount> getCounts(int size, int baseline) {
		List<BallotCount> counts = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			counts.add(new BallotCount("id" + i, "name" + i, baseline + i, baseline + i));
		}
		return counts;
	}

}

