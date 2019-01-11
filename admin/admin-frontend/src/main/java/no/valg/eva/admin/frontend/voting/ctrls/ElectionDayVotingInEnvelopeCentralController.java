package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.String.format;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_KONVOLUTTER_SENTRALT;

@Named
@ViewScoped
public class ElectionDayVotingInEnvelopeCentralController extends VotingController {

    private static final long serialVersionUID = 7086247904879309247L;

    @Inject
    private VotingRegistrationService votingRegistrationService;

    @Override
    public ValggeografiNivaa getPollingPlaceElectionGeoLevel() {
        return KOMMUNE;
    }

    @Override
    public void resetData() {
        setVoting(null);
    }

    @Override
    public boolean isShowDeleteAdvanceVotingLink() {
        return false;
    }

    /**
     * Dev note: this method is not needed for election day voting
     */
    @Override
    public void deleteAdvanceVoting() {
        throw new EvoteException("Not possible to delete election day voting");
    }

    @Override
    public StemmegivningsType votingType() {
        return VALGTINGSTEMME_KONVOLUTTER_SENTRALT;
    }

    @Getter @Setter
    private Voting voting;

    @Override
    public void registerVoting(Voter voter, MvElection mvElection, Municipality municipality, VotingPhase votingPhase) {
        execute(() -> {
            ElectionGroup electionGroup = mvElection.getElectionGroup();
            voting = votingRegistrationService.registerElectionDayVotingInEnvelopeCentrally(getUserData(), electionGroup, municipality, voter, getVotingCategory(), votingPhase);
            addMessage(voter, voting);
        });
    }

    private void addMessage(Voter voter, Voting voting) {
        String messageId = registerVotingCentrallyMessageId(voting.getVotingCategory());
        String melding = buildVotingMessage(voter, voting, messageId);
        buildDetailMessage(melding, SEVERITY_INFO);
    }

    private String registerVotingCentrallyMessageId(VotingCategory votingCategory) {
        return format("@voting.markOff.registerVoteCentrally[%s]", votingCategory.getId());
    }
}
