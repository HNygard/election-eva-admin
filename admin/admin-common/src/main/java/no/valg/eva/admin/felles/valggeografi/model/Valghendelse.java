package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;

public class Valghendelse extends Valggeografi<ValghendelseSti> {
	public Valghendelse(ValghendelseSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.VALGHENDELSE;
	}
}
