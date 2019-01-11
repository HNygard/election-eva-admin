package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.Test;

public class EmptyElectionCardControllerTest extends BaseFrontendTest {
	@Test
	public void init_gittKontekstMedValggeografiFraAnnenValghendelse_kasterException() throws Exception {
		EmptyElectionCardController controller = initializeMocks(EmptyElectionCardController.class);
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_EVENT);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_111111_11_11_1111);
		when(getInjectMock(UserDataController.class).getUserData().operatorValghierarkiSti()).thenReturn(VALGHENDELSE_STI);
		when(getInjectMock(UserDataController.class).getUserData().operatorValggeografiSti()).thenReturn(KOMMUNE_STI);
		Kontekst data = new Kontekst();
		data.setValghierarkiSti(VALGDISTRIKT_STI);
		data.setValggeografiSti(new KommuneSti("222222", "11", "11", "1111"));
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());
		
		controller.init();

		getServletContainer()
				.verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|1][geografi|nivaer|3|tjeneste|LAG_NYTT_VALGKORT][side|uri|null]");
	}
}
