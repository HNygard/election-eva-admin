package no.valg.eva.admin.reports.jasper;

import no.evote.exception.EvoteException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

@ApplicationScoped
public class ReportTemplatesDigestProducer {
	@Produces
	@Named("reportTemplatesDigest")
	public String getReportTemplatesDigest() {
		try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/report_templates_digest.properties"), UTF_8)) {
			Properties digestProperties = new Properties();
			digestProperties.load(reader);
			return digestProperties.getProperty("digest", "");
		} catch (IOException e) {
			throw new EvoteException("Couldn't determine digest", e);
		}
	}
}
