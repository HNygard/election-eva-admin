package no.evote.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import no.evote.exception.EvoteException;

import org.testng.annotations.Test;

public class CommitIdProducerTest {

	private static final String EXPECTED_COMMIT_ID = "deadbeef";
	private static final String VERSION_PROPERTIES_CONTENT = "commitId=" + EXPECTED_COMMIT_ID + "\n";

	@Test
	public void testGetCommitId() throws Exception {
		VersionResourceStreamProvider versionResourceStreamProvider = mock(VersionResourceStreamProvider.class);
		when(versionResourceStreamProvider.getVersionPropertiesInputStream()).thenReturn(new ByteArrayInputStream(VERSION_PROPERTIES_CONTENT.getBytes(UTF_8)));
		CommitIdProducer commitIdProducer = new CommitIdProducer(versionResourceStreamProvider);
		commitIdProducer.init();
		assertEquals(commitIdProducer.getCommitId(), EXPECTED_COMMIT_ID);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testGetCommitId_whenVersionPropertiesFileIsMissing() throws Exception {
		CommitIdProducer commitIdProducer = new CommitIdProducer(new VersionResourceStreamProvider("NonExistingPropertyFile.properties"));
		commitIdProducer.init();
		assertEquals(commitIdProducer.getCommitId(), EXPECTED_COMMIT_ID);
	}
	
}
