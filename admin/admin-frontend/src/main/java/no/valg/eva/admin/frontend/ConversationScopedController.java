package no.valg.eva.admin.frontend;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;

public abstract class ConversationScopedController extends BaseController {

	@Inject
	private Conversation conversation;

	@PostConstruct
	public void init() {
		if (conversation.isTransient()) {
			conversation.begin();
		}
		conversation.setTimeout(EvoteConstants.CONVERSATION_TIMEOUT);
		doInit();
	}

	/**
	 * Template method for ensuring that subclasses does not shadow this class's init method called in post construct face. Implement post construct actions,
	 * like calling service methods in back end, here.
	 */
	protected abstract void doInit();

	public Conversation getConversation() {
		return conversation;
	}

	public String getCid() {
		return conversation.getId();
	}
}
