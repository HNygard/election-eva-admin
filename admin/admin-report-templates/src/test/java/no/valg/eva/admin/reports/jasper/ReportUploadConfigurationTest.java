package no.valg.eva.admin.reports.jasper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.testng.annotations.Test;


public class ReportUploadConfigurationTest {
	@Test
	public void testCreateConfiguration() throws Exception {
		ReportUploadConfiguration reportUploadConfiguration = new ReportUploadConfiguration(getClass().getResourceAsStream("/ReportConfig.xml"));
		assertThat(reportUploadConfiguration.getCategoryFolders()).hasSize(9);
		assertThat(((Map) reportUploadConfiguration.getCategoryFolders().get(0)).get("uri")).isEqualTo("010.configuration");
		assertThat(((Map) reportUploadConfiguration.getCategoryFolders().get(0)).get("name")).isEqualTo("@reporting.report.category.configuration");

		assertThat(reportUploadConfiguration.getReportTemplates()).hasSize(32);
		assertThat(((Map) reportUploadConfiguration.getReportTemplates().get(0)).get("path")).isEqualTo("Rapport 1/report_1/report_1.jrxml");
		assertThat(((Map) reportUploadConfiguration.getReportTemplates().get(0)).get("replaceWithBlank")).isEqualTo(true);
	}
}

