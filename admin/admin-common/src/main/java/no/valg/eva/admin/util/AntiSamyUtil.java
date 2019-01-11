package no.valg.eva.admin.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import no.evote.exception.EvoteException;

import no.evote.util.EvoteProperties;
import org.apache.log4j.Logger;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

public final class AntiSamyUtil {
	private static final Logger LOG = Logger.getLogger(AntiSamyFilter.class);

	private static final String POLICY_FILE_LOCATION = "antisamy-eva.xml";

	private final AntiSamy as;
	private final Policy policy;

	private static final AntiSamyUtil INSTANCE = new AntiSamyUtil();

	private AntiSamyUtil() {
		try {
			String policyFile = EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_UTIL_ANTI_SAMY_FILTER_POLICY_FILE, true);
			if (policyFile == null) {
				policy = Policy.getInstance(this.getClass().getClassLoader().getResourceAsStream(POLICY_FILE_LOCATION));
				LOG.info("Using built-in policy file");
			} else {
				policy = Policy.getInstance(new FileInputStream(policyFile));
				LOG.info("Using policy file from properties: " + policyFile);
			}
		} catch (PolicyException | FileNotFoundException e) {
			throw new EvoteException(e.getMessage(), e);
		}
		as = new AntiSamy();
	}

	public static AntiSamyUtil getInstance() {
		return INSTANCE;
	}

	public String applyPolicy(final String localeText) throws ScanException, PolicyException {
		CleanResults cr = as.scan(localeText, policy);
		return cr.getCleanHTML();
	}
}
