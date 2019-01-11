package no.evote.service.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import no.evote.util.EvoteProperties;

/**
 * Replaces host name in WSDL file with whatever is specified as DeployURL in evote.properties.
 */
@WebFilter(urlPatterns = { "/GenericFileDownloadWSService", "/GenericFileUploadWSService"})
public class WSDLRewriter implements Filter {

	/**
	 * Wrapper for catching any output.
	 */
	public static final class WsdlResponseWrapper extends HttpServletResponseWrapper {

		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		private final PrintWriter printWriter = new PrintWriter(outputStream);

		public WsdlResponseWrapper(final HttpServletResponse response) {
			super(response);
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new ServletOutputStream() {

				@Override
				public void write(final int b) throws IOException {
					outputStream.write(b);
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(final WriteListener arg0) {
				}

			};
		}

		public byte[] getOutputStreamContent() {
			return outputStream.toByteArray();
		}

		@Override
		public PrintWriter getWriter() {
			return printWriter;
		}

		public String getWriterContent() {
			printWriter.close();
			return new String(outputStream.toByteArray());
		}
	}

	private String requestedHostname;

	public WSDLRewriter() {
		requestedHostname = EvoteProperties.getProperty(EvoteProperties.DEPLOY_URL);
		if (!requestedHostname.endsWith("/")) {
			requestedHostname += "/";
		}
	}

	@Override
	public void destroy() {
		// To conform with interface
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		// We only want to replace host name when the WSDL is downloaded, so check for the wsdl parameter
		if (request.getParameter("wsdl") != null) {
			WsdlResponseWrapper myResponse = new WsdlResponseWrapper((HttpServletResponse) response);
			filterChain.doFilter(request, myResponse);
			boolean isResponseOutputStream = myResponse.getOutputStreamContent().length > 0;

			StringBuilder responseBuffer;
			if (isResponseOutputStream) {
				responseBuffer = new StringBuilder(new String(myResponse.getOutputStreamContent()));
			} else {
				responseBuffer = new StringBuilder(myResponse.getWriterContent());
			}

			if (request instanceof HttpServletRequest && (null != requestedHostname) && !requestedHostname.isEmpty()) {
				// Find attributes matching ocation=", such as schemaLocation and location, and replace them with deploy URL:
				int index = responseBuffer.indexOf("ocation=\"");
				while (index > -1) {
					
					int endIndex = responseBuffer.indexOf("/", index + 17) + 1;
					responseBuffer.replace(index + 9, endIndex, requestedHostname);
					index = responseBuffer.indexOf("ocation=\"", endIndex);
					
				}
			}

			// Forward the response
			if (isResponseOutputStream) {
				response.getOutputStream().write(responseBuffer.toString().getBytes());
			} else {
				response.getWriter().print(responseBuffer);
			}

		} else {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		// To conform with interface
	}

}
