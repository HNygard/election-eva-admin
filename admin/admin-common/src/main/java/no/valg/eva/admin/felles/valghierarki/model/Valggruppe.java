package no.valg.eva.admin.felles.valghierarki.model;

import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public class Valggruppe extends Valghierarki<ValggruppeSti> {
	public Valggruppe(ValggruppeSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValghierarkiNivaa nivaa() {
		return ValghierarkiNivaa.VALGGRUPPE;
	}

	@Override
	public ValggeografiNivaa valggeografiNivaa() {
		return null;
	}
}
