package no.valg.eva.admin.frontend.faces;

import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MultiPageMessagesSupportTest extends BaseFrontendTest {

    private static final String MULTI_PAGE_MESSAGES_SUPPORT = "MULTI_PAGE_MESSAGES_SUPPORT";
	private MultiPageMessagesSupport multiPageMessagesSupport;

	@BeforeMethod
	public void setup() throws Exception {
		multiPageMessagesSupport = initializeMocks(MultiPageMessagesSupport.class);
	}

	@Test
    public void getPhaseId_returnsAny() {
		assertThat(multiPageMessagesSupport.getPhaseId()).isEqualTo(PhaseId.ANY_PHASE);
	}

	@Test
    public void beforePhase_withPhaseIdInvokeApplication() {
		PhaseEvent event = createMock(PhaseEvent.class);
		when(event.getPhaseId()).thenReturn(PhaseId.INVOKE_APPLICATION);

		multiPageMessagesSupport.beforePhase(event);
	}

	@Test
    public void beforePhase_withPhaseIdInvokeApplication_verifySession() {
		PhaseEvent event = createMock(PhaseEvent.class);
		when(event.getPhaseId()).thenReturn(PhaseId.INVOKE_APPLICATION);
		addMessages(event.getFacesContext(), getFacesMessages(), true);

		multiPageMessagesSupport.beforePhase(event);

		Map<String, Object> map = event.getFacesContext().getExternalContext().getSessionMap();
		assertThat(map.containsKey(MULTI_PAGE_MESSAGES_SUPPORT)).isTrue();
		List<FacesMessage> message = (List<FacesMessage>) map.get(MULTI_PAGE_MESSAGES_SUPPORT);
		assertThat(message.size()).isEqualTo(2);
	}

	@Test
    public void beforePhase_withPhaseIdRenderResponse_verifySession() {
		PhaseEvent event = createMock(PhaseEvent.class);
		when(event.getPhaseId()).thenReturn(PhaseId.RENDER_RESPONSE);
		addMessages(event.getFacesContext(), getFacesMessages(), false);

		multiPageMessagesSupport.beforePhase(event);

		Map<String, Object> map = event.getFacesContext().getExternalContext().getSessionMap();
		assertThat(map.containsKey(MULTI_PAGE_MESSAGES_SUPPORT)).isFalse();
	}

	@Test
    public void afterPhase_withPhaseIdRenderResponse_verifyNothing() {
		PhaseEvent event = createMock(PhaseEvent.class);
		when(event.getPhaseId()).thenReturn(PhaseId.RENDER_RESPONSE);
		addMessages(event.getFacesContext(), getFacesMessages(), true);

		multiPageMessagesSupport.afterPhase(event);
	}

	@Test
    public void afterPhase_withPhaseIdInvokeApplication_verifySession() {
		PhaseEvent event = createMock(PhaseEvent.class);
		when(event.getPhaseId()).thenReturn(PhaseId.INVOKE_APPLICATION);
		addMessages(event.getFacesContext(), getFacesMessages(), false);

		multiPageMessagesSupport.afterPhase(event);

		Map<String, Object> map = event.getFacesContext().getExternalContext().getSessionMap();
		assertThat(map.containsKey(MULTI_PAGE_MESSAGES_SUPPORT)).isTrue();
		List<FacesMessage> message = (List<FacesMessage>) map.get(MULTI_PAGE_MESSAGES_SUPPORT);
		assertThat(message.size()).isEqualTo(2);
	}

	private List<FacesMessage> getFacesMessages() {
		List<FacesMessage> result = new ArrayList<>();
		result.add(new FacesMessage("Message 1"));
		result.add(new FacesMessage("Message 2"));
		return result;
	}

	private Iterator<FacesMessage> addMessages(FacesContext ctx, final List<FacesMessage> messages, boolean addSessionKey) {
		when(ctx.getMessages(any())).thenReturn(new Iterator<FacesMessage>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < messages.size();
			}

			@Override
			public FacesMessage next() {
				return messages.get(index++);
			}

			@Override
			public void remove() {
			}
		});
		Map<String, Object> m = new HashMap<>();
		if (addSessionKey) {
			m.put(MULTI_PAGE_MESSAGES_SUPPORT, new ArrayList<FacesMessage>());
		}
		when(ctx.getExternalContext().getSessionMap()).thenReturn(m);
		return ctx.getMessages(null);
	}
}
