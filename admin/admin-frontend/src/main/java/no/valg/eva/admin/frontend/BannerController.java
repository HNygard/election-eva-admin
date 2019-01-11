package no.valg.eva.admin.frontend;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class BannerController implements Serializable {

	private boolean enabled;
	private String banner;
	private String backgroundColor;
	private String textColor;
	private BannerProperties bannerProperties;

	public BannerController() {

	}

	@Inject
	public BannerController(BannerProperties bannerProperties) {
		this.bannerProperties = bannerProperties;
	}

	@PostConstruct
	public void initialize() {
		enabled = bannerProperties.isEnabled();

		if (enabled) {
			banner = bannerProperties.getBannerText();
			backgroundColor = "#" + bannerProperties.getBannerBackgroundColor();
			textColor = "#" + bannerProperties.getBannerTextColor();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getBanner() {

		if (!enabled) {
			return null;
		}

		return banner;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public String getTextColor() {
		return textColor;
	}
}
