package no.valg.eva.admin.frontend.manntall.widgets;

import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;

public interface ManntallsSokListener {

	void manntallsSokInit();

	void manntallsSokVelger(Voter velger);

	void manntallsSokTomtResultat();

	ValggruppeSti getValggruppeSti();

	KommuneSti getKommuneSti();

	String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType);

}
