package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import no.evote.constants.SQLConstants;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * View for calculation of votings/markoffs per contest
 */
@Entity
@Table(name = "contest_voting")
@NamedQueries({
		@NamedQuery(
				name = "ContestVoting.countApprovedElectoralRollMarkOffsVotingPollingDistrict",
				query = "SELECT COUNT(cv) FROM ContestVoting cv where cv.id.contestPk = :conpk AND "
						+ "cv.votingCategoryId = :cat AND cv.votingPollingDistrictPk = :pdpk AND approved = true"),
		@NamedQuery(name = "ContestVoting.countElectoralRollCheckCatApproved", query = "SELECT COUNT(cv) FROM ContestVoting cv where cv.id.contestPk = :conpk "
				+ "AND cv.votingCategoryId = :cat AND cv.pollingDistrictPk = :pdpk AND lateValidation = :late AND approved = true"),
		@NamedQuery(
				name = "ContestVoting.countElectoralRollCheckCatNotRejected",
				query = "SELECT COUNT(cv) FROM ContestVoting cv where cv.id.contestPk = :conpk "
						+ "AND cv.votingCategoryId = :cat AND cv.pollingDistrictPk = :pdpk AND lateValidation = :late AND votingRejectionPk IS NULL"),
		@NamedQuery(
				name = "ContestVoting.countElectoralRollCheckCatMunicipalityApproved",
				query = "SELECT COUNT(cv) FROM ContestVoting cv where cv.id.contestPk = :conpk AND "
						+ "cv.votingCategoryId = :cat AND cv.municipalityPk = :mpk AND lateValidation = :late AND approved = true"),
		@NamedQuery(
				name = "ContestVoting.countElectoralRollCheckCatMunicipalityNotRejected",
				query = "SELECT COUNT(cv) FROM ContestVoting cv where cv.id.contestPk = :conpk AND "
						+ "cv.votingCategoryId = :cat AND cv.municipalityPk = :mpk AND lateValidation = :late AND votingRejectionPk IS NULL") })

public class ContestVoting implements java.io.Serializable {

	private ContestVotingId id;
	private Long mvElectionPk;
	private Long mvAreaPk;
	private Long electionEventPk;
	private Long electionGroupPk;
	private Long electionPk;
	private Long countryPk;
	private Long countyPk;
	private Long municipalityPk;
	private Long boroughPk;
	private Long pollingDistrictPk;
	private Long votingCountryPk;
	private Long votingCountyPk;
	private Long votingMunicipalityPk;
	private Long votingBoroughPk;
	private Long votingPollingDistrictPk;
	private Long votingPollingPlacePk;
	private Long voterPk;
	private String electionEventId;
	private String electionGroupId;
	private String electionId;
	private String contestId;
	private String voterId;
	private LocalDate dateOfBirth;
	private String nameLine;
	private String countryId;
	private String countyId;
	private String municipalityId;
	private String boroughId;
	private String pollingDistrictId;
	private String votingCountryId;
	private String votingCountyId;
	private String votingMunicipalityId;
	private String votingBoroughId;
	private String votingPollingDistrictId;
	private String votingPollingPlaceId;
	private Integer votingNumber;
	private DateTime castTimestamp;
	private DateTime validationTimestamp;
	private Boolean lateValidation;
	private Boolean approved;
	private String votingCategoryId;
	private Boolean earlyVoting;
	private String votingCategoryName;
	private String electionEventName;
	private String electionGroupName;
	private String electionName;
	private String contestName;
	private String countryName;
	private String countyName;
	private String municipalityName;
	private String boroughName;
	private String pollingDistrictName;
	private String votingCountryName;
	private String votingCountyName;
	private String votingMunicipalityName;
	private String votingBoroughName;
	private String votingPollingDistrictName;
	private String votingPollingPlaceName;
	private Long votingRejectionPk;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "contestPk", column = @Column(name = "contest_pk", nullable = false)),
			@AttributeOverride(name = "votingPk", column = @Column(name = "voting_pk", nullable = false)) })
	public ContestVotingId getId() {
		return id;
	}

	public void setId(final ContestVotingId id) {
		this.id = id;
	}

	@Column(name = "mv_election_pk")
	public Long getMvElectionPk() {
		return mvElectionPk;
	}

	public void setMvElectionPk(final Long mvElectionPk) {
		this.mvElectionPk = mvElectionPk;
	}

	@Column(name = "mv_area_pk")
	public Long getMvAreaPk() {
		return mvAreaPk;
	}

	public void setMvAreaPk(final Long mvAreaPk) {
		this.mvAreaPk = mvAreaPk;
	}

	@Column(name = SQLConstants.ELECTION_EVENT_PK)
	public Long getElectionEventPk() {
		return electionEventPk;
	}

	public void setElectionEventPk(final Long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	@Column(name = "election_group_pk")
	public Long getElectionGroupPk() {
		return electionGroupPk;
	}

	public void setElectionGroupPk(final Long electionGroupPk) {
		this.electionGroupPk = electionGroupPk;
	}

	@Column(name = "election_pk")
	public Long getElectionPk() {
		return electionPk;
	}

	public void setElectionPk(final Long electionPk) {
		this.electionPk = electionPk;
	}

	@Column(name = "country_pk")
	public Long getCountryPk() {
		return countryPk;
	}

	public void setCountryPk(final Long countryPk) {
		this.countryPk = countryPk;
	}

	@Column(name = "county_pk")
	public Long getCountyPk() {
		return countyPk;
	}

	public void setCountyPk(final Long countyPk) {
		this.countyPk = countyPk;
	}

	@Column(name = "municipality_pk")
	public Long getMunicipalityPk() {
		return municipalityPk;
	}

	public void setMunicipalityPk(final Long municipalityPk) {
		this.municipalityPk = municipalityPk;
	}

	@Column(name = "borough_pk")
	public Long getBoroughPk() {
		return boroughPk;
	}

	public void setBoroughPk(final Long boroughPk) {
		this.boroughPk = boroughPk;
	}

	@Column(name = "polling_district_pk")
	public Long getPollingDistrictPk() {
		return pollingDistrictPk;
	}

	public void setPollingDistrictPk(final Long pollingDistrictPk) {
		this.pollingDistrictPk = pollingDistrictPk;
	}

	@Column(name = "voting_country_pk")
	public Long getVotingCountryPk() {
		return votingCountryPk;
	}

	public void setVotingCountryPk(final Long votingCountryPk) {
		this.votingCountryPk = votingCountryPk;
	}

	@Column(name = "voting_county_pk")
	public Long getVotingCountyPk() {
		return votingCountyPk;
	}

	public void setVotingCountyPk(final Long votingCountyPk) {
		this.votingCountyPk = votingCountyPk;
	}

	@Column(name = "voting_municipality_pk")
	public Long getVotingMunicipalityPk() {
		return votingMunicipalityPk;
	}

	public void setVotingMunicipalityPk(final Long votingMunicipalityPk) {
		this.votingMunicipalityPk = votingMunicipalityPk;
	}

	@Column(name = "voting_borough_pk")
	public Long getVotingBoroughPk() {
		return votingBoroughPk;
	}

	public void setVotingBoroughPk(final Long votingBoroughPk) {
		this.votingBoroughPk = votingBoroughPk;
	}

	@Column(name = "voting_polling_district_pk")
	public Long getVotingPollingDistrictPk() {
		return votingPollingDistrictPk;
	}

	public void setVotingPollingDistrictPk(final Long votingPollingDistrictPk) {
		this.votingPollingDistrictPk = votingPollingDistrictPk;
	}

	@Column(name = "voting_polling_place_pk")
	public Long getVotingPollingPlacePk() {
		return votingPollingPlacePk;
	}

	public void setVotingPollingPlacePk(final Long votingPollingPlacePk) {
		this.votingPollingPlacePk = votingPollingPlacePk;
	}

	@Column(name = "voter_pk")
	public Long getVoterPk() {
		return voterPk;
	}

	public void setVoterPk(final Long voterPk) {
		this.voterPk = voterPk;
	}

	@Column(name = "election_event_id", length = 8)
	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	@Column(name = "election_group_id", length = 8)
	public String getElectionGroupId() {
		return electionGroupId;
	}

	public void setElectionGroupId(final String electionGroupId) {
		this.electionGroupId = electionGroupId;
	}

	@Column(name = "election_id", length = 8)
	public String getElectionId() {
		return electionId;
	}

	public void setElectionId(final String electionId) {
		this.electionId = electionId;
	}

	@Column(name = "contest_id", length = 8)
	public String getContestId() {
		return contestId;
	}

	public void setContestId(final String contestId) {
		this.contestId = contestId;
	}

	@Column(name = "voter_id", length = 11)
	public String getVoterId() {
		return voterId;
	}

	public void setVoterId(final String voterId) {
		this.voterId = voterId;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "date_of_birth", length = 13)
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Column(name = "name_line")
	public String getNameLine() {
		return nameLine;
	}

	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Column(name = "country_id", length = 2)
	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	@Column(name = "county_id", length = 2)
	public String getCountyId() {
		return countyId;
	}

	public void setCountyId(final String countyId) {
		this.countyId = countyId;
	}

	@Column(name = "municipality_id", length = 4)
	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Column(name = "borough_id", length = 6)
	public String getBoroughId() {
		return boroughId;
	}

	public void setBoroughId(final String boroughId) {
		this.boroughId = boroughId;
	}

	@Column(name = "polling_district_id", length = 4)
	public String getPollingDistrictId() {
		return pollingDistrictId;
	}

	public void setPollingDistrictId(final String pollingDistrictId) {
		this.pollingDistrictId = pollingDistrictId;
	}

	@Column(name = "voting_country_id")
	public String getVotingCountryId() {
		return votingCountryId;
	}

	public void setVotingCountryId(final String votingCountryId) {
		this.votingCountryId = votingCountryId;
	}

	@Column(name = "voting_county_id")
	public String getVotingCountyId() {
		return votingCountyId;
	}

	public void setVotingCountyId(final String votingCountyId) {
		this.votingCountyId = votingCountyId;
	}

	@Column(name = "voting_municipality_id")
	public String getVotingMunicipalityId() {
		return votingMunicipalityId;
	}

	public void setVotingMunicipalityId(final String votingMunicipalityId) {
		this.votingMunicipalityId = votingMunicipalityId;
	}

	@Column(name = "voting_borough_id")
	public String getVotingBoroughId() {
		return votingBoroughId;
	}

	public void setVotingBoroughId(final String votingBoroughId) {
		this.votingBoroughId = votingBoroughId;
	}

	@Column(name = "voting_polling_district_id")
	public String getVotingPollingDistrictId() {
		return votingPollingDistrictId;
	}

	public void setVotingPollingDistrictId(final String votingPollingDistrictId) {
		this.votingPollingDistrictId = votingPollingDistrictId;
	}

	@Column(name = "voting_polling_place_id")
	public String getVotingPollingPlaceId() {
		return votingPollingPlaceId;
	}

	public void setVotingPollingPlaceId(final String votingPollingPlaceId) {
		this.votingPollingPlaceId = votingPollingPlaceId;
	}

	@Column(name = "voting_number")
	public Integer getVotingNumber() {
		return votingNumber;
	}

	public void setVotingNumber(final Integer votingNumber) {
		this.votingNumber = votingNumber;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "cast_timestamp", length = 29)
	public DateTime getCastTimestamp() {
		return castTimestamp;
	}

	public void setCastTimestamp(DateTime castTimestamp) {
		this.castTimestamp = castTimestamp;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "validation_timestamp", length = 29)
	public DateTime getValidationTimestamp() {
		return validationTimestamp;
	}

	public void setValidationTimestamp(DateTime validationTimestamp) {
		this.validationTimestamp = validationTimestamp;
	}

	@Column(name = "late_validation")
	public Boolean getLateValidation() {
		return lateValidation;
	}

	public void setLateValidation(final Boolean lateValidation) {
		this.lateValidation = lateValidation;
	}

	@Column(name = "approved")
	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(final Boolean approved) {
		this.approved = approved;
	}

	@Column(name = "voting_category_id", length = 4)
	public String getVotingCategoryId() {
		return votingCategoryId;
	}

	public void setVotingCategoryId(final String votingCategoryId) {
		this.votingCategoryId = votingCategoryId;
	}

	@Column(name = "early_voting")
	public Boolean getEarlyVoting() {
		return earlyVoting;
	}

	public void setEarlyVoting(final Boolean earlyVoting) {
		this.earlyVoting = earlyVoting;
	}

	@Column(name = "voting_category_name")
	public String getVotingCategoryName() {
		return votingCategoryName;
	}

	public void setVotingCategoryName(final String votingCategoryName) {
		this.votingCategoryName = votingCategoryName;
	}

	@Column(name = "election_event_name")
	public String getElectionEventName() {
		return electionEventName;
	}

	public void setElectionEventName(final String electionEventName) {
		this.electionEventName = electionEventName;
	}

	@Column(name = "election_group_name")
	public String getElectionGroupName() {
		return electionGroupName;
	}

	public void setElectionGroupName(final String electionGroupName) {
		this.electionGroupName = electionGroupName;
	}

	@Column(name = "election_name")
	public String getElectionName() {
		return electionName;
	}

	public void setElectionName(final String electionName) {
		this.electionName = electionName;
	}

	@Column(name = "contest_name")
	public String getContestName() {
		return contestName;
	}

	public void setContestName(final String contestName) {
		this.contestName = contestName;
	}

	@Column(name = "country_name")
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(final String countryName) {
		this.countryName = countryName;
	}

	@Column(name = "county_name")
	public String getCountyName() {
		return countyName;
	}

	public void setCountyName(final String countyName) {
		this.countyName = countyName;
	}

	@Column(name = "municipality_name")
	public String getMunicipalityName() {
		return municipalityName;
	}

	public void setMunicipalityName(final String municipalityName) {
		this.municipalityName = municipalityName;
	}

	@Column(name = "borough_name")
	public String getBoroughName() {
		return boroughName;
	}

	public void setBoroughName(final String boroughName) {
		this.boroughName = boroughName;
	}

	@Column(name = "polling_district_name")
	public String getPollingDistrictName() {
		return pollingDistrictName;
	}

	public void setPollingDistrictName(final String pollingDistrictName) {
		this.pollingDistrictName = pollingDistrictName;
	}

	@Column(name = "voting_country_name")
	public String getVotingCountryName() {
		return votingCountryName;
	}

	public void setVotingCountryName(final String votingCountryName) {
		this.votingCountryName = votingCountryName;
	}

	@Column(name = "voting_county_name")
	public String getVotingCountyName() {
		return votingCountyName;
	}

	public void setVotingCountyName(final String votingCountyName) {
		this.votingCountyName = votingCountyName;
	}

	@Column(name = "voting_municipality_name")
	public String getVotingMunicipalityName() {
		return votingMunicipalityName;
	}

	public void setVotingMunicipalityName(final String votingMunicipalityName) {
		this.votingMunicipalityName = votingMunicipalityName;
	}

	@Column(name = "voting_borough_name")
	public String getVotingBoroughName() {
		return votingBoroughName;
	}

	public void setVotingBoroughName(final String votingBoroughName) {
		this.votingBoroughName = votingBoroughName;
	}

	@Column(name = "voting_polling_district_name")
	public String getVotingPollingDistrictName() {
		return votingPollingDistrictName;
	}

	public void setVotingPollingDistrictName(final String votingPollingDistrictName) {
		this.votingPollingDistrictName = votingPollingDistrictName;
	}

	@Column(name = "voting_polling_place_name")
	public String getVotingPollingPlaceName() {
		return votingPollingPlaceName;
	}

	public void setVotingPollingPlaceName(final String votingPollingPlaceName) {
		this.votingPollingPlaceName = votingPollingPlaceName;
	}

	@Column(name = "voting_rejection_pk")
	public Long getVotingRejectionPk() {
		return votingRejectionPk;
	}

	public void setVotingRejectionPk(final Long votingRejectionPk) {
		this.votingRejectionPk = votingRejectionPk;
	}
}
