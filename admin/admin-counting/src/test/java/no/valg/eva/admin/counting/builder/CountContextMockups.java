package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;

public final class CountContextMockups {

	public static CountContext countContext(ElectionPath electionPath, CountCategory countCategory) {
		return new CountContext(electionPath, countCategory);
	}

	private CountContextMockups() {

	}
}
