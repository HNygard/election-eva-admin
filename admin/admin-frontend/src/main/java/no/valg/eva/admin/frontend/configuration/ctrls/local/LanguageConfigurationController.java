package no.valg.eva.admin.frontend.configuration.ctrls.local;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

@Named
@ViewScoped
public class LanguageConfigurationController extends ConfigurationController {

	private LocaleId localeId;

	@Override
	public void init() {
		if (isCountyLevel()) {
			setLocaleId(getCountyConfigStatus().getLocaleId());
		} else if (isMunicipalityLevel()) {
			setLocaleId(getMunicipalityConfigStatus().getLocaleId());
		} else {
			throw unsupportedLevel();
		}
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.LANGUAGE;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.language.name";
	}

	@Override
	boolean hasAccess() {
		return isCountyLevel() || isMunicipalityLevel();
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isCountyLevel()) {
			getCountyConfigStatus().setLanguage(value);
		} else if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setLanguage(value);
		} else {
			throw unsupportedLevel();
		}
	}

	@Override
	public boolean isDoneStatus() {
		if (isCountyLevel()) {
			return getCountyConfigStatus().isLanguage();
		} else if (isMunicipalityLevel()) {
			return getMunicipalityConfigStatus().isLanguage();
		} else {
			throw unsupportedLevel();
		}
	}

	@Override
	boolean canBeSetToDone() {
		return true;
	}

	@Override
	public void saveDone() {
		saveLanguage();
	}

	public String getHeaderHint() {
		if (isCountyLevel()) {
			return "@config.local.language.header_choose_hint_county";
		} else if (isMunicipalityLevel()) {
			return "@config.local.language.header_choose_hint_municipality";
		} else {
			throw unsupportedLevel();
		}
	}

	private void saveLanguage() {
		if (!isEditable()) {
			return;
		}

		execute(() -> {
			String[] params;
			if (isCountyLevel()) {
				getCountyConfigStatus().setLocaleId(localeId);
				params = new String[] { getCountyConfigStatus().getCountyName(), getMessageProvider().get(localeId.getName()) };
			} else if (isMunicipalityLevel()) {
				getMunicipalityConfigStatus().setLocaleId(localeId);
				params = new String[] { getMunicipalityConfigStatus().getMunicipalityName(), getMessageProvider().get(localeId.getName()) };
			} else {
				throw unsupportedLevel();
			}
			if (saveDone(true)) {
				MessageUtil.buildDetailMessage("@config.local.language.updatedMessage", params, FacesMessage.SEVERITY_INFO);
			}
			init();
		});
	}

	public LocaleId getLocaleId() {
		return localeId;
	}

	public void setLocaleId(LocaleId localeId) {
		this.localeId = localeId;
	}

}
