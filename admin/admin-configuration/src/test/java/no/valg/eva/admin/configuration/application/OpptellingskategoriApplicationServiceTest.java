package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.service.OpptellingskategoriDomainService;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class OpptellingskategoriApplicationServiceTest extends MockUtilsTestCase {
	@Test
	public void countCategoriesForValgSti_gittUserDataOgValgSti_returnererCountCategories() throws Exception {
		OpptellingskategoriApplicationService service = initializeMocks(OpptellingskategoriApplicationService.class);
		OpptellingskategoriDomainService domainService = getInjectMock(OpptellingskategoriDomainService.class);

		UserData userData = createMock(UserData.class);
		ValgSti valgSti = createMock(ValgSti.class);
		List<CountCategory> countCategories = createListMock();

		when(domainService.countCategoriesForValgSti(userData.operatorValggeografiSti(), valgSti)).thenReturn(countCategories);

		List<CountCategory> resultat = service.countCategoriesForValgSti(userData, valgSti);

		assertThat(resultat).isEqualTo(countCategories);
	}

	@Test
	public void countCategories_gittUserData_returnererCountCategories() throws Exception {
		OpptellingskategoriApplicationService service = initializeMocks(OpptellingskategoriApplicationService.class);
		OpptellingskategoriDomainService domainService = getInjectMock(OpptellingskategoriDomainService.class);

		UserData userData = createMock(UserData.class);
		List<CountCategory> countCategories = createListMock();

		when(domainService.countCategories(userData.operatorValggeografiSti())).thenReturn(countCategories);

		List<CountCategory> resultat = service.countCategories(userData);

		assertThat(resultat).isEqualTo(countCategories);
	}
}
