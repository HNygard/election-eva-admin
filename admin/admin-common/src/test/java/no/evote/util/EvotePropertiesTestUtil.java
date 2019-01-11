package no.evote.util;

/**
 * Kun til testformål
 */
public final class EvotePropertiesTestUtil {

	private EvotePropertiesTestUtil() {
		// Unngå instansiering av denen klassen
	}
	
	public static void reinitializeProperties() {
		EvoteProperties.clearProperties();
		EvoteProperties.readProperties();
	}
	
	public static void setProperty(String property, String value) {
		EvoteProperties.setProperty(property, value);
	}
}
