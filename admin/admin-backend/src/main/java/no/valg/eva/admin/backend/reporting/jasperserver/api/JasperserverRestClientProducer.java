package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.evote.util.EvoteProperties.JASPERSERVER_BASE_URL;
import static no.evote.util.EvoteProperties.JASPERSERVER_CONNECTION_TIMEOUT_SECONDS;
import static no.evote.util.EvoteProperties.JASPERSERVER_CONTEXT;
import static no.evote.util.EvoteProperties.JASPERSERVER_PASSWORD;
import static no.evote.util.EvoteProperties.JASPERSERVER_USERNAME;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.backend.reporting.jasperserver.SimpleSessionMaintainingFilter;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.cache.CacheInterceptor;
import org.jboss.resteasy.client.jaxrs.cache.LightweightBrowserCache;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Produces a REST client for a sub set of JasperServer REST API, defined in {@link JasperRestApi}
 */
@ApplicationScoped
public class JasperserverRestClientProducer {
	private static final Logger LOGGER = Logger.getLogger(JasperserverRestClientProducer.class);

	private String username = "jasperadmin";
	private String password = "jasperadmin";
	private String baseUrl = "http://localhost:8081/";
	private String context = "jasperserver";
	private long timeout;

	@Inject
	public JasperserverRestClientProducer(
			@EvaConfigProperty @Named(JASPERSERVER_USERNAME) final String username,
			@EvaConfigProperty @Named(JASPERSERVER_PASSWORD) final String password,
			@EvaConfigProperty @Named(JASPERSERVER_BASE_URL) final String baseUrl,
			@EvaConfigProperty @Named(JASPERSERVER_CONTEXT) final String context,
			@EvaConfigProperty @Named(JASPERSERVER_CONNECTION_TIMEOUT_SECONDS) long timeout) {
		this(username, password, baseUrl);
		this.context = context;
		this.timeout = timeout;
	}

	public JasperserverRestClientProducer(final String username, final String password, final String baseUrl) {
		this.username = username;
		this.password = password;
		this.baseUrl = baseUrl;
	}

	@PostConstruct
	public void init() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Produces
	public JasperRestApiNoTimeout createJasperRestApiNoTimeout() {
		return buildProxy(restEasyClientBuilderWithNoTimeout(), JasperRestApiNoTimeout.class);
	}

	@Produces
	public JasperRestApiWithTimeout createJasperRestApiWithTimeOut() {
		return buildProxy(restEasyClientBuilderWithTimeout(), JasperRestApiWithTimeout.class);
	}

	<T extends JasperRestApi> T buildProxy(ResteasyClientBuilder clientBuilderWithTimeout, Class<T> proxyInterfaceClass) {
		return clientBuilderWithTimeout
				.build()
				.register(new BasicAuthentication(username, password))
				.register(new CacheInterceptor(new LightweightBrowserCache()))
				.register(new SimpleSessionMaintainingFilter())
				.register(new ClientRequestFilter() {
					@Override
					public void filter(final ClientRequestContext requestContext) throws IOException {
						try {
							requestContext.setUri(new URI(URLDecoder.decode(requestContext.getUri().toString(), "UTF-8")));
						} catch (URISyntaxException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
				})
				.target(baseUrl)
				.path(context)
				.proxy(proxyInterfaceClass);
	}

	private ResteasyClientBuilder restEasyClientBuilderWithNoTimeout() {
		return new ResteasyClientBuilder();
	}

	private ResteasyClientBuilder restEasyClientBuilderWithTimeout() {
		return new ResteasyClientBuilder()
				.establishConnectionTimeout(timeout, SECONDS)
				.socketTimeout(timeout, SECONDS);
	}
}
