package no.valg.eva.admin.frontend;

import javax.inject.Named;

import no.evote.util.EvoteProperties;

@Named
public class BannerProperties {

	BannerProperties() {

	}

	//Because testing EvoteProperties.java is too complex and needs to be refactored
	public boolean isEnabled() {
		return EvoteProperties.getBooleanProperty(EvoteProperties.NO_VALG_EVA_ADMIN_BANNER_ENABLED, false);
	}

	//Because testing EvoteProperties.java is too complex and needs to be refactored
	public String getBannerText() {
		return EvoteProperties.getProperty(EvoteProperties.NO_VALG_EVA_ADMIN_BANNER_TEXT);
	}

	//Because testing EvoteProperties.java is too complex and needs to be refactored
	public String getBannerBackgroundColor() {
		return EvoteProperties.getProperty(EvoteProperties.NO_VALG_EVA_ADMIN_BANNER_BACKGROUND_COLOR);
	}

	//Because testing EvoteProperties.java is too complex and needs to be refactored
	public String getBannerTextColor() {
		return EvoteProperties.getProperty(EvoteProperties.NO_VALG_EVA_ADMIN_BANNER_TEXT_COLOR);
	}
}
