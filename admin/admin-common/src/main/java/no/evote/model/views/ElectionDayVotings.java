package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;

@Entity
@Table(name = "election_day_votings")
@NamedQueries({ @NamedQuery(name = "ElectionDayVotings.get", query = "SELECT edv FROM ElectionDayVotings edv WHERE edv.contest.pk = :contestPk AND "
		+ "edv.mvArea.pollingDistrict.pk = :pollingDistrictPk AND edv.votingCategory.id = :votingCategoryId ORDER BY edv.electionDay.date ") })
public class ElectionDayVotings implements java.io.Serializable, ContextSecurable {

	private ElectionDayVotingsId id;
	private Contest contest;
	private MvArea mvArea;
	private ElectionDay electionDay;
	private VotingCategory votingCategory;
	private Integer votings;

	public ElectionDayVotings() {
	}

	public ElectionDayVotings(Contest contest, MvArea mvArea, VotingCategory votingCategory, ElectionDay electionDay) {
		super();
		this.contest = contest;
		this.mvArea = mvArea;
		this.votingCategory = votingCategory;
		this.electionDay = electionDay;
		this.id = new ElectionDayVotingsId(contest.getPk(), electionDay.getPk(), mvArea.getPk(), votingCategory.getPk());
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "contestPk", column = @Column(name = "contest_pk", nullable = false)),
			@AttributeOverride(name = "mvAreaPk", column = @Column(name = "mv_area_pk", nullable = false)),
			@AttributeOverride(name = "electionDayPk", column = @Column(name = "election_day_pk", nullable = false)),
			@AttributeOverride(name = "votingCategoryPk", column = @Column(name = "voting_category_pk", nullable = false)) })
	public ElectionDayVotingsId getId() {
		return id;
	}

	public void setId(ElectionDayVotingsId id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "contest_pk", insertable = false, updatable = false)
	public Contest getContest() {
		return contest;
	}

	public void setContest(Contest contest) {
		this.contest = contest;
	}

	@ManyToOne
	@JoinColumn(name = "mv_area_pk", insertable = false, updatable = false)
	public MvArea getMvArea() {
		return mvArea;
	}

	public void setMvArea(MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@ManyToOne
	@JoinColumn(name = "election_day_pk", insertable = false, updatable = false)
	public ElectionDay getElectionDay() {
		return electionDay;
	}

	public void setElectionDay(ElectionDay electionDay) {
		this.electionDay = electionDay;
	}

	@ManyToOne
	@JoinColumn(name = "voting_category_pk", insertable = false, updatable = false)
	public VotingCategory getVotingCategory() {
		return votingCategory;
	}

	public void setVotingCategory(VotingCategory votingCategory) {
		this.votingCategory = votingCategory;
	}

	@Min(0)
	@Column(name = "votings", insertable = false, updatable = false)
	public Integer getVotings() {
		return votings;
	}

	public void setVotings(Integer votings) {
		this.votings = votings;
	}

	@Override
	public Long getAreaPk(AreaLevelEnum level) {
		return mvArea.getAreaPk(level);
	}

	@Override
	public Long getElectionPk(ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return contest.getPk();
		} else {
			return null;
		}
	}
}
