package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;

@Entity
@Table(name = "leveling_seat", uniqueConstraints = { @UniqueConstraint(columnNames = { "leveling_seat_settlement_pk", "rank_number" }),
		@UniqueConstraint(columnNames = "leveling_seat_quotient_pk") })
@AttributeOverride(name = "pk", column = @Column(name = "leveling_seat_pk"))
@NamedQueries({
	@NamedQuery(name = "LevelingSeat.findByElectionPk", query = "select l from LevelingSeat l where l.levelingSeatSettlement.election.pk = :electionPk "
		+ "order by l.seatNumber")
})
public class LevelingSeat extends VersionedEntity {
	private LevelingSeatSettlement levelingSeatSettlement;
	private int rankNumber;
	private LevelingSeatQuotient levelingSeatQuotient;
	private boolean sameQuotientAsNext;
	private boolean sameVotesAsNext;
	private boolean contestLeveled;
	private boolean partyLeveled;
	private Integer seatNumber;
	private CandidateSeat candidateSeat;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "leveling_seat_settlement_pk", nullable = false)
	public LevelingSeatSettlement getLevelingSeatSettlement() {
		return levelingSeatSettlement;
	}

	public void setLevelingSeatSettlement(LevelingSeatSettlement levelingSeatSettlement) {
		this.levelingSeatSettlement = levelingSeatSettlement;
	}

	@Column(name = "rank_number")
	public int getRankNumber() {
		return rankNumber;
	}

	public void setRankNumber(int rankNumber) {
		this.rankNumber = rankNumber;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "leveling_seat_quotient_pk", nullable = false)
	public LevelingSeatQuotient getLevelingSeatQuotient() {
		return levelingSeatQuotient;
	}

	public void setLevelingSeatQuotient(LevelingSeatQuotient levelingSeatQuotient) {
		this.levelingSeatQuotient = levelingSeatQuotient;
	}

	@Column(name = "same_quotient_as_next", nullable = false)
	public boolean isSameQuotientAsNext() {
		return sameQuotientAsNext;
	}

	public void setSameQuotientAsNext(boolean sameQuotientAsNext) {
		this.sameQuotientAsNext = sameQuotientAsNext;
	}

	@Column(name = "same_votes_as_next", nullable = false)
	public boolean isSameVotesAsNext() {
		return sameVotesAsNext;
	}

	public void setSameVotesAsNext(boolean sameVotesAsNext) {
		this.sameVotesAsNext = sameVotesAsNext;
	}

	@Column(name = "contest_leveled", nullable = false)
	public boolean isContestLeveled() {
		return contestLeveled;
	}

	public void setContestLeveled(boolean contestLeveled) {
		this.contestLeveled = contestLeveled;
	}

	@Column(name = "party_leveled", nullable = false)
	public boolean isPartyLeveled() {
		return partyLeveled;
	}

	public void setPartyLeveled(boolean partyLeveled) {
		this.partyLeveled = partyLeveled;
	}

	@Column(name = "seat_number", nullable = true)
	public Integer getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}

	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "candidate_seat_pk", nullable = true)
	public CandidateSeat getCandidateSeat() {
		return candidateSeat;
	}

	public void setCandidateSeat(CandidateSeat candidateSeat) {
		this.candidateSeat = candidateSeat;
	}
}
