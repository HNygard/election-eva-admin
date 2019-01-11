package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.evote.exception.EvoteSecurityException;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.VALGTING_ORDINAERE;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_ORDINAER;

/**
 * Controller for valgting ordinære.
 */
@Named
@ViewScoped
public class ValgtingOrdinaerController extends ValgtingRegistreringController {

	@Override
	public StemmegivningsType getStemmegivningsType() {
		return VALGTINGSTEMME_ORDINAER;
	}

	@Override
	public ValggeografiNivaa getStemmestedNiva() {
		return STEMMESTED;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		// Filteret sørger for å filtrere vekk stemmesteder som det ikke skal registreres valgting på.
		setup.leggTil(geografi(getStemmestedNiva()).medFilter(VALGTING_ORDINAERE));
		return setup;
	}

	@Override
	public void kontekstKlar() {
		if (kommune().harIkkeElektroniskManntall()) {
			throw new EvoteSecurityException("Funksjon ikke tilgengelig for kommune uten elektronisk manntall!");
		}
	}

	private Kommune kommune() {
		return getKommuneListe().get(0);
	}

	@Override
	public void registrerStemmegivning() {
		if (!getStemmested().getMunicipality().isElectronicMarkoffs()) {
			buildDetailMessage("@voting.markOff.noElectronicVotes", SEVERITY_ERROR);
			return;
		}

		boolean fremmedstemme = isVelgerSammeKommuneMenIkkeSammeStemmekrets();
		execute(() -> {
			setStemmegivning(votingService.markOffVoter(getUserData(), getStemmested().getPollingPlace(), getValgGruppe().getElectionGroup(), getVelger(),fremmedstemme, ELECTION_DAY));
			String melding = byggStemmegivningsMelding(getVelger(), getStemmegivning(), "@voting.markOff.voterMarkedOff");
			buildDetailMessage(melding, SEVERITY_INFO);
		});
		manntallsSokWidget.reset();
		manntallsSokVelger(null);
	}

	public String getTittel() {
		if (getStemmested() == null) {
			return "";
		}
		return getMessageProvider().get("@voting.searchElectionDay.header") + " " + getStemmested().getPollingPlace().getName();
	}

	@Override
	public List<Kommune> getKommuneListe() {
		if (kommuneListe == null) {
			kommuneListe = singletonList(valggeografiService.kommune(getUserData(), getStemmested().valggeografiSti().tilStemmestedSti().kommuneSti()));
		}
		return kommuneListe;
	}
}
