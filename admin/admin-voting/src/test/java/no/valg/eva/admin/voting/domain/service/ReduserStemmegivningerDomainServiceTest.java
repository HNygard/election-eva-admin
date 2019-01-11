package no.valg.eva.admin.voting.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiTestCase;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReduserStemmegivningerDomainServiceTest extends ReduserGeografiTestCase {

	private ReduserStemmegivningerDomainService reduserStemmegivningerDomainService;

	@BeforeMethod
	public void setup() throws Exception {
		super.setup();
		reduserStemmegivningerDomainService = initializeMocks(ReduserStemmegivningerDomainService.class);
		when(getInjectMock(ReduserGeografiDomainService.class).finnOmraaderSomSkalSlettes(any(), any(), any())).thenReturn(emptyList());
	}

	@Test(dataProvider = "reduksjonsomraadedata")
    public void reduserVelgere_sletterVelgere(AreaLevelEnum omraadeniva, MvArea omraade) {
		when(getInjectMock(ReduserGeografiDomainService.class).finnOmraaderSomSkalSlettes(any(), any(), eq(omraadeniva))).thenReturn(singletonList(omraade));

		reduserStemmegivningerDomainService.reduserStemmegivninger(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(VotingRepository.class)).deleteVotings(any(), eq(omraade), eq(0));
		verify(getInjectMock(VotingRepository.class)).slettStemmegivningerFraVelgereTilhoerendeI(any());
		
		verifyNoMoreInteractions(getInjectMock(VotingRepository.class));
	}

	@DataProvider
	public Object[][] reduksjonsomraadedata() {
		return new Object[][] {
			{ POLLING_PLACE, stemmested("123456.47.01.0201.020100.0004.0002", STEMMESTED_3_PK) },
			{ POLLING_DISTRICT, krets("123456.47.01.0201.020100.0003", KRETS_3_PK) },
			{ MUNICIPALITY, kommune("123456.47.02.0203", KOMMUNE_3_PK) },
			{ COUNTY, fylke("123456.47.03", FYLKE_3_PK) },
		};
	}

	@Test(dataProvider = "reduksjonsomraadedata")
    public void flyttVelgere_flytterVelgereTilLedigKrets(AreaLevelEnum omraadeniva, MvArea omraade) {
		MvArea krets = krets("123456.47.02.0201.020100.0002", KRETS_2_PK);
		when(getInjectMock(ReduserGeografiDomainService.class).finnOmraaderSomSkalSlettes(any(), any(), eq(omraadeniva))).thenReturn(singletonList(omraade));
		when(getInjectMock(ReduserGeografiDomainService.class).finnBeholdtOmraade(any(), eq(omraade), any())).thenReturn(krets);

		reduserStemmegivningerDomainService.flyttStemmegivninger(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(VotingRepository.class)).flyttStemmegivningerAvgittI(omraade, krets);
		verify(getInjectMock(VotingRepository.class)).flyttStemmegivningerForVelgereI(omraade, krets);
		verifyNoMoreInteractions(getInjectMock(VotingRepository.class));
	}
}
