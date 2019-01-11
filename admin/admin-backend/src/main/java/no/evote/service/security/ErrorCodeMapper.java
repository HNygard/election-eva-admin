package no.evote.service.security;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteInitiationException;
import no.evote.exception.EvoteSecurityException;

public final class ErrorCodeMapper {
	private static final String DEFAULT = "999";
	private static final String SECURITY = "111";
	private static final String INIT = "222";
	private static final String APPLICATION = "555";

	public ErrorCodeMapper() {
	}

	public String map(final Exception e) {
		if (e instanceof EvoteSecurityException) {
			return SECURITY;
		}

		if (e instanceof EvoteInitiationException) {
			return INIT;
		}
		
		if (e instanceof EvoteException) {
			return APPLICATION;
		}

		return DEFAULT;
	}
}
