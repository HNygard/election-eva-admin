package no.valg.eva.admin.frontend.counting.view;

import static no.valg.eva.admin.frontend.counting.view.CompareBallotCountView.BallotCountViewType.BALLOT_COUNT;
import static no.valg.eva.admin.frontend.counting.view.CompareBallotCountView.BallotCountViewType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.counting.model.BallotCount;

import org.testng.annotations.Test;



public class CompareBallotCountViewTest {

	@Test
	public void testEmptyView_checkState() throws Exception {
		CompareBallotCountView view = new CompareBallotCountView();

		assertState(view, null, null, null, null, null, UNKNOWN, "", "diff-zero");
	}

	@Test
	public void testWithBallotCount_checkState() throws Exception {
		BallotCount count = new BallotCount("id", "name", 101, 100);
		CompareBallotCountView view = new CompareBallotCountView(count);

		assertState(view, "name", "id", 201, 100, 101, BALLOT_COUNT, "", "diff-zero");
	}

	@Test
	public void testWithNameKey_checkState() throws Exception {
		CompareBallotCountView view = new CompareBallotCountView("name");

		assertState(view, "name", null, null, null, null, UNKNOWN, "bold", "bold diff-zero");

	}

	@Test
	public void testWithCount_checkState() throws Exception {
		CompareBallotCountView view = new CompareBallotCountView(100);

		assertState(view, null, null, 100, null, null, UNKNOWN, "", "diff-zero");

	}

	private void assertState(CompareBallotCountView view, String name, String id, Integer count, Integer modCount, Integer unmodCount,
			CompareBallotCountView.BallotCountViewType type, String styleClass, String styleClassForDiff) {
		assertThat(view.getNameKey()).isEqualTo(name);
		assertThat(view.getId()).isEqualTo(id);
		assertThat(view.getCount()).isEqualTo(count);
		assertThat(view.getModifiedCount()).isEqualTo(modCount);
		assertThat(view.getUnmodifiedCount()).isEqualTo(unmodCount);
		assertThat(view.getDiff()).isNull();
		assertThat(view.getStyleClass()).isEqualTo(styleClass);
		assertThat(view.getType()).isEqualTo(type);
		assertThat(view.isRejectedBallotCountType()).isFalse();
		assertThat(view.getStyleClassForDiff()).isEqualTo(styleClassForDiff);
	}

}

