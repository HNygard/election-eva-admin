package no.valg.eva.admin.counting.domain.model;


import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;

/**
 * Base class for count categories
 */
@MappedSuperclass
public abstract class CountCategory extends VersionedEntity {

	protected VoteCountCategory voteCountCategory;
	protected ElectionGroup electionGroup;
	protected boolean centralPreliminaryCount = true;
	protected boolean pollingDistrictCount;
	protected boolean specialCover;

	protected boolean countCategoryEnabled;
	protected boolean countCategoryEditable;
	protected boolean technicalPollingDistrictCountConfigurable;

	/**
	 * @return id of VoteCountCategory
	 */
	public String key() {
		return voteCountCategory.getId();
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vote_count_category_pk", nullable = false)
	public VoteCountCategory getVoteCountCategory() {
		return voteCountCategory;
	}

	public void setVoteCountCategory(final VoteCountCategory voteCountCategory) {
		this.voteCountCategory = voteCountCategory;
	}

	@Column(name = "special_cover", nullable = false)
	public boolean isSpecialCover() {
		return specialCover;
	}

	public void setSpecialCover(final boolean specialCover) {
		this.specialCover = specialCover;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "election_group_pk", nullable = false)
	public ElectionGroup getElectionGroup() {
		return electionGroup;
	}

	public void setElectionGroup(final ElectionGroup electionGroup) {
		this.electionGroup = electionGroup;
	}

	@Column(name = "central_preliminary_count", nullable = false)
	public boolean isCentralPreliminaryCount() {
		return this.centralPreliminaryCount;
	}

	public void setCentralPreliminaryCount(final boolean centralPreliminaryCount) {
		this.centralPreliminaryCount = centralPreliminaryCount;
	}

	@Column(name = "polling_district_count", nullable = false)
	public boolean isPollingDistrictCount() {
		return this.pollingDistrictCount;
	}

	public void setPollingDistrictCount(final boolean pollingDistrictCount) {
		this.pollingDistrictCount = pollingDistrictCount;
	}

}
