package no.valg.eva.admin.backend.application.schedule;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.common.rbac.SecurityNone;

import org.joda.time.DateTime;

/**
 * Checks every 10 minutes if any locale texts have been changed and emits a {@link RefreshResourceBundlesEvent}. Also emits an event the first time after
 * server restart regardless of any actual change.
 */
@Singleton
public class LocaleTextChangesDetector {
	private DateTime lastSavedUpdatedLocaleText;

	@Inject
	private Event<RefreshResourceBundlesEvent> refreshResourceBundlesEvent;

	@Inject
	private LocaleTextRepository localeTextRepository;

	@SecurityNone
	@Schedule(minute = "*/10", hour = "*", persistent = false)
	public void checkLocaleTextTimestamp() {
		DateTime lastUpdated = localeTextRepository.lastUpdatedTimestamp();
		if (lastSavedUpdatedLocaleText == null || lastUpdated.isAfter(lastSavedUpdatedLocaleText)) {
			refreshResourceBundlesEvent.fire(new RefreshResourceBundlesEvent(lastSavedUpdatedLocaleText == null ? lastUpdated : null));
			lastSavedUpdatedLocaleText = lastUpdated;
		}
	}
}
