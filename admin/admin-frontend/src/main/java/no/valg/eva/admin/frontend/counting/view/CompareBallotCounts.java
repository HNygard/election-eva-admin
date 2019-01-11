package no.valg.eva.admin.frontend.counting.view;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.BallotCount;

public class CompareBallotCounts extends ArrayList<CompareBallotCountView> {

	public CompareBallotCounts(int size) {
		for (int i = 0; i < size; i++) {
			add(new CompareBallotCountView());
		}
	}

	public CompareBallotCounts(List<BallotCount> counts) {
		for (BallotCount c : counts) {
			add(new CompareBallotCountView(c));
		}
	}

	public CompareBallotCounts(List<BallotCount> counts, List<BallotCount> base) {
		int i = 0;
		for (BallotCount count : counts) {
			CompareBallotCountView row = new CompareBallotCountView(count);
			row.setDiff(count.getCount() - base.get(i).getCount());
			add(row);
			i++;
		}
	}

}
