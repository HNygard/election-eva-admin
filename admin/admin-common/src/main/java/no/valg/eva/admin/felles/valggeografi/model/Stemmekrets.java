package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;

public class Stemmekrets extends Valggeografi<StemmekretsSti> {
	private final boolean kommuneStemmekrets;
	private final String fylkeskommuneNavn;
	private final String kommuneNavn;
	private final String bydelNavn;

	public Stemmekrets(StemmekretsSti sti, String navn, boolean kommuneStemmekrets, String fylkeskommuneNavn, String kommuneNavn, String bydelNavn) {
		super(sti, navn);
		this.kommuneStemmekrets = kommuneStemmekrets;
		this.fylkeskommuneNavn = fylkeskommuneNavn;
		this.kommuneNavn = kommuneNavn;
		this.bydelNavn = bydelNavn;
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.STEMMEKRETS;
	}

	public boolean isKommuneStemmekrets() {
		return kommuneStemmekrets;
	}

	public String fylkeskommuneNavn() {
		return fylkeskommuneNavn;
	}

	public String kommuneNavn() {
		return kommuneNavn;
	}

	public String bydelNavn() {
		return bydelNavn;
	}
}
