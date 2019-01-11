package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.service.ValghierarkiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SlettValgoppgjoerValghierarkiApplicationServiceTest extends MockUtilsTestCase {
	private SlettValgoppgjoerValghierarkiApplicationService service;
	private ValghierarkiDomainService domainService;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(SlettValgoppgjoerValghierarkiApplicationService.class);
		domainService = getInjectMock(ValghierarkiDomainService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void valg_gittUserDataOgValggruppeSti_returnererValg() throws Exception {
		ValggruppeSti valggruppeSti = valggruppeSti();
		List<Valg> valg = valg();
		when(domainService.valg(valggruppeSti, operatorValggeografiSti(), true, null)).thenReturn(valg);
		List<Valg> resultat = service.valg(userData, valggruppeSti, null);
		assertThat(resultat).isSameAs(valg);
	}

	private ValggeografiSti operatorValggeografiSti() {
		return userData.operatorValggeografiSti();
	}

	private ValggruppeSti valggruppeSti() {
		return createMock(ValggruppeSti.class);
	}

	private List<Valg> valg() {
		return createListMock();
	}

}
