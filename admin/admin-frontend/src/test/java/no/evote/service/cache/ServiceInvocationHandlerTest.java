package no.evote.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.NoSuchEJBException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import no.evote.presentation.cache.EntityCache;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ServiceInvocationHandlerTest extends BaseFrontendTest {

	private EntityCache entityCacheMock;
	private Cache cache;
	private TestService service;

	@BeforeMethod
	public void setup() {
		entityCacheMock = createMock(EntityCache.class);
		CacheConfiguration conf = new CacheConfiguration();
		conf.setName("cache");
		cache = new Cache(conf);
		cache.initialise();
		service = new TestService();
	}

	@Test
	public void invoke_withCacheMethod_verifyCachedValue() throws Throwable {
		ServiceInvocationHandler handler = initializeMocks(new TestServiceInvocationHandler());

		Object result = handler.invoke(null, service.getClass().getMethod("cacheMethod", long.class), new Object[] { 1L });

		assertThat(result).isEqualTo("cacheMethodResult 1");
		Cache cache = getPrivateField("serviceInvocationCache", Cache.class);
		assertThat(cache.isKeyInCache("TestService.cacheMethod,1")).isTrue();
	}

	@Test
	public void invoke_withCacheInvalidateMethodCalledTwice_verifyCacheInvalidationAndCachePutOnlyOnce() throws Throwable {
		ServiceInvocationHandler handler = initializeMocks(new TestServiceInvocationHandler());

		Object result1 = handler.invoke(null, service.getClass().getMethod("cacheInvalidateMethod", long.class), new Object[] { 1L });
		Object result2 = handler.invoke(null, service.getClass().getMethod("cacheInvalidateMethod", long.class), new Object[] { 1L });

		assertThat(result1).isEqualTo("cacheInvalidateMethod 1");
		assertThat(result1).isEqualTo(result2);
		verify(entityCacheMock, times(1)).remove(ElectionEvent.class, 1L);
		Cache cache = getPrivateField("serviceInvocationCache", Cache.class);
		assertThat(cache.isKeyInCache("TestService.cacheInvalidateMethod,1")).isTrue();

	}

	@Test
	public void invoke_withElectionEventMethod_verifyCacheInvalidationAndCachePutOnlyOnce() throws Throwable {
		ServiceInvocationHandler handler = initializeMocks(new TestServiceInvocationHandler());

		ElectionEvent event = new ElectionEvent();
		event.setPk(1L);
		Object result = handler.invoke(null, service.getClass().getMethod("electionEventMethod", ElectionEvent.class), new Object[] { event });

		assertThat(result).isEqualTo("electionEventMethod ElectionEvent[pk=1]");
		verify(entityCacheMock, times(1)).remove(ElectionEvent.class, 1L);
		Cache cache = getPrivateField("serviceInvocationCache", Cache.class);
		assertThat(cache.isKeyInCache("TestService.electionEventMethod,ElectionEvent[pk=1]")).isTrue();
	}

	@Test(expectedExceptions = NoSuchMethodException.class)
	public void invoke_withNoExistingMethod() throws Throwable {
		ServiceInvocationHandler handler = initializeMocks(new TestServiceInvocationHandler());

		handler.invoke(null, service.getClass().getMethod("notExistingMethod", long.class), new Object[] { 1L });
	}

	@Test
	public void invoke_withNoSuchEJBExceptionMethod_verifyOneCacheInvalidationBecauseOfRetry() throws Throwable {
		ServiceInvocationHandler handler = initializeMocks(new TestServiceInvocationHandler());

		try {
			handler.invoke(null, service.getClass().getMethod("noSuchEJBExceptionMethod", long.class), new Object[] { 1L });
		} catch (NoSuchEJBException e) {
			verify(entityCacheMock, times(2)).remove(ElectionEvent.class, 1L);
			return;
		}
		fail("Expected InvocationTargetException did not occur");
	}

	public class TestService {
		@Cacheable
		public String cacheMethod(long input) {
			return "cacheMethodResult " + input;
		}

		@Cacheable
		@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 0)
		public String cacheInvalidateMethod(long input) {
			return "cacheInvalidateMethod " + input;
		}

		@Cacheable
		@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 0)
		public String electionEventMethod(ElectionEvent input) {
			return "electionEventMethod " + input;
		}

		@Cacheable
		@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 0)
		public String noSuchEJBExceptionMethod(long input) throws InvocationTargetException {
			throw new NoSuchEJBException(String.valueOf(input));
		}
	}

	public class TestServiceInvocationHandler extends ServiceInvocationHandler {
		public TestServiceInvocationHandler() {
			super(service.getClass(), null, entityCacheMock, cache);
		}

		@Override
		Object lookupImpl() {
			return service;
		}
	}

}
