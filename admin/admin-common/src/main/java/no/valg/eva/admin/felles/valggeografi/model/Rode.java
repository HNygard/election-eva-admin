package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.RodeSti;

public class Rode extends Valggeografi<RodeSti> {
	public Rode(RodeSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.RODE;
	}
}
