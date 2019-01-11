package no.valg.eva.admin.configuration.domain.model.filter;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;

import java.util.function.Predicate;

import no.valg.eva.admin.configuration.domain.model.MvElection;

public enum ElectionFilterEnum implements Predicate<MvElection> {
	FOR_BF((Predicate<MvElection>) mvElection -> mvElection.getAreaLevel() == BOROUGH.getLevel()),
	FOR_MUNICIPALITY_LIST_PROPOSALS((Predicate<MvElection>) mvElection
		-> mvElection.getAreaLevel() == MUNICIPALITY.getLevel() || mvElection.getAreaLevel() == BOROUGH.getLevel());

	private Predicate<MvElection> filter;

	ElectionFilterEnum(Predicate<MvElection> filter) {
		this.filter = filter;
	}

	public boolean test(MvElection mvElection) {
		return filter.test(mvElection);
	}
}
