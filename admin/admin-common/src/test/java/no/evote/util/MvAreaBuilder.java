package no.evote.util;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public class MvAreaBuilder {
	private MvArea value;

	public MvAreaBuilder(AreaPath path) {
		value = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(value.getActualAreaLevel()).thenReturn(path.getLevel());
		when(value.getAreaLevel()).thenReturn(path.getLevel().getLevel());
		when(value.getAreaId()).thenReturn(path.getLeafId());
		when(value.getAreaLevelString()).thenReturn("@area_level[" + path.getLevel().getLevel() + "].name");
		when(value.getAreaPath()).thenReturn(path.path());
		when(value.areaPath()).thenReturn(path);
		when(value.getAreaName()).thenReturn(path.getLevel().name());
		when(value.getPath()).thenReturn(path.path());
		when(value.isRoot()).thenReturn(path.isRootLevel());
		when(value.isCountyLevel()).thenReturn(path.isCountyLevel());
		when(value.isMunicipalityLevel()).thenReturn(path.isMunicipalityLevel());
		when(value.isBoroughLevel()).thenReturn(path.isBoroughLevel());
		when(value.isPollingDistrictLevel()).thenReturn(path.isPollingDistrictLevel());

		setupRoot(path);
		setupCountry(path);
		setupCounty(path);
		setupMunicipality(path);
		setupBorough(path);
		setupPollingDistrict(path);
		setupPollingPlace(path);
	}

	public MvAreaBuilder withLocale(String language) {
		Locale locale = mock(Locale.class, RETURNS_DEEP_STUBS);
		when(locale.getPk()).thenReturn(1L);
		when(locale.getId()).thenReturn(language);
		when(locale.getName()).thenReturn(language);
		AreaPath path = AreaPath.from(value.getAreaPath());
		if (include(path, AreaLevelEnum.COUNTY)) {
			when(value.getCounty().getLocale()).thenReturn(locale);
		}
		if (include(path, AreaLevelEnum.MUNICIPALITY)) {
			when(value.getMunicipality().getLocale()).thenReturn(locale);
		}
		return this;
	}

	private void setupRoot(AreaPath path) {
		if (include(path, AreaLevelEnum.ROOT)) {
			when(value.getElectionEventName()).thenReturn("ElectionEvent " + path.getElectionEventId());
			when(value.getElectionEventId()).thenReturn(path.getElectionEventId());
			if (path.isRootLevel()) {
				when(value.getAreaName()).thenReturn("ElectionEvent " + path.getElectionEventId());
				when(value.getAreaPk(AreaLevelEnum.ROOT)).thenReturn(Long.parseLong(path.getElectionEventId()));
			}
		} else {
			when(value.getElectionEventName()).thenReturn(null);
			when(value.getElectionEventId()).thenReturn(null);
		}
	}

	private void setupCountry(AreaPath path) {
		if (include(path, AreaLevelEnum.COUNTRY)) {
			when(value.getCountry().getPk()).thenReturn(Long.parseLong(path.getCountryId()));
			when(value.getCountry().getId()).thenReturn(path.getCountryId());
			when(value.getCountry().getName()).thenReturn("Country " + path.getCountryId());
			when(value.getCountryId()).thenReturn(path.getCountryId());
			when(value.getCountryName()).thenReturn("Country " + path.getCountryId());
			if (path.isCountryLevel()) {
				when(value.getAreaName()).thenReturn("Country " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.COUNTRY)).thenReturn(Long.parseLong(path.getCountryId()));
			}
		} else {
			when(value.getCountry()).thenReturn(null);
			when(value.getCountryId()).thenReturn(null);
			when(value.getCountryName()).thenReturn(null);
		}
	}

	private void setupCounty(AreaPath path) {
		if (include(path, AreaLevelEnum.COUNTY)) {
			when(value.getCounty().getPk()).thenReturn(Long.parseLong(path.getCountyId()));
			when(value.getCounty().getId()).thenReturn(path.getCountyId());
			when(value.getCounty().getName()).thenReturn("County " + path.getCountyId());
			when(value.getCountyId()).thenReturn(path.getCountyId());
			when(value.getCountyName()).thenReturn("County " + path.getCountyId());
			if (path.isCountyLevel()) {
				when(value.getAreaName()).thenReturn("County " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.COUNTY)).thenReturn(Long.parseLong(path.getCountyId()));
			}
		} else {
			when(value.getCounty()).thenReturn(null);
			when(value.getCountyId()).thenReturn(null);
			when(value.getCountyName()).thenReturn(null);
		}
	}

	private void setupMunicipality(AreaPath path) {
		if (include(path, AreaLevelEnum.MUNICIPALITY)) {
			when(value.getMunicipality().getPk()).thenReturn(Long.parseLong(path.getMunicipalityId()));
			when(value.getMunicipality().getId()).thenReturn(path.getMunicipalityId());
			when(value.getMunicipality().getName()).thenReturn("Municipality " + path.getMunicipalityId());
			when(value.getMunicipalityId()).thenReturn(path.getMunicipalityId());
			when(value.getMunicipalityName()).thenReturn("Municipality " + path.getMunicipalityId());
			if (path.isMunicipalityLevel()) {
				when(value.getAreaName()).thenReturn("Municipality " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.MUNICIPALITY)).thenReturn(Long.parseLong(path.getMunicipalityId()));
			}
		} else {
			when(value.getMunicipality()).thenReturn(null);
			when(value.getMunicipalityId()).thenReturn(null);
			when(value.getMunicipalityName()).thenReturn(null);
		}
	}

	private void setupBorough(AreaPath path) {
		if (include(path, AreaLevelEnum.BOROUGH)) {
			when(value.getBorough().getPk()).thenReturn(Long.parseLong(path.getBoroughId()));
			when(value.getBorough().getId()).thenReturn(path.getBoroughId());
			when(value.getBorough().getName()).thenReturn("Borough " + path.getBoroughId());
			when(value.getBoroughId()).thenReturn(path.getBoroughId());
			when(value.getBoroughName()).thenReturn("Borough " + path.getBoroughId());
			if (path.isBoroughLevel()) {
				when(value.getAreaName()).thenReturn("Borough " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.BOROUGH)).thenReturn(Long.parseLong(path.getBoroughId()));
				when(value.getBorough().isMunicipality1()).thenReturn(path.getBoroughId().endsWith("00"));
				when(value.isBoroughLevel()).thenReturn(true);
			}
		} else {
			when(value.getBorough()).thenReturn(null);
			when(value.getBoroughId()).thenReturn(null);
			when(value.getBoroughName()).thenReturn(null);
		}
	}

	private void setupPollingDistrict(AreaPath path) {
		if (include(path, AreaLevelEnum.POLLING_DISTRICT)) {
			when(value.getPollingDistrict().getPk()).thenReturn(Long.parseLong(path.getPollingDistrictId()));
			when(value.getPollingDistrict().getId()).thenReturn(path.getPollingDistrictId());
			when(value.getPollingDistrict().getName()).thenReturn("PollingDistrict " + path.getPollingDistrictId());
			when(value.getPollingDistrictId()).thenReturn(path.getPollingDistrictId());
			when(value.getPollingDistrictName()).thenReturn("PollingDistrict " + path.getPollingDistrictId());
			if (path.isPollingDistrictLevel()) {
				when(value.getAreaName()).thenReturn("PollingDistrict " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.POLLING_DISTRICT)).thenReturn(Long.parseLong(path.getPollingDistrictId()));
				when(value.getPollingDistrict().isMunicipality()).thenReturn(path.getPollingDistrictId().equals("0000"));
				when(value.isPollingDistrictLevel()).thenReturn(true);
			}
		} else {
			when(value.getPollingDistrict()).thenReturn(null);
			when(value.getPollingDistrictId()).thenReturn(null);
			when(value.getPollingDistrictName()).thenReturn(null);
		}
	}

	private void setupPollingPlace(AreaPath path) {
		if (include(path, AreaLevelEnum.POLLING_PLACE)) {
			when(value.getPollingPlace().getPk()).thenReturn(Long.parseLong(path.getPollingPlaceId()));
			when(value.getPollingPlace().getId()).thenReturn(path.getPollingPlaceId());
			when(value.getPollingPlace().getName()).thenReturn("PollingPlace " + path.getPollingPlaceId());
			when(value.getPollingPlaceId()).thenReturn(path.getPollingPlaceId());
			when(value.getPollingPlaceName()).thenReturn("PollingPlace " + path.getPollingPlaceId());
			if (path.isPollingPlaceLevel()) {
				when(value.getAreaName()).thenReturn("PollingPlace " + path.getCountryId());
				when(value.getAreaPk(AreaLevelEnum.POLLING_PLACE)).thenReturn(Long.parseLong(path.getPollingPlaceId()));
				when(value.isPollingPlaceLevel()).thenReturn(true);
			}
		} else {
			when(value.getPollingPlace()).thenReturn(null);
			when(value.getPollingPlaceId()).thenReturn(null);
			when(value.getPollingPlaceName()).thenReturn(null);
		}
	}

	private boolean include(AreaPath path, AreaLevelEnum level) {
		return !level.lowerThan(path.getLevel());
	}

	public MvArea getValue() {
		return value;
	}
}
