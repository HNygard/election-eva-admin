package no.evote.logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.interceptor.InvocationContext;

import org.apache.log4j.spi.LoggingEvent;

public class SQLCollectorAppender extends org.apache.log4j.AppenderSkeleton implements Serializable {

	public static class SQLCollection {
		private static final int PERIOD = 169;
		private final String className;
		private final String methodName;
		private final List<String> sqlList = new ArrayList<String>();

		public SQLCollection(final InvocationContext ctx) {
			className = ctx.getTarget().getClass().getName();
			methodName = ctx.getMethod().getName();
		}

		public void addSQL(final String sql) {
			sqlList.add(sql);
		}

		public String getSQLReport() {
			StringBuilder report = new StringBuilder();
			for (String sql : sqlList) {
				report.append("[");
				int index = PERIOD;
				while (index < sql.length()) {
					String substr = sql.substring(index - PERIOD, index);
					String pre = substr.substring(0, substr.lastIndexOf(' '));
					String post = substr.substring(substr.lastIndexOf(' '));
					report.append(pre).append("\n");
					report.append(post);
					index += PERIOD;
				}
				report.append(sql.substring(index - PERIOD)).append("],\n");
			}
			return report.toString();
		}

		public boolean hasSQL() {
			return !sqlList.isEmpty();
		}

		public String getCalledMethod() {
			return new StringBuilder().append(className).append(".").append(methodName).toString();
		}
	};

	private SQLCollection sqlCollection;

	@Override
	public void close() {
		// Empty to conform with interface
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(final LoggingEvent event) {
		sqlCollection.addSQL(event.getMessage().toString());
	}

	public void setContext(final InvocationContext ctx) {
		sqlCollection = new SQLCollection(ctx);
	}

	public String getSQLReport() {
		return sqlCollection.getSQLReport();
	}

	public String getCalledMethod() {
		return sqlCollection.getCalledMethod();
	}

	public boolean hasSQL() {
		return sqlCollection.hasSQL();
	}
}
