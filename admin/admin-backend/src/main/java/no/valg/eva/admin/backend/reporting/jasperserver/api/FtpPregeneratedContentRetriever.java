package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static java.lang.String.format;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_HOST;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_PWD;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_USER;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.log4j.Logger.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.inject.Named;

import no.evote.util.EvaConfigProperty;

import org.apache.log4j.Logger;

public class FtpPregeneratedContentRetriever implements PregeneratedContentRetriever {
	private static final Logger LOGGER = getLogger(FtpPregeneratedContentRetriever.class);
	private final String ftpUrlPrefix;

	@Inject
	public FtpPregeneratedContentRetriever(
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_HOST) String ftpHost,
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_USER) String ftpUser,
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_PWD) String ftpPwd) {
		this.ftpUrlPrefix = format("ftp://%s:%s@%s/PregeneratedOutput/", ftpUser, ftpPwd, ftpHost);
	}

	@Override
	public byte[] tryPreGeneratedReport(String fileName) {
		try {
			URLConnection ftpConnection = new URL(ftpUrlPrefix + fileName).openConnection();
			return toByteArray(ftpConnection.getInputStream());
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	@Override
	public String getRepositoryType() {
		return "ftp";
	}

}
