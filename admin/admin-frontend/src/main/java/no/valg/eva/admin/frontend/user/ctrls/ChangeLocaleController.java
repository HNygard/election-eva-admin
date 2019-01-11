package no.valg.eva.admin.frontend.user.ctrls;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.evote.service.TranslationService;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * Controller for changing locale, used by changeLocale.xhtml.
 */
@Named
@RequestScoped
public class ChangeLocaleController extends BaseController {

	@Inject
	private TranslationService translationService;

	@Inject
	private UserData userData;

	@Inject
	private MessageProvider messageProvider;

	private List<Locale> locales;

	@PostConstruct
	public void init() {
		locales = translationService.getAllLocales();
	}

	public Locale getCurrentLocale() {
		return userData.getLocale();
	}

	public void setCurrentLocale(Locale locale) {
		userData.setLocale(locale);

		// Tell the MessageProvider to change locale
		messageProvider.reloadBundle();
	}

	public List<Locale> getLocales() {
		return locales;
	}
}
