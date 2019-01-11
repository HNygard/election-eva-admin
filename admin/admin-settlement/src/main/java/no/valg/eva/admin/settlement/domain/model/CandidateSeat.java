package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Settlement: Seats assigned by the modified Sainte-Lagüe method
 */
@Entity
@Table(name = "candidate_seat", uniqueConstraints = { @UniqueConstraint(columnNames = { "settlement_pk", "candidate_pk" }),
		@UniqueConstraint(columnNames = { "settlement_pk", "seat_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "candidate_seat_pk"))
@NamedQueries({ @NamedQuery(name = "CandidateSeat.findBySettlement", query = "SELECT cs FROM CandidateSeat cs WHERE"
		+ " cs.settlement.pk = :settlementPk ORDER BY cs.seatNumber") })
public class CandidateSeat extends VersionedEntity implements java.io.Serializable {
	private static final int QUOTIENT_SCALE = 6;

	private Settlement settlement;
	private Candidate candidate;
	private Affiliation affiliation;
	private int seatNumber;
	private BigDecimal quotient;
	private int dividend;
	private BigDecimal divisor;
	private boolean elected;

	public CandidateSeat() {
	}

	public CandidateSeat(Candidate candidate, Affiliation affiliation, int seatNumber, int dividend, BigDecimal divisor, boolean elected) {
		this.candidate = candidate;
		this.affiliation = affiliation;
		this.seatNumber = seatNumber;
		this.dividend = dividend;
		this.divisor = divisor;
		this.elected = elected;
		updateQuotient();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_pk", nullable = false)
	public Settlement getSettlement() {
		return this.settlement;
	}

	public void setSettlement(final Settlement settlement) {
		this.settlement = settlement;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_pk", nullable = false)
	public Candidate getCandidate() {
		return this.candidate;
	}

	public void setCandidate(final Candidate candidate) {
		this.candidate = candidate;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "affiliation_pk", nullable = false)
	public Affiliation getAffiliation() {
		return this.affiliation;
	}

	public void setAffiliation(final Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	@Column(name = "seat_number", nullable = false)
	public int getSeatNumber() {
		return this.seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	public void updateSeatNumberAndElectedState(int seatNumber, int numberOfPositionsInContest) {
		this.seatNumber = seatNumber;
		this.elected = seatNumber <= numberOfPositionsInContest;
	}

	@Column(name = "quotient", nullable = false, precision = 14)
	public BigDecimal getQuotient() {
		return this.quotient;
	}

	public void setQuotient(final BigDecimal quotient) {
		this.quotient = quotient;
	}

	@Column(name = "dividend", nullable = false)
	public int getDividend() {
		return this.dividend;
	}

	public void setDividend(final int dividend) {
		this.dividend = dividend;
		updateQuotient();
	}

	@Column(name = "divisor", nullable = false, precision = 5)
	public BigDecimal getDivisor() {
		return this.divisor;
	}

	public void setDivisor(final BigDecimal divisor) {
		this.divisor = divisor;
		updateQuotient();
	}

	@Column(name = "elected", nullable = false)
	public boolean isElected() {
		return this.elected;
	}

	public void setElected(final boolean elected) {
		this.elected = elected;
	}

	private void updateQuotient() {
		if (ZERO.equals(divisor)) {
			quotient = new BigDecimal("0.000000");
		} else if (divisor != null) {
			quotient = BigDecimal.valueOf(dividend).divide(divisor, QUOTIENT_SCALE, HALF_UP);
		} else {
			quotient = null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (getPk() != null) {
			return super.equals(o);
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateSeat)) {
			return false;
		}
		CandidateSeat that = (CandidateSeat) o;
		return new EqualsBuilder()
				.append(settlement, that.settlement)
				.append(candidate, that.candidate)
				.append(affiliation, that.affiliation)
				.append(seatNumber, that.seatNumber)
				.append(quotient, that.quotient)
				.append(dividend, that.dividend)
				.append(divisor, that.divisor)
				.append(elected, that.elected)
				.isEquals();
	}

	@Override
	public int hashCode() {
		if (getPk() != null) {
			return super.hashCode();
		}
		return new HashCodeBuilder(17, 37)
				.append(settlement)
				.append(candidate)
				.append(affiliation)
				.append(seatNumber)
				.append(quotient)
				.append(dividend)
				.append(divisor)
				.append(elected)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("candidate", candidate)
				.append("affiliation", affiliation)
				.append("seatNumber", seatNumber)
				.append("quotient", quotient)
				.append("dividend", dividend)
				.append("divisor", divisor)
				.append("elected", elected)
				.toString();
	}

	public void accept(SettlementVisitor visitor) {
		visitor.visit(this);
	}

	@Transient
	public String getCandidateNameLine() {
		return candidate.getNameLine();
	}

	@Transient
	public Integer getCandidateDisplayOrder() {
		return candidate.getDisplayOrder();
	}

	@Transient
	public Contest getContest() {
		return getSettlement().getContest();
	}

	@Transient
	public Party getParty() {
		return getAffiliation().getParty();
	}
}
