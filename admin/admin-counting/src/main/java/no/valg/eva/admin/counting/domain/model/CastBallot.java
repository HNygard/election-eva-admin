package no.valg.eva.admin.counting.domain.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.BinaryData;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.filter.Filter;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Cast ballots that are either rejected, modified or unmodified. When created they are usually either rejected or modified. When rejecteded they are created by
 * import from EVA Skanning as proposed rejected cast ballots. When modified they are created by import from EVA Skanning or manually created in EVA Admin when
 * processing modified ballots.
 * 
 * A proposed rejected cast ballot may be changed to an unmodified cast ballot during processing of proposed rejected cast ballots.
 */
@Entity
@EntityListeners({EntityWriteListener.class})
@Table(name = "cast_vote", uniqueConstraints = @UniqueConstraint(columnNames = { "ballot_count_pk", "cast_vote_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "cast_vote_pk"))
@NamedQueries({
		@NamedQuery(
				name = "CastBallot.getByBallotCount",
				query = "SELECT cb FROM CastBallot cb WHERE cb.ballotCount.pk = :bcpk"),
		@NamedQuery(
				name = "CastBallot.getByBallotCountAndId",
				query = "SELECT cb FROM CastBallot cb WHERE cb.ballotCount.pk = :bcpk AND cb.id = :id"),
		@NamedQuery(name = "CastBallot.findNextUnassignedModifiedBallot",
				query = "SELECT cb from CastBallot cb "
						+ "where cb.type = 'MODIFIED' and cb.ballotCount.pk = :ballotCountPk "
						+ "and not exists (select m from ModifiedBallotBatchMember m where m.castBallot = cb)")
})
public class CastBallot extends VersionedEntity implements java.io.Serializable, ContextSecurable {
	private BallotCount ballotCount;
	private BinaryData binaryData;
	private String id;
	private Type type;

	private ModifiedBallotBatchMember modifiedBallotBatchMember;
	private Set<CandidateVote> candidateVotes = new HashSet<>();

	public CastBallot() {
	}

	public CastBallot(BallotCount ballotCount, Type type) {
		this.ballotCount = ballotCount;
		this.type = type;
		ballotCount.addCastBallotAndSetCastBallotId(this);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ballot_count_pk", nullable = false)
	public BallotCount getBallotCount() {
		return ballotCount;
	}

	public void setBallotCount(final BallotCount ballotCount) {
		this.ballotCount = ballotCount;
	}

	@Transient
	public boolean isEarlyVoting() {
		return getBallotCount().isEarlyVoting();
	}

	@Transient
	public Set<Candidate> getBallotCandidates() {
		return getBallotCount().getBallotCandidates();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scan_binary_data_pk")
	public BinaryData getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(final BinaryData binaryData) {
		this.binaryData = binaryData;
	}

	@Column(name = "cast_vote_id", nullable = false, length = 10)
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@OneToOne(mappedBy = "castBallot")
	public ModifiedBallotBatchMember getModifiedBallotBatchMember() {
		return modifiedBallotBatchMember;
	}

	public void setModifiedBallotBatchMember(ModifiedBallotBatchMember modifiedBallotBatchMember) {
		this.modifiedBallotBatchMember = modifiedBallotBatchMember;
	}

	@OneToMany(mappedBy = "castBallot", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<CandidateVote> getCandidateVotes() {
		return candidateVotes;
	}

	public void setCandidateVotes(Set<CandidateVote> candidateVotes) {
		this.candidateVotes = candidateVotes;
	}

	public void addNewCandidateVote(Candidate candidate, VoteCategory voteCategory, Integer renumberPosition) {
		CandidateVote candidateVote = new CandidateVote(candidate, voteCategory, this, renumberPosition);
		getCandidateVotes().add(candidateVote);
	}

	@Transient
	public Set<CandidateVote> getFilteredCandidateVotes(Filter<CandidateVote> candidateVoteFilter) {
		Set<CandidateVote> filteredCandidateVotes = new LinkedHashSet<>();
		for (CandidateVote candidateVote : candidateVotes) {
			if (candidateVoteFilter.filter(candidateVote)) {
				filteredCandidateVotes.add(candidateVote);
			}
		}
		return filteredCandidateVotes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CastBallot other = (CastBallot) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return ballotCount.getAreaPk(level);
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		return ballotCount.getElectionPk(level);
	}

	public String partyName() {
		return getBallotCount().partyName();
	}

	/**
	 * Updates existing candidate votes, removes any write in and personal vote candidates not passed in.
	 */
	public void updateCandidateVotes(Set<CandidateVote> candidateVotes) {
		for (CandidateVote candidateVote : candidateVotes) {
			candidateVote.setCastBallot(this);
		}
		Set<CandidateVote> candidateVotesForRemoval = candidateVotesForRemoval(candidateVotes);

		getCandidateVotes().addAll(candidateVotes);
		getCandidateVotes().removeAll(candidateVotesForRemoval);
	}

	/* candidate votes other than baseline votes to be removed from this */
    private Set<CandidateVote> candidateVotesForRemoval(Set<CandidateVote> candidateVotes) {
		Set<CandidateVote> candidatesForRemoval = new HashSet<>(getCandidateVotes());
		for (CandidateVote candidateVote : candidateVotes) {
			candidatesForRemoval.remove(candidateVote); // NB! removeAll funker faktisk ikke
		}
        Set<CandidateVote> result = new HashSet<>(candidatesForRemoval);
        for (CandidateVote candidateVote : candidatesForRemoval) {
            if (candidateVote.isBaseline()) {
                result.remove(candidateVote);
            }
        }
        return result;	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("type", type)
				.toString();
	}

	public void accept(CountingVisitor countingVisitor) {
		if (countingVisitor.include(this)) {
			countingVisitor.visit(this);
			for (CandidateVote candidateVote : getCandidateVotes()) {
				candidateVote.accept(countingVisitor);
			}
		}
	}

	public enum Type {
		REJECTED,
		MODIFIED,
		UNMODIFIED
	}
}
