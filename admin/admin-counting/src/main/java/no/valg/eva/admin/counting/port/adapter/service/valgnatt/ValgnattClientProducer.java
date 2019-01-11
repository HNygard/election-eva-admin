package no.valg.eva.admin.counting.port.adapter.service.valgnatt;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.evote.util.EvoteProperties.VALGNATT_BASE_URL;
import static no.evote.util.EvoteProperties.VALGNATT_CONNECTION_TIMEOUT_SECONDS;
import static no.evote.util.EvoteProperties.VALGNATT_CONTEXT;

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

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Produces a REST client for Valgnatt REST API, defined in {@link ValgnattApi}
 */
@ApplicationScoped
public class ValgnattClientProducer {
	private static final Logger LOGGER = Logger.getLogger(ValgnattClientProducer.class);

	private String baseUrl = "http://localhost:8081/";
	private String context = "valgnatt";
	private long timeout;

	@Inject
	public ValgnattClientProducer(
			@EvaConfigProperty @Named(VALGNATT_BASE_URL) final String baseUrl,
			@EvaConfigProperty @Named(VALGNATT_CONTEXT) final String context,
			@EvaConfigProperty @Named(VALGNATT_CONNECTION_TIMEOUT_SECONDS) long timeout) {
		this(baseUrl);
		this.context = context;
		this.timeout = timeout;
	}

	public ValgnattClientProducer(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@PostConstruct
	public void init() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Produces
	public ValgnattApi createValgnattApi() {
		return buildProxy(restEasyClientBuilderWithTimeout(), ValgnattApi.class);
	}

	<T extends ValgnattApi> T buildProxy(ResteasyClientBuilder clientBuilderWithTimeout, Class<T> proxyInterfaceClass) {
		return clientBuilderWithTimeout
				.build()
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

	private ResteasyClientBuilder restEasyClientBuilderWithTimeout() {
		return new ResteasyClientBuilder()
				.establishConnectionTimeout(timeout, SECONDS)
				.socketTimeout(timeout, SECONDS);
	}
}
