package no.valg.eva.admin.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;

/**
 * Creates an {@link EntityManagerFactory} from <code>{@value #PERSISTENCE_XML}</code>. Standard classloading rules are applied, and the first file that is
 * found is used. The module should provide its own <code>test/resources/META-INF/persistence.xml</code>.
 * <p/>
 * Creating an {@link EntityManagerFactory} is an expensive operation, so its reference should be kept as long as possible.
 * <p/>
 * NB: This class depends on Hibernate-internals, and is prone to break by future Hibernate updates.
 */
class EntityManagerFactoryUtil {
	public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

	private final TestPersistenceXmlAwareClassloader classloaderForTesting = new TestPersistenceXmlAwareClassloader();

	public EntityManagerFactory createEntityManagerFactory(final String persistenceUnitName) {
		Map<String, Collection<? extends ClassLoader>> integration = new HashMap<>();
		integration.put(AvailableSettings.CLASSLOADERS, Collections.singleton(classloaderForTesting));

		ParsedPersistenceXmlDescriptor persistenceUnit = new CustomPersistenceXmlParser(integration)
				.parsePersistenceXmlUsingHibernateInternals(classloaderForTesting.persistenceXml, persistenceUnitName);

		return Bootstrap.getEntityManagerFactoryBuilder(persistenceUnit, integration, classloaderForTesting).build();
	}

	private static final class TestPersistenceXmlAwareClassloader extends ClassLoader {
		private final URL persistenceXml;

        private TestPersistenceXmlAwareClassloader() {
			persistenceXml = persistenceXmlUrl();
			if (persistenceXml == null) {
				throw new RuntimeException("Could not find resource on classpath: " + PERSISTENCE_XML);
			}
		}

		@Override
		public Enumeration<URL> getResources(final String name) throws IOException {
			if ("META-INF/persistence.xml".equals(name)) {
				return Collections.enumeration(Arrays.asList(persistenceXmlUrl()));
			}
			return super.getResources(name);
		}

		private URL persistenceXmlUrl() {
			try {
				Enumeration<URL> resources = getClass().getClassLoader().getResources(PERSISTENCE_XML);
				if (resources.hasMoreElements()) {
					return resources.nextElement();
				} else {
					throw new RuntimeException("Unable to find " + PERSISTENCE_XML);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Wraps the Hibernate-internal {@link PersistenceXmlParser}, to enable loading of one specific <code>persistence.xml</code>.
	 */
	private static class CustomPersistenceXmlParser {
		private final PersistenceXmlParser persistenceXmlParser;
		private final Map integration;

		public CustomPersistenceXmlParser(final Map integration) {
			this.persistenceXmlParser = new PersistenceXmlParser(
					ClassLoaderServiceImpl.fromConfigSettings(integration),
					PersistenceUnitTransactionType.RESOURCE_LOCAL
					);
			this.integration = integration;
		}

		ParsedPersistenceXmlDescriptor parsePersistenceXmlUsingHibernateInternals(final URL persistenceXmlUrl, final String persistenceUnitName) {
			try {
				Method parsePersistenceXmlMethod = PersistenceXmlParser.class.getDeclaredMethod("parsePersistenceXml", URL.class, Map.class);
				parsePersistenceXmlMethod.setAccessible(true);
				List<ParsedPersistenceXmlDescriptor> persistenceUnits = (List<ParsedPersistenceXmlDescriptor>) parsePersistenceXmlMethod.invoke(
						persistenceXmlParser, persistenceXmlUrl, integration);
				for (ParsedPersistenceXmlDescriptor persistenceUnit : persistenceUnits) {
					if (persistenceUnit.getName().equals(persistenceUnitName)) {
						return persistenceUnit;
					}
				}
				throw new RuntimeException("Did not find persistence unit " + persistenceUnitName);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
