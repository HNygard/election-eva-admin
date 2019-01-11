package no.evote.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import no.valg.eva.admin.crypto.CmsEncoder;
import no.valg.eva.admin.crypto.CryptoException;
import no.valg.eva.admin.crypto.Pkcs12Decoder;
import no.valg.eva.admin.crypto.SertifikatOgNøkkel;
import no.valg.eva.admin.util.IOUtil;

public final class SignIt {
	private final SertifikatOgNøkkel p12;

	private final String inputFilename;
	private final String outputFilename;

	public SignIt(final String[] args) throws IOException, CryptoException {

		if (args.length != 4) {
			usage(System.out);
		}

		String p12Filename = args[0];
		String password = args[1];
		try (InputStream p12InputStream = new FileInputStream(p12Filename)) {
			p12 = new Pkcs12Decoder().lesPkcs12(p12InputStream, password);
		}

		inputFilename = args[2];

		outputFilename = args[3];

	}

	public void run() throws IOException, CryptoException {
		byte[] bytesToBeSigned = IOUtil.getBytes(new File(inputFilename));
		byte[] signature = sign(bytesToBeSigned);

		try (FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFilename))) {
			fileOutputStream.write(signature);
		}
	}

	public byte[] sign(final byte[] bytesToBeSigned) throws CryptoException {
		return new CmsEncoder().signer(p12.sertifikat(), p12.sertifikatkjede(), p12.nøkkel(), bytesToBeSigned);
	}

	public void usage(final PrintStream out) {
		out.println("Usage: java -jar admin-tools.jar sign-it p12-file password input-file output-file");
		throw new IllegalStateException();
	}
}
