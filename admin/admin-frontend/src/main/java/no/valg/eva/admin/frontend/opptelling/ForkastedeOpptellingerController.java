package no.valg.eva.admin.frontend.opptelling;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant.ALT_VELG_BYDEL;

import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

public abstract class ForkastedeOpptellingerController extends OpptellingerController {
	public static KontekstvelgerOppsett initKontekstvelgerOppsett() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALG));
		oppsett.leggTil(opptellingskategori());
		oppsett.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ALT_VELG_BYDEL));
		return oppsett;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		return initKontekstvelgerOppsett();
	}
}
