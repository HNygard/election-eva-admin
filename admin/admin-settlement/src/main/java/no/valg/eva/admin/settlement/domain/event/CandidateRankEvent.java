package no.valg.eva.admin.settlement.domain.event;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CandidateRankEvent implements Event {
	private final Candidate candidate;
	private final Affiliation affiliation;
	private final BigDecimal votes;
	private final Integer rankNumber;

	public CandidateRankEvent(Candidate candidate, Affiliation affiliation, BigDecimal votes, Integer rankNumber) {
		this.candidate = candidate;
		this.affiliation = affiliation;
		this.votes = votes;
		this.rankNumber = rankNumber;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public BigDecimal getVotes() {
		return votes;
	}

	public Integer getRankNumber() {
		return rankNumber;
	}

	public CandidateRank toCandidateRank() {
		return new CandidateRank(candidate, affiliation, votes, rankNumber);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateRankEvent)) {
			return false;
		}
		CandidateRankEvent that = (CandidateRankEvent) o;
		return new EqualsBuilder()
				.append(candidate, that.candidate)
				.append(affiliation, that.affiliation)
				.append(votes, that.votes)
				.append(rankNumber, that.rankNumber)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(candidate)
				.append(affiliation)
				.append(votes)
				.append(rankNumber)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("candidate", candidate)
				.append("affiliation", affiliation)
				.append("votes", votes)
				.append("rankNumber", rankNumber)
				.toString();
	}
}
