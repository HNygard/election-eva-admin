package no.valg.eva.admin.frontend.test.kontekstvelger;

import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.panel.KontekstvelgerPanel;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

public class KontekstvelgerTabellAssert<P extends KontekstvelgerPanel, R extends KontekstvelgerRad, I, T extends KontekstvelgerTabell<P, R, I>>
		extends AbstractAssert<KontekstvelgerTabellAssert<P, R, I, T>, T> {
	public KontekstvelgerTabellAssert(T aktuellTabell) {
		super(aktuellTabell, KontekstvelgerTabellAssert.class);
	}

	public void harAntallRaderLikMed(int forventetAntallRader) {
		isNotNull();
		if (actual.getAntallRader() != forventetAntallRader) {
			failWithMessage("Antall rader: <%d>; Forventent antall rader: <%d>", actual.getAntallRader(), forventetAntallRader);
		}
	}

	public void harIngenRader() {
		isNotNull();
		if (actual.getAntallRader() != 0) {
			failWithMessage("Antall rader: <%d>; Forventent ingen rader", actual.getAntallRader());
		}
	}

	public void harValgtRadLikMed(R forventetRad) {
		isNotNull();
		if (!Objects.areEqual(actual.getValgtRad(), forventetRad)) {
			failWithMessage("Valgt rad: <%s>; Forventet rad: <%s>", actual.getValgtRad(), forventetRad);
		}
	}

	public void harIkkeValgtRad() {
		isNotNull();
		if (actual.isRadValgt()) {
			failWithMessage("Valgt rad: <%s>; Forventet ikke valgt rad", actual.getValgtRad());
		}
	}
}
