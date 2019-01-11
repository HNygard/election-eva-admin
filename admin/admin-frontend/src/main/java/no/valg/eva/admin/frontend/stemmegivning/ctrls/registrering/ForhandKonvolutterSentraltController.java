package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import static no.valg.eva.admin.common.AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.WARN;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_KONVOLUTTER_SENTRALT;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;

/**
 * Controller for forhåndstemmer konvolutter sentralt.
 */
@Named
@ViewScoped
public class ForhandKonvolutterSentraltController extends ForhandRegistreringController {

	private MvArea kommune;

	@Override
	public ValggeografiNivaa getStemmestedNiva() {
		return KOMMUNE;
	}

	@Override
	public StemmegivningsType getStemmegivningsType() {
		return FORHANDSSTEMME_KONVOLUTTER_SENTRALT;
	}

	@Override
	public void kontekstKlar() {
		// Controller er satt opp med kommune nivå, men selve stemmegivning gjøres på konvolutt stemmested. Derfor endre dette.
		setSentInnkommet(getStemmegivningsType().isForhandSentInnkomne());
		kommune = getStemmested();
		MvArea konvolutt = getMvAreaService().findByMunicipalityAndPollingPlaceId(getUserData(), getStemmested().getMunicipality().getPk(),
				CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID);
		setStemmested(konvolutt);
		sjekkAvkrysningsmanntallKjort();
	}

	@Override
	public void registrerStemmegivning() {
		registrerStemmegivningKonvolutt();
	}

	@Override
	public void manntallsSokVelger(Voter velger) {
		super.manntallsSokVelger(velger);
		if (velger == null) {
			sjekkAvkrysningsmanntallKjort();
		}
	}

	void sjekkAvkrysningsmanntallKjort() {
		if (getStemmegivningsType().isForhandIkkeSentInnkomne() && getStemmested() != null && getStemmested().getMunicipality().isAvkrysningsmanntallKjort()) {
			getStatiskeMeldinger().add(new Melding(WARN, "@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort"));
		}
	}

	@Override
	public List<PageTitleMetaModel> getPageTitleMeta() {
		return getPageTitleMetaBuilder().area(kommune);
	}
}
