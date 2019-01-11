package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.counting.domain.model.CountCategory;

/**
 * Specifies default values for the vote count categories, and whether a configuration of count categories can be done locally
 */
@Entity
@Table(
		name = "election_vote_count_category",
		uniqueConstraints = @UniqueConstraint(columnNames = { "election_group_pk", "vote_count_category_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "election_vote_count_category_pk"))
@NamedQueries({
		@NamedQuery(name = "ElectionVoteCountCategory.findElectionVoteCountCategories", query = "SELECT evcc FROM ElectionVoteCountCategory evcc "
				+ "WHERE evcc.electionGroup.pk = :electionGroupPk ORDER BY evcc.voteCountCategory.id") })
public class ElectionVoteCountCategory extends CountCategory {

	@Column(name = "count_category_enabled", nullable = false)
	public boolean isCountCategoryEnabled() {
		return countCategoryEnabled;
	}

	public void setCountCategoryEnabled(final boolean enabled) {
		this.countCategoryEnabled = enabled;
	}

	@Column(name = "count_category_editable", nullable = false)
	public boolean isCountCategoryEditable() {
		return countCategoryEditable;
	}

	public void setCountCategoryEditable(final boolean countCategoryEditable) {
		this.countCategoryEditable = countCategoryEditable;
	}

	@Column(name = "technical_polling_district_count_configurable", nullable = false)
	public boolean isTechnicalPollingDistrictCountConfigurable() {
		return technicalPollingDistrictCountConfigurable;
	}

	public void setTechnicalPollingDistrictCountConfigurable(final boolean technicalPollingDistrictCountConfigurable) {
		this.technicalPollingDistrictCountConfigurable = technicalPollingDistrictCountConfigurable;
	}

	@Transient
	public CountingMode getCountingMode() {
		return CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, false);
	}
}
