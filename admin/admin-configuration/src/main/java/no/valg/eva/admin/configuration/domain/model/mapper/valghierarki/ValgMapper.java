package no.valg.eva.admin.configuration.domain.model.mapper.valghierarki;

import static no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti.valgSti;

import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.valghierarki.model.Valg;

public final class ValgMapper {
	private ValgMapper() {
	}

	public static Valg valg(MvElectionDigest mvElectionDigest) {
		return new Valg(valgSti(mvElectionDigest.electionPath()), mvElectionDigest.electionHierarchyName(),
				mvElectionDigest.valggeografiNivaa(), mvElectionDigest.getElectionDigest().isEnkeltOmrade(), mvElectionDigest.getElectionGroupName());
	}
}
