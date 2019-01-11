package no.valg.eva.admin.test;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.StopWatch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTakeTimeTest extends MockUtilsTestCase {

	private static Map<String, Long> tests = new HashMap<>();
	private final StopWatch stopWatch = new StopWatch();

	private boolean verboseTests() {
		return System.getProperty("verboseTests") != null;
	}

	@BeforeMethod
	public void before(final Method m) {
		if (verboseTests()) {
			stopWatch.reset();
			stopWatch.start();
		}
	}

	@AfterMethod
	public void end(final Method m) {
		if (m != null && verboseTests()) {
			long time = stopWatch.getTime();
			String methodName = formatString(m.toString());
			tests.put(methodName, time);
			System.out.println(methodName + " " + time + "ms");
		}
	}

	@AfterSuite
	public void tearDown() {
		if (verboseTests()) {
			System.out.println("\n\nTestTime summary: ");

			List<Map.Entry<String, Long>> valueList = new LinkedList<>(tests.entrySet());

			valueList.sort(Comparator.comparing(Entry::getValue));

			for (Map.Entry<String, Long> entry : valueList) {
				System.out.println(entry.getKey() + " " + entry.getValue() + "ms");
			}
			System.out.println("\n\n");
		}
	}

	private String formatString(final String s) {
		final int length = 130;
		if (s.length() > length) {
			return s.substring(0, length);
		}
		return String.format("%1$-" + length + "s", s);
	}
}
