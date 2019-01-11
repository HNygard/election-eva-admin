package no.valg.eva.admin.frontend.test.kontekstvelger.valghierarki;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiRad;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTabell;
import no.valg.eva.admin.frontend.test.kontekstvelger.KontekstvelgerTabellAssert;

public class ValghierarkiTabellAssert<S extends ValghierarkiSti, V extends Valghierarki<S>>
		extends KontekstvelgerTabellAssert<ValghierarkiPanel, ValghierarkiRad<S>, ValghierarkiNivaa, ValghierarkiTabell<S, V>> {
	public ValghierarkiTabellAssert(ValghierarkiTabell<S, V> aktuellTabell) {
		super(aktuellTabell);
	}

	public static <S extends ValghierarkiSti, V extends Valghierarki<S>> ValghierarkiTabellAssert<S, V> assertThat(ValghierarkiTabell<S, V> aktuellTabell) {
		return new ValghierarkiTabellAssert<>(aktuellTabell);
	}
}
