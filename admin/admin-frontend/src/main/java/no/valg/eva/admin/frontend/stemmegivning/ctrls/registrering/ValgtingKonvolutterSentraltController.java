package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_KONVOLUTTER_SENTRALT;

/**
 * Controller for forhåndstemmer konvolutter sentralt.
 */
@Named
@ViewScoped
public class ValgtingKonvolutterSentraltController extends ValgtingRegistreringController {

	private static final long serialVersionUID = 7152579642612715404L;
	
	@Inject
    private VotingRegistrationService votingRegistrationService;

	@Override
	public ValggeografiNivaa getStemmestedNiva() {
		return KOMMUNE;
	}

	@Override
	public StemmegivningsType getStemmegivningsType() {
		return VALGTINGSTEMME_KONVOLUTTER_SENTRALT;
	}

	@Override
	public void registrerStemmegivning() {
		execute(() -> {
            /*DEV-NOTE: hardkodet VotingPhase her siden denne måte å registrere på uansett skal fjernes.*/
            ElectionGroup electionGroup = getValgGruppe().getElectionGroup();
            VotingCategory votingCategory = VotingCategory.fromId(getStemmetype());

			Voting voting = votingRegistrationService.registerElectionDayVotingInEnvelopeCentrally(getUserData(), electionGroup, getStemmested().getMunicipality(), getVelger(), votingCategory, ELECTION_DAY);
			setStemmegivning(voting);

			String textId = "@voting.markOff.registerVoteCentrally[" + getStemmegivning().getVotingCategory().getId() + "]";
			String melding = byggStemmegivningsMelding(getVelger(), getStemmegivning(), textId);
			buildDetailMessage(melding, SEVERITY_INFO);
		});
		manntallsSokWidget.reset();
		manntallsSokVelger(null);
	}
}
