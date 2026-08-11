package no.valg.eva.admin.reconstruction;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import no.valg.eva.admin.backend.application.service.SystemPasswordApplicationService;

import org.apache.log4j.Logger;

/**
 * NOT PART OF EVA ADMIN. Added to get past the system passphrase gate.
 *
 * <p>
 * This package name exists to make that obvious: nothing under
 * {@code no.valg.eva.admin.reconstruction} was published by Valgdirektoratet.
 *
 * <h2>Why this exists</h2>
 *
 * {@code LifecycleFilter} refuses to serve any request until the system
 * passphrase has been entered, and the released source contains no way to enter
 * it. The passphrase lives in memory, so it cannot be seeded with SQL, and the
 * only remote surface is
 *
 * <pre>
 * public interface SystemPasswordService {
 *     boolean isPasswordSet();
 * }
 * </pre>
 *
 * a getter with no setter. Whatever performed this in the real system was
 * withheld along with {@code admin-other}. See NF-021.
 *
 * <h2>What it disables, stated plainly</h2>
 *
 * In production this passphrase is the key that unlocks EVA's signing material:
 * {@code isPasswordCorrect} decrypts an election-signing PKCS#12 with it, so an
 * operator entering it is what proves the deployment is authorised to sign. This
 * class replaces a human decision with a startup constant, which means:
 *
 * <ul>
 * <li>No operator has approved that this deployment may run.</li>
 * <li>Any signing key loaded into this database would be unlocked automatically.</li>
 * </ul>
 *
 * On an empty database that is harmless, because
 * {@code SystemPasswordApplicationService.isPasswordCorrect} short-circuits to
 * {@code true} when no signing keys exist, so no real secret is being guessed or
 * bypassed. It stops being harmless the moment genuine key material is loaded.
 *
 * This belongs in a local reconstruction for inspection and nowhere else.
 *
 * <h2>Turning it off</h2>
 *
 * Set {@code EVA_AUTO_UNLOCK=false} in the environment and the gate behaves as
 * the release intended: the application stays disabled.
 */
@Startup
@Singleton
@DependsOn("DatabaseSchemaCheckerBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class SystemPassphraseAutoUnlock {

	private static final Logger LOG = Logger.getLogger(SystemPassphraseAutoUnlock.class);

	private static final String ENABLED_ENV = "EVA_AUTO_UNLOCK";
	private static final String PASSPHRASE_ENV = "EVA_SYSTEM_PASSPHRASE";
	private static final String DEFAULT_PASSPHRASE = "reconstruction";

	@Inject
	private SystemPasswordApplicationService systemPasswordApplicationService;

	@PostConstruct
	public void unlock() {
		if ("false".equalsIgnoreCase(env(ENABLED_ENV, "true"))) {
			LOG.info("EVA_AUTO_UNLOCK=false, leaving the system passphrase gate as the release intends.");
			return;
		}

		if (systemPasswordApplicationService.isPasswordSet()) {
			LOG.info("System passphrase already set, nothing to do.");
			return;
		}

		systemPasswordApplicationService.setSystemPassword(env(PASSPHRASE_ENV, DEFAULT_PASSPHRASE));

		LOG.warn("*************************************************************");
		LOG.warn("SystemPassphraseAutoUnlock set the system passphrase at startup.");
		LOG.warn("This is NOT EVA Admin behaviour. It replaces an operator's");
		LOG.warn("decision with a constant, and exists only so the released");
		LOG.warn("source can be inspected. See NF-021 and D011.");
		LOG.warn("*************************************************************");
	}

	private static String env(String name, String fallback) {
		String value = System.getenv(name);
		return value == null || value.isEmpty() ? fallback : value;
	}
}
