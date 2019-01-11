package no.valg.eva.admin.voting.repository;

import no.evote.model.views.ForeignEarlyVoting;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides database access for Voting. DEV-NOTE: Repositories trenger ikke å implementeres med separate interfaces slik som applikasjonstjenester/EJB-er.
 */
public interface VotingRepository {

	long countUnapprovedAdvanceVotings(AreaPath areaPath);

	/**
	 * Creates voting (stemmegivning).
	 * @return new instance
	 */
	Voting create(UserData userData, Voting voting);

	/**
	 * Updates voting.
	 */
	Voting update(UserData userData, Voting voting);

	/**
	 * Deletes voting.
	 * @param pk primary key of instance to delete
	 */
	void delete(final UserData userData, Long pk);

	/**
	 * Finds by primary key.
	 * @param pk primary key of voting
	 * @return Voting
	 */
	Voting findByPk(UserData userData, Long pk);

	/**
	 * Finds voting category by id.
	 * @return VotingCategory
	 */
	VotingCategory findVotingCategoryById(String id);

	/**
	 * Deletes votings for election, area and category
	 * @param mvElection election
	 * @param mvArea area
	 * @param selectedVotingCategoryPk primary key of vote category
	 */
	void deleteVotings(MvElection mvElection, MvArea mvArea, Integer selectedVotingCategoryPk);

	/**
	 * Deletes voting number for election and area
	 * @param mvElection election
	 * @param mvArea area
	 */
	void deleteSeqVotingNumber(MvElection mvElection, final MvArea mvArea);

	int slettStemmegivningerFraVelgereTilhoerendeI(ValggeografiSti valggeografiSti);
	
	/**
	 * Finds approved votings for voter and election group
	 * @param voterPk primary key of voter
	 * @param electionGroupPk primary key of election group
	 * @return list of Voting
	 */
	List<Voting> getApprovedVotingsForVoterByElectionGroup(UserData userData, Long voterPk, Long electionGroupPk);

	/**
	 * Gets information about election days with markoff information.
	 * @param isManualContest is this a manual contest?
	 * @param pollingDistrict the polling district
	 * @param votingCategory the voting category
	 * @return a list of object arrays, where each array index contains 0: election day date, 1: election day pk, 2: number of votes
	 */
	List<Object[]> getElectionDaysWithMarkoffs(boolean isManualContest, PollingDistrict pollingDistrict, VotingCategory votingCategory);

	/**
	 * Retrieve all votings for a voter in an election group
	 */
	List<Voting> getVotingsByElectionGroupAndVoter(Long voterPk, Long electionGroupPk);

	/**
	 * Retrieve all votings for a voter in an election group
	 */
	List<Voting> getVotingsByElectionGroupVoterAndMunicipality(Long voterPk, Long electionGroupPk, Long municipalityPk);

	/**
	 * Retrieve all votings for a voter in an election group, except electronic votings and the votings where receivedTimestamp is null i.e. FA votings
	 */
	List<Voting> getReceivedVotingsByElectionGroupAndVoter(Long voterPk, Long electionGroupPk);

	List<Voting> getRejectedVotingsByElectionGroupAndMunicipality(String municipalityId, Long electionGroupPk);

	/**
	 * Returns an object array with the following indexes:<br/>
	 * 0: voting category id<br/>
	 * 1: proevet (boolean)<br/>
	 * 2: number of votings<br/>
	 * 3: late validation<br/>
	 */
	List<Object[]> findVotingStatistics(Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate,
			int votingNumberStart, int votingNumberEnd, boolean includeLateValidation, String[] votingCategories, boolean includeLateAdvanceVotings);

	/**
	 * Used during negative approval of votings.
	 * <p/>
	 * 0: nameLine (String)<br/>
	 * 1: votingNumber (Integer)<br/>
	 * 2: status (String)<br/>
	 * 3: voterId (String)<br/>
	 * 4: receivedTimestamp (Date)<br/>
	 * 5: votingCategoryId (String)<br/>
	 * 6: votingCategoryName (String)<br/>
	 * 7: fictitious (Boolean)<br/>
	 */
	List<Object[]> findAdvanceVotingPickList(Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate,
			int votingNumberStart, int votingNumberEnd);

	/**
	 * Returns a list of object arrays, where each object array contains:<br/>
	 * 0: nameLine (String)<br/>
	 * 1: votingNumber (Integer)<br/>
	 * 2: status (String)<br/>
	 * 3: voterId (String)<br/>
	 * 4: receivedTimestamp (Date)<br/>
	 * 5: votingCategoryId (String)<br/>
	 * 6: votingCategoryName (String)<br/>
	 * 7: fictitious (Boolean)<br/>
	 */
	List<Object[]> findElectionDayVotingPickList(long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd, String... votingCats);

	int updateAdvanceVotingsApproved(Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate, int votingNumberStart, int votingNumberEnd);

	int updateElectionDayVotingsApproved(long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd, String... votingCats);

	Voting findVotingByVotingNumber(long municipalityPk, Long electionGroupPk, long votingNumber, boolean earlyVoting);
	
	Voting findVotingByVotingNumber(no.valg.eva.admin.common.configuration.model.Municipality municipality, VotingDto votingDto);

	boolean hasApprovedVoting(Voter voter);

	/**
	 * @deprecated: Bruk heller enum-verdier
	 */
	@Deprecated
	List<VotingCategory> findAdvanceVotingCategories();

	List<ForeignEarlyVoting> findForeignEarlyVotingsSentFromMunicipality(Long electionGroupPk, String municipalityId);

	/**
	 * @deprecated: GUI boer heller bruke enum verdiene for stemmekategori
	 */
	@Deprecated
	List<VotingCategory> findAllVotingCategories();

	void flyttStemmegivningerForVelgereI(MvArea fraOmraade, MvArea tilStemmekrets);

	void flyttStemmegivningerAvgittI(MvArea fraOmraade, MvArea tilStemmested);

	List<Voting> findApprovedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup);

	List<Voting> findApprovedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation);

	List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup);

	List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation);

	List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate, VotingPhase votingPhase);

	long countApprovedEnvelopeVotings(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate);

	long countRejectedEnvelopeVotings(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate);

	List<Voting> findRejectedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup);

	List<Voting> findRejectedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation);

    List<Voting> findVotingsToConfirmForVoter(Voter voter);
}
