package no.valg.eva.admin.frontend.i18n;

import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Per user object that keeps track of user locale and bundle, and provides an interface for getting localized texts.
 */
@Named
@SessionScoped
public class MessageProvider implements Serializable {
	private static final long serialVersionUID = 4916969220262459792L;
	private static final String HELP_TEXT_XHTML_ERROR = "@help.xhtml-error";

	private static final String PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<html>";
	private static final String POSTFIX = "</html>";
	private static final Logger LOG = Logger.getLogger(MessageProvider.class);
	private final Map<String, Boolean> validXHTML = new HashMap<String, Boolean>();

	@Inject
	private TranslationProvider translationProvider;

	@Inject
	private FacesContextBroker facesContextBroker;
	
	@PostConstruct
	public void postConstruct() {
		reloadBundle();
	}

	private void setFacesMessageBundle() {
		FacesContext context = facesContextBroker.getContext();
		if (context != null && context.getApplication() != null) {
			context.getApplication().setMessageBundle(ResourceBundleManager.EVA_MESSAGES_BUNDLE);
		} else {
			LOG.warn("Fikk ikke satt JSF message bundle - fikk ikke tak i kontekst/applikasjon for JSF");
		}
	}

	public String get(final String key, final Object... params) {
		return translationProvider.get(key, params);
	}

	public String getWithTranslatedParams(final String key, final String... params) {
		return translationProvider.getWithTranslatedParams(key, params);
	}

	public String getByElectionEvent(final String key, final Long electionEventPk) {
		return translationProvider.getByElectionEvent(key, electionEventPk);
	}

	public void reloadBundle() {
		translationProvider.reloadBundle();
		setFacesMessageBundle();
		resetXHTMLValidations();
	}

	private void resetXHTMLValidations() {
		validXHTML.clear();
	}

	public String getValidXHTML(final String key) {
		String msg = this.get(key);
		if (!msg.startsWith("???")) {
			return msg;
		}

		boolean valid = validXHTML.containsKey(key) ? validXHTML.get(key) : true;

		if (!validXHTML.containsKey(key)) {
			SAXBuilder builder = new SAXBuilder();
			Reader in = new StringReader(PREFIX + msg + POSTFIX);

			try {
				builder.build(in);
			} catch (JDOMException e) {
				LOG.debug(e);
				valid = false;
			} catch (IOException e) {
				LOG.debug(e);
				valid = false;
			}

			validXHTML.put(key, valid);
			LOG.trace("The key " + key + " is " + (valid ? "valid" : "invalid") + ", and we're caching it");
		} else {
			LOG.trace("The key " + key + " is ??? or cached");
		}

		if (!valid) {
			msg = MessageFormat.format(get(HELP_TEXT_XHTML_ERROR), key);
		}

		return msg;
	}
}
