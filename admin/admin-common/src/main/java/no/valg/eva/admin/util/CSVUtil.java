package no.valg.eva.admin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;

import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;

public final class CSVUtil {

	private static final transient String DELIMITER = "\t";

	private CSVUtil() {
	}

	public static byte[] createCsvFromRowData(final List<List<String>> tableData) {
		StringBuilder csvBuffer = new StringBuilder();

		for (List<String> row : tableData) {
			for (int i = 0; i < row.size(); i++) {
				String value = row.get(i);
				if (value == null) {
					value = "";
				}
				csvBuffer.append(value);
				if (i < row.size()) {
					csvBuffer.append(DELIMITER);
				}
			}
			csvBuffer.append("\n");
		}

		try {
			return csvBuffer.toString().getBytes(EvoteConstants.CHARACTER_SET);
		} catch (UnsupportedEncodingException uee) {
			throw new EvoteException("Error writing csv", uee);
		}
	}

	public static List<List<String>> getRowsFromFile(final InputStream stream, final int skip) throws IOException {
		return getRowsFromFile(stream, skip, DELIMITER, EvoteConstants.CHARACTER_SET);
	}

	public static List<List<String>> getRowsFromFile(final InputStream stream, final int skip, final String delimiter, final String encoding) throws IOException {
		List<List<String>> rows = new ArrayList<>();

		try {
			InputStreamReader reader = new InputStreamReader(stream, encoding);
			BufferedReader bufferedReader = new BufferedReader(reader);
			int counter = 0;
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (counter == 0 && !line.matches("^[@\\w]+.*")) {
					line = line.substring(1);
				}

				if (counter++ < skip) {
					continue;
				}

				List<String> row = parseRow(line, delimiter);
				if (row != null) {
					rows.add(row);
				}
			}
		} catch (IOException ioe) {
			throw new EvoteException("Error reading from csv", ioe);
		} finally {
			stream.close();
		}

		return rows;
	}

	private static List<String> parseRow(final String row, final String delimiter) {
		List<String> fields = new ArrayList<>();
		StrTokenizer tokenizer = new StrTokenizer(row, delimiter);
		tokenizer.setIgnoreEmptyTokens(false);
		tokenizer.setEmptyTokenAsNull(true);
		tokenizer.setTrimmerMatcher(StrMatcher.noneMatcher());

		while (tokenizer.hasNext()) {
			String value = tokenizer.next();
			if (value != null && value.startsWith("\"")) {
				value = value.replaceAll("^\"", "");
				value = value.replaceAll("\"\\s*$", "");
				value = value.replaceAll("\"\"", "\"");
			}
			fields.add(value);
		}
		return fields;
	}

}
