package no.evote.service.producer;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.sf.ehcache.Cache;
import no.evote.presentation.cache.EntityCache;
import no.evote.presentation.cache.GenericCacheManager;
import no.evote.service.cache.ServiceInvocationHandler;

/**
 * Produces an EJB wrapped in a client side proxy, see {@link ServiceInvocationHandler}}
 * @param <T> The class that is to be wrapped and produced
 */
@SuppressWarnings({ "unchecked" })
@ApplicationScoped
public abstract class AbstractServiceProducer<T> implements Serializable {

	private T wrappedService;

	@Inject
	private EntityCache entityCache;

	@Inject
	private GenericCacheManager genericCacheManager;

	private Cache serviceInvocationCache;

	private T wrapInProxy(String serviceName) {
		Class<T> klass = returnedClass();
		serviceInvocationCache = genericCacheManager.getCache("serviceInvocationCache");
		InvocationHandler handler = new ServiceInvocationHandler(klass, serviceName, entityCache, serviceInvocationCache);
		return (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[] { klass }, handler);
	}

	/**
	 * Returns a service instance wrapped in a proxy object that handles EJB lookups
	 */
	protected T produceService() {
		return produceService(null);
	}

	/**
	 * Returns a service instance wrapped in a proxy object that handles EJB lookups
	 */
	protected T produceService(String serviceName) {
		if (wrappedService == null) {
			wrappedService = wrapInProxy(serviceName);
		}
		return wrappedService;
	}

	/**
	 * Find a types class. This won't handle every case, but should be sufficient for us.
	 * @return The type's class
	 */
	public Class<T> returnedClass() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}

	@PreDestroy
	public void shutdown() {
		serviceInvocationCache.dispose();
	}

}
