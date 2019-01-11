package no.valg.eva.admin.frontend.reporting;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@Singleton
@Startup
public class ReportContentCacheCleaner {
	@Inject
	private Event<CleanReportCacheEvent> cleanReportCacheEventEvent;

	@Schedule(hour = "*", minute = "*/10", persistent = false)
	public void cleanUpReportContentCache() {
		cleanReportCacheEventEvent.fire(new CleanReportCacheEvent());
	}
}
