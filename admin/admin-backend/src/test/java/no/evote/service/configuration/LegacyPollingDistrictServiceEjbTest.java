package no.evote.service.configuration;

import no.evote.dto.ConfigurationDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyPollingDistrictServiceEjbTest extends MockUtilsTestCase {
	private static final long PD_MV_AREA_PK_1 = 111;
	private static final long PD_MV_AREA_PK_2 = 222;
	private static final long PD_MV_AREA_PK_3 = 333;
	private static final String POLLING_DISTRICT_ID_1 = "1111";
	private static final String POLLING_DISTRICT_ID_2 = "2222";
	private static final String POLLING_DISTRICT_ID_3 = "3333";
	private static final String POLLING_DISTRICT_NAME_2 = "pollingDistrict2";
	private static final String BOROUGH_ID = "111111";
	private static final String BOROUGH_NAME = "borough";
	private static final String CONFIGURATION_NAME_PD_2 = BOROUGH_NAME + ", " + POLLING_DISTRICT_NAME_2;
	private static final String CONFIGURATION_ID_PD_2 = BOROUGH_ID + "." + POLLING_DISTRICT_ID_2;
	private LegacyPollingDistrictServiceEjb pollingDistrictServiceEjb;
	private PollingDistrictRepository stubPollingDistrictRepository;
	private MvAreaRepository stubMvAreaRepository;
	private VoterRepository stubVoterRepository;
	@Mock
	private UserData mockUserData;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		pollingDistrictServiceEjb = initializeMocks(LegacyPollingDistrictServiceEjb.class);
		stubPollingDistrictRepository = getInjectMock(PollingDistrictRepository.class);
		stubMvAreaRepository = getInjectMock(MvAreaRepository.class);
		stubVoterRepository = getInjectMock(VoterRepository.class);
	}

	@Test
	public void testGetPollingDistrictsMissingVoters() {
		PollingDistrict stubPollingDistrict1 = buildPollingDistrict(POLLING_DISTRICT_ID_1, null, PD_MV_AREA_PK_1);
		PollingDistrict stubPollingDistrict2 = buildPollingDistrict(POLLING_DISTRICT_ID_2, POLLING_DISTRICT_NAME_2, PD_MV_AREA_PK_2);
		PollingDistrict stubPollingDistrict3 = buildPollingDistrict(POLLING_DISTRICT_ID_3, null, PD_MV_AREA_PK_3);
		buildPollingDistricts(stubPollingDistrict1, stubPollingDistrict2, stubPollingDistrict3);

		AreaPath areaPath = AreaPath.from("111111.22.33.4444");
		when(stubVoterRepository.hasVoters(PD_MV_AREA_PK_1)).thenReturn(true);
		when(stubVoterRepository.hasVoters(PD_MV_AREA_PK_2)).thenReturn(false);
		when(stubVoterRepository.hasVoters(PD_MV_AREA_PK_3)).thenReturn(true);
		KommuneSti kommuneSti = ValggeografiSti.kommuneSti(areaPath);

		List<ConfigurationDto> result = pollingDistrictServiceEjb.getPollingDistrictsMissingVoters(mockUserData, kommuneSti);

		assertThat(result).containsExactly(new ConfigurationDto(CONFIGURATION_ID_PD_2, CONFIGURATION_NAME_PD_2));
	}

	private PollingDistrict buildPollingDistrict(String pollingDistrictId, String pollingDistrictName, long mvAreaPk) {
		return buildPollingDistrict(BOROUGH_ID, BOROUGH_NAME, pollingDistrictId, pollingDistrictName, mvAreaPk);
	}

	private PollingDistrict buildPollingDistrict(
			String boroughId, String boroughName, String pollingDistrictId, String pollingDistrictName, long mvAreaPk) {
		PollingDistrict stubPollingDistrict = mock(PollingDistrict.class, RETURNS_DEEP_STUBS);
		when(stubPollingDistrict.getId()).thenReturn(pollingDistrictId);
		when(stubPollingDistrict.getName()).thenReturn(pollingDistrictName);
		when(stubPollingDistrict.getBorough().getId()).thenReturn(boroughId);
		when(stubPollingDistrict.getBorough().getName()).thenReturn(boroughName);
		MvArea stubMvArea = mock(MvArea.class);
		when(stubMvArea.getPk()).thenReturn(mvAreaPk);
		when(stubMvAreaRepository.findSingleByPollingDistrictIdAndMunicipalityPk(eq(pollingDistrictId), anyLong())).thenReturn(stubMvArea);
		return stubPollingDistrict;
	}

	private List<PollingDistrict> buildPollingDistricts(PollingDistrict... stubPollingDistricts) {
		List<PollingDistrict> stubPollingDistrictList = asList(stubPollingDistricts);
		when(stubPollingDistrictRepository.getPollingDistrictsByMunicipality(anyLong())).thenReturn(stubPollingDistrictList);
		return stubPollingDistrictList;
	}
}
