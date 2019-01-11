package no.evote.service.util;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.joda.time.DateTime;

@ApplicationScoped
public class TaskLogger {
	private final Map<String, DateTime> taskLog = new HashMap<>();

	public void logTask(final String taskName, final DateTime timestamp) {
		taskLog.put(taskName, timestamp);
	}

	public Map<String, DateTime> getLog() {
		return taskLog;
	}
}
