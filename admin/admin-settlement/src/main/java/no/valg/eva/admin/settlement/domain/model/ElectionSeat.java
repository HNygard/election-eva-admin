package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.FetchType.LAZY;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Party;

@Entity
@Table(name = "election_seat", uniqueConstraints = { @UniqueConstraint(columnNames = { "election_settlement_pk", "seat_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "election_seat_pk"))
public class ElectionSeat extends VersionedEntity {
	private ElectionSettlement electionSettlement;
	private int seatNumber;
	private Party party;
	private BigDecimal quotient;
	private int dividend;
	private BigDecimal divisor;
	private boolean sameQuotientAsNext;
	private boolean sameVotesAsNext;
	private boolean elected;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "election_settlement_pk", nullable = false)
	public ElectionSettlement getElectionSettlement() {
		return electionSettlement;
	}

	public void setElectionSettlement(ElectionSettlement electionSettlement) {
		this.electionSettlement = electionSettlement;
	}

	@Column(name = "seat_number", nullable = false)
	public int getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	@Column(name = "quotient", nullable = false, precision = 14)
	public BigDecimal getQuotient() {
		return quotient;
	}

	public void setQuotient(BigDecimal quotient) {
		this.quotient = quotient;
	}

	@Column(name = "dividend", nullable = false)
	public int getDividend() {
		return dividend;
	}

	public void setDividend(int dividend) {
		this.dividend = dividend;
	}

	@Column(name = "divisor", nullable = false, precision = 5)
	public BigDecimal getDivisor() {
		return divisor;
	}

	public void setDivisor(BigDecimal divisor) {
		this.divisor = divisor;
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

	@Column(name = "elected", nullable = false)
	public boolean isElected() {
		return elected;
	}

	public void setElected(boolean elected) {
		this.elected = elected;
	}
}
