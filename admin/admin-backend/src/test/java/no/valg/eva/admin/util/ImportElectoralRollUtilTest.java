package no.valg.eva.admin.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static no.valg.eva.admin.util.ImportElectoralRollUtil.*;

public class ImportElectoralRollUtilTest {
	private static final String INITIAL_ELECTORAL_ROLL_FILE = classpathResourceFilename("INIT_20110302.TXT");
	private static final String INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER = classpathResourceFilename("INIT_WRONG_HEADER.txt");
	private static final String INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER_SHORT = classpathResourceFilename("INIT_WRONG_HEADER_SHORT.txt");
	private static final String INITIAL_ELECTORAL_ROLL_FILE_EMPTY = classpathResourceFilename("INIT_EMPTY.txt");
	private static final String SCHEDULED_ELECTORAL_ROLL_FILE = classpathResourceFilename("electoralRoll/UPDATE_20101122.TXT");
	private static final String NO_FILE = "noFile.txt";
	private static final String ELECTORAL_ROLL_FOLDER = classpathResourceFilename("electoralRoll");

	@Test
	public void testIsFileInitialBatchFile() {
		Assert.assertTrue(isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE));

		Assert.assertFalse(isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE_EMPTY));
		Assert.assertFalse(isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER));
		Assert.assertFalse(isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER_SHORT));
		Assert.assertFalse(isFileInitialBatchFile(SCHEDULED_ELECTORAL_ROLL_FILE));
		Assert.assertFalse(isFileInitialBatchFile(NO_FILE));
		Assert.assertFalse(isFileInitialBatchFile(""));
		Assert.assertFalse(isFileInitialBatchFile(null));
	}

	@Test
	public void testDirListByAscendingName() {
		Assert.assertTrue(dirListByAscendingFileName(new File(ELECTORAL_ROLL_FOLDER)).size() > 0);
		Assert.assertTrue(dirListByAscendingFileName(new File("")).isEmpty());
		Assert.assertTrue(dirListByAscendingFileName(new File(NO_FILE)).isEmpty());
	}

	@Test
	public void testGetBufferedReaderForFile() {
		Assert.assertTrue(getBufferedReaderForFile(INITIAL_ELECTORAL_ROLL_FILE) != null);
		Assert.assertTrue(getBufferedReaderForFile(SCHEDULED_ELECTORAL_ROLL_FILE) != null);
		Assert.assertTrue(getBufferedReaderForFile(ELECTORAL_ROLL_FOLDER) == null);
		Assert.assertTrue(getBufferedReaderForFile(NO_FILE) == null);
		Assert.assertTrue(getBufferedReaderForFile("") == null);
		Assert.assertTrue(getBufferedReaderForFile(null) == null);
	}

	private static String classpathResourceFilename(String resourceOnClasspath) {
		URL url = ImportElectoralRollUtilTest.class.getClassLoader().getResource(resourceOnClasspath);
		try {
			File file = new File(url.toURI());
			return file.getAbsolutePath();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
