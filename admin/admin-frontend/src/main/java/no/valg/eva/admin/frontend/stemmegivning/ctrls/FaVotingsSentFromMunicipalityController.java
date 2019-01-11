package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.model.views.ForeignEarlyVoting;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class FaVotingsSentFromMunicipalityController extends KontekstAvhengigController {

	@Inject
	@EjbProxy
	private transient VotingService votingService;

	private List<ForeignEarlyVoting> foreignEarlyVotingsList;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(KOMMUNE));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		foreignEarlyVotingsList = votingService.findForeignEarlyVotingsSentFromMunicipality(
				getUserData(),
				kontekst.valggruppeSti(),
				kontekst.kommuneSti());
	}

	public List<ForeignEarlyVoting> getForeignEarlyVotingsList() {
		return foreignEarlyVotingsList;
	}

}
