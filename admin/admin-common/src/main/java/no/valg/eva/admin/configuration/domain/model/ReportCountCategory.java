package no.valg.eva.admin.configuration.domain.model;

import static java.lang.String.format;

import java.util.EnumSet;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.counting.domain.model.CountCategory;

/**
 * Speficies the vote count categories which should be counted, and whether the preliminary count should be performed at the municipality level or at a lower
 * level
 */
@Entity
@Table(
		name = "report_count_category",
		uniqueConstraints = @UniqueConstraint(columnNames = { "election_group_pk", "municipality_pk", "vote_count_category_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "report_count_category_pk"))
@NamedQueries({
		@NamedQuery(name = "ReportCountCategory.findByMunAndEGAndVCC", query = "SELECT rcc FROM ReportCountCategory rcc WHERE rcc.municipality.pk = :mPk "
				+ "AND rcc.electionGroup.pk = :egPk AND rcc.voteCountCategory.pk = :vccPk"),
		@NamedQuery(name = "ReportCountCategory.findByMunAndEG", query = "SELECT rcc FROM ReportCountCategory rcc WHERE rcc.municipality.pk = :municipalityPk "
				+ "AND rcc.electionGroup.pk = :electionGroupPk"),
		@NamedQuery(name = "ReportCountCategory.findReportCountCategories", query = "SELECT rcc FROM ReportCountCategory rcc "
				+ "WHERE rcc.municipality.pk = :municipalityPk AND rcc.electionGroup.pk = :electionGroupPk ORDER BY rcc.voteCountCategory.id") })
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "ReportCountCategory.findCountingModeByMvAreaForMunicipalityAndContest",
				query = "SELECT rcc.central_preliminary_count, rcc.polling_district_count, rcc.technical_polling_district_count FROM report_count_category rcc "
						+ "JOIN election_group eg ON eg.election_group_pk = rcc.election_group_pk "
						+ "JOIN election e ON e.election_group_pk = eg.election_group_pk "
						+ "JOIN contest c ON c.election_pk = e.election_pk "
						+ "WHERE c.contest_pk = ?2 "
						+ "AND rcc.municipality_pk IN (SELECT m.municipality_pk FROM municipality m "
						+ "JOIN mv_area a ON a.municipality_pk = m.municipality_pk "
						+ "WHERE a.mv_area_pk = ?1) "
						+ "AND rcc.vote_count_category_pk IN (SELECT vcc.vote_count_category_pk FROM vote_count_category vcc "
						+ "JOIN report_count_category rcc ON rcc.vote_count_category_pk = vcc.vote_count_category_pk "
						+ "WHERE vcc.vote_count_category_id = 'VO')") })
public class ReportCountCategory extends CountCategory implements java.io.Serializable, Comparable<ReportCountCategory>, ContextSecurable {

	private Municipality municipality;
	private boolean technicalPollingDistrictCount;

	/**
	 * Creates instance with data from centrally defined category
	 * @param category
	 *            centrally defined category
	 */
	public ReportCountCategory(final ElectionVoteCountCategory category) {

		voteCountCategory = category.getVoteCountCategory();
		electionGroup = category.getElectionGroup();
		centralPreliminaryCount = category.isCentralPreliminaryCount();
		pollingDistrictCount = category.isPollingDistrictCount();
		specialCover = category.isSpecialCover();

		countCategoryEnabled = category.isCountCategoryEnabled();
		countCategoryEditable = category.isCountCategoryEditable();
		technicalPollingDistrictCountConfigurable = category.isTechnicalPollingDistrictCountConfigurable();
	}

	public ReportCountCategory() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_pk", nullable = false)
	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(final Municipality municipality) {
		this.municipality = municipality;
	}

	@Column(name = "technical_polling_district_count")
	public boolean isTechnicalPollingDistrictCount() {
		return technicalPollingDistrictCount;
	}

	public void setTechnicalPollingDistrictCount(final boolean technicalPollingDistrictCount) {
		this.technicalPollingDistrictCount = technicalPollingDistrictCount;
	}

	@Override
	public int compareTo(final ReportCountCategory reportCountCategory) {
		return voteCountCategory.getId().compareTo(reportCountCategory.voteCountCategory.getId());
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.MUNICIPALITY)) {
			return municipality.getPk();
		}
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_GROUP)) {
			return electionGroup.getPk();
		}
		return null;
	}

	@Transient
	public boolean isEnabled() {
		return countCategoryEnabled;
	}

	public void setEnabled(final boolean enabled) {
		countCategoryEnabled = enabled;
	}

	@Transient
	public boolean isEditable() {
		return countCategoryEditable;
	}

	public void setEditable(final boolean editable) {
		countCategoryEditable = editable;
	}

	@Transient
	public boolean isTechnicalPollingDistrictCountConfigurable() {
		return technicalPollingDistrictCountConfigurable;
	}

	public void setTechnicalPollingDistrictCountConfigurable(final boolean techIsConfigurable) {
		technicalPollingDistrictCountConfigurable = techIsConfigurable;
	}

	@Transient
	public CountingMode getCountingMode() {
		return CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}

	/**
	 * Validates that count mode one of the expected count modes.
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             when validation fails
	 */
	@Transient
	public void validateCountingMode(final CountingMode expectedCountingMode, final CountingMode... otherExpectedCountingModes) {
		CountingMode actualCountingMode = getCountingMode();
		if (otherExpectedCountingModes != null && otherExpectedCountingModes.length > 0) {
			EnumSet<CountingMode> expectedCountingModes = EnumSet.of(expectedCountingMode, otherExpectedCountingModes);
			if (!expectedCountingModes.contains(actualCountingMode)) {
				throw new IllegalArgumentException(format(
						"expected count mode for report count category to be one of <%s>, but was <%s>",
						expectedCountingModes,
						actualCountingMode));
			}
		} else if (actualCountingMode != expectedCountingMode) {
			throw new IllegalArgumentException(format(
					"expected count mode for report count category to be <%s>, but was <%s>",
					expectedCountingMode,
					actualCountingMode));
		}
	}

	@Transient
	public no.valg.eva.admin.common.counting.model.CountCategory getCountCategory() {
		return voteCountCategory.getCountCategory();
	}

	@Transient
	public boolean isPollingDistrictOrTechnicalPollingDistrictCount() {
		return isPollingDistrictCount() || isTechnicalPollingDistrictCount();
	}
}
