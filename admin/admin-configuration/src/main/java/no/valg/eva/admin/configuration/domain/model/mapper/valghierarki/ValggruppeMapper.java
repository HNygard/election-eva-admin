package no.valg.eva.admin.configuration.domain.model.mapper.valghierarki;

import static no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti.valggruppeSti;

import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;

public final class ValggruppeMapper {
	private ValggruppeMapper() {
	}

	public static Valggruppe valggruppe(MvElectionDigest mvElectionDigest) {
		return new Valggruppe(valggruppeSti(mvElectionDigest.electionPath()), mvElectionDigest.electionHierarchyName());
	}
}
