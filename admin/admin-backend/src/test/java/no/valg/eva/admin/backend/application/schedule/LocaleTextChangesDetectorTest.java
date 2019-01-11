package no.valg.eva.admin.backend.application.schedule;

import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;

import static org.joda.time.DateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LocaleTextChangesDetectorTest extends MockUtilsTestCase {

	private static final DateTime NOW = now();
	private LocaleTextChangesDetector localeTextChangesDetector;
	private Event mockEvent;

	@BeforeMethod
	public void setUp() throws Exception {
		localeTextChangesDetector = initializeMocks(LocaleTextChangesDetector.class);
		when(getInjectMock(LocaleTextRepository.class).lastUpdatedTimestamp()).thenReturn(NOW.minusSeconds(10)).thenReturn(NOW);
		mockEvent = mock(Event.class);
		mockFieldValue("refreshResourceBundlesEvent", mockEvent);
	}

	@Test
	public void whenCalledFirstTime_sendsEventRegardlessOfActualLastLocaleTextChange() {
		localeTextChangesDetector.checkLocaleTextTimestamp();
		verify(mockEvent).fire(any(RefreshResourceBundlesEvent.class));
	}

	@Test
	public void whenCalledMulitpleTimes_sendsOnlyTwoEvents() {
		localeTextChangesDetector.checkLocaleTextTimestamp();
		localeTextChangesDetector.checkLocaleTextTimestamp();
		localeTextChangesDetector.checkLocaleTextTimestamp();
		localeTextChangesDetector.checkLocaleTextTimestamp();
		localeTextChangesDetector.checkLocaleTextTimestamp();
		verify(mockEvent, times(2)).fire(any(RefreshResourceBundlesEvent.class));
	}
}

