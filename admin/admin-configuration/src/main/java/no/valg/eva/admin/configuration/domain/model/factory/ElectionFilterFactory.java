package no.valg.eva.admin.configuration.domain.model.factory;

import static no.valg.eva.admin.configuration.domain.model.filter.ElectionFilterEnum.FOR_BF;
import static no.valg.eva.admin.configuration.domain.model.filter.ElectionFilterEnum.FOR_MUNICIPALITY_LIST_PROPOSALS;

import java.util.Optional;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.filter.ElectionFilterEnum;

public final class ElectionFilterFactory {

	private ElectionFilterFactory() {
		// Avoid instantiation
	}
	
	public static Optional<ElectionFilterEnum> build(UserData userData, CountCategory countCategory, Process process) {
		switch (process) {
			case LOCAL_CONFIGURATION:
				return buildForLocalConfiguration(userData);
			case COUNTING:
				return buildForCounting(countCategory);
			default:
				return Optional.empty();
		}
	}

	private static Optional<ElectionFilterEnum> buildForLocalConfiguration(UserData userData) {
		if (userData.getOperatorAreaPath().isMunicipalityLevel()) {
			return Optional.of(FOR_MUNICIPALITY_LIST_PROPOSALS);
		}
		return Optional.empty();
	}

	private static Optional<ElectionFilterEnum> buildForCounting(CountCategory countCategory) {
		if (countCategory == CountCategory.BF) {
			return Optional.of(FOR_BF);
		}
		return Optional.empty();
	}
}
