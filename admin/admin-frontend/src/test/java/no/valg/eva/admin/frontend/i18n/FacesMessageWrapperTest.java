package no.valg.eva.admin.frontend.i18n;

import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FacesMessageWrapperTest extends BaseFrontendTest {

	private static final String PARAM_1 = "param1";
	private static final String PARAM_2 = "param2";
	private FacesMessageWrapper facesMessageWrapper;

	@BeforeMethod
	public void setUp() throws Exception {
		this.facesMessageWrapper = initializeMocks(FacesMessageWrapper.class);
	}

	@Test
	public void get_withString_verifyMessageProvider() {
		String s = "s";

		facesMessageWrapper.get(s);

		verify(getMessageProviderMock()).get(s);
	}

	@Test
	public void get_withStringAndString_verifyMessageProvider() {
		String s = "s";

		facesMessageWrapper.get(s, PARAM_1);

		verify(getMessageProviderMock()).get(eq(s), eq(PARAM_1));
	}

	@Test
	public void get_withStringStringAndString_verifyMessageProvider() {
		String s = "s";

		facesMessageWrapper.get(s, PARAM_1, PARAM_2);

		verify(getMessageProviderMock()).get(eq(s), eq(PARAM_1), eq(PARAM_2));
	}

	@Test
	public void getByElectionEvent_verifyMessageProvider() {
		String s = "s";

		facesMessageWrapper.getByElectionEvent(s, 1L);

		verify(getMessageProviderMock()).getByElectionEvent(eq(s), eq(1L));
	}

	@Test
	public void getMultiple() {
		when(getMessageProviderMock().get(anyString())).thenReturn("Hey");

		String result = facesMessageWrapper.getMultiple("@key.1@key.2");

		assertThat(result).isEqualTo("Hey. Hey. ");

	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void size() {
		facesMessageWrapper.size();
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void isEmpty() {
		facesMessageWrapper.isEmpty();
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void containsKey() {
		facesMessageWrapper.containsKey("");
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void containsValue() {
		facesMessageWrapper.containsValue("");
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void put() {
		facesMessageWrapper.put("", "");
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void remove() {
		facesMessageWrapper.remove("");
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void putAll() {
		facesMessageWrapper.putAll(new HashMap());
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void clear() {
		facesMessageWrapper.clear();
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void keySet() {
		facesMessageWrapper.keySet();
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void values() {
		facesMessageWrapper.values();
	}

	@Test(expectedExceptions = IllegalAccessError.class)
	public void entrySet() {
		facesMessageWrapper.entrySet();
	}
}
