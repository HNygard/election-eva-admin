package no.valg.eva.admin.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.evote.service.TranslationService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.testng.annotations.Test;

public class LocaleControllerTest extends BaseFrontendTest {

	@Test
	public void init_withLocales_verifyLocaleIdMap() throws Exception {
		LocaleController ctrl = initializeMocks(LocaleController.class);
		stub_getAllLocales();

		ctrl.init();

		Map<String, LocaleId> localeIdMap = ctrl.getLocaleIdMap();
		assertThat(localeIdMap).hasSize(2);
		List<String> keys = new ArrayList<>(localeIdMap.keySet());
		assertThat(keys.get(0)).isEqualTo("nb_NO");
		assertThat(keys.get(1)).isEqualTo("nn_NO");
	}

	private List<Locale> stub_getAllLocales() throws Exception {
		List<Locale> result = new ArrayList<>();
		result.add(locale("nb_NO"));
		result.add(locale("nn_NO"));
		when(getInjectMock(TranslationService.class).getAllLocales()).thenReturn(result);
		return result;
	}

	private Locale locale(String id) {
		Locale result = new Locale();
		result.setId(id);
		result.setName("@locale[" + id + "].name");
		return result;
	}
}
