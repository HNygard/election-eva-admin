package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static javax.persistence.FetchType.LAZY;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;

@Entity
@Table(name = "leveling_seat_quotient", uniqueConstraints = { @UniqueConstraint(columnNames = { "leveling_seat_settlement_pk", "contest_pk", "party_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "leveling_seat_quotient_pk"))
public class LevelingSeatQuotient extends VersionedEntity {
	private static final int SCALE_SIX_DECIMALS = 6;

	private LevelingSeatSettlement levelingSeatSettlement;
	private Contest contest;
	private Party party;
	private int partyVotes;
	private int partySeats;
	private int contestVotes;
	private int contestSeats;
	private BigDecimal quotient;
	private BigDecimal dividend;
	private BigDecimal divisor;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "leveling_seat_settlement_pk", nullable = false)
	public LevelingSeatSettlement getLevelingSeatSettlement() {
		return levelingSeatSettlement;
	}

	public void setLevelingSeatSettlement(LevelingSeatSettlement levelingSeatSettlement) {
		this.levelingSeatSettlement = levelingSeatSettlement;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return contest;
	}

	public void setContest(Contest contest) {
		this.contest = contest;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	@Column(name = "party_votes")
	public int getPartyVotes() {
		return partyVotes;
	}

	public void setPartyVotes(int partyVotes) {
		this.partyVotes = partyVotes;
		updateDividend();
	}

	@Transient
	public void incrementPartyVotes(int increment) {
		setPartyVotes(getPartyVotes() + increment);
	}

	@Column(name = "party_seats")
	public int getPartySeats() {
		return partySeats;
	}

	public void setPartySeats(int partySeats) {
		this.partySeats = partySeats;
		updateDividend();
	}

	private void updateDividend() {
		setDividend(bigDecimal(getPartyVotes()).divide(bigDecimal(getPartySeats() * 2 + 1), SCALE_SIX_DECIMALS, HALF_UP));
	}

	public void incrementPartySeats(int increment) {
		setPartySeats(getPartySeats() + increment);
	}

	@Column(name = "contest_votes")
	public int getContestVotes() {
		return contestVotes;
	}

	public void setContestVotes(int contestVotes) {
		this.contestVotes = contestVotes;
		updateDivisor();
		updateQuotient();
	}

	@Column(name = "contest_seats")
	public int getContestSeats() {
		return contestSeats;
	}

	public void setContestSeats(int contestSeats) {
		this.contestSeats = contestSeats;
		updateDivisor();
		updateQuotient();
	}

	private void updateDivisor() {
		int theContestSeats = getContestSeats();
		if (contestVotes != 0 && theContestSeats != 0) {
			setDivisor(bigDecimal(getContestVotes()).divide(bigDecimal(theContestSeats), SCALE_SIX_DECIMALS, HALF_UP));
		} else {
			setDivisor(ZERO);
		}
	}

	private BigDecimal bigDecimal(int value) {
		return BigDecimal.valueOf(value);
	}

	private void updateQuotient() {
		BigDecimal theDividend = getDividend();
		BigDecimal theDivisor = getDivisor();
		if (theDividend != null && !ZERO.equals(theDivisor)) {
			setQuotient(theDividend.divide(theDivisor, SCALE_SIX_DECIMALS, HALF_UP));
		} else {
			setQuotient(ZERO);
		}
	}

	@Column(name = "quotient")
	public BigDecimal getQuotient() {
		return quotient;
	}

	public void setQuotient(BigDecimal quotient) {
		this.quotient = quotient;
	}

	@Column(name = "dividend")
	public BigDecimal getDividend() {
		return dividend;
	}

	public void setDividend(BigDecimal dividend) {
		this.dividend = dividend;
	}

	@Column(name = "divisor")
	public BigDecimal getDivisor() {
		return divisor;
	}

	public void setDivisor(BigDecimal divisor) {
		this.divisor = divisor;
	}
}
