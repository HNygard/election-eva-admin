package no.valg.eva.admin.frontend.configuration;

/**
 * Modus for en konfigurasjonsfane. Dekker hvilket modus som foregår inni fanen, men ikke om fanen som helhet
 * er ferdigstilt eller ikke.
 */
public enum ConfigurationMode {
	CREATE,
	READ,
	UPDATE,
	DELETE,
	SEARCH
}
