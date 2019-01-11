package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.FORHAND_ORDINAERE;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_ORDINAER;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;

/**
 * Controller for forhåndstemmer ordinære.
 */
@Named
@ViewScoped
public class ForhandOrdinaerController extends ForhandRegistreringController {

	@Override
	public ValggeografiNivaa getStemmestedNiva() {
		return STEMMESTED;
	}

	@Override
	public StemmegivningsType getStemmegivningsType() {
		return FORHANDSSTEMME_ORDINAER;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		// Filteret sørger for å filtrere vekk stemmesteder som det ikke skal registreres forhåndstemmer på.
		setup.leggTil(geografi(getStemmestedNiva()).medFilter(FORHAND_ORDINAERE));
		return setup;
	}

	@Override
	public void kontekstKlar() {
	}

	@Override
	public void registrerStemmegivning() {
		if (isForhandsstemmeRettIUrne()) {
			registrerStemmegivningUrne();
		} else {
			registrerStemmegivningKonvolutt();
		}
	}

	public String getTittel() {
		if (getStemmested() != null) {
			String key;
			if (isForhandsstemmeRettIUrne()) {
				key = "@voting.searchAdvanceBallotBox.header";
			} else {
				key = "@voting.searchAdvance.header";
			}
			return getMessageProvider().get(key) + " " + getStemmested().getPollingPlace().getName();
		}
		return "";
	}

	public boolean isSentInnkommetDisabled() {
		return isForhandsstemmeRettIUrne();
	}
}
