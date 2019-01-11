package no.valg.eva.admin.util;

import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

/**
 * DEV-NOTE: Dette virker å være et verktøy som strengt tatt ikke hører hjemme i admin-applikasjonen, men feks kan passe i admin-tools.
 * 
 * Calculates the checksum/digest recursively for a directory tree, optionally only considering files matching a regular expression. The resulting value is
 * saved in a properties file where the property "digest" holds the calculated value, e.g.
 * 
 * <pre>
 *     digest=2ba06fb51b71f05313633e9145453ce5
 * </pre>
 */
public final class DirectoryTreeDigestCalculator {
	private static final Logger LOGGER = Logger.getLogger(DirectoryTreeDigestCalculator.class);
	public static final int BUFFER_SIZE = 8192;

	private DirectoryTreeDigestCalculator() {
	}

	public static void main(String... args) {
		Options options = new Options();
		options.addOption("d", "directory", true, "Path to directory to be digested");
		options.addOption("f", "digest-file", true, "Path to property file file with property with digest value");
		options.addOption("v", "verbose", false, "Display filenames that are included in digest");
		options.addOption("p", "pattern", true, "Filename regular expression");

		try {
			final CommandLine commandLine = new GnuParser().parse(options, args);
			final boolean verbose = commandLine.hasOption("v");
			final boolean hasPattern = commandLine.hasOption("p");
			File digestFile = new File(commandLine.getOptionValue("f", "digest.properties"));
			boolean created = digestFile.getParentFile().mkdirs();
			if (created && verbose) {
				LOGGER.info("Created directory " + digestFile.getParent());
			}
			final MessageDigest md = getMessageDigest();
			try {
				walkFileTree(Paths.get(commandLine.getOptionValue("d", ".")), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (!hasPattern || file.getFileName().toString().matches(commandLine.getOptionValue("p"))) {
							if (verbose) {
								LOGGER.info(file.getFileName());
							}
							try (InputStream inputStream = new FileInputStream(file.toFile())) {
								byte[] buffer = new byte[BUFFER_SIZE];
								int numOfBytesRead;
								while ((numOfBytesRead = inputStream.read(buffer)) > 0) {
									md.update(buffer, 0, numOfBytesRead);
								}
							}
						}
						return CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new Exception(e);
			}
			try (PrintWriter writer = new PrintWriter(digestFile, "UTF-8")) {
				writer.append("digest=");
				for (byte mdByte : md.digest()) {
					writer.append(format("%02x", mdByte));
				}
				writer.append('\n');
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				throw new Exception(e);
			}
		} catch (Exception e) {
			LOGGER.error(e);
			System.exit(-1);
		}
	}

	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("md5 not supported", e);
		}
	}
}
