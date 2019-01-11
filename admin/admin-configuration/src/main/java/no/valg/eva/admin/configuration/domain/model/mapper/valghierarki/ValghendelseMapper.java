package no.valg.eva.admin.configuration.domain.model.mapper.valghierarki;


import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;

public final class ValghendelseMapper {
	private ValghendelseMapper() {
	}

	public static Valghendelse valghendelse(MvElectionDigest mvElectionDigest) {
		return new Valghendelse(ValghierarkiSti.valghendelseSti(mvElectionDigest.electionPath()), mvElectionDigest.electionHierarchyName());
	}
}
