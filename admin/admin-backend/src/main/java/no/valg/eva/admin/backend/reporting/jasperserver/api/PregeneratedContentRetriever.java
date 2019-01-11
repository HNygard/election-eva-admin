package no.valg.eva.admin.backend.reporting.jasperserver.api;

public interface PregeneratedContentRetriever {
	byte[] tryPreGeneratedReport(String fileName);

	String getRepositoryType();
}
