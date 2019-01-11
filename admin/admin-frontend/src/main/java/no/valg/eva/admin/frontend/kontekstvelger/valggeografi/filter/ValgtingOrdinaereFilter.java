package no.valg.eva.admin.frontend.kontekstvelger.valggeografi.filter;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;

public class ValgtingOrdinaereFilter<S extends ValggeografiSti, V extends Valggeografi<S>> {

	public List<V> filter(List<V> valggeografiListe) {
		return valggeografiListe.stream()
				.filter(kunForhandStemmesteder())
				.filter(forkastKommuneStemmekrets())
				.collect(Collectors.toList());
	}

	private Predicate<V> forkastKommuneStemmekrets() {
		return valggeografi -> valggeografi.sti().nivaa() != STEMMEKRETS || !((Stemmekrets) valggeografi).isKommuneStemmekrets();
	}

	private Predicate<V> kunForhandStemmesteder() {
		return valggeografi -> valggeografi.sti().nivaa() != STEMMESTED || ((Stemmested) valggeografi).isValgting();
	}

}
