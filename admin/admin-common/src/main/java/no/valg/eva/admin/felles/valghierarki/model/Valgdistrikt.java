package no.valg.eva.admin.felles.valghierarki.model;

import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public class Valgdistrikt extends Valghierarki<ValgdistriktSti> {
	private final ValggeografiNivaa valggeografiNivaa;

	public Valgdistrikt(ValgdistriktSti sti, String navn, ValggeografiNivaa valggeografiNivaa) {
		super(sti, navn);
		this.valggeografiNivaa = valggeografiNivaa;
	}

	@Override
	public ValghierarkiNivaa nivaa() {
		return ValghierarkiNivaa.VALGDISTRIKT;
	}

	@Override
	public ValggeografiNivaa valggeografiNivaa() {
		return valggeografiNivaa;
	}
}
