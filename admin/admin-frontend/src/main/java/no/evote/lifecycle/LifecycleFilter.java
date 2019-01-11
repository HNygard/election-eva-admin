package no.evote.lifecycle;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.evote.service.security.SystemPasswordService;
import no.valg.eva.admin.frontend.status.StatusPropertiesProvider;

import org.apache.log4j.Logger;

public class LifecycleFilter implements Filter {

	private final Logger log = Logger.getLogger(LifecycleFilter.class);

	@Inject
	private SystemPasswordService passwordService;

	@Inject
	private StatusPropertiesProvider statusPropertiesProvider;
	
	private ApplicationStatus appStatus = ApplicationStatus.UNKNOWN;

	@Override
	public void destroy() {
		// To conform with interface
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		if (appStatus != ApplicationStatus.ENABLED) {
			tryToEnableApplication();
		}

		if (appStatus == ApplicationStatus.ENABLED) {
			chain.doFilter(req, res);
		} else {
			String errorMessage;

			switch (appStatus) {
			case MISSING_SCANNING_COUNT_CERT:
				errorMessage = "Missing scanning count certificate, unable to continue.";
				break;
			case SYSTEM_PASSPHRASE_NOT_SET:
				errorMessage = "System passphrase has not been entered, unable to continue.";
				break;
			case DIFFERENT_VERSION_ON_FRONTEND_AND_BACKEND:
				errorMessage = "Frontend and backend versions are different, unable to continue.";
				break;
			default:
				errorMessage = "Unknown error, unable to continue. See error logs for more information..";
				break;
			}

			res.reset();
			res.setContentType("text/plain");
			PrintWriter writer = res.getWriter();
			writer.append(errorMessage);
			writer.flush();
		}
	}

	private void tryToEnableApplication() {
		if (checkRequiredCertificates() && systemPassphraseHasBeenSet() && sameVersionOnFrontendAndBackend()) {
			appStatus = ApplicationStatus.ENABLED;
		}
	}

	private boolean checkRequiredCertificates() {
		log.debug("Checking required certificates. Which is none (for the moment).");
		return true;
	}

	private boolean systemPassphraseHasBeenSet() {
		log.debug("Checking if system passphrase has been set.");
		if (!passwordService.isPasswordSet()) {
			log.debug("System passphrase not set.");
			appStatus = ApplicationStatus.SYSTEM_PASSPHRASE_NOT_SET;
			return false;
		}
		return true;
	}

	private boolean sameVersionOnFrontendAndBackend() {
		log.debug("Checking if frontend and backend versions match.");

		Properties statusProperties = statusPropertiesProvider.getStatusProperties();
		String frontendVersion = statusProperties.getProperty("frontend-version");
		String backendVersion = statusProperties.getProperty("backend-version");

		log.debug("Frontend version is " + frontendVersion + ". Backend version is: " + backendVersion);
		boolean isSameVersion = frontendVersion != null && frontendVersion.equals(backendVersion);
		
		if (!isSameVersion) {
			appStatus = ApplicationStatus.DIFFERENT_VERSION_ON_FRONTEND_AND_BACKEND;
		}
		
		return isSameVersion;
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		tryToEnableApplication();
	}

	private static enum ApplicationStatus {
		MISSING_SCANNING_COUNT_CERT, SYSTEM_PASSPHRASE_NOT_SET, DIFFERENT_VERSION_ON_FRONTEND_AND_BACKEND, ENABLED, UNKNOWN
	}
}
