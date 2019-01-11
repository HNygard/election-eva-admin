package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;
import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CandidateSeat implements Serializable {
	private final Candidate candidate;
	private final Affiliation affiliation;
	private final int seatNumber;
	private final BigDecimal quotient;
	private final int dividend;
	private final BigDecimal divisor;
	private final boolean elected;

	public CandidateSeat(Candidate candidate, Affiliation affiliation, int seatNumber, BigDecimal quotient, int dividend, BigDecimal divisor, boolean elected) {
		this.candidate = candidate;
		this.affiliation = affiliation;
		this.seatNumber = seatNumber;
		this.quotient = quotient;
		this.dividend = dividend;
		this.divisor = divisor;
		this.elected = elected;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	public BigDecimal getQuotient() {
		return quotient;
	}

	public int getDividend() {
		return dividend;
	}

	public BigDecimal getDivisor() {
		return divisor;
	}

	public boolean isElected() {
		return elected;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateSeat)) {
			return false;
		}
		CandidateSeat that = (CandidateSeat) o;
		return new EqualsBuilder()
				.append(seatNumber, that.seatNumber)
				.append(dividend, that.dividend)
				.append(elected, that.elected)
				.append(candidate, that.candidate)
				.append(affiliation, that.affiliation)
				.append(quotient, that.quotient)
				.append(divisor, that.divisor)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
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
}
