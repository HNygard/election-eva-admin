package no.valg.eva.admin.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.emptyList;

public final class ImportElectoralRollUtil {
	private static final transient Logger LOGGER = Logger.getLogger(ImportElectoralRollUtil.class);
	private static final String ISO_8859_1 = "ISO-8859-1";

	private ImportElectoralRollUtil() {
	}

	public static boolean isFileInitialBatchFile(final String filePath) {
		if (!StringUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file.isFile()) {
				FileReader fileReader = null;
				BufferedReader reader = null;
				try {
					fileReader = new FileReader(file);
					reader = new BufferedReader(fileReader);
					
					char[] initial = new char[14];
					char c;
					for (int i = 0; i < 14; i++) {
						c = (char) reader.read();
						if (Character.isLetterOrDigit(c)) {
							initial[i] = c;
						} else {
							return false;
						}
					}
					String str = new String(initial);
					int number = Integer.valueOf(str.substring(2, 7));
					return (number == 0);
				} catch (IOException | NumberFormatException e) {
					LOGGER.error(e.getMessage(), e);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
					if (fileReader != null) {
						try {
							fileReader.close();
						} catch (IOException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
					
				}
			}
		}
		return false;
	}

	public static BufferedReader getBufferedReaderForFile(final String filePath) {
		InputStreamReader isr;
		FileInputStream fis;
		if (!StringUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file.exists()) {
				try {
					fis = new FileInputStream(file);
					isr = new InputStreamReader(fis, ISO_8859_1);
					LOGGER.info("FILE: " + isr.getEncoding());
					return new BufferedReader(isr);
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	public static String buildAddressLine1(final String adressenavn, final String husnrBruksnr, final String bokstavFestenr, final String adressetype,
			final String kommuneNr, final String gateKode, final String underNr) {
		StringBuilder addressLine1 = new StringBuilder(adressenavn);
		if (adressetype.equalsIgnoreCase("O")) {
			try {
				Integer husNr = Integer.parseInt(husnrBruksnr);
				if (husNr > 0) {
					addressLine1.append(" ");
					addressLine1.append(husNr.toString());
				}
				Integer bokstav = Integer.parseInt(bokstavFestenr);
				
				if (bokstav > 9900) {
					addressLine1.append(" ");
					addressLine1.append((char) (bokstav - 9900 + 64));
				}
				
			} catch (NumberFormatException e) {
				LOGGER.error("Could not build address line");
				return "";
			}
		} else if (adressetype.equalsIgnoreCase("M")) {
			addressLine1
				.append(" ")
				.append(kommuneNr)
				.append("/")
				.append(gateKode)
				.append("/")
				.append(husnrBruksnr)
				.append("/")
				.append(bokstavFestenr)
				.append("/")
				.append(underNr);
		}
		return addressLine1.toString();
	}

	public static List<File> dirListByAscendingFileName(final File folder) {
		if (!folder.isDirectory()) {
			return emptyList();
		}

		File[] files = folder.listFiles(File::isFile);
		if (files == null) {
			return emptyList();
		}

		Arrays.sort(files, Comparator.comparing(File::getName));
		return Arrays.asList(files);
	}

}
