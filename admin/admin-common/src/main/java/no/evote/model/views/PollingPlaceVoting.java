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

@Entity
@Table(name = "polling_place_voting")
@NamedQueries({ @NamedQuery(name = "PollingPlaceVoting.findAdvancedPollingPlaceByMunicipality", query = "SELECT ppv from PollingPlaceVoting ppv WHERE"
		+ " ppv.electionEventPk = :electionEventPk AND ppv.municipalityId = :municipalityId AND ppv.id.earlyVoting = TRUE " + "ORDER BY ppv.pollingPlaceId") })
public class PollingPlaceVoting implements java.io.Serializable {

	private PollingPlaceVotingId id;
	private Long electionEventPk;
	private String areaPath;
	private Integer areaLevel;
	private Long countryPk;
	private Long countyPk;
	private Long municipalityPk;
	private Long boroughPk;
	private Long pollingDistrictPk;
	private String countryId;
	private String countyId;
	private String municipalityId;
	private String boroughId;
	private String pollingDistrictId;
	private String pollingPlaceId;
	private String countryName;
	private String countyName;
	private String municipalityName;
	private String boroughName;
	private String pollingDistrictName;
	private String pollingPlaceName;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "pollingPlacePk", column = @Column(name = "polling_place_pk", nullable = false)),
			@AttributeOverride(name = "earlyVoting", column = @Column(name = "early_voting", nullable = false)) })
	public PollingPlaceVotingId getId() {
		return this.id;
	}

	public void setId(final PollingPlaceVotingId id) {
		this.id = id;
	}

	@Column(name = SQLConstants.ELECTION_EVENT_PK)
	public Long getElectionEventPk() {
		return this.electionEventPk;
	}

	public void setElectionEventPk(final Long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	@Column(name = "area_path")
	public String getAreaPath() {
		return this.areaPath;
	}

	public void setAreaPath(final String areaPath) {
		this.areaPath = areaPath;
	}

	@Column(name = "area_level")
	public Integer getAreaLevel() {
		return this.areaLevel;
	}

	public void setAreaLevel(final Integer areaLevel) {
		this.areaLevel = areaLevel;
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

	@Column(name = "polling_district_pk")
	public Long getPollingDistrictPk() {
		return this.pollingDistrictPk;
	}

	public void setPollingDistrictPk(final Long pollingDistrictPk) {
		this.pollingDistrictPk = pollingDistrictPk;
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

	@Column(name = "polling_place_id", length = 4)
	public String getPollingPlaceId() {
		return this.pollingPlaceId;
	}

	public void setPollingPlaceId(final String pollingPlaceId) {
		this.pollingPlaceId = pollingPlaceId;
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

	@Column(name = "polling_place_name")
	public String getPollingPlaceName() {
		return this.pollingPlaceName;
	}

	public void setPollingPlaceName(final String pollingPlaceName) {
		this.pollingPlaceName = pollingPlaceName;
	}

}
