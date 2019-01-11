package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;

public class VotingControllerMock extends VotingController {

    private static final long serialVersionUID = -7427756275387778783L;

    public VotingControllerMock() {
        super();
    }

    @Override
    public boolean isShowDeleteAdvanceVotingLink() {
        return false;
    }

    @Override
    public void deleteAdvanceVoting() {

    }

    @Override
    public StemmegivningsType votingType() {
        return StemmegivningsType.FORHANDSSTEMME_ORDINAER;
    }

    @Override
    public void registerVoting(Voter voter, MvElection mvElection, Municipality municipality, VotingPhase votingPhase) {

    }

    @Override
    public ValggeografiNivaa getPollingPlaceElectionGeoLevel() {
        return null;
    }

    @Override
    public void resetData() {

    }
}