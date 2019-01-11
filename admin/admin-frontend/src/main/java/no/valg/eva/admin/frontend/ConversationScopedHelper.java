package no.valg.eva.admin.frontend;

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;

@Named
@ConversationScoped
@Deprecated
public class ConversationScopedHelper implements Serializable {

	@Inject
	private Conversation conversation;

	/**
	 * Starts a JSF conversation. Useful to invoke from view, before a Form that uses Ajax on a conversation scoped controller/backing bean.
	 */
	public void startConversation() {
		if (conversation.isTransient()) {
			conversation.begin();
		}
		conversation.setTimeout(EvoteConstants.CONVERSATION_TIMEOUT);
	}
}
