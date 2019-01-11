package no.valg.eva.admin.common;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.evote.constants.AreaLevelEnum;

/**
 * Value object for area path. EEVENT.CO.CT.MUNI.... where EEVENT is election event, CO country, CT county and MUNI municipality.
 */
public class AreaPath implements Serializable, Comparable<AreaPath> {
	public static final String MUNICIPALITY_POLLING_DISTRICT_ID = "0000";
	public static final String CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID = "9999";

	public static final String COUNTRY_ID = "47";
	public static final String OSLO_COUNTY_ID = "03";
	public static final String OSLO_MUNICIPALITY_ID = "0301";

	private static final String AREA_PATH_REGEX = "^(\\d{6})(\\.(\\d{2})(\\.(\\d{2})(\\.(\\d{4})(\\.(\\d{6})(\\.(\\d{4})(\\.(\\d{4})(\\.(\\d{2}))?)?)?)?)?)?)?$";
	// regex groups
	private static final int GROUP_NO_ELECTION_EVENT = 1;
	private static final int GROUP_NO_COUNTRY = 3;
	private static final int GROUP_NO_COUNTY = 5;
	private static final int GROUP_NO_MUNICIPALITY = 7;
	private static final int GROUP_NO_BOROUGH = 9;
	private static final int GROUP_NO_POLLING_DISTRICT = 11;
	private static final int GROUP_NO_POLLING_PLACE = 13;
	private static final int GROUP_NO_POLLING_STATION = 15;
	private static final Pattern AREA_PATH_PATTERN = Pattern.compile(AREA_PATH_REGEX);
	private static final String SAMI_COUNTY_ID = "00";
	private final String path;
	private final AreaLevelEnum level;
	private final String electionEventId;
	private final String countryId;
	private final String countyId;
	private final String municipalityId;
	private final String boroughId;
	private final String pollingDistrictId;
	private final String pollingPlaceId;
	private final String pollingStationId;

	public AreaPath(final String path) {
		this.path = path;

		Matcher pathMatcher = AREA_PATH_PATTERN.matcher(path);
		if (!pathMatcher.matches()) {
			throw new IllegalArgumentException(format("illegal path <%s>, must match <%s>", path, AREA_PATH_PATTERN));
		}

		electionEventId = pathMatcher.group(GROUP_NO_ELECTION_EVENT);
		AreaLevelEnum areaLevelEnum = AreaLevelEnum.ROOT;
		countryId = pathMatcher.group(GROUP_NO_COUNTRY);
		if (countryId != null) {
			areaLevelEnum = AreaLevelEnum.COUNTRY;
		}
		countyId = pathMatcher.group(GROUP_NO_COUNTY);
		if (countyId != null) {
			areaLevelEnum = AreaLevelEnum.COUNTY;
		}
		municipalityId = pathMatcher.group(GROUP_NO_MUNICIPALITY);
		if (municipalityId != null) {
			areaLevelEnum = MUNICIPALITY;
		}
		boroughId = pathMatcher.group(GROUP_NO_BOROUGH);
		if (boroughId != null) {
			areaLevelEnum = AreaLevelEnum.BOROUGH;
		}
		pollingDistrictId = pathMatcher.group(GROUP_NO_POLLING_DISTRICT);
		if (pollingDistrictId != null) {
			areaLevelEnum = AreaLevelEnum.POLLING_DISTRICT;
		}
		pollingPlaceId = pathMatcher.group(GROUP_NO_POLLING_PLACE);
		if (pollingPlaceId != null) {
			areaLevelEnum = AreaLevelEnum.POLLING_PLACE;
		}
		pollingStationId = pathMatcher.group(GROUP_NO_POLLING_STATION);
		if (pollingStationId != null) {
			areaLevelEnum = AreaLevelEnum.POLLING_STATION;
		}
		this.level = areaLevelEnum;
	}

	public static AreaPath from(String areaPath) {
		return new AreaPath(areaPath);
	}

	public static AreaPath from(String electionEventId, String countryId) {
		return AreaPath.from(electionEventId + "." + countryId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId, String municipalityId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId + "." + municipalityId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId, String municipalityId, String boroughId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId + "." + municipalityId + "." + boroughId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId, String municipalityId, String boroughId, String pollingDistrictId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId + "." + municipalityId + "." + boroughId + "." + pollingDistrictId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId, String municipalityId, String boroughId, String pollingDistrictId,
								String pollingPlaceId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId + "." + municipalityId + "." + boroughId + "." + pollingDistrictId
				+ "." + pollingPlaceId);
	}

	public static AreaPath from(String electionEventId, String countryId, String countyId, String municipalityId, String boroughId, String pollingDistrictId,
								String pollingPlaceId, String pollingStationId) {
		return AreaPath.from(electionEventId + "." + countryId + "." + countyId + "." + municipalityId + "." + boroughId + "." + pollingDistrictId
				+ "." + pollingPlaceId + "." + pollingStationId);
	}

	public String path() {
		return path;
	}

	public AreaLevelEnum getLevel() {
		return level;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountyId() {
		return countyId;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public String getBoroughId() {
		return boroughId;
	}

	public String getPollingDistrictId() {
		return pollingDistrictId;
	}

	public String getPollingPlaceId() {
		return pollingPlaceId;
	}

	public String getPollingStationId() {
		return pollingStationId;
	}

	/**
	 * @return true if this contains subPath, that is this is a parent of subPath
	 */
	public boolean contains(final AreaPath subPath) {
		return subPath.path.startsWith(path);
	}

	/**
	 * @return true if this is part of parentPath
	 */
	public boolean isSubpathOf(final AreaPath parentPath) {
		return path.startsWith(parentPath.path);
	}

	/**
	 * @return county path of this, illegalState if not municipality or below
	 */
	public AreaPath toCountyPath() {
		if (countyId == null) {
			throw new IllegalStateException(format("path to county can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId));
	}

	/**
	 * @return municipality path of this, illegalState if not municipality or below
	 */
	public AreaPath toMunicipalityPath() {
		if (municipalityId == null) {
			throw new IllegalStateException(format("path to municipality can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId, municipalityId));
	}

	public AreaPath toBoroughPath() {
		if (getBoroughId() == null) {
			throw new IllegalStateException(format("path to borough can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, getBoroughId()));
	}

	public AreaPath toBoroughSubPath(String boroughId) {
		if (municipalityId == null) {
			throw new IllegalStateException(format("path to municipality cannot be found for <%s>", path));
		}
		if (boroughId == null) {
			throw new IllegalArgumentException("borough id cannot be null");
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, boroughId));
	}

	public AreaPath toPollingDistrictPath() {
		if (getPollingDistrictId() == null) {
			throw new IllegalStateException(format("path to polling district can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, boroughId, getPollingDistrictId()));
	}

	public AreaPath toPollingDistrictSubPath(String pollingDistrictId) {
		if (boroughId == null) {
			throw new IllegalStateException(format("path to borough cannot be found for <%s>", path));
		}
		if (pollingDistrictId == null) {
			throw new IllegalArgumentException("polling district id cannot be null");
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, boroughId, pollingDistrictId));
	}

	public AreaPath toMunicipalityPollingDistrictPath() {
		String municipalityBoroughId = getMunicipalityBoroughId();
		return toBoroughSubPath(municipalityBoroughId).toPollingDistrictSubPath(MUNICIPALITY_POLLING_DISTRICT_ID);
	}

	public void validateAreaPath(AreaLevelEnum first, AreaLevelEnum... rest) {
		EnumSet<AreaLevelEnum> validLevels = EnumSet.of(first, rest);
		AreaLevelEnum areaLevelEnum = getLevel();
		if (!validLevels.contains(areaLevelEnum)) {
			throw new IllegalArgumentException(format(
					"expected area path to be one of <%s>, but was <%s>",
					validLevels,
					areaLevelEnum));
		}
	}

	private String getMunicipalityBoroughId() {
		return municipalityId + "00";
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AreaPath)) {
			return false;
		}
		AreaPath areaPath = (AreaPath) o;
		return path.equals(areaPath.path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		return path;
	}

	public AreaPath add(String id) {
		return AreaPath.from(path() + "." + id);
	}

	AreaPath getParentPath() {
		return new AreaPath(path.replaceAll("\\.[0-9]+$", ""));
	}

	public boolean isMunicipalityPollingDistrict() {
		return MUNICIPALITY_POLLING_DISTRICT_ID.equals(pollingDistrictId);
	}

	public boolean isMunicipalityBorough() {
		return getMunicipalityBoroughId().equals(boroughId);
	}

	public boolean isCentralEnvelopeRegistrationPollingPlace() {
		return CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID.equals(pollingPlaceId);
	}

	boolean isAdvanceVotingPollingPlace() {
		return level == AreaLevelEnum.POLLING_PLACE && isMunicipalityPollingDistrict();
	}

	public boolean isRootLevel() {
		return getLevel() == AreaLevelEnum.ROOT;
	}

	public boolean isCountryLevel() {
		return getLevel() == AreaLevelEnum.COUNTRY;
	}

	public boolean isCountyLevel() {
		return getLevel() == AreaLevelEnum.COUNTY;
	}

	public boolean isMunicipalityLevel() {
		return getLevel() == MUNICIPALITY;
	}

	public boolean isBoroughLevel() {
		return getLevel() == AreaLevelEnum.BOROUGH;
	}

	public boolean isPollingDistrictLevel() {
		return getLevel() == AreaLevelEnum.POLLING_DISTRICT;
	}

	public boolean isPollingPlaceLevel() {
		return getLevel() == AreaLevelEnum.POLLING_PLACE;
	}

	boolean isPollingStationLevel() {
		return getLevel() == AreaLevelEnum.POLLING_STATION;
	}

	public void assertLevel(AreaLevelEnum areaLevelEnum) {
		if (getLevel() != areaLevelEnum) {
			String levelDescription = areaLevelEnum.getLevelDescription();
			throw new IllegalArgumentException(format("illegal %s path: %s", levelDescription, this));
		}
	}

	public AreaPath toAreaLevelPath(AreaLevelEnum areaLevelEnum) {
		switch (areaLevelEnum) {
		case ROOT:
			return toRootPath();
		case COUNTRY:
			return toCountryPath();
		case COUNTY:
			return toCountyPath();
		case MUNICIPALITY:
			return toMunicipalityPath();
		case BOROUGH:
			return toBoroughPath();
		case POLLING_DISTRICT:
			return toPollingDistrictPath();
		case POLLING_PLACE:
			return toPollingPlacePath();
		case POLLING_STATION:
			return toPollingStationPath();
		default:
			throw new IllegalArgumentException(format("unsupported area level: <%s>", areaLevelEnum));
		}
	}

	public AreaPath toPollingPlacePath() {
		if (getPollingPlaceId() == null) {
			throw new IllegalStateException(format("path to polling place can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, boroughId, pollingDistrictId, getPollingPlaceId()));
	}

	private AreaPath toPollingStationPath() {
		if (getPollingStationId() == null) {
			throw new IllegalStateException(format("path to polling station can not be found for <%s>", path));
		}
		return from(path(electionEventId, countryId, countyId, municipalityId, boroughId, pollingDistrictId, getPollingPlaceId(), getPollingStationId()));
	}

	public AreaPath toCountryPath() {
		if (getCountryId() == null) {
			throw new IllegalStateException(format("path to country can not be found for <%s>", path));
		}
		return from(path(electionEventId, getCountryId()));
	}

	public AreaPath toRootPath() {
		return from(electionEventId);
	}

	public boolean isTechnicalPollingDistrict() {
		if (!isPollingDistrictLevel()) {
			return false;
		}
		if (!isMunicipalityBorough()) {
			return false;
		}
		return !isMunicipalityPollingDistrict();
	}

	private String path(String... pathElements) {
		return join(pathElements, '.');
	}

	/**
	 * Returns the last path element
	 */
	public String getLeafId() {
		return path.substring(path.lastIndexOf('.') + 1);
	}

	@Override
	public int compareTo(AreaPath areaPath) {
		if (areaPath == null) {
			return 1;
		}
		return path.compareTo(areaPath.path);
	}

	public boolean isSamiValgkretsPath() {
		return SAMI_COUNTY_ID.equals(getCountyId());
	}

	public void assertMunicipalityLevel() {
		assertLevel(MUNICIPALITY);
	}
}
