package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.service.ValggeografiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LagNyttValgkortValggeografiApplicationServiceTest extends MockUtilsTestCase {
	private LagNyttValgkortValggeografiApplicationService service;
	private ValggeografiDomainService domainService;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(LagNyttValgkortValggeografiApplicationService.class);
		domainService = getInjectMock(ValggeografiDomainService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void fylkeskommuner_gittUserDataOgValghierarkiSti_returnererFylkeskommunerForValghendelse() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		List<Fylkeskommune> fylkeskommuner = fylkeskommuner();
		when(domainService.fylkeskommuner(operatorValghendelseSti(), valghierarkiSti)).thenReturn(fylkeskommuner);
		List<Fylkeskommune> resultat = service.fylkeskommuner(userData, valghierarkiSti, null);
		assertThat(resultat).isSameAs(fylkeskommuner);
	}

	@Test
	public void kommuner_gittUserDataOgFylkeskommuneStiOgValghierarkiSti_returnererKommunerForFylkeskommune() throws Exception {
		FylkeskommuneSti fylkeskommuneSti = fylkeskommuneSti();
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		List<Kommune> kommuner = kommuner();
		when(domainService.kommuner(fylkeskommuneSti, valghierarkiSti)).thenReturn(kommuner);
		List<Kommune> resultat = service.kommuner(userData, fylkeskommuneSti, valghierarkiSti, null);
		assertThat(resultat).isSameAs(kommuner);
	}

	private ValghendelseSti operatorValghendelseSti() {
		return userData.operatorValggeografiSti().valghendelseSti();
	}

	private ValghierarkiSti valghierarkiSti() {
		return createMock(ValghierarkiSti.class);
	}

	private FylkeskommuneSti fylkeskommuneSti() {
		return createMock(FylkeskommuneSti.class);
	}

	private List<Fylkeskommune> fylkeskommuner() {
		return createListMock();
	}

	private List<Kommune> kommuner() {
		return createListMock();
	}

}
