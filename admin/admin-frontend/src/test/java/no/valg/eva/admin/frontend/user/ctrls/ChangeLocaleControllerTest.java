package no.valg.eva.admin.frontend.user.ctrls;

import static org.mockito.Mockito.verify;

import no.evote.service.TranslationService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.testng.annotations.Test;

public class ChangeLocaleControllerTest extends BaseFrontendTest {

	@Test
	public void init_verifyAdminLocalesLoaded() throws Exception {
		ChangeLocaleController ctrl = initializeMocks(ChangeLocaleController.class);

		ctrl.init();

		verify(getInjectMock(TranslationService.class)).getAllLocales();
	}

	@Test
	public void setCurrentLocale_withLocale_verifyUserDataAndResourceBundleUpdate() throws Exception {
		ChangeLocaleController ctrl = initializeMocks(ChangeLocaleController.class);
		Locale locale = createMock(Locale.class);

		ctrl.setCurrentLocale(locale);

		verify(getUserDataMock()).setLocale(locale);
		verify(getInjectMock(MessageProvider.class)).reloadBundle();
	}
}
