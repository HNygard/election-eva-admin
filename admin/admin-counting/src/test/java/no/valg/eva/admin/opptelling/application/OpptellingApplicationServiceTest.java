package no.valg.eva.admin.opptelling.application;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.FYLKESVALGSTYRET;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.VALGSTYRET;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static org.mockito.Mockito.verify;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.opptelling.domain.service.OpptellingDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpptellingApplicationServiceTest extends MockUtilsTestCase {
	private OpptellingApplicationService service;
	private OpptellingDomainService domainService;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(OpptellingApplicationService.class);
		domainService = getInjectMock(OpptellingDomainService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiSti_kallerDomainService() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		service.slettOpptellinger(userData, valghierarkiSti, valggeografiSti, null, null);
		verify(domainService).slettOpptellinger(valghierarkiSti, valggeografiSti);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgCountcategories_kallerDomainService() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		CountCategory[] countCatgories = {FO, VO};
		service.slettOpptellinger(userData, valghierarkiSti, valggeografiSti, countCatgories, null);
		verify(domainService).slettOpptellinger(valghierarkiSti, valggeografiSti, countCatgories);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgStyretyper_kallerDomainService() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		service.slettOpptellinger(userData, valghierarkiSti, valggeografiSti, null, styretyper);
		verify(domainService).slettOpptellinger(valghierarkiSti, valggeografiSti, styretyper);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgCountCategoriesOgStyretyper_kallerDomainService() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		CountCategory[] countCatgories = {FO, VO};
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		service.slettOpptellinger(userData, valghierarkiSti, valggeografiSti, countCatgories, styretyper);
		verify(domainService).slettOpptellinger(valghierarkiSti, valggeografiSti, countCatgories, styretyper);
	}
}
