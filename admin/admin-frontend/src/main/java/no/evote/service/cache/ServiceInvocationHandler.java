package no.evote.service.cache;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.MarshalException;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import no.evote.model.BaseEntity;
import no.evote.presentation.cache.EntityCache;
import no.evote.security.UserData;
import no.valg.eva.admin.util.ServiceLookupUtil;

import org.apache.log4j.Logger;

/**
 * This class represents client side proxies for our services/EJB's. The proxies are used for logging, caching and failover.
 */
public class ServiceInvocationHandler implements InvocationHandler, Serializable {

	private static final Logger LOG = Logger.getLogger(ServiceInvocationHandler.class);
	private final Class<?> lookupClass;
	private final String serviceName;
	private final EntityCache entityCache;
	private final Cache serviceInvocationCache;
	private Object impl = null;

	public ServiceInvocationHandler(final Class<?> lookupClass, String serviceName, final EntityCache entityCache, final Cache serviceInvocationCache) {
		this.lookupClass = lookupClass;
		this.serviceName = serviceName;
		this.entityCache = entityCache;
		this.serviceInvocationCache = serviceInvocationCache;
	}

	/**
	 * This is the invocation point of all EJB's in the backend. This method checks whether the result is/can be cached, does logging and failover.
	 * "Grunnlagsdata" i.e. data that is never changed, is marked by the @Cacheable annotation. In addition there are methods marked with @CacheInvalidate,
	 * which invalidates the entity cache, a separate cache for non-static entities that are frequently used.
	 */
	public Object invoke(final Object proxy, final Method method, final Object[] args, final boolean retry) throws Throwable {
		if (impl == null) {
			impl = lookupImpl();
		}

		// Check whether result is in cache
		String cacheKey = null;
		boolean cacheable = isCacheable(method);

		if (cacheable) {
			Element result = null;
			cacheKey = generateCacheKey(method, args);
			if (serviceInvocationCache.isKeyInCache(cacheKey)) {
				result = serviceInvocationCache.get(cacheKey);
			}

			if (result != null) {
				return result.getObjectValue();
			}
		}

		// Check whether this invocation should invalidate an entry in the eni
		if (isCacheInvalidator(method)) {
			CacheInvalidate annotation = method.getAnnotation(CacheInvalidate.class);
			Class<?> entityClass = annotation.entityClass();
			Object entityParam = args[annotation.entityParam()];
			Long invalidateKey = null;

			if (entityParam instanceof BaseEntity) {
				invalidateKey = ((BaseEntity) entityParam).getPk();
			} else if (entityParam instanceof Long) {
				invalidateKey = (Long) entityParam;
			}
			entityCache.remove(entityClass, invalidateKey);
		}

		try {
			// Invoke method and put result in cache if it is cacheable
			Object result = method.invoke(impl, args);
			if (cacheable) {
				serviceInvocationCache.put(new Element(cacheKey, result));
			}
			return result;
		} catch (final InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();

			if (targetException instanceof NoSuchEJBException
                    || (targetException instanceof EJBException && ((EJBException) targetException).getCausedByException() instanceof MarshalException)) {
                if (retry) {
                    // Force re-lookup of the EJB and try calling the method again
                    LOG.warn("Detected stale instance of " + lookupClass.toString() + ", attempting to get new and retrying", ex);
                    impl = null;
                    return invoke(proxy, method, args, false);
                } else {
                    // Error was unrecoverable, set impl to null so that we look it up again on next invocation
                    LOG.warn("Failed retrying on " + lookupClass.toString() + ", giving up", ex);
                    impl = null;
                }
            }

			throw targetException;
		}

	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		return invoke(proxy, method, args, true);
	}

	private boolean isCacheable(final Method method) {
		return method.getAnnotation(Cacheable.class) != null;
	}

	private boolean isCacheInvalidator(final Method method) {
		return method.getAnnotation(CacheInvalidate.class) != null;
	}

	/**
	 * Generate a cache key that is unique for each method with parameters
	 */
	private String generateCacheKey(final Method method, final Object[] args) {
		StringBuilder cacheKey = new StringBuilder();
		if (serviceName == null) {
			cacheKey.append(lookupClass.getSimpleName());
		} else {
			cacheKey.append(serviceName);
		}
		cacheKey.append(".");
		cacheKey.append(method.getName());

		if (args != null) {
			for (Object param : args) {
				if (param != null && !(param instanceof UserData)) {
					cacheKey.append(',');
					cacheKey.append(param.toString());
				}
			}
		}
		return cacheKey.toString();
	}
	
	Object lookupImpl() {
		return ServiceLookupUtil.lookupService(lookupClass, serviceName);
	}

}
