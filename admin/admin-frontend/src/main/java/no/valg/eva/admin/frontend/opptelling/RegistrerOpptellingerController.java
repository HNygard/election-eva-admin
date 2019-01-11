package no.valg.eva.admin.frontend.opptelling;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant.ALT_VELG_BYDEL;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class RegistrerOpptellingerController extends OpptellingerController {
	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		oppsett.leggTil(hierarki(VALG));
		oppsett.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ALT_VELG_BYDEL));
		return oppsett;
	}

	@Override
	protected String url() {
		return "/secure/counting/startCounting.xhtml?category=%s&contestPath=%s&areaPath=%s&fraMeny=true";
	}
}
