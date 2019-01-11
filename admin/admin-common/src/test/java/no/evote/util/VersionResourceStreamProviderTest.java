package no.evote.util;

import static org.testng.Assert.assertNotNull;

import no.evote.exception.EvoteException;

import org.testng.annotations.Test;

public class VersionResourceStreamProviderTest {
	@Test
	public void getVersionPropertiesInputStream_ReadsThisTestClass_returnsANonEmptyStream() throws Exception {
		String myOwnClassName = "/" + getClass().getName().replace('.', '/') + ".class";
		VersionResourceStreamProvider versionResourceStreamProvider = new VersionResourceStreamProvider(myOwnClassName);
		assertNotNull(versionResourceStreamProvider.getVersionPropertiesInputStream());
	}

	@Test(expectedExceptions = EvoteException.class)
	public void getVersionPropertiesInputStream_ReadsNonExistingFile_throwsException()  {
		new VersionResourceStreamProvider("NonExistingPropertyFile.properties").getVersionPropertiesInputStream();
	}
	
	@Test
	public void runNoArgsConstructor() {
		new VersionResourceStreamProvider();
	}
}
