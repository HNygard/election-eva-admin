package no.valg.eva.admin.settlement.domain.event;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;

public class CandidateVoteCountEvent implements Event {
	private final Affiliation affiliation;
	private final Candidate candidate;
	private final VoteCategory voteCategory;
	private final Integer rankNumber;
	private final BigDecimal votes;
	private final BigDecimal earlyVotingVotes;
	private final BigDecimal electionDayVotes;

	public CandidateVoteCountEvent(
			Affiliation affiliation, Candidate candidate, VoteCategory voteCategory, Integer rankNumber, BigDecimal votes, BigDecimal earlyVotingVotes,
			BigDecimal electionDayVotes) {
		this.affiliation = affiliation;
		this.candidate = candidate;
		this.voteCategory = voteCategory;
		this.rankNumber = rankNumber;
		this.votes = votes;
		this.earlyVotingVotes = earlyVotingVotes;
		this.electionDayVotes = electionDayVotes;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public VoteCategory getVoteCategory() {
		return voteCategory;
	}

	public Integer getRankNumber() {
		return rankNumber;
	}

	public BigDecimal getVotes() {
		return votes;
	}

	public BigDecimal getEarlyVotingVotes() {
		return earlyVotingVotes;
	}

	public BigDecimal getElectionDayVotes() {
		return electionDayVotes;
	}

	public CandidateVoteCount toCandidateVoteCount() {
		return new CandidateVoteCount(candidate, affiliation, voteCategory, rankNumber, votes, earlyVotingVotes, electionDayVotes);
	}
}
