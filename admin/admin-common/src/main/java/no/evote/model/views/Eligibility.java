package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;

import no.evote.constants.SQLConstants;
import no.valg.eva.admin.configuration.domain.model.MunicipalityAgeLimit;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

/**
 * View for calculation of eligible contests per voter
 */
@Entity
@Table(name = "eligibility")
@NamedQueries({
		@NamedQuery(
				name = "Eligibility.findTheoreticalEligibilityForVoterInGroup",
				query = "SELECT e FROM Eligibility e WHERE e.electionGroupPk = :electionGroupPk AND " + "e.id.mvAreaPk = :mvAreaPk AND :eligible = TRUE") })
@NamedNativeQueries({
		@NamedNativeQuery(name = "Eligibility.findMaxEndDateOfBirth", query = "SELECT municipality_id, "
				+ "max(end_date_of_birth) as must_be_born_before FROM eligibility "
				+ "WHERE election_event_pk = ?0 "
				+ "GROUP BY municipality_id",
				resultSetMapping = "MunicipalityAgeLimit") })
@SqlResultSetMappings({
	@SqlResultSetMapping(
		name = "MunicipalityAgeLimit",
		classes = {
			@ConstructorResult(
				targetClass = MunicipalityAgeLimit.class,
				columns = {
					@ColumnResult(name = "municipality_id"),
					@ColumnResult(name = "must_be_born_before")
				})
		}) })
public class Eligibility implements java.io.Serializable {

	private EligibilityId id;
	private Long electionEventPk;
	private Long electionGroupPk;
	private Long electionPk;
	private Long countryPk;
	private Long countyPk;
	private Long municipalityPk;
	private Long boroughPk;
	private String electionEventId;
	private String electionGroupId;
	private String electionId;
	private String contestId;
	private String countryId;
	private String countyId;
	private String municipalityId;
	private String boroughId;
	private String pollingDistrictId;
	private String electionEventName;
	private String electionGroupName;
	private String electionName;
	private String contestName;
	private String countryName;
	private String countyName;
	private String municipalityName;
	private String boroughName;
	private String pollingDistrictName;
	private LocalDate endDateOfBirth;
	private LocalDate electionEndDateOfBirth;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "mvElectionPk", column = @Column(name = "mv_election_pk", nullable = false)),
			@AttributeOverride(name = "mvAreaPk", column = @Column(name = "mv_area_pk", nullable = false)) })
	public EligibilityId getId() {
		return this.id;
	}

	public void setId(final EligibilityId id) {
		this.id = id;
	}

	@Column(name = SQLConstants.ELECTION_EVENT_PK)
	public Long getElectionEventPk() {
		return this.electionEventPk;
	}

	public void setElectionEventPk(final Long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	@Column(name = "election_group_pk")
	public Long getElectionGroupPk() {
		return this.electionGroupPk;
	}

	public void setElectionGroupPk(final Long electionGroupPk) {
		this.electionGroupPk = electionGroupPk;
	}

	@Column(name = "election_pk")
	public Long getElectionPk() {
		return this.electionPk;
	}

	public void setElectionPk(final Long electionPk) {
		this.electionPk = electionPk;
	}

	@Column(name = "country_pk")
	public Long getCountryPk() {
		return this.countryPk;
	}

	public void setCountryPk(final Long countryPk) {
		this.countryPk = countryPk;
	}

	@Column(name = "county_pk")
	public Long getCountyPk() {
		return this.countyPk;
	}

	public void setCountyPk(final Long countyPk) {
		this.countyPk = countyPk;
	}

	@Column(name = "municipality_pk")
	public Long getMunicipalityPk() {
		return this.municipalityPk;
	}

	public void setMunicipalityPk(final Long municipalityPk) {
		this.municipalityPk = municipalityPk;
	}

	@Column(name = "borough_pk")
	public Long getBoroughPk() {
		return this.boroughPk;
	}

	public void setBoroughPk(final Long boroughPk) {
		this.boroughPk = boroughPk;
	}

	@Column(name = "election_event_id", length = 8)
	public String getElectionEventId() {
		return this.electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	@Column(name = "election_group_id", length = 8)
	public String getElectionGroupId() {
		return this.electionGroupId;
	}

	public void setElectionGroupId(final String electionGroupId) {
		this.electionGroupId = electionGroupId;
	}

	@Column(name = "election_id", length = 8)
	public String getElectionId() {
		return this.electionId;
	}

	public void setElectionId(final String electionId) {
		this.electionId = electionId;
	}

	@Column(name = "contest_id", length = 8)
	public String getContestId() {
		return this.contestId;
	}

	public void setContestId(final String contestId) {
		this.contestId = contestId;
	}

	@Column(name = "country_id", length = 2)
	public String getCountryId() {
		return this.countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	@Column(name = "county_id", length = 2)
	public String getCountyId() {
		return this.countyId;
	}

	public void setCountyId(final String countyId) {
		this.countyId = countyId;
	}

	@Column(name = "municipality_id", length = 4)
	public String getMunicipalityId() {
		return this.municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Column(name = "borough_id", length = 6)
	public String getBoroughId() {
		return this.boroughId;
	}

	public void setBoroughId(final String boroughId) {
		this.boroughId = boroughId;
	}

	@Column(name = "polling_district_id", length = 4)
	public String getPollingDistrictId() {
		return this.pollingDistrictId;
	}

	public void setPollingDistrictId(final String pollingDistrictId) {
		this.pollingDistrictId = pollingDistrictId;
	}

	@Column(name = "election_event_name")
	public String getElectionEventName() {
		return this.electionEventName;
	}

	public void setElectionEventName(final String electionEventName) {
		this.electionEventName = electionEventName;
	}

	@Column(name = "election_group_name")
	public String getElectionGroupName() {
		return this.electionGroupName;
	}

	public void setElectionGroupName(final String electionGroupName) {
		this.electionGroupName = electionGroupName;
	}

	@Column(name = "election_name")
	public String getElectionName() {
		return this.electionName;
	}

	public void setElectionName(final String electionName) {
		this.electionName = electionName;
	}

	@Column(name = "contest_name")
	public String getContestName() {
		return this.contestName;
	}

	public void setContestName(final String contestName) {
		this.contestName = contestName;
	}

	@Column(name = "country_name")
	public String getCountryName() {
		return this.countryName;
	}

	public void setCountryName(final String countryName) {
		this.countryName = countryName;
	}

	@Column(name = "county_name")
	public String getCountyName() {
		return this.countyName;
	}

	public void setCountyName(final String countyName) {
		this.countyName = countyName;
	}

	@Column(name = "municipality_name")
	public String getMunicipalityName() {
		return this.municipalityName;
	}

	public void setMunicipalityName(final String municipalityName) {
		this.municipalityName = municipalityName;
	}

	@Column(name = "borough_name")
	public String getBoroughName() {
		return this.boroughName;
	}

	public void setBoroughName(final String boroughName) {
		this.boroughName = boroughName;
	}

	@Column(name = "polling_district_name")
	public String getPollingDistrictName() {
		return this.pollingDistrictName;
	}

	public void setPollingDistrictName(final String pollingDistrictName) {
		this.pollingDistrictName = pollingDistrictName;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "end_date_of_birth", length = 13)
	public LocalDate getEndDateOfBirth() {
		return endDateOfBirth;
	}

	public void setEndDateOfBirth(LocalDate endDateOfBirth) {
		this.endDateOfBirth = endDateOfBirth;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "election_end_date_of_birth", length = 13)
	public LocalDate getElectionEndDateOfBirth() {
		return electionEndDateOfBirth;
	}

	public void setElectionEndDateOfBirth(LocalDate electionEndDateOfBirth) {
		this.electionEndDateOfBirth = electionEndDateOfBirth;
	}
}
