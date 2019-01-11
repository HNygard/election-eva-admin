package no.valg.eva.admin.frontend.kontekstvelger.panel;

import java.io.Serializable;
import java.util.List;

import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

public interface KontekstvelgerPanel<T extends KontekstvelgerTabell, V> extends Serializable {
	String getId();

	String getNavn();

	List<T> getTabeller();

	void initOppsett(KontekstvelgerOppsett oppsett);

	void initTabeller(Kontekst kontekst);

	void velg(Object tabellId);

	V valgtVerdi();

	boolean erValgbar();

	default boolean erVerdiValgt() {
		return valgtVerdi() != null;
	}

	default boolean erVerdiValgtEllerIkkeValgbar() {
		return erVerdiValgt() || !erValgbar();
	}

	default boolean erVerdiIkkeValgtMenValgbar() {
		return !erVerdiValgt() && erValgbar();
	}
}
