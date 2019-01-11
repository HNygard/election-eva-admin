package no.valg.eva.admin.util;

import no.evote.exception.EvoteException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

public final class AntiSamyFilter {
	private static final AntiSamyUtil ANTISAMY = AntiSamyUtil.getInstance();

	private AntiSamyFilter() {
	}

	public static String filter(final String localeText) {
		String result;
		if (localeText == null) {
			return null;
		}
		try {
			result = ANTISAMY.applyPolicy(localeText);
		} catch (ScanException | PolicyException e) {
			throw new EvoteException(e.getMessage(), e);
		}

		// Filter text with antiSamy, but unescape HTML because we would otherwise have to do this in the XHTML files anyway
		return StringEscapeUtils.unescapeHtml4(result);
	}

}
