package no.evote.service.security;

import java.security.Provider;
import java.security.Security;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * This EJB reorders the JCA providers upon startup, as a fix for EVAADMIN-410.
 */
@Startup
@Singleton
public class ReorderJcaProvidersBean {
	@PostConstruct
	public void makeBouncyCastleLastJcaProvider() {
		Provider bouncyCastle = Security.getProvider("BC");
		if (bouncyCastle != null) {
			Security.removeProvider(bouncyCastle.getName());
			Security.addProvider(bouncyCastle);
		}
	}
}
