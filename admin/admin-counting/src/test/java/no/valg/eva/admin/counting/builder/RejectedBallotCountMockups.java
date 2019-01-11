package no.valg.eva.admin.counting.builder;

import static java.util.Arrays.asList;

import java.util.List;

import no.valg.eva.admin.common.counting.model.RejectedBallotCount;

public final class RejectedBallotCountMockups {

	public static List<RejectedBallotCount> rejectedBallotCountList() {
		return asList(dtoRejectedBallotCount("FA", 1, "Mangler off. stempel"));
	}

	public static RejectedBallotCount dtoRejectedBallotCount(String id, int count, String name) {
		RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();

		rejectedBallotCount.setId(id);
		rejectedBallotCount.setCount(count);
		rejectedBallotCount.setName(name);
		return rejectedBallotCount;
	}

	private RejectedBallotCountMockups() {
		// no instances allowed
	}
}
