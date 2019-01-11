package no.evote.dto;

import java.io.Serializable;

public class CandidateVoteCountDto implements Serializable {
	private final Long candidatePk;
	private final String firstName;
	private final String lastName;
	private final Integer displayOrder;
	private final Long affiliationPk;
	private final Integer rankNumber;
	private double baseline;
	private double personal;
	private double renumber;
	private double strikeout;
	private double writein;

	public CandidateVoteCountDto(final Long candidatePk, final String firstName, final String lastName, final Long affiliationPk, final int rankNumber,
			final Integer displayOrder, final double value, final String voteCategoryId) {
		this.candidatePk = candidatePk;
		this.firstName = firstName;
		this.lastName = lastName;
		this.affiliationPk = affiliationPk;
		this.displayOrder = displayOrder;
		this.rankNumber = rankNumber;

		setVotes(value, voteCategoryId);
	}

	public final void setVotes(final double value, final String voteCategoryId) {
		if ("baseline".equals(voteCategoryId)) {
			this.baseline += value;
		} else if ("personal".equals(voteCategoryId)) {
			this.personal += value;
		} else if ("renumber".equals(voteCategoryId)) {
			this.renumber += value;
		} else if ("strikeout".equals(voteCategoryId)) {
			this.strikeout += value;
		} else if ("writein".equals(voteCategoryId)) {
			this.writein += value;
		}
	}

	public Long getCandidatePk() {
		return candidatePk;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Long getAffiliationPk() {
		return affiliationPk;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public Integer getRankNumber() {
		return rankNumber;
	}

	public double getBaseline() {
		return baseline;
	}

	public double getPersonal() {
		return personal;
	}

	public double getRenumber() {
		return renumber;
	}

	public double getStrikeout() {
		return strikeout;
	}

	public double getWritein() {
		return writein;
	}

	public double getTotal() {
		return baseline + personal + renumber + writein + strikeout;
	}

}
