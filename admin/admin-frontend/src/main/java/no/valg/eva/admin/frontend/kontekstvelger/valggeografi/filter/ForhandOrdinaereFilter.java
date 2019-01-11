package no.valg.eva.admin.frontend.kontekstvelger.valggeografi.filter;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;

public class ForhandOrdinaereFilter<S extends ValggeografiSti, V extends Valggeografi<S>> {

	public List<V> filter(List<V> valggeografiListe) {
		return valggeografiListe.stream()
				.filter(kunKommuneBydel())
				.filter(kunKommuneStemmekrets())
				.filter(kunForhandStemmesteder())
				.filter(forkastKonvoluttSentralt())
				.collect(Collectors.toList());
	}

	private Predicate<V> kunKommuneBydel() {
		return valggeografi -> valggeografi.sti().nivaa() != BYDEL || ((Bydel) valggeografi).isKommuneBydel();
	}

	private Predicate<V> kunKommuneStemmekrets() {
		return valggeografi -> valggeografi.sti().nivaa() != STEMMEKRETS || ((Stemmekrets) valggeografi).isKommuneStemmekrets();
	}

	private Predicate<V> kunForhandStemmesteder() {
		return valggeografi -> valggeografi.sti().nivaa() != STEMMESTED || !((Stemmested) valggeografi).isValgting();
	}

	private Predicate<V> forkastKonvoluttSentralt() {
		return valggeografi -> valggeografi.sti().nivaa() != STEMMESTED || !valggeografi.sti().areaPath().isCentralEnvelopeRegistrationPollingPlace();
	}

}
