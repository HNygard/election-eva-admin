package no.valg.eva.admin.frontend.reporting;

import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;



public class ReportContentCacheCleanerTest extends BaseFrontendTest {

	@Test
	public void cleanUpReportContentCache_firesCleanReportCacheEvent() throws Exception {
		ReportContentCacheCleaner cleaner = initializeMocks(ReportContentCacheCleaner.class);
		Event<CleanReportCacheEvent> mock = createMock(Event.class);
		MockUtils.setPrivateField(cleaner, "cleanReportCacheEventEvent", mock);

		cleaner.cleanUpReportContentCache();

		verify(mock).fire(any(CleanReportCacheEvent.class));
	}
}

