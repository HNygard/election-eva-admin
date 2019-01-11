package no.valg.eva.admin.frontend.common;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Named
@ApplicationScoped
public class UserAgentParser implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(UserAgentParser.class);
	private static final String CACHE_DIR = System.getProperty("java.io.tmpdir");
	private UserAgentStringParser parser = null;
	
	private final Cache<String, ReadableUserAgent> cache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterWrite(2, TimeUnit.HOURS)
			.build();
	

	@PostConstruct
	public void init() {
		// Due to EVAADMIN-1953, we always delete the cached uas.xml file on application start to make we get a fresh one.
		File file = new File(CACHE_DIR, "uas.xml");
		if (file.exists()) {
			LOGGER.info("Deleting " + file.getAbsolutePath() + ": " + file.delete());
		}
		parser = UADetectorServiceFactory.getCachingAndUpdatingParser();
	}

	public UserAgent parse(final FacesContext facesContext) {
		return parse((HttpServletRequest) facesContext.getExternalContext().getRequest());
	}

	public UserAgent parse(final HttpServletRequest request) {
		return parse(request.getHeader("User-Agent"));
	}

	public UserAgent parse(final String userAgentString) {
		ReadableUserAgent result = cache.getIfPresent(userAgentString);
		if (result == null) {
			result = parser.parse(userAgentString);
			cache.put(userAgentString, result);
		}
		return new UserAgent(result);
	}

	public void shutdown() {
		parser.shutdown();
	}

}
