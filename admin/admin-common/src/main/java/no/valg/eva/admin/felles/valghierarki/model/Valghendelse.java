package no.valg.eva.admin.felles.valghierarki.model;

import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public class Valghendelse extends Valghierarki<ValghendelseSti> {
	public Valghendelse(ValghendelseSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValghierarkiNivaa nivaa() {
		return ValghierarkiNivaa.VALGHENDELSE;
	}

	@Override
	public ValggeografiNivaa valggeografiNivaa() {
		return null;
	}
}
