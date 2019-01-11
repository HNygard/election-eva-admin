package no.evote.model.views;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ElectionDayVotingsId implements java.io.Serializable {

	private Long contestPk;
	private Long electionDayPk;
	private Long mvAreaPk;
	private Long votingCategoryPk;

	public ElectionDayVotingsId() {
	}

	public ElectionDayVotingsId(final Long contestPk, final Long electionDayPk, final Long mvAreaPk, final Long votingCategoryPk) {
		super();
		this.contestPk = contestPk;
		this.electionDayPk = electionDayPk;
		this.mvAreaPk = mvAreaPk;
		this.votingCategoryPk = votingCategoryPk;
	}

	@Column(name = "contest_pk", nullable = false, insertable = false, updatable = false)
	public Long getContestPk() {
		return contestPk;
	}

	public void setContestPk(final Long contestPk) {
		this.contestPk = contestPk;
	}

	@Column(name = "mv_area_pk", nullable = false)
	public Long getMvAreaPk() {
		return mvAreaPk;
	}

	public void setMvAreaPk(final Long mvAreaPk) {
		this.mvAreaPk = mvAreaPk;
	}

	@Column(name = "election_day_pk", nullable = false)
	public Long getElectionDayPk() {
		return electionDayPk;
	}

	public void setElectionDayPk(final Long electionDayPk) {
		this.electionDayPk = electionDayPk;
	}

	@Column(name = "voting_category_pk", nullable = false)
	public Long getVotingCategoryPk() {
		return votingCategoryPk;
	}

	public void setVotingCategoryPk(final Long votingCategoryPk) {
		this.votingCategoryPk = votingCategoryPk;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof ElectionDayVotingsId) {
			return EqualsBuilder.reflectionEquals(o, this, false);
		}
		return false;
	}
}
