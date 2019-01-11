package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.util.ImportElectoralRollUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Parses and SKD electoral roll file into records
 */
public class SkdVoterFileParser implements Iterable<VoterRecord>, Iterator<VoterRecord> {

	private static final Logger LOGGER = Logger.getLogger(SkdVoterFileParser.class);
	public static final int RECORD_SIZE = 517;

	private final String header;
	private final BufferedReader reader;
	private int rowNumber = 0;
	
	private String nextRecord = null;

	public SkdVoterFileParser(final String filename) throws IOException {
		reader = ImportElectoralRollUtil.getBufferedReaderForFile(filename);
		header = reader.readLine();

		if (header == null || header.length() != RECORD_SIZE || !header().equals("HD")) {
			throw new IOException("Invalid or non-existent header in import file for electoral roll");
		}
	}

	
	public String header() {
		return header.substring(0, 0 + 2);
	}
	
	public String kjorenr() {
		return header.substring(2, 2 + 5);
	}

	public String antall() {
		return header.substring(7, 7 + 7);
	}
	

	public int getRowNumber() {
		return rowNumber;
	}

	@Override
	public SkdVoterRecord next() {
		try {
			if (nextRecord == null) {
				nextRecord = reader.readLine();
			}
	
			if (isValidRecord(nextRecord)) {
				SkdVoterRecord record = new SkdVoterRecord(nextRecord, kjorenr());
				rowNumber += 1;
				return record;
			} else {
				throw new IllegalVoterRecord();
			}
		} catch (IOException e) {
			throw new IllegalVoterRecord();
		} finally {
			nextRecord = null;
		}
	}

	@Override
	public boolean hasNext() {
		try {
			if (nextRecord == null) {
				nextRecord = reader.readLine();
			}
			
			if (!isValidRecord(nextRecord)) {
				return false;
			}

		} catch (IOException e) {
			return false;
		}
		return true;
	}

	protected boolean isValidRecord(String record) {
		if (record == null) {
			return false;
		}
		if (record.length() == 1 && String.valueOf(record.charAt(0)).equalsIgnoreCase(String.valueOf('\032'))) {
			return false;
		}
		if (record.length() < RECORD_SIZE) {
			return false;
		}
		return true;
	}

	public void release() {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	@Override
	public Iterator<VoterRecord> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Not implemented");
	}
}
