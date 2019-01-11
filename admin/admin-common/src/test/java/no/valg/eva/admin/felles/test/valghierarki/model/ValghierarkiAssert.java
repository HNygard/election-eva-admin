package no.valg.eva.admin.felles.test.valghierarki.model;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Objects;

public class ValghierarkiAssert<S extends ValghierarkiSti> extends AbstractAssert<ValghierarkiAssert<S>, Valghierarki<S>> {
	private Objects objects;

	public ValghierarkiAssert(Valghierarki<S> actual) {
		super(actual, ValghierarkiAssert.class);
		objects = Objects.instance();
	}

	public static final <S extends ValghierarkiSti> ValghierarkiAssert<S> assertThat(Valghierarki<S> actual) {
		return new ValghierarkiAssert<>(actual);
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
