package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import no.evote.constants.SQLConstants;

@Entity
@Table(name = "contest_rel_area")
@NamedNativeQueries({

@NamedNativeQuery(name = "ContestRelArea.findAllAllowed", query = "select ca.* from contest_rel_area ca "
		+ "join mv_election e on (text2ltree(e.election_path) @> text2ltree(ca.election_path)) "
		+ "where ca.area_level <= 5 and e.mv_election_pk = ?1 and ca.mv_area_pk = ?2", resultClass = ContestRelArea.class) })
@NamedQueries({ @NamedQuery(name = "ContestRelArea.findUnique", query = "SELECT cra FROM ContestRelArea cra WHERE cra.id.mvElectionPk = :mvElectionPk "
		+ "AND cra.id.mvAreaPk = :mvAreaPk") })
public class ContestRelArea implements java.io.Serializable {

	private ContestRelAreaId id;
	private Long electionEventPk;
	private Long electionPk;
	private Long contestPk;
	private Long electionGroupPk;
	private String electionPath;
	private String electionGroupName;
	private String electionName;
	private String contestName;
	private Integer contestAreaLevel;
	private String areaPath;
	private Integer areaLevel;
	private String countryName;
	private String countyName;
	private String municipalityName;
	private String boroughName;
	private String pollingDistrictName;

	private String areaNumber;
	private Integer reportingUnitPk;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "mvElectionPk", column = @Column(name = "mv_election_pk", nullable = false)),
			@AttributeOverride(name = "mvAreaPk", column = @Column(name = "mv_area_pk", nullable = false)) })
	public ContestRelAreaId getId() {
		return this.id;
	}

	public void setId(final ContestRelAreaId id) {
		this.id = id;
	}

	@Column(name = SQLConstants.ELECTION_EVENT_PK)
	public Long getElectionEventPk() {
		return this.electionEventPk;
	}

	public void setElectionEventPk(final Long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	@Column(name = "election_pk")
	public Long getElectionPk() {
		return this.electionPk;
	}

	public void setElectionPk(final Long electionPk) {
		this.electionPk = electionPk;
	}

	@Column(name = "contest_pk")
	public Long getContestPk() {
		return this.contestPk;
	}

	public void setContestPk(final Long contestPk) {
		this.contestPk = contestPk;
	}

	@Column(name = "election_group_pk")
	public Long getElectionGroupPk() {
		return this.electionGroupPk;
	}

	public void setElectionGroupPk(final Long electionGroupPk) {
		this.electionGroupPk = electionGroupPk;
	}

	@Column(name = "election_path")
	public String getElectionPath() {
		return this.electionPath;
	}

	public void setElectionPath(final String electionPath) {
		this.electionPath = electionPath;
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

	@Column(name = "contest_area_level")
	public Integer getContestAreaLevel() {
		return this.contestAreaLevel;
	}

	public void setContestAreaLevel(final Integer contestAreaLevel) {
		this.contestAreaLevel = contestAreaLevel;
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

	@Transient
	public String getAreaNumber() {
		return areaNumber;
	}

	public void setAreaNumber(final String areaNumber) {
		this.areaNumber = areaNumber;
	}

	@Transient
	public Integer getReportingUnitPk() {
		return reportingUnitPk;
	}

	public void setReportingUnitPk(final Integer reportingUnitPk) {
		this.reportingUnitPk = reportingUnitPk;
	}

}
