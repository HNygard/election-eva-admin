package no.valg.eva.admin.util;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

public final class ServiceLookupUtil {

	private static final Logger LOG = Logger.getLogger(ServiceLookupUtil.class);

	private ServiceLookupUtil() {
	}

	public static <T> T lookupService(final Class<T> clazz) {
		return lookupService(clazz, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T lookupService(final Class<T> clazz, String serviceName) {
		T service;

		try {
			Properties props = new Properties();
			props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			props.put("jboss.naming.client.ejb.context", true);

			InitialContext context = new InitialContext(props);

			String jndiName = remoteJndiNameForService(clazz, serviceName);

			service = (T) context.lookup(jndiName);
		} catch (NamingException ne) {
			throw new IllegalStateException(ne);
		}

		return service;
	}

	@SuppressWarnings("unchecked")
	public static <T> T lookupServiceForTest(final Class<T> clazz, final Context context) {
		T service;

		try {
			String jndiName = remoteJndiNameForService(clazz, null);
			service = (T) context.lookup(jndiName);
		} catch (NamingException ne) {
			throw new IllegalStateException(ne);
		}

		return service;
	}

	private static <T> String remoteJndiNameForService(Class<T> clazz, String serviceName) {
		String jndiName;
		if (serviceName == null) {
			jndiName = "ejb:/admin-backend//" + clazz.getSimpleName() + "!" + clazz.getCanonicalName();
			LOG.debug("Looking up backend service " + clazz.getSimpleName() + " with JNDI name " + jndiName);
		} else {
			jndiName = "ejb:/admin-backend//" + serviceName + "!" + clazz.getCanonicalName();
			LOG.debug("Looking up backend service " + serviceName + " with JNDI name " + jndiName);
		}
		return jndiName;
	}

}
