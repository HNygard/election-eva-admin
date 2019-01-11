package no.evote.service.voting;

import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.model.views.ForeignEarlyVoting;
import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;

public interface VotingService extends Serializable {

    long countUnapprovedAdvanceVotings(UserData userData, AreaPath areaPath);

    List<Voting> getVotingsByElectionGroupAndVoter(UserData userData, Long voterPk, Long electionPk);

    List<Voting> getVotingsByElectionGroupVoterAndMunicipality(UserData userData, Long voterPk, Long electionPk, Long municpalityPk);

    List<Voting> getRejectedVotingsByElectionGroupAndMunicipality(UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti);

    /**
     * @deprecated Bruk en av de mer spesifikke update metodene under for valgtings- og forhåndsstemmegivninger.
     */
    @Deprecated
    Voting update(UserData userData, Voting voting);

    /**
     * @deprecated erstattet av {@link #delete(UserData, Voting)} ()
     */
    @Deprecated
    void delete(final UserData userData, Long pk);

    void delete(final UserData userData, Voting voting);

    /**
     * Returns a list of Voting Data transfer objects which each represent statistics of a voting category. The parameters is used to filter what data is used.
     *
     * @param includeLateAdvanceVotings flag telling whether to in addition also include advance votings where late_validation is true
     */
    List<VotingDto> findVotingStatistics(final UserData userData, final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk,
                                         final LocalDate startDate, final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd,
                                         final boolean includeLateValidation,
                                         final String[] votingCategories, final boolean includeLateAdvanceVotings);

    /**
     * Get votings that must be manually processed in a negative approval process. The method does not return votings with late validation. The the parameters
     * is used to filter what data is used.
     */
    List<PickListItem> findAdvanceVotingPickList(final UserData userData, final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk,
                                                 final LocalDate startDate, final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd);

    /**
     * Finds election day votes for negative approval.
     *
     * @return list of PickListDto
     */
    List<PickListItem> findElectionDayVotingPickList(final UserData userData, final long municipalityPk, final Long electionGroupPk,
                                                     final int votingNumberStart, final int votingNumberEnd, String... votingCats);

    /**
     * Approves every advance vote with the filter criteria, this is used to approve votings in a negative approval process. The method does not approve votings
     * with late validation. The the parameters is used to filter what data is used.
     */
    int updateAdvanceVotingsApproved(final UserData userData, final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk,
                                     final LocalDate startDate, final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd);

    /**
     * Approves election day votes
     */
    int updateElectionDayVotingsApproved(final UserData userData, final long municipalityPk, final Long electionGroupPk, final int votingNumberStart,
                                         final int votingNumberEnd, String... votingCats);

    Voting findVotingByVotingNumber(
            UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti, long votingNumber, boolean earlyVoting);

    @Cacheable
    VotingCategory findVotingCategoryById(UserData userData, String id);

    @Cacheable
    List<VotingCategory> findAdvanceVotingCategories(UserData userData);

    List<ForeignEarlyVoting> findForeignEarlyVotingsSentFromMunicipality(UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti);

    void deleteVotings(UserData userData, MvElection mvElection, MvArea mvArea, Integer selectedVotingCategoryPk);

    void deleteSeqVotingNumber(UserData userData, MvElection mvElection, MvArea mvArea);

    List<VotingCategory> findAllVotingCategories(UserData userData);

    /**
     * Marks off advance vote in electoral roll. (Krysser av for forhåndsstemme i manntall.)
     * @deprecated use {@link VotingInEnvelopeService#registerAdvanceVotingInEnvelope(UserData, PollingPlace, ElectionGroup, Municipality, Voter, no.valg.eva.admin.common.voting.VotingCategory, boolean, VotingPhase)} 
     */
    @Deprecated
    Voting markOffVoterAdvance(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voter, boolean isVoterInLoggedInMunicipality, String selectedVotingCategoryId, String ballotBoxId, VotingPhase votingPhase);

    /**
     * Register vote from central location in municipality.
     *
     * @deprecated use {@link VotingInEnvelopeService#registerElectionDayVotingInEnvelopeCentrally(UserData, ElectionGroup, no.valg.eva.admin.configuration.domain.model.Municipality, Voter, no.valg.eva.admin.common.voting.VotingCategory, VotingPhase)} 
     */
    @Deprecated
    Voting registerVoteCentrally(UserData userData, ElectionGroup electionGroup, Voter voter, String selectedVotingCategoryId, MvArea currentMvArea, VotingPhase votingPhase);

    /**
     * Marks off vote for election day, also handles special cover.. bør håndteres litt anderledes.
     *
     * @return voting
     */
    Voting markOffVoter(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voterSearchResult, boolean fremmedstemme, VotingPhase votingPhase);

    /**
     * Marks off advance vote in ballot box, forhåndsstemme i urne.
     *
     * @return voting
     */
    Voting markOffVoterAdvanceVoteInBallotBox(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voterSearchResult, boolean isVoterInLoggedInMunicipality, VotingPhase votingPhase);

    Voting updateAdvanceVotingApproved(UserData userData, Voting votingForApproval);

    VelgerSomSkalStemme hentVelgerSomSkalStemme(UserData userData, StemmegivningsType stemmegivningsType, ElectionPath valggruppeSti, AreaPath stemmestedSti, Voter velger);
}
