package no.valg.eva.admin.util;

import org.apache.commons.lang3.ObjectUtils;

public final class LangUtil {

	private LangUtil() {
		// Intentionally empty
	}

	public static Integer zeroIfNull(Integer potentiallyNullNumber) {
		return ObjectUtils.defaultIfNull(potentiallyNullNumber, 0);
	}
}
