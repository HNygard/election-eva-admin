package no.valg.eva.admin.frontend;

import java.io.Serializable;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * Retrieves localized messages by ID, as MessageManager, but verifies that they can be parsed as XHTML. This should be used when there is XHTML allowed in the
 * messages, such as with help text.
 */
@Named("htmlMsgs")
public class HtmlMsgs extends HashMap<Object, Object> implements Serializable {

	@Inject
	private MessageProvider messageProvider;

	/**
	 * Get help text, but make sure it's a valid XHTML fragment first.
	 * @param msgId
	 *            The i18n ID of the help text
	 * @return The i18n text, after it's been checked for XHTML validation
	 */
	@Override
	public String get(final Object msgId) {
		return messageProvider.getValidXHTML((String) msgId);
	}
}
