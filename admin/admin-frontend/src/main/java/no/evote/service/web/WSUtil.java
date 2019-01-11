package no.evote.service.web;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

public final class WSUtil {

	private WSUtil() {
	}

	public static InetAddress getClientAddress(final WebServiceContext wsContext) throws UnknownHostException {
		MessageContext mc = wsContext.getMessageContext();
		String ipAddress = ((HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST)).getRemoteAddr();
		return InetAddress.getByName(ipAddress);
	}
}
