package no.evote.presentation.cache;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Hjelpeklasse for instansiering av ehcache-konfigurasjonen.
 * 
 * Støttet tidligere cluster-cache, men dette har ikke vært i bruk, og er derfor tatt bort.
 */
@ApplicationScoped
public class GenericCacheManager implements Serializable {

	private static final Logger LOG = Logger.getLogger(GenericCacheManager.class);
	private transient CacheManager manager;

	@PostConstruct
	public void init() {
		LOG.debug("init()");

		LOG.debug("Bruker lokal cache (eneste typen støttet p.t.");
		CacheManager.create(getClass().getClassLoader().getResourceAsStream("ehcache-local.xml"));
		manager = CacheManager.getInstance();
	}

	@PreDestroy
	public void shutdown() {
		manager.shutdown();
	}

	public Cache getCache(final String name) {
		return manager.getCache(name);
	}

}
