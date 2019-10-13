package no.valg.eva.admin.backend.application.schedule;

import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.backend.application.service.SystemPasswordApplicationService;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.common.application.buypass.RevocationListUpdater;

import org.apache.log4j.Logger;

@Stateless
@Default
public class RevocationListScheduler {
	private static final Logger LOGGER = Logger.getLogger(RevocationListScheduler.class);

	@Inject
	private SystemPasswordApplicationService systemPasswordService;
	@Inject
	private RevocationListUpdater revocationListUpdater;

	@Schedules({ @Schedule(hour = "*", minute = "*/5") })
	public void updateCertificateRevocationLists() {
		if (!systemPasswordService.isPasswordSet()) {
			return;
		}
		if (!Boolean.valueOf(EvoteProperties.getProperty(EvoteProperties.BUYPASS_PERFORM_SCHEDULED_CRL_IMPORT, "false"))) {
			LOGGER.info("updateCertificateRevocationLists - node is not configured to perform scheduled CRL update");
			return;
		}

		LOGGER.info("updateCertificateRevocationLists start");
		try {
			revocationListUpdater.updateIfNeeded();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.info("updateCertificateRevocationLists end");
	}
}
