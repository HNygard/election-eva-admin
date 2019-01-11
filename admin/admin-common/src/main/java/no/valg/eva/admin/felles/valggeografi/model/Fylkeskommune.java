package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;

public class Fylkeskommune extends Valggeografi<FylkeskommuneSti> {
	public Fylkeskommune(FylkeskommuneSti sti, String navn) {
		super(sti, navn);
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.FYLKESKOMMUNE;
	}
}
