package no.valg.eva.admin.backend.reporting.jasperserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.NewCookie;

/**
 * A simple client side filter that collects all cookies received and send them back with all client requests
 */
public class SimpleSessionMaintainingFilter implements ClientRequestFilter, ClientResponseFilter {

	private static Map<String, NewCookie> cookies = new HashMap<>();

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		List<Object> newCookies = new ArrayList<>();
		for (Map.Entry<String, NewCookie> cookieEntry : cookies.entrySet()) {
			if (!cookieExpired(cookieEntry)) {
				newCookies.add(cookieEntry.getValue().getName() + "=" + cookieEntry.getValue().getValue());
			}
		}
		requestContext.getHeaders().put("Cookie", newCookies);
	}

	private boolean cookieExpired(Map.Entry<String, NewCookie> cookieEntry) {
		Date expiry = cookieEntry.getValue().getExpiry();
		return expiry != null && expiry.before(new Date());
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		cookies.putAll(responseContext.getCookies());
	}
}
