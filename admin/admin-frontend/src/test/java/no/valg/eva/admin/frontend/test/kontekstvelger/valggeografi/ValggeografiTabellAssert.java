package no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiRad;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTabell;
import no.valg.eva.admin.frontend.test.kontekstvelger.KontekstvelgerTabellAssert;

import org.assertj.core.util.Objects;

public class ValggeografiTabellAssert<S extends ValggeografiSti, V extends Valggeografi<S>>
		extends KontekstvelgerTabellAssert<ValggeografiPanel, ValggeografiRad<S>, ValggeografiNivaa, ValggeografiTabell<S, V>> {
	public ValggeografiTabellAssert(ValggeografiTabell<S, V> aktuellTabell) {
		super(aktuellTabell);
	}

	public static <S extends ValggeografiSti, V extends Valggeografi<S>> ValggeografiTabellAssert<S, V> assertThat(ValggeografiTabell<S, V> aktuellTabell) {
		return new ValggeografiTabellAssert<>(aktuellTabell);
	}

	public void harValgtStiLikMed(S forventetSti) {
		isNotNull();
		if (!Objects.areEqual(actual.valgtSti(), forventetSti)) {
			failWithMessage("Valgt sti: <%s>; Forventet sti: <%s>", actual.valgtSti(), forventetSti);
		}
	}

	public void harIkkeValgtSti() {
		isNotNull();
		if (actual.valgtSti() != null) {
			failWithMessage("Valgt sti: <%s>; Forventet ikke valgt sti", actual.valgtSti());
		}
	}
}
