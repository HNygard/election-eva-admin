package no.valg.eva.admin.frontend.i18n;

import no.valg.eva.admin.BaseFrontendTest;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageProviderTest extends BaseFrontendTest {

	private MessageProvider messageProvider;

	@BeforeMethod
	public void setup() throws Exception {
		messageProvider = initializeMocks(MessageProvider.class);
		when(getInjectMock(TranslationProvider.class).get(anyString(), any())).thenAnswer(getAnswer());
		when(getInjectMock(TranslationProvider.class).getWithTranslatedParams(anyString(), ArgumentMatchers.<String>any())).thenAnswer(getAnswer());
	}

	@Test
	public void postConstruct_verifyState() {
		messageProvider.postConstruct();

		verify(getInjectMock(TranslationProvider.class)).reloadBundle();
		verify(getFacesContextMock().getApplication()).setMessageBundle(ResourceBundleManager.EVA_MESSAGES_BUNDLE);
	}

	@Test
	public void get_withString_returnsString() {
		assertThat(messageProvider.get("@myString")).isEqualTo("TP: @myString");
	}

	@Test
	public void get_withStringAndArgs_returnsString() {
		assertThat(messageProvider.get("@myString", "1", "2")).isEqualTo("TP: [@myString, 1, 2]");
	}

	@Test
	public void getWithTranslatedParams_withString_returnsString() {
		assertThat(messageProvider.getWithTranslatedParams("@myString")).isEqualTo("TP: @myString");
	}

	@Test
	public void getWithTranslatedParams_withStringAndArgs_returnsString() {
		assertThat(messageProvider.getWithTranslatedParams("@myString", "1", "2")).isEqualTo("TP: [@myString, 1, 2]");
	}

	@Test
	public void getByElectionEvent_withKeyAndPK_verifyTranslationProvider() {
		String key = "key";
		Long electionEventPk = 100L;

		messageProvider.getByElectionEvent(key, electionEventPk);

		verify(getInjectMock(TranslationProvider.class)).getByElectionEvent(key, electionEventPk);
	}

	@Test
	public void getValidXHTML_withTextFound_returnsText() {
		when(getInjectMock(TranslationProvider.class).get("@hello")).thenReturn("Hello");

		assertThat(messageProvider.getValidXHTML("@hello")).isEqualTo("Hello");
	}

	@Test
	public void getValidXHTML_withNoTextFoundAndValidHthml_returnsText() {
		when(getInjectMock(TranslationProvider.class).get("@hello")).thenReturn("???@hello???");

		assertThat(messageProvider.getValidXHTML("@hello")).isEqualTo("???@hello???");
	}

	@Test
	public void getValidXHTML_withNoTextFoundAndInvalidHthml_returnsText() {
		when(getInjectMock(TranslationProvider.class).get("@hello")).thenReturn("???<???");

		assertThat(messageProvider.getValidXHTML("@hello")).isEqualTo("TP: @help.xhtml-error");
	}

	private Answer<String> getAnswer() {
		return new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				if (args.length == 1 || (args.length == 2 && args[1] == null)) {
					return "TP: " + args[0];
				}
				return "TP: " + Arrays.toString(invocation.getArguments());
			}
		};
	}

}

