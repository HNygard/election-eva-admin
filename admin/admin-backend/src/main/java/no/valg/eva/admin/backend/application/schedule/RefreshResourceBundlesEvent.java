package no.valg.eva.admin.backend.application.schedule;

import org.joda.time.DateTime;

public class RefreshResourceBundlesEvent {
	private DateTime lastDatabaseLocaleTextTimeStamp;

	public RefreshResourceBundlesEvent(DateTime lastDatabaseLocaleTextTimeStamp) {
		this.lastDatabaseLocaleTextTimeStamp = lastDatabaseLocaleTextTimeStamp;
	}

	public RefreshResourceBundlesEvent() {
	}

	/**
	 * Returns timestamp of when any locale text was created or updated last. If this is null, it is certain that there has been an update from the enclosing
	 * runtime. If it has a value, it represents the last time a text was created or updated, but it may have originated from another runtime (jvm or node) or
	 * even before this runtime was started. In other words it is unknown whether this is an actual update or not. It is left to the caller to decide if this is
	 * an update to act upon.
	 * 
	 * @return last saved timestamp of locale text creat eor update
	 */
	public DateTime getLastDatabaseLocaleTextTimeStamp() {
		return lastDatabaseLocaleTextTimeStamp;
	}
}
