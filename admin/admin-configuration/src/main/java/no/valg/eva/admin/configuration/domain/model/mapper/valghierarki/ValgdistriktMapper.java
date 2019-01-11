package no.valg.eva.admin.configuration.domain.model.mapper.valghierarki;

import static no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti.valgdistriktSti;

import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;

public final class ValgdistriktMapper {
	private ValgdistriktMapper() {
	}

	public static Valgdistrikt valgdistrikt(MvElectionDigest mvElectionDigest) {
		return new Valgdistrikt(valgdistriktSti(mvElectionDigest.electionPath()), mvElectionDigest.electionHierarchyName(), mvElectionDigest.valggeografiNivaa());
	}
}
