package no.valg.eva.admin.settlement.domain.event;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CandidateSeatEvent implements Event {
	private final Affiliation affiliation;
	private final Candidate candidate;
	private final Integer dividend;
	private final BigDecimal divisor;

	public CandidateSeatEvent(Affiliation affiliation, Candidate candidate, BigDecimal divisor) {
		this.affiliation = affiliation;
		this.candidate = candidate;
		this.dividend = null;
		this.divisor = divisor;
	}

	public CandidateSeatEvent(Affiliation affiliation, Candidate candidate, int dividend) {
		this.affiliation = affiliation;
		this.candidate = candidate;
		this.dividend = dividend;
		this.divisor = null;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Integer getDividend() {
		return dividend;
	}

	public BigDecimal getDivisor() {
		return divisor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateSeatEvent)) {
			return false;
		}
		CandidateSeatEvent that = (CandidateSeatEvent) o;
		return new EqualsBuilder()
				.append(affiliation, that.affiliation)
				.append(candidate, that.candidate)
				.append(dividend, that.dividend)
				.append(divisor, that.divisor)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(affiliation)
				.append(candidate)
				.append(dividend)
				.append(divisor)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("affiliation", affiliation)
				.append("candidate", candidate)
				.append("dividend", dividend)
				.append("divisor", divisor)
				.toString();
	}
}
