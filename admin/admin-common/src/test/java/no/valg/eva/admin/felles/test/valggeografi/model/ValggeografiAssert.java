package no.valg.eva.admin.felles.test.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Objects;

public class ValggeografiAssert<S extends ValggeografiSti> extends AbstractAssert<ValggeografiAssert<S>, Valggeografi<S>> {
	private Objects objects;

	public ValggeografiAssert(Valggeografi<S> actual) {
		super(actual, ValggeografiAssert.class);
		objects = Objects.instance();
	}

	public static <S extends ValggeografiSti> ValggeografiAssert<S> assertThat(Valggeografi<S> actual) {
		return new ValggeografiAssert<>(actual);
	}

	public void harStiLikMed(S sti) {
		S actualSti = actual.sti();
		objects.assertEqual(info, actualSti, sti);
	}

	public void harNavnLikMed(String navn) {
		String actualNavn = actual.navn();
		objects.assertEqual(info, actualNavn, navn);
	}
}
