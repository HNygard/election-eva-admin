package no.valg.eva.admin.crypto;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Representerer X509-sertifikat og privat nøkkel fra en PKCS12-fil, også kalt PFX. I EVA Admin er filtypen kjent som P12.
 * 
 * Privat nøkkel må behandles som en hemmelighet.
 */
public class SertifikatOgNøkkel {
	private final X509Certificate sertifikat;
	private final Certificate[] sertifikatkjede;
	private final PrivateKey nøkkel;

	SertifikatOgNøkkel(X509Certificate sertifikat, Certificate[] sertifikatkjede, PrivateKey nøkkel) {
		this.sertifikatkjede = sertifikatkjede;
		Objects.requireNonNull(sertifikat);
		Objects.requireNonNull(nøkkel);

		this.sertifikat = sertifikat;
		this.nøkkel = nøkkel;
	}

	public X509Certificate sertifikat() {
		return sertifikat;
	}

	public Certificate[] sertifikatkjede() {
		return sertifikatkjede;
	}

	public PrivateKey nøkkel() {
		return nøkkel;
	}
}
