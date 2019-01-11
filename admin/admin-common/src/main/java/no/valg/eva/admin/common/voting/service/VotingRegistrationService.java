package no.valg.eva.admin.common.voting.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

import java.io.Serializable;

public interface VotingRegistrationService extends Serializable {

    Voting registerAdvanceVotingInEnvelope(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Municipality municipality, Voter voter, VotingCategory votingCategory, boolean lateArrivalVotingCategory, VotingPhase votingPhase);

    Voting registerElectionDayVotingInEnvelopeCentrally(UserData userData, ElectionGroup electionGroup, no.valg.eva.admin.configuration.domain.model.Municipality municipality, Voter voter, VotingCategory votingCategory, VotingPhase votingPhase);
}
