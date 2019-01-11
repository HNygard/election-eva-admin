package no.valg.eva.admin.frontend.security;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpGetter {
	private static final Logger LOGGER = Logger.getLogger(HttpGetter.class);
	private static final int DEFAULT_MAX_PER_ROUTE = 20;
	private final CloseableHttpClient httpClient;

	public HttpGetter() {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
		httpClient = HttpClients.custom()
				.setConnectionManager(cm)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.build();
	}

	public byte[] getResponse(final String url) throws IOException {
		HttpGet getMethod = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(getMethod)) {
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode != HttpServletResponse.SC_OK) {
				throw new RuntimeException("Got response " + responseCode + " from " + url);
			}

			HttpEntity messageEntity = response.getEntity();
			return EntityUtils.toByteArray(messageEntity);
		}
	}

	public void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
			LOGGER.info("Failed closing http client", e);
		}
	}
}
