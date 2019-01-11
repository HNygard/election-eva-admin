package no.evote.presentation.cache;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import no.evote.service.GenericService;

import org.apache.log4j.Logger;

/**
 * This is a generic base cache, an abstraction over EHCache.
 */
@ApplicationScoped
public class EntityCache implements Serializable {

	private static final Logger LOG = Logger.getLogger(EntityCache.class);
	private transient Cache cache;

	@Inject
	private GenericService genericService;

	@Inject
	private GenericCacheManager genericCacheManager;

	@PostConstruct
	public void init() {
		cache = genericCacheManager.getCache("entityCache");
	}

	@PreDestroy
	public void shutdown() {
		cache.dispose();
	}

	/**
	 * Lookup object in cache with primary key
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> T get(UserData userData, Class<T> entityClass, Long pk) {
		String cacheKey = getCacheKey(entityClass, pk);
		Element cacheElem = cache.get(cacheKey);

		if (cacheElem != null) {
			LOG.trace("Cache hit: " + cacheKey);
			return (T) cacheElem.getValue();
		} else {
			T value = (T) genericService.findByPk(userData, entityClass, pk);
			cache.put(new Element(cacheKey, value));
			LOG.trace("Cache miss: " + cacheKey);
			return value;
		}
	}

	/**
	 * Generate a unique cache key
	 */
	private String getCacheKey(Class<?> entityClass, Long pk) {
		return entityClass.getSimpleName() + "-" + pk;
	}

	/**
	 * Remove object from cache with primary key
	 */
	public void remove(Class<?> entityClass, Long pk) {
		LOG.trace("Cache invalidate: " + getCacheKey(entityClass, pk));
		cache.remove(getCacheKey(entityClass, pk));
	}

}
