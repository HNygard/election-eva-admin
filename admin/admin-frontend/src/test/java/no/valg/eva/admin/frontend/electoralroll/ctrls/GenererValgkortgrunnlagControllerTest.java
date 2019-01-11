package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.service.ValgkortgrunnlagService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenererValgkortgrunnlagControllerTest extends BaseFrontendTest {
	
	@Test
	public void genererValgkortgrunnlag_alltid_starterJobb() throws Exception {
		GenererValgkortgrunnlagController ctrl = initializeMocks(GenererValgkortgrunnlagController.class);		
		
		ctrl.genererValgkortgrunnlag();

		verify(getInjectMock(ValgkortgrunnlagService.class)).genererValgkortgrunnlag(any(), anyBoolean());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@electoralRoll.genererValgkortgrunnlag.startet");
	}
	
	@Test(dataProvider = "forutsetninger")
	public void init_hvisForutsetningerMangler_viserAdvarsel(GenererValgkortgrunnlagStatus forutsetning, String forventetMelding) throws Exception {
		GenererValgkortgrunnlagController ctrl = initializeMocks(GenererValgkortgrunnlagController.class);
		when(getInjectMock(ValgkortgrunnlagService.class).sjekkForutsetningerForGenerering(any())).thenReturn(forutsetning);
		
		ctrl.init();

		assertFacesMessage(FacesMessage.SEVERITY_WARN, forventetMelding);
		assertThat(ctrl.isStatusOk()).isFalse();
		
	}
	
	@DataProvider
	public Object[][] forutsetninger() {
		return new Object[][] { 
			{ GenererValgkortgrunnlagStatus.MANNTALLSNUMRE_MANGLER, "@electoralRoll.genererValgkortgrunnlag.manglerManntallsnumre" },
			{ GenererValgkortgrunnlagStatus.TOMT_MANNTALL, "@electoralRoll.genererValgkortgrunnlag.tomtManntall" },
		};
	}
}
