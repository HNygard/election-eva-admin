package no.valg.eva.admin.common.counting.model.modifiedballots;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;

import org.testng.annotations.Test;

public class ModifiedBallotTest {

	private static final boolean NOT_DONE = false;
	private static final int RENUMBER_POSITION = 1;
	private static final int ANOTHER_RENUMBER_POSITION = 2;
	private static final int RENUMBER_POSITION_GREATER_THAN_NO_CANDIDATES_ON_LIST = 3;
	private static final int RENUMBER_POSITION_TO_LAST_CANDIDATE_ON_LIST = 2;
	private static final long CANDIDATE_PK = 1L;
	private static final long ANOTHER_CANDIDATE_PK = 2L;
	public static final int MAX_RENUMBER = 10;

	@Test(expectedExceptions = ValidateException.class)
	public void validate_personalVoteOnCandidateNotOnBallot_illegalStateException() {
		Ballot ballot = buildBallot(MAX_RENUMBER);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		Candidate candidate = buildCandidate(CANDIDATE_PK, null);
		modifiedBallot.addPersonVotesFor(candidate);

		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_writeInForCandidateNotOnOtherBallotsInSameContest_illegalStateException() {
		Ballot ballot = buildBallot(MAX_RENUMBER);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		Candidate candidate = buildCandidate(CANDIDATE_PK, null);
		modifiedBallot.addWriteInFor(candidate);

		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_ballotIdDifferentOnBallotAndModifiedBallot_illegalArgumentException() {
		Ballot ballot = buildBallot(MAX_RENUMBER);
		Ballot anotherBallot = new Ballot(new BallotId("2"), createModifiedBallotConfiguration(1, 1));
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);

		modifiedBallot.validate(anotherBallot);
	}

    private ModifiedBallotConfiguration createModifiedBallotConfiguration(Integer maxWriteIns, Integer maxRenumber) {
        return new ModifiedBallotConfiguration(true, true, true, true, true, maxWriteIns, maxRenumber);
    }

    @Test(expectedExceptions = ValidateException.class)
	public void validate_tooManyWriteInCandidates_illegalStateException() {
		Ballot ballot = buildBallotWithZeroWriteIns();
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		Candidate candidate = buildCandidate(CANDIDATE_PK, null);
		modifiedBallot.addWriteInFor(candidate);
		ballot.getCandidatesForWriteIn().add(candidate);

		modifiedBallot.validate(ballot);
	}

	private Ballot buildBallotWithZeroWriteIns() {
		return new Ballot(new BallotId("1"), createModifiedBallotConfiguration(0, 1));
	}

	@Test
    public void validate_renumberLimitNull_skipsValidationOfMaxRenumber() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION);
		Candidate anotherCandidate = buildCandidate(ANOTHER_CANDIDATE_PK, ANOTHER_RENUMBER_POSITION);
		Ballot ballot = buildBallot(null, candidate, anotherCandidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);
		modifiedBallot.addPersonVotesFor(anotherCandidate);
		
		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingsToSameCandidate_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION);
		Candidate anotherCandidate = buildCandidate(ANOTHER_CANDIDATE_PK, RENUMBER_POSITION);
		Ballot ballot = buildBallot(MAX_RENUMBER, candidate, anotherCandidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);
		modifiedBallot.addPersonVotesFor(anotherCandidate);
		
		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingToCandidateNoNotOnList_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION_GREATER_THAN_NO_CANDIDATES_ON_LIST);
		Candidate unmodifiedCandidate = buildCandidate(ANOTHER_CANDIDATE_PK, null);
		Ballot ballot = buildBallot(MAX_RENUMBER, candidate, unmodifiedCandidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);
		modifiedBallot.addPersonVotesFor(unmodifiedCandidate);
		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingToCandidateNoHigherThanMaxRenumber_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION_TO_LAST_CANDIDATE_ON_LIST);
		Candidate unmodifiedCandidate = buildCandidate(ANOTHER_CANDIDATE_PK, null);
		int maxRenumber = 1;
		Ballot ballot = buildBallot(maxRenumber, candidate, unmodifiedCandidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);
		modifiedBallot.addPersonVotesFor(unmodifiedCandidate);
		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingTooManyCandidates_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION);
		Candidate anotherCandidate = buildCandidate(ANOTHER_CANDIDATE_PK, ANOTHER_RENUMBER_POSITION);
		int maxRenumber = 1;
		Ballot ballot = buildBallot(maxRenumber, candidate, anotherCandidate);
		
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);
		modifiedBallot.addPersonVotesFor(anotherCandidate);
		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingAndPersonVoteToSameCandidate_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION);
		candidate.setPersonalVote(true);
		Ballot ballot = buildBallot(MAX_RENUMBER, candidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);

		modifiedBallot.validate(ballot);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void validate_renumberingAndStrikeOutOnSameCandidate_illegalStateException() {
		Candidate candidate = buildCandidate(CANDIDATE_PK, RENUMBER_POSITION);
		candidate.setStrikedOut(true);
		Ballot ballot = buildBallot(MAX_RENUMBER, candidate);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		modifiedBallot.addPersonVotesFor(candidate);

		modifiedBallot.validate(ballot);
	}

	@Test
	public void validate_whenASubsetOfCandidatesHaveBeenChosen_noErrors() {
		Ballot ballot = buildBallot(MAX_RENUMBER);
		ModifiedBallot modifiedBallot = buildModifiedBallot(ballot);
		Candidate candidate = buildCandidate(CANDIDATE_PK, null);
		modifiedBallot.addWriteInFor(candidate);
		ballot.getCandidatesForWriteIn().add(candidate);

		modifiedBallot.validate(ballot);

		// No exception from validate method means 'OK'
	}

	private Candidate buildCandidate(long candidatePk, Integer renumberPosition) {
		Candidate candidate = new Candidate("kandidat1", new CandidateRef(candidatePk), "someParty", new Long(candidatePk).intValue());
		candidate.setRenumberPosition(renumberPosition);
		return candidate;
	}

	private Ballot buildBallot(Integer maxRenumber, Candidate... candidates) {
		Ballot ballot = new Ballot(new BallotId("1"), createModifiedBallotConfiguration(1, maxRenumber));
		for (Candidate candidate : candidates) {
			ballot.addForPersonalVote(candidate);
		}
		return ballot;
	}

	private ModifiedBallot buildModifiedBallot(Ballot ballot) {
		return new ModifiedBallot(null, 0, "", ballot.getBallotId(), NOT_DONE);
	}
}
