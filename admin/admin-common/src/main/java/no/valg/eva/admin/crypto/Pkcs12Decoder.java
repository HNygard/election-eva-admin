package no.valg.eva.admin.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Leser PKCS12-filer, også kalt PFX. I EVA Admin sin terminologi er filtypen kjent som P12.
 */
public class Pkcs12Decoder {

	private static final String PKCS12_KEYSTORE_TYPE = "PKCS12";
	private static final String BOUNCY_CASTLE_PROVIDER_ID = "BC";

	/**
	 * @param pkcs12Fil PKCS12-fil som {@link InputStream}. Leses i denne metoden, men lukkes ikke.
	 * @param pkcs12Passord passordet til PKCS12-fila.
	 * @return {@code true} hvis passordet er riktig.
	 */
	public boolean validerPassord(InputStream pkcs12Fil, String pkcs12Passord) throws CryptoException {
		KeyStore p12Keystore = pkcs12BouncyCastleKeystore();
		try {
			return validerPassord(p12Keystore, pkcs12Fil, pkcs12Passord);
		} catch (NoSuchAlgorithmException | CertificateException e) {
			throw new CryptoException("Klarte ikke lese PKCS12", e);
		}
	}

	private boolean validerPassord(KeyStore p12Keystore, InputStream pkcs12Fil, String pkcs12Passord)
			throws NoSuchAlgorithmException, CertificateException {
		try {
			p12Keystore.load(pkcs12Fil, pkcs12Passord.toCharArray());
			return true;
		} catch (IOException e) {
			// API-mapping fra exception til boolean returverdi.
			// KeyStore.load() kaster IOException ved feil passord.
			// I følge JavaDoc burde e.getCause() vært et UnrecoverableKeyException, men det er det ikke.
			return false;
		}
	}

	/**
	 * @param pkcs12Fil PKCS12-fil som {@link InputStream}. Leses i denne metoden, men lukkes ikke.
	 * @param pkcs12Passord passordet til PKCS12-fila.
	 * @return sertifikat og privat nøkkel.
	 */
	public SertifikatOgNøkkel lesPkcs12(InputStream pkcs12Fil, String pkcs12Passord) throws CryptoException {
		char[] passord = pkcs12Passord.toCharArray();
		KeyStore keystore = pkcs12BouncyCastleKeystore();

		try {
			keystore.load(pkcs12Fil, passord);

			if (keystore.size() != 1) {
				throw new RuntimeException("Forventer kun ett alias i en p12-fil");
			}

			String alias = finnNøkkelAlias(keystore);

			X509Certificate sertifikat = (X509Certificate) keystore.getCertificate(alias);
			Certificate[] sertifikatkjede = keystore.getCertificateChain(alias);
			PrivateKey nøkkel = (PrivateKey) keystore.getKey(alias, passord);

			return new SertifikatOgNøkkel(sertifikat, sertifikatkjede, nøkkel);

		} catch (UnrecoverableKeyException | KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new CryptoException("Klarte ikke lese PKCS12", e);
		}
	}

	private KeyStore pkcs12BouncyCastleKeystore() throws CryptoException {
		try {
			return KeyStore.getInstance(PKCS12_KEYSTORE_TYPE, BOUNCY_CASTLE_PROVIDER_ID);
		} catch (KeyStoreException | NoSuchProviderException e) {
			throw new CryptoException("Klarte ikke finne PKCS12 keystore", e);
		}
	}

	private String finnNøkkelAlias(KeyStore keystore) throws CryptoException, KeyStoreException {
		Enumeration<String> aliases = keystore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (keystore.isKeyEntry(alias)) {
				return alias;
			}
		}
		throw new CryptoException("Fant ikke nøkkelens alias i keystore");
	}
}
