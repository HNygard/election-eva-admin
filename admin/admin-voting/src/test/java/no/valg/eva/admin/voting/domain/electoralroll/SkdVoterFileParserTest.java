package no.valg.eva.admin.voting.domain.electoralroll;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SkdVoterFileParserTest {

	private static final String SKD_TEST_FILE_6_PERSON_4_IN_SAMI_LINE_BREAKS = "INIT_20110302.TXT";
	private static final String SKD_TEST_FILE_MISSING_LINE_ENDING_LAST_LINE = "INIT_NO_LINE_BREAK_LAST_LINE.txt";
	private static final int SKD_TEST_FILE_NUMBER_OF_SAMI_RECORDS = 4;
	private static final int SKD_TEST_FILE_NUMBER_OF_RECORDS = 6;
	public static final String SKD_TEST_FILE_FIRST_RECORD_REGDATO = "02122010";

	@Test
	public void testParseEligibleInSamiElection() throws IOException, URISyntaxException {
		SkdVoterFileParser parser = getParser(SKD_TEST_FILE_6_PERSON_4_IN_SAMI_LINE_BREAKS);
		int countPersonsEligibleinSamiElection = 0;
		for (VoterRecord voterRecord : parser) {
			if (voterRecord.eligibleInSamiElection()) {
				countPersonsEligibleinSamiElection++;
			}
		}
		parser.release();

		Assert.assertEquals(countPersonsEligibleinSamiElection, SKD_TEST_FILE_NUMBER_OF_SAMI_RECORDS);
	}
	
	@Test
	public void next_forAllFiles_loopsThroughAllRecords() throws Exception {
		loopThroughAndValidate(SKD_TEST_FILE_6_PERSON_4_IN_SAMI_LINE_BREAKS, SKD_TEST_FILE_NUMBER_OF_RECORDS);
	}
	
	@Test
	public void next_forFilesWithMissingLastNewline_loopsThroughAllRecords() throws Exception {
		loopThroughAndValidate(SKD_TEST_FILE_MISSING_LINE_ENDING_LAST_LINE, SKD_TEST_FILE_NUMBER_OF_RECORDS);
	}

	protected void loopThroughAndValidate(String fileName, int expectedNumberOfRecords) throws IOException, URISyntaxException {
		SkdVoterFileParser parser = getParser(fileName);

		int recordCount = 0;
		for (VoterRecord voterRecord : parser) {
			recordCount++;
			assertThat(voterRecord).isNotNull();
		}
		parser.release();

		assertThat(recordCount).isEqualTo(expectedNumberOfRecords);
	}

	protected SkdVoterFileParser getParser(String skdTestFile6Person4InSamiLineBreaks) throws IOException, URISyntaxException {
		return new SkdVoterFileParser(classpathResourceFilename(skdTestFile6Person4InSamiLineBreaks));
	}

	private String classpathResourceFilename(String resourceOnClasspath) throws URISyntaxException {
		URL url = getClass().getClassLoader().getResource(resourceOnClasspath);
		File file = new File(url.toURI());
		return file.getAbsolutePath();
	}
	
	@Test
	public void regdato_always_isReadCorrectly() throws Exception {
		SkdVoterFileParser parser = getParser(SKD_TEST_FILE_6_PERSON_4_IN_SAMI_LINE_BREAKS);
		SkdVoterRecord record = parser.next();
		assertThat(record.regDato()).isEqualTo(SKD_TEST_FILE_FIRST_RECORD_REGDATO);
	}

	@Test
	public void next_always_readsAddressOfVoter() throws Exception {
		SkdVoterFileParser parser = getParser(SKD_TEST_FILE_6_PERSON_4_IN_SAMI_LINE_BREAKS);
		SkdVoterRecord record = parser.next();
		assertThat(record.adresse().trim()).isEqualTo("Major Forbus gate 10");
	}

}
