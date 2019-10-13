package no.evote.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;

/**
 */
@ApplicationScoped
public class CommitIdProducer {
	private String commitId;
	@Inject
	private VersionResourceStreamProvider versionResourceStreamProvider;

	public CommitIdProducer() {
	}

	public CommitIdProducer(VersionResourceStreamProvider versionResourceStreamProvider) {
		this.versionResourceStreamProvider = versionResourceStreamProvider;
	}

	@PostConstruct
	void init() {
		try (Reader reader = new InputStreamReader(versionResourceStreamProvider.getVersionPropertiesInputStream(), UTF_8)) {
			Properties versionProperties = new Properties();
			versionProperties.load(reader);
			commitId = versionProperties.getProperty("commitId", "");
		} catch (IOException e) {
			throw new EvoteException("Couldn't determine version", e);
		}
	}
	
	@Produces
	@Named("commitId")
	public String getCommitId() {
		return commitId;
	}
}

