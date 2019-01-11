
package no.valg.eva.admin.util;

import java.util.Properties;

public final class PropertyUtil {

	private PropertyUtil() {
	}

	public static Properties addPrefixToPropertyKeys(String prefix, Properties propertiesWithoutPrefix) {
		Properties propertiesWithPrefix = new Properties();
		propertiesWithoutPrefix.forEach((k, v) -> propertiesWithPrefix.setProperty(prefix + k, (String) v));
		return propertiesWithPrefix;
	}

}
