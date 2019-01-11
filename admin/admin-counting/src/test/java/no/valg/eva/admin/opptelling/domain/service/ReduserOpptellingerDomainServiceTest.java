package no.valg.eva.admin.opptelling.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiTestCase;
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

public class ReduserOpptellingerDomainServiceTest extends ReduserGeografiTestCase {

	private ReduserOpptellingerDomainService reduserOpptellingerDomainService;

	@BeforeMethod
	public void setup() throws Exception {
		super.setup();
		reduserOpptellingerDomainService = initializeMocks(ReduserOpptellingerDomainService.class);
		when(getInjectMock(ReduserGeografiDomainService.class).finnOmraaderSomSkalSlettes(any(), any(), any())).thenReturn(emptyList());
	}

	@Test(dataProvider = "reduksjonsomraadedata")
    public void reduserVelgere_dersomReduksjonsmodusErSlett_sletterVelgere(AreaLevelEnum omraadeniva, MvArea omraade) {
		when(getInjectMock(ReduserGeografiDomainService.class).finnOmraaderSomSkalSlettes(any(), any(), eq(omraadeniva)))
			.thenReturn(singletonList(omraade));

		reduserOpptellingerDomainService.reduserOpptellinger(geografiSpesifikasjon, valghendelseSti());

		verify(getInjectMock(OpptellingDomainService.class)).slettOpptellinger(any(), any());
		verifyNoMoreInteractions(getInjectMock(OpptellingDomainService.class));
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

}
