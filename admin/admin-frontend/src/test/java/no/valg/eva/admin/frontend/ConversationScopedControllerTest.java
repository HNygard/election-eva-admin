package no.valg.eva.admin.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.context.Conversation;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.Test;


public class ConversationScopedControllerTest extends BaseFrontendTest {

	@Test
	public void init_withTransientConversation_checkState() throws Exception {
		MyConversationScopedController ctrl = initializeMocks(new MyConversationScopedController());
		when(getInjectMock(Conversation.class).isTransient()).thenReturn(true);
		ctrl = spy(ctrl);

		ctrl.init();

		verify(ctrl).doInit();
		verify(getInjectMock(Conversation.class)).begin();
		verify(getInjectMock(Conversation.class)).setTimeout(EvoteConstants.CONVERSATION_TIMEOUT);
	}

	@Test
	public void getCid_shouldReturnCorrectCid() throws Exception {
		MyConversationScopedController ctrl = initializeMocks(new MyConversationScopedController());
		when(getInjectMock(Conversation.class).getId()).thenReturn("12345");

		assertThat(ctrl.getCid()).isEqualTo("12345");
	}

	private class MyConversationScopedController extends ConversationScopedController {
		@Override
		protected void doInit() {

		}
	}

}
