package no.evote.service.util;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import no.evote.logging.SQLCollectorAppender;
import no.evote.util.EvoteProperties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SQLCollectorInterceptor implements Serializable {
	private final Logger log = Logger.getLogger(SQLCollectorInterceptor.class);
	private final SQLCollectorAppender sqlCollectorAppender = new SQLCollectorAppender();
	private boolean disabled = true;

	@PostConstruct
	public void init() {
		disabled = !Boolean.parseBoolean(EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_SERVICE_UTIL_SQLCOLLECTOR_INTERCEPTOR_ENABLED, Boolean.FALSE));
	}

	@AroundInvoke
	public Object aroundInvoke(final InvocationContext ctx) throws Exception {
		if (disabled) {
			return ctx.proceed();
		}

		configureLogger();

		sqlCollectorAppender.setContext(ctx);
		try {
			return ctx.proceed();
		} finally {
			log.debug("[Method: " + sqlCollectorAppender.getCalledMethod() + "]");
			if (sqlCollectorAppender.hasSQL()) {
				log.debug("[SQL: " + sqlCollectorAppender.getSQLReport() + "]");
			}
		}
	}

	private void configureLogger() {
		Logger hibernateSQLLogger = Logger.getLogger("org.hibernate.SQL");
		hibernateSQLLogger.setAdditivity(false);
		hibernateSQLLogger.setLevel(Level.DEBUG);
		hibernateSQLLogger.removeAllAppenders();
		hibernateSQLLogger.addAppender(sqlCollectorAppender);
	}
}
