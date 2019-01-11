package no.valg.eva.admin.backend.reporting.jasperserver;

import no.evote.service.backendmock.FakeEvent;
import no.valg.eva.admin.backend.application.schedule.RefreshResourceBundlesEvent;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi;
import no.valg.eva.admin.backend.reporting.jasperserver.api.VersionInfo;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.reports.jasper.ReportUploadConfiguration;
import no.valg.eva.admin.reports.jasper.ReportsUploader;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ReportTemplateUploaderTest extends MockUtilsTestCase {
	private static final String NEW_COMMIT_ID = "newCommitId";
	private static final String NEW_DIGEST = "newDigest";
	private static final String PREVIOUS_COMMIT_ID = "previousCommitId";
	private static final String PREVIOUS_DIGEST = "previousDigest";
	public static final String TEMP_FILE_PATH = "temp file path";
	private ReportTemplateUploader reportTemplateUploader;
	private ReportsUploader reportsUploader;

	@BeforeMethod
	public void setUp() throws Exception {
		final File fakeTempFile = mock(File.class);
		when(fakeTempFile.getAbsolutePath()).thenReturn(TEMP_FILE_PATH);

		reportTemplateUploader = new ReportTemplateUploader("user", "pwd", "url", "context", true, NEW_COMMIT_ID, NEW_DIGEST, null, "", "02:00", false,
				null, null, null, null) {
			@Override
			protected void runUploader(boolean onlyRefreshResourceBundles, DateTime lastDatabaseLocaleTextTimeStamp) {
				try {
					Thread.sleep(5);
					super.runUploader(onlyRefreshResourceBundles, lastDatabaseLocaleTextTimeStamp);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected InputStream getConfigurationAsStream() {
				return new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<reportConfig/>\n").getBytes());
			}

			@Override
			protected Event<UploadReportTemplatesDoneEvent> getUploadReportTemplatesDoneEventEvent() {
				return new FakeEvent<>();
			}

			@Override
			protected Event<RefreshResourceBundlesEvent> getUploadReportTemplatesEventEvent() {
				return new Event<RefreshResourceBundlesEvent>() {
					@Override
					public void fire(RefreshResourceBundlesEvent refreshResourceBundlesEvent) {
						consumeUploadReportTemplatesEvent(new UploadReportTemplatesEvent());
					}

					@Override
					public Event<RefreshResourceBundlesEvent> select(Annotation... annotations) {
						return null;
					}

					@Override
					public <U extends RefreshResourceBundlesEvent> Event<U> select(Class<U> aClass, Annotation... annotations) {
						return null;
					}

					@Override
					public <U extends RefreshResourceBundlesEvent> Event<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
						return null;
					}
				};
			}
		};
		initializeMocks(reportTemplateUploader);
		reportsUploader = mockField("reportsUploader", ReportsUploader.class);
		VersionInfo fakeVersionInfo = new VersionInfo();
		fakeVersionInfo.setCommitId(PREVIOUS_COMMIT_ID);
		fakeVersionInfo.setDigest(PREVIOUS_DIGEST);
		when(getInjectMock(JasperRestApi.class).getVersionInfo()).thenReturn(fakeVersionInfo);
		Locale adminLocale = new Locale();
		adminLocale.setId("nb-NO");
		Locale electionEventLocale1 = new Locale();
		electionEventLocale1.setId("nb-NO");
		Locale electionEventLocale2 = new Locale();
		electionEventLocale2.setId("nn-NO");
	}

	@Test
	public void whenNewCommitId_TemplatesAreUploaded() {
		reportTemplateUploader.uploadReportTemplates(null);
		checkScriptWasRunWithResourcesAndTemplates();

	}

	@Test
	public void whenSameCommitIdAndDigest_TemplatesAreNotUploaded() throws Exception {
		mockFieldValue("commitId", PREVIOUS_COMMIT_ID);
		mockFieldValue("digest", PREVIOUS_DIGEST);
		reportTemplateUploader.uploadReportTemplates(null);
		checkScriptWasRunWithResourcesAndTemplates();
	}

	@Test
	public void whenNewCommitIdButSameDigest_TemplatesAreNotUploaded() throws Exception {
		mockFieldValue("commitId", NEW_COMMIT_ID);
		mockFieldValue("digest", PREVIOUS_DIGEST);
		reportTemplateUploader.uploadReportTemplates(null);
		checkScriptWasRunWithResourcesAndTemplates();
	}

	@Test
	public void whenNewCommitIdAndNewDigest_TemplatesAreUploaded() throws Exception {
		mockFieldValue("commitId", NEW_COMMIT_ID);
		mockFieldValue("digest", NEW_COMMIT_ID);
		reportTemplateUploader.uploadReportTemplates(null);
		checkScriptWasRunWithResourcesAndTemplates();
	}

	@Test
	public void whenNewCommitIdButDisabled_TemplatesAreNotUploaded() throws Exception {
		mockFieldValue("autoUploadEnabled", false);
		reportTemplateUploader.uploadReportTemplates(null);
		checkScriptWasNotRun();
	}

	@Test
	public void whenRefreshResourceBundlesEvent_isReceived_scriptIsRun() {
		reportTemplateUploader.consumeRefreshResourceBundlesEvent(new RefreshResourceBundlesEvent());
		checkScriptWasRunWithResourcesOnly();
	}

	@Test(enabled = false)
	public void whenMultipleRefreshResourceBundlesEventAreFired_scriptIsRunOnlyTwice() throws Exception {
		new Thread(() -> reportTemplateUploader.consumeRefreshResourceBundlesEvent(null)).start();
		Thread.sleep(1);
		reportTemplateUploader.consumeRefreshResourceBundlesEvent(null);
		reportTemplateUploader.consumeRefreshResourceBundlesEvent(null);
		reportTemplateUploader.consumeRefreshResourceBundlesEvent(null);
		Thread.sleep(500);
		checkScriptWasRunWithResourcesOnly(2);
	}

	private void checkScriptWasNotRun() {
		verify(reportsUploader, never()).uploadAllTemplates(any(ReportUploadConfiguration.class), any(List.class), anyBoolean(), any(DateTime.class),
				any(List.class), any(List.class));
	}

	private void checkScriptWasRunWithResourcesOnly() {
        verify(reportsUploader).uploadAllTemplates(any(ReportUploadConfiguration.class), any(List.class), eq(true), any(), any(List.class),
				any(List.class));
	}

	private void checkScriptWasRunWithResourcesOnly(int times) {
		verify(reportsUploader, times(times)).uploadAllTemplates(any(ReportUploadConfiguration.class), any(List.class), anyBoolean(), any(DateTime.class),
				any(List.class), any(List.class));
	}

	private void checkScriptWasRunWithResourcesAndTemplates() {
        verify(reportsUploader).uploadAllTemplates(any(ReportUploadConfiguration.class), any(List.class), eq(false), any(), any(List.class),
				any(List.class));
	}
}

