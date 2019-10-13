package no.valg.eva.admin.backend.reporting.jasperserver;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.tuple.Pair.of;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.service.TranslationServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

@ApplicationScoped
@Default
public class ElectionEventResourceBundleProvider {
	private static final Logger LOG = Logger.getLogger(ReportTemplateUploader.class);
	private static final String LOGO_FILE_NAME = "valglogo.jpg"; 
	public static final String REPORT_BUNDLE_NAME = "no.valg.eva.admin.report_templates.ReportMessages";

	@Inject
	private LocaleRepository localeRepository;
	@Inject
	private TranslationServiceBean translationService;
	@Inject
	private ElectionEventRepository electionEventRepository;

	public List<Pair<String, byte[]>> resourceBundles(String bundleBaseName, String defaultLocale) {
		List<Pair<String, byte[]>> resourceBundles = new ArrayList<>();
		List<Locale> allLocales = localeRepository.findAllLocales();
		for (Locale locale : allLocales) {
			resourceBundles.addAll(createResourceBundlesForGlobalTexts(locale, defaultLocale, bundleBaseName));
		}

		for (ElectionEvent electionEvent : electionEventRepository.findAll()) {
			for (Locale locale : electionEventRepository.getLocalesForEvent(electionEvent)) {
				Map<String, String> localeTexts = translationService.getLocaleTexts(electionEvent, locale.getPk());
				try {
					byte[] bundleBytes = createResourceBundle(localeTexts);
					String variant = electionEvent.getId();
					String bundleName = format("{0}_{1}{2,choice,0#|1#_}{3}.properties", bundleBaseName, locale.toJavaLocale(), variant.length(),
							variant);
					resourceBundles.add(of(bundleName, bundleBytes));
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
		
		resourceBundles.add(logoResource());
		
		return resourceBundles;
	}

	private List<Pair<String, byte[]>> createResourceBundlesForGlobalTexts(Locale locale, String defaultLocale, String bundleBase) {

		String bundleName = toBundleName(REPORT_BUNDLE_NAME, locale.toJavaLocale());
		String resourceName = toResourceName(bundleName, "properties");
		Properties properties = new Properties();
		try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName), StandardCharsets.ISO_8859_1)) {
			properties.load(reader);
		} catch (IOException e) {
			LOG.error("IOException ved lasting av " + resourceName, e);
		} catch (Exception e) {
			LOG.error("Feil ved lasting av " + resourceName, e);
		}
		Map<String, String> globalTextAsMap = properties.keySet().stream().collect(Collectors.toMap(key -> (String) key, key -> (String) properties.get(key)));

		List<Pair<String, byte[]>> resourceBundleForGlobalTexts = new ArrayList<>();
		try {
			byte[] bundleBytes = createResourceBundle(globalTextAsMap);
			resourceBundleForGlobalTexts.add(of(format("{0}_{1}.properties", bundleBase, locale.toJavaLocale()), bundleBytes));
			if (defaultLocale.equals(locale.getId())) {
				resourceBundleForGlobalTexts.add(of(format("{0}.properties", bundleBase), bundleBytes));
			}
		} catch (IOException e) {
			LOG.error(e);
		}
		return resourceBundleForGlobalTexts;
	}

	/**
	 * Fra java.util.ResourceBundle
	 */
	private String toResourceName(String bundleName, String suffix) {
		StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
		sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
		return sb.toString();
	}

	/**
	 * Fra java.util.ResourceBundle - men uten quality checks feil
	 */
	private String toBundleName(String baseName, java.util.Locale locale) {
		if (locale == java.util.Locale.ROOT) {
			return baseName;
		}

		String language = locale.getLanguage();
		String script = locale.getScript();
		String country = locale.getCountry();
		String variant = locale.getVariant();

		if (Objects.equals(language, "") && Objects.equals(country, "") && Objects.equals(variant, "")) {
			return baseName;
		}

		StringBuilder sb = new StringBuilder(baseName);
		sb.append('_');
		if (!Objects.equals(script, "")) {
			if (!Objects.equals(variant, "")) {
				sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
			} else if (!Objects.equals(country, "")) {
				sb.append(language).append('_').append(script).append('_').append(country);
			} else {
				sb.append(language).append('_').append(script);
			}
		} else {
			if (!Objects.equals(variant, "")) {
				sb.append(language).append('_').append(country).append('_').append(variant);
			} else if (!Objects.equals(country, "")) {
				sb.append(language).append('_').append(country);
			} else {
				sb.append(language);
			}
		}
		return sb.toString();
	}

	private byte[] createResourceBundle(Map<String, String> localeTexts) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, ISO_8859_1));
		for (Map.Entry<String, String> entry : localeTexts.entrySet()) {
			printWriter.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
		}
		printWriter.close();
		return out.toByteArray();
	}
	
	private Pair<String, byte[]> logoResource() {
		try {
			return Pair.of(
					LOGO_FILE_NAME,
					IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(LOGO_FILE_NAME))
			);
		} catch (IOException e) {
			LOG.error("Could not read logo image file", e);
			return null;
		}
	} 
}
