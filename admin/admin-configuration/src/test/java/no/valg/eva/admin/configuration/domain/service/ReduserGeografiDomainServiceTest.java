package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReduserGeografiDomainServiceTest extends ReduserGeografiTestCase {

	private ReduserGeografiDomainService reduserGeografiDomainService;
	
	@BeforeMethod
	public void setup() throws Exception {
		super.setup();
		reduserGeografiDomainService = initializeMocks(ReduserGeografiDomainService.class);
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), any())).thenReturn(emptyList());
	}

	@Test
    public void reduserGeografi_gittSpesifikasjonSomIkkeNevnerEtFylke_sletterFylket() {
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), eq(COUNTY))).thenReturn(fylker());
		
		reduserGeografiDomainService.reduserGeografi(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(CountyRepository.class)).delete(any(), eq(FYLKE_3_PK));
		verifyNoMoreInteractions(getInjectMock(CountyRepository.class));
	}

	@Test
    public void reduserGeografi_gittSpesifikasjonSomIkkeNevnerEnKommune_sletterKommunen() {
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), eq(MUNICIPALITY))).thenReturn(kommuner());

		reduserGeografiDomainService.reduserGeografi(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(MunicipalityRepository.class)).delete(any(), eq(KOMMUNE_2_PK));
		verify(getInjectMock(MunicipalityRepository.class)).delete(any(), eq(KOMMUNE_3_PK));
		verifyNoMoreInteractions(getInjectMock(MunicipalityRepository.class));
	}

	@Test
    public void reduserGeografi_gittSpesifikasjonSomIkkeNevnerEnKrets_sletterKretsen() {
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), eq(POLLING_DISTRICT))).thenReturn(kretser());

		reduserGeografiDomainService.reduserGeografi(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(PollingDistrictRepository.class)).delete(any(), eq(KRETS_3_PK));
		verifyNoMoreInteractions(getInjectMock(PollingDistrictRepository.class));
	}

	@Test
    public void reduserGeografi_gittSpesifikasjonSomIkkeNevnerEtStemmested_sletterStemmestedet() {
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), eq(POLLING_PLACE))).thenReturn(stemmesteder());

		reduserGeografiDomainService.reduserGeografi(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(PollingPlaceRepository.class)).delete(any(), eq(STEMMESTED_3_PK));
		verifyNoMoreInteractions(getInjectMock(PollingPlaceRepository.class));
	}
	
	@Test(dataProvider = "omraadenivaaTestdata")
	public void finnOmraadenivaaerForFlytting_returnererNivaaerSomSkalSjekkesForPotensielleNyeOmraaderAaFlytteTil(
		MvArea omraadeSomSkalSlettes, AreaLevelEnum omraadenivaaForResultat, List<AreaLevelEnum> forventetResultat) {
		assertThat(reduserGeografiDomainService.finnOmraadenivaaerForFlytting(omraadeSomSkalSlettes, omraadenivaaForResultat))
			.isEqualTo(forventetResultat);
	}
	
	@DataProvider
	private Object[][] omraadenivaaTestdata() {
		return new Object[][] {
			{ fylke(), COUNTY, singletonList(COUNTRY)},
			{ fylke(), MUNICIPALITY, singletonList(COUNTRY) },
			{ fylke(), POLLING_DISTRICT, singletonList(COUNTRY) },
			{ fylke(), POLLING_PLACE, singletonList(COUNTRY) },
			{ kommune(), COUNTY, singletonList(COUNTRY)},
			{ kommune(), MUNICIPALITY, asList(COUNTY, COUNTRY) },
			{ kommune(), POLLING_DISTRICT, asList(COUNTY, COUNTRY) },
			{ kommune(), POLLING_PLACE, asList(COUNTY, COUNTRY) },
			{ krets(), COUNTY, singletonList(COUNTRY)},
			{ krets(), MUNICIPALITY, asList(COUNTY, COUNTRY) },
			{ krets(), POLLING_DISTRICT, asList(MUNICIPALITY, COUNTY, COUNTRY) },
			{ krets(), POLLING_PLACE, asList(MUNICIPALITY, COUNTY, COUNTRY) },
			{ stemmested(), COUNTY, singletonList(COUNTRY)},
			{ stemmested(), MUNICIPALITY, asList(COUNTY, COUNTRY) },
			{ stemmested(), POLLING_DISTRICT, asList(MUNICIPALITY, COUNTY, COUNTRY) },
			{ stemmested(), POLLING_PLACE, asList(POLLING_DISTRICT, MUNICIPALITY, COUNTY, COUNTRY) },
		};
	}

	private MvArea fylke() {
		return fylke("123456.47.01", FYLKE_1_PK);
	}

	private MvArea stemmested() {
		return stemmested("123456.47.02.0201.020100.0004.0000", STEMMESTED_1_PK);
	}

	private MvArea krets() {
		return krets("123456.47.02.0201.020100.0001", KRETS_1_PK);
	}

	private MvArea kommune() {
		return kommune("123456.47.02.0201", KOMMUNE_1_PK);
	}

	@Test
	public void finneBeholdtOmraade_returnererForsteOmraadeSomIkkeSkalSlettes() {
		when(getInjectMock(MvAreaRepository.class).finnFor(any(), eq(POLLING_PLACE))).thenReturn(stemmesteder());
		MvArea omraade = fylke();
		
		MvArea beholdtOmraade = reduserGeografiDomainService.finnBeholdtOmraade(geografiSpesifikasjon, omraade, POLLING_PLACE);

		assertThat(beholdtOmraade).isEqualTo(stemmesteder().get(0));
	}
	
}
