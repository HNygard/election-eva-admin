package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public abstract class LocalConfigApplicationServiceTest extends MockUtilsTestCase {

	static final AreaPath ROOT = AreaPath.from("111111");
	static final AreaPath COUNTY = AreaPath.from("111111.22.33");
	protected static final AreaPath MUNICIPALITY = AreaPath.from(COUNTY.path() + ".4444");
	static final AreaPath MUNICIPALITY_BOROUGH = AreaPath.from(MUNICIPALITY.path() + "." + MUNICIPALITY.getLeafId() + "00");
	static final AreaPath MUNICIPALITY_POLLING_DISTRICT = AreaPath.from(MUNICIPALITY_BOROUGH.path() + ".0000");
	private static final AreaPath POLLING_DISTRICT = AreaPath.from(MUNICIPALITY_BOROUGH.path() + ".0001");
	static final AreaPath POLLING_PLACE = AreaPath.from(POLLING_DISTRICT.path() + ".0001");

    protected UserData userData() {
        return createMock(UserData.class);
    }

    public Municipality stub_municipalityByElectionEventAndId(Municipality municipality) {
        when(getInjectMock(MunicipalityRepository.class).municipalityByElectionEventAndId(anyLong(), any())).thenReturn(municipality);
        return municipality;
    }

    Municipality stub_municipalityRepository_findByPk(Municipality municipality) {
        when(getInjectMock(MunicipalityRepository.class).findByPk(any())).thenReturn(municipality);
        return municipality;
    }

    protected no.valg.eva.admin.configuration.domain.model.PollingDistrict stub_pollingDistrictRepository_findByPk(
            no.valg.eva.admin.configuration.domain.model.PollingDistrict pollingDistrict) {
        when(getInjectMock(PollingDistrictRepository.class).findByPk(anyLong())).thenReturn(pollingDistrict);
        return pollingDistrict;
    }

	protected no.valg.eva.admin.configuration.domain.model.PollingPlace stub_pollingPlaceServiceBean_findByPk(
			no.valg.eva.admin.configuration.domain.model.PollingPlace place) {
		when(getInjectMock(PollingPlaceRepository.class).findByPk(anyLong())).thenReturn(place);
		return place;
	}

    ReportingUnit reportingUnitEntity(AreaPath areaPath) {
        ReportingUnit result = createMock(ReportingUnit.class);
        MvArea mvArea = new MvAreaBuilder(areaPath).getValue();
        when(result.getMvArea()).thenReturn(mvArea);
        return result;
    }

    no.valg.eva.admin.common.configuration.model.local.ReportingUnit reportingUnit(AreaPath areaPath) {
        no.valg.eva.admin.common.configuration.model.local.ReportingUnit result = createMock(
                no.valg.eva.admin.common.configuration.model.local.ReportingUnit.class);
        when(result.getAreaPath()).thenReturn(areaPath);
        return result;
    }

    public MunicipalityBuilder municipality(String id) {
        return new MunicipalityBuilder(id);
    }

    no.valg.eva.admin.configuration.domain.model.PollingDistrict pollingDistrictEntity(String id) {
        no.valg.eva.admin.configuration.domain.model.PollingDistrict result = createMock(no.valg.eva.admin.configuration.domain.model.PollingDistrict.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn("PollingDistrict " + id);
        when(result.isMunicipality()).thenReturn("0000".equals(id));
        return result;
    }

    no.valg.eva.admin.configuration.domain.model.PollingPlace pollingPlaceEntity(String id) {
        no.valg.eva.admin.configuration.domain.model.PollingPlace result = createMock(no.valg.eva.admin.configuration.domain.model.PollingPlace.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn("PollingPlace " + id);
        return result;
    }

    ElectionDayPollingPlace electionDayPollingPlace(String id) {
        ElectionDayPollingPlace result = createMock(ElectionDayPollingPlace.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn("ElectionDayPollingPlace " + id);
        when(result.getInfoText()).thenReturn("InfoText " + id);
        return result;
    }

    public class MunicipalityBuilder {
        private Municipality value;

        public MunicipalityBuilder(String id) {
            this(AreaPath.from("111111.22.33." + id));
        }

        public MunicipalityBuilder(AreaPath areaPath) {
            value = createMock(Municipality.class);
            when(value.getId()).thenReturn(areaPath.getLeafId());
            when(value.getName()).thenReturn("Municipality " + areaPath.getLeafId());
            when(value.areaPath()).thenReturn(areaPath);
        }

        public MunicipalityBuilder withPollingDistricts(no.valg.eva.admin.configuration.domain.model.PollingDistrict... pollingDistricts) {
            List<no.valg.eva.admin.configuration.domain.model.PollingDistrict> list = new ArrayList<>();
            Collections.addAll(list, pollingDistricts);
            when(value.pollingDistricts()).thenReturn(list);
            return this;
        }

        public MunicipalityBuilder withAdvancePollingPlaces(String... ids) {
            PollingPlacesBuilder builder = new PollingPlacesBuilder(ids);
            when(value.pollingPlacesAdvance()).thenReturn(builder.getValues());
            for (PollingPlace place : builder.getValues()) {
                when(value.pollingPlacesAdvanceById(place.getId())).thenReturn(place);
            }
            return this;
        }

        public MunicipalityBuilder withOrdinaryPollingDistricts(PollingDistrictType type, String... districtIds) {
            PollingDistrictsBuilder builder = new PollingDistrictsBuilder(districtIds).withType(type).withPlaces();
            when(value.regularPollingDistricts(false, true)).thenReturn(builder.getValues());
            if (type == PollingDistrictType.REGULAR) {
                when(value.regularPollingDistricts(anyBoolean(), anyBoolean())).thenReturn(builder.getValues());
            }
            for (PollingDistrict district : builder.getValues()) {
                when(district.getPollingPlaces()).thenReturn(new HashSet<>());
                when(value.regularPollingDistrictById(district.getId(), false, true)).thenReturn(district);
            }
            return this;
        }

        public MunicipalityBuilder withTechnicalPollingDistricts(String... districtIds) {
            PollingDistrictsBuilder builder = new PollingDistrictsBuilder(districtIds).withType(PollingDistrictType.TECHNICAL);
            when(value.technicalPollingDistricts()).thenReturn(builder.getValues());
            return this;
        }

        public MunicipalityBuilder withBoroughs(String... boroughIds) {
            Set<Borough> list = new HashSet<>();
            for (String id : boroughIds) {
                list.add(new BoroughBuilder(id).getValue());
            }
            when(value.getBoroughs()).thenReturn(list);
            return this;
        }

        public MunicipalityBuilder withOpeningHours() {
            when(value.getOpeningHours()).thenReturn(new HashSet<>());
            return this;
        }

        public Municipality getValue() {
            return value;
        }
    }

    class BoroughBuilder {
        private Borough value;

        BoroughBuilder(String id) {
            this(AreaPath.from("111111.22.33.4444." + id));
        }

        BoroughBuilder(AreaPath areaPath) {
            value = createMock(Borough.class);
            when(value.getId()).thenReturn(areaPath.getLeafId());
            when(value.getName()).thenReturn("Borough " + areaPath.getLeafId());
            when(value.isMunicipality1()).thenReturn(areaPath.getLeafId().endsWith("00"));
            when(value.areaPath()).thenReturn(areaPath);
        }

        public Borough getValue() {
            return value;
        }
    }

    public class PollingPlacesBuilder {
        private List<no.valg.eva.admin.configuration.domain.model.PollingPlace> values = new ArrayList<>();

        public PollingPlacesBuilder(String... ids) {
            for (String id : ids) {
                values.add(pollingPlaceEntity(id));
            }
        }

        public PollingPlacesBuilder withElectionDayVoting(boolean value) {
            for (no.valg.eva.admin.configuration.domain.model.PollingPlace pp : values) {
                when(pp.isElectionDayVoting()).thenReturn(value);
            }
            return this;
        }

        public PollingPlacesBuilder withPollingDistrict(String id, PollingDistrictType type) {
            for (no.valg.eva.admin.configuration.domain.model.PollingPlace pp : values) {
                PollingDistrict pd = pollingDistrictEntity(id);
                when(pd.type()).thenReturn(type);
                when(pp.getPollingDistrict()).thenReturn(pd);
            }
            return this;
        }

        public List<no.valg.eva.admin.configuration.domain.model.PollingPlace> getValues() {
            return values;
        }
    }

    public class PollingDistrictsBuilder {
        private List<no.valg.eva.admin.configuration.domain.model.PollingDistrict> values = new ArrayList<>();

        public PollingDistrictsBuilder(String... ids) {
            for (String id : ids) {
                values.add(pollingDistrictEntity(id));
            }
        }

        public PollingDistrictsBuilder withType(PollingDistrictType type) {
            for (no.valg.eva.admin.configuration.domain.model.PollingDistrict pd : values) {
                when(pd.type()).thenReturn(type);
            }
            return this;
        }

        PollingDistrictsBuilder withPlaces() {
            for (no.valg.eva.admin.configuration.domain.model.PollingDistrict pd : values) {
                PollingPlacesBuilder placesBuilder = new PollingPlacesBuilder(pd.getId()).withElectionDayVoting(!"0000".equals(pd.getId()));
                when(pd.getPollingPlaces()).thenReturn(new HashSet<>(placesBuilder.getValues()));
            }
            return this;
        }

        public List<no.valg.eva.admin.configuration.domain.model.PollingDistrict> getValues() {
            return values;
        }
    }

}
