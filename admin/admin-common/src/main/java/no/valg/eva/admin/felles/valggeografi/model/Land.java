package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.LandSti;

public class Land extends Valggeografi<LandSti> {
	public Land(LandSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.LAND;
	}
}
