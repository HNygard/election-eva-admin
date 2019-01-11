package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LevelingSeat implements Serializable {
	private final int rankNumber;
	private final Integer seatNumber;
	private final String contestName;
	private final String partyId;
	private final String candidateName;
	private final Integer displayOrder;

	public LevelingSeat(int rankNumber, Integer seatNumber, String contestName, String partyId, String candidateName, Integer displayOrder) {
		this.rankNumber = rankNumber;
		this.seatNumber = seatNumber;
		this.contestName = contestName;
		this.partyId = partyId;
		this.candidateName = candidateName;
		this.displayOrder = displayOrder;
	}

	public int getRankNumber() {
		return rankNumber;
	}

	public Integer getSeatNumber() {
		return seatNumber;
	}

	public String getContestName() {
		return contestName;
	}

	public String getPartyId() {
		return partyId;
	}

	public String getCandidateName() {
		return candidateName;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LevelingSeat)) {
			return false;
		}
		LevelingSeat that = (LevelingSeat) o;
		return new EqualsBuilder()
				.append(rankNumber, that.rankNumber)
				.append(seatNumber, that.seatNumber)
				.append(contestName, that.contestName)
				.append(partyId, that.partyId)
				.append(candidateName, that.candidateName)
				.append(displayOrder, that.displayOrder)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(rankNumber)
				.append(seatNumber)
				.append(contestName)
				.append(partyId)
				.append(candidateName)
				.append(displayOrder)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("rankNumber", rankNumber)
				.append("seatNumber", seatNumber)
				.append("contestName", contestName)
				.append("partyId", partyId)
				.append("candidateName", candidateName)
				.append("displayOrder", displayOrder)
				.toString();
	}
}
