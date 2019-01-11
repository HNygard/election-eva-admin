package no.valg.eva.admin.configuration.domain.model.filter;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;

import java.util.function.Predicate;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public final class MvAreaDigestFilter {
	private MvAreaDigestFilter() {
	}

	public static Predicate<MvAreaDigest> valggeografiMatcherValghierarkiSti(MvElectionRepository mvElectionRepository, ValghierarkiSti valghierarkiSti) {
		if (valghierarkiSti != null && valghierarkiSti.nivaa().compareTo(VALG) >= 0) {
			return m -> mvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, m.valggeografiSti());
		}
		return m -> true;
	}

	public static Predicate<MvAreaDigest> valggeografiMatcherValgeografiSti(ValggeografiSti valggeografiSti) {
		return m -> {
			if (m.valggeografiNivaa().compareTo(valggeografiSti.nivaa()) >= 0) {
				return m.valggeografiSti().likEllerUnder(valggeografiSti);
			}
			return valggeografiSti.likEllerUnder(m.valggeografiSti());
		};
	}
}
