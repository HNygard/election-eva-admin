package no.valg.eva.admin.test;

import no.evote.security.UserData;
import no.evote.util.MockUtils;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.util.Service;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import javax.enterprise.inject.Instance;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static java.time.Duration.between;
import static no.evote.util.MockUtils.getMock;
import static no.evote.util.MockUtils.hasMock;
import static no.evote.util.MockUtils.mockInjects;
import static no.evote.util.MockUtils.setPrivateField;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Abstract testclass that uses MockUtils to create mock for all injected fields in class.
 */
@SuppressWarnings("unchecked")
public abstract class MockUtilsTestCase {

	protected static final AreaPath AREA_PATH_ROOT = AreaPath.from("111111");
	protected static final AreaPath AREA_PATH_COUNTY = AreaPath.from("111111.22.33");
	protected static final AreaPath AREA_PATH_MUNICIPALITY = AreaPath.from("111111.22.33.4444");
	protected static final AreaPath AREA_PATH_BOROUGH = AreaPath.from("111111.22.33.4444.555555");
	protected static final AreaPath AREA_PATH_POLLING_DISTRICT = AreaPath.from("111111.22.33.4444.555555.6666");
	protected static final AreaPath AREA_PATH_POLLING_PLACE_ENVELOPE = AreaPath.from("111111.22.33.4444.555555.6666.0000");
	protected static final AreaPath AREA_PATH_POLLING_PLACE = AreaPath.from("111111.22.33.4444.555555.6666.7777");
	protected static final AreaPath AREA_PATH_POLLING_STATION = AreaPath.from("111111.22.33.4444.555555.6666.7777.88");
	protected static final ElectionPath ELECTION_PATH_ELECTION_EVENT = ElectionPath.from("111111");
	protected static final ElectionPath ELECTION_PATH_ELECTION_GROUP = ElectionPath.from("111111.22");
	protected static final ElectionPath ELECTION_PATH_ELECTION = ElectionPath.from("111111.22.33");
	protected static final ElectionPath ELECTION_PATH_CONTEST = ElectionPath.from("111111.22.33.444444");

	private Object testObject;
	private List<Object> mocks = new ArrayList<>();

	protected void initializeMocks() throws IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException {
		initializeMocks(null);
	}

	@SuppressWarnings("unchecked")
	protected <T> T initializeMocks(Class<T> cls) throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
		if (cls != null) {
			MockUtils.MockContent<T> mockContent = mockInjects(cls);
			this.testObject = mockContent.getInstance();
			mocks = mockContent.getMocks();
		}
		return (T) this.testObject;
	}

	protected <T> T initializeMocks(T t) throws NoSuchFieldException, IllegalAccessException {
		if (t != null) {
			this.testObject = t;
			mocks = mockInjects(t);
		}
		return t;
	}

	protected boolean hasInjectMock(Class clazz) {
		try {
			return hasMock(mocks, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> T getInjectMock(Class<T> mockClass) {
		try {
			return getMock(mocks, mockClass, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T mockField(String fieldName, Class<T> mockClass) throws NoSuchFieldException, IllegalAccessException {
		Object mock = mock(mockClass, RETURNS_DEEP_STUBS);
		setPrivateField(assertTestObject(), fieldName, mock);
		return (T) mock;
	}

	protected void mockFieldValue(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		setPrivateField(assertTestObject(), fieldName, value);
	}

	protected <T> Instance<T> mockInstance(String fieldName, Class<T> instanceGetClass) {
		Instance instance = mock(Instance.class);
		Object getClass = mock(instanceGetClass, RETURNS_DEEP_STUBS);
		when(instance.get()).thenReturn(getClass);
		try {
			setPrivateField(assertTestObject(), fieldName, instance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (Instance<T>) instance;
	}

	protected <T> T getMockedInstance(String fieldName, Class<T> instanceGetClass) throws NoSuchFieldException, IllegalAccessException {
		Instance<T> instance = getPrivateField(fieldName, Instance.class);
		return instance.get();
	}

	protected <T> T getMockedInstance(Object o, String fieldName, Class<T> instanceGetClass) throws NoSuchFieldException, IllegalAccessException {
		Instance<T> instance = getPrivateField(o, fieldName, Instance.class);
		return instance.get();
	}

	protected <T> T getPrivateField(String fieldName, Class<T> mockClass) throws NoSuchFieldException, IllegalAccessException {
		return (T) MockUtils.getPrivateField(assertTestObject(), fieldName);
	}

	protected <T> T getPrivateField(Object o, String fieldName, Class<T> mockClass) throws NoSuchFieldException, IllegalAccessException {
		return (T) MockUtils.getPrivateField(o, fieldName);
	}

	protected <T> T createMock(Class<T> mockClass) {
		if (mockClass.isEnum()) {
			return mockClass.getEnumConstants()[0];
		}
		return mock(mockClass, RETURNS_DEEP_STUBS);
	}

	protected <T> T anyOf(Class<T> type) {
		return createMock(type);
	}

	protected <T extends Enum<T>> T anyOf(T first, T... rest) {
		return anyOf(EnumSet.of(first, rest));
	}

	protected <T extends Enum<T>> T anyOf(EnumSet<T> enumSet) {
		return enumSet.stream().findFirst().orElse(null);
	}

	protected <T extends Enum<T>> T anyBut(T first, T... rest) {
		return anyBut(EnumSet.of(first, rest));
	}

	protected <T extends Enum<T>> T anyBut(EnumSet<T> enumSet) {
		return EnumSet.complementOf(enumSet).stream().findFirst().orElse(null);
	}

	protected <T> List<T> createListMock() {
		return mock(List.class);
	}

	protected <K, V> Map<K, V> createMapMock() {
		return mock(Map.class);
	}

	protected <T, R> Function<T, R> createFunctionMock() {
		return mock(Function.class);
	}

	protected Object assertTestObject() {
		if (testObject == null) {
			throw new RuntimeException("TestObject not set. Call setUp(Object) first!");
		}
		return testObject;
	}

	protected <T> T stub(Class<T> stubType) {
		return mock(stubType);
	}

	protected <T> T stub(Class<T> stubType, Answer defaultAnswer) {
		return mock(stubType, defaultAnswer);
	}

	protected Stubber on(final Call call) {
		return doAnswer(invocation -> {
			try {
				call.perform(invocation.getArguments());
			} catch (Exception e) {
				return "";
			}
			return "";
		});
	}

	protected void isUserSupport(UserData userData, boolean value) {
		when(userData.getOperatorRole().getRole().isUserSupport()).thenReturn(value);
	}

	public interface Call {
		void perform(Object[] args) throws Exception;
	}
	
	protected void expectException(Class exceptionClass, Service service) {
		boolean failed = false;
		try {
			service.execute();
		} catch (Exception e) {
			failed = true;
			if (e.getClass() != exceptionClass) {
				throw new RuntimeException(format("Expected exception of type %s, but got %s", exceptionClass, e.getClass()));
			}
		}
		if (!failed) {
			throw new RuntimeException(format("Expected code til fail with exception of type %s, but code did not throw exception", exceptionClass));
		}
	}
	
	protected void assertTimeDiffLessThan(LocalDateTime localDateTime1, LocalDateTime localDateTime2, long seconds){
		long diffInSeconds = between(localDateTime1, localDateTime2).getSeconds();
		assertTrue(diffInSeconds < seconds, "Timediff is bigger than expected limit: " + seconds + ", actual diff: " + diffInSeconds + ", ");
	}
}
