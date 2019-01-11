package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;

public class Stemmested extends Valggeografi<StemmestedSti> {

	private boolean valgting;

	public Stemmested(StemmestedSti sti, String navn, boolean valgting) {
		super(sti, navn);
		this.valgting = valgting;
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.STEMMESTED;
	}

	public boolean isValgting() {
		return valgting;
	}
}
