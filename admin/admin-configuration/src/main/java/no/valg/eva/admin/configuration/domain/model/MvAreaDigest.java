package no.valg.eva.admin.configuration.domain.model;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

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
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.model.BaseEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

/**
 * Inneholder basis info for et område.
 */
@Entity
@Immutable
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.MvAreaDigest")
@Table(name = "mv_area")
@AttributeOverride(name = "pk", column = @Column(name = "mv_area_pk"))
@NamedQueries({
		@NamedQuery(
				name = "MvAreaDigest.findSingleByPath",
				query = "SELECT mva "
						+ "FROM MvAreaDigest mva "
						+ "WHERE mva.areaPath = :path",
				hints = { @QueryHint(name = "org.hibernate.cacheable", value = "true") }) })
@NamedNativeQueries(value = {
		@NamedNativeQuery(
				name = "MvAreaDigest.findDigestsByPathAndLevel",
				query = "SELECT * "
						+ "FROM mv_area mva "
						+ "WHERE text2ltree(mva.area_path) <@ text2ltree(?1) AND mva.area_level = ?2 "
						+ "ORDER BY mva.area_path",
				resultClass = MvAreaDigest.class),
		@NamedNativeQuery(
				name = "MvAreaDigest.findFirstDigestByPathAndLevel",
				query = "SELECT * "
						+ "FROM mv_area mva "
						+ "WHERE text2ltree(mva.area_path) <@ text2ltree(?1) AND mva.area_level = ?2 "
						+ "ORDER BY mva.area_path LIMIT 1",
				resultClass = MvAreaDigest.class) })
public class MvAreaDigest extends BaseEntity {

	private String areaPath;
	private int areaLevel;
	private String electionEventName;
	private String countryName;
	private String countyName;
	private String municipalityName;
	private String boroughName;
	private String pollingDistrictName;
	private String pollingPlaceName;
	private String pollingStationFirst;
	private String pollingStationLast;
	private MunicipalityDigest municipalityDigest;
	private BoroughDigest boroughDigest;
	private PollingDistrictDigest pollingDistrictDigest;
	private PollingPlaceDigest pollingPlaceDigest;

	public MvAreaDigest() {
		// For hibernate
	}

	public MvAreaDigest(String areaPath) {
		this.areaPath = areaPath;
	}
	
	@Column(name = "area_path", nullable = false, length = 37)
	@StringNotNullEmptyOrBlanks
	@Size(max = 37)
	public String getAreaPath() {
		return this.areaPath;
	}

	private void setAreaPath(String areaPath) {
		this.areaPath = areaPath;
	}

	@Column(name = "area_level", nullable = false)
	public int getAreaLevel() {
		return this.areaLevel;
	}

	private void setAreaLevel(int areaLevel) {
		this.areaLevel = areaLevel;
	}

	@Column(name = "election_event_name", nullable = false, length = 100)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getElectionEventName() {
		return this.electionEventName;
	}

	private void setElectionEventName(String electionEventName) {
		this.electionEventName = electionEventName;
	}

	@Column(name = "country_name", length = 50)
	@Size(max = 50)
	public String getCountryName() {
		return this.countryName;
	}

	private void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	@Column(name = "county_name", length = 50)
	@Size(max = 50)
	public String getCountyName() {
		return this.countyName;
	}

	private void setCountyName(String countyName) {
		this.countyName = countyName;
	}

	@Column(name = "municipality_name", length = 50)
	@Size(max = 50)
	public String getMunicipalityName() {
		return this.municipalityName;
	}

	private void setMunicipalityName(String municipalityName) {
		this.municipalityName = municipalityName;
	}

	@Column(name = "borough_name", length = 50)
	@Size(max = 50)
	public String getBoroughName() {
		return this.boroughName;
	}

	private void setBoroughName(String boroughName) {
		this.boroughName = boroughName;
	}

	@Column(name = "polling_district_name", length = 50)
	@Size(max = 50)
	public String getPollingDistrictName() {
		return this.pollingDistrictName;
	}

	private void setPollingDistrictName(String pollingDistrictName) {
		this.pollingDistrictName = pollingDistrictName;
	}

	@Column(name = "polling_place_name", length = 50)
	@Size(max = 50)
	public String getPollingPlaceName() {
		return this.pollingPlaceName;
	}

	private void setPollingPlaceName(String pollingPlaceName) {
		this.pollingPlaceName = pollingPlaceName;
	}

	@Column(name = "polling_station_first", length = 2)
	@Size(max = 2)
	public String getPollingStationFirst() {
		return pollingStationFirst;
	}

	private void setPollingStationFirst(String pollingStationFirst) {
		this.pollingStationFirst = pollingStationFirst;
	}

	@Column(name = "polling_station_last", length = 2)
	@Size(max = 2)
	public String getPollingStationLast() {
		return pollingStationLast;
	}

	private void setPollingStationLast(String pollingStationLast) {
		this.pollingStationLast = pollingStationLast;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "municipality_pk")
	public MunicipalityDigest getMunicipalityDigest() {
		return this.municipalityDigest;
	}
	
	public void setMunicipalityDigest(MunicipalityDigest municipalityDigest) {
		this.municipalityDigest = municipalityDigest;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "borough_pk")
	public BoroughDigest getBoroughDigest() {
		return this.boroughDigest;
	}

	public void setBoroughDigest(BoroughDigest boroughDigest) {
		this.boroughDigest = boroughDigest;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "polling_district_pk")
	public PollingDistrictDigest getPollingDistrictDigest() {
		return this.pollingDistrictDigest;
	}

	public void setPollingDistrictDigest(PollingDistrictDigest pollingDistrictDigest) {
		this.pollingDistrictDigest = pollingDistrictDigest;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "polling_place_pk")
	public PollingPlaceDigest getPollingPlaceDigest() {
		return this.pollingPlaceDigest;
	}

	public void setPollingPlaceDigest(PollingPlaceDigest pollingPlaceDigest) {
		this.pollingPlaceDigest = pollingPlaceDigest;
	}

	public AreaPath areaPath() {
		return AreaPath.from(getAreaPath());
	}

	public String areaName() {
		switch (AreaLevelEnum.getLevel(areaLevel)) {
		case ROOT:
			return electionEventName;
		case COUNTRY:
			return countryName;
		case COUNTY:
			return countyName;
		case MUNICIPALITY:
			return municipalityName;
		case BOROUGH:
			return boroughName;
		case POLLING_DISTRICT:
			return pollingDistrictName;
		case POLLING_PLACE:
			return pollingPlaceName;
		default:
			return pollingStationFirst + "-" + pollingStationLast;
		}
	}

	public ValggeografiNivaa valggeografiNivaa() {
		return ValggeografiNivaa.fra(areaLevel);
	}

	public ValggeografiSti valggeografiSti() {
		return ValggeografiSti.fra(areaPath());
	}
}
