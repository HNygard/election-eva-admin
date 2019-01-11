package no.valg.eva.admin.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;

/**
 * Klasse som signerer og verifiserer signaturer i CMS/PKCS#7-format (PEM/ASCII-base64, ikke DER/binært).
 */
public class CmsEncoder {

	private static final String BOUNCY_CASTLE_PROVIDER_ID = "BC";
	private static final String SHA_256_WITH_RSA = "SHA256withRSA";
	private static final int PEM_BUFFER_SIZE = 5000;

	/**
	 * Signerer data med det angitte sertifikatet og nøkkelen.
	 * 
	 * @param sertifikat sertifikat som som hører til den private nøkkelen
	 * @param sertifikatkjede sertifikatkjeden som skal tas med i signaturen
	 * @param nøkkel privat nøkkel til å signere med
	 * @param data data som skal signeres @return signatur over dataene
	 */
	public byte[] signer(X509Certificate sertifikat, Certificate[] sertifikatkjede, PrivateKey nøkkel, byte[] data) throws CryptoException {
		try {
			byte[] asn1Message = buildSignedAsn1Message(sertifikat, sertifikatkjede, nøkkel, data);
			return asn1ToPem(asn1Message);
		} catch (IOException | CMSException | CertificateEncodingException | OperatorCreationException e) {
			throw new CryptoException("Klarte ikke signere data", e);
		}
	}

	private byte[] buildSignedAsn1Message(X509Certificate sertifikat, Certificate[] sertifikatkjede, PrivateKey nøkkel, byte[] data)
			throws CertificateEncodingException, OperatorCreationException, CMSException, IOException {

		CMSSignedDataGenerator cmsGenerator = buildSignedDataGenerator(sertifikat, sertifikatkjede, nøkkel);
		CMSSignedData cmsSignedData = cmsGenerator.generate(new CMSProcessableByteArray(data));

		return cmsSignedData.getEncoded();
	}

	private CMSSignedDataGenerator buildSignedDataGenerator(X509Certificate sertifikat, Certificate[] sertifikatkjede, PrivateKey nøkkel)
			throws CertificateEncodingException, OperatorCreationException, CMSException {

		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

		JcaCertStore certStore = new JcaCertStore(Arrays.asList(sertifikatkjede));
		generator.addCertificates(certStore);

		SignerInfoGenerator signerInfoGenerator = new JcaSimpleSignerInfoGeneratorBuilder().setProvider(BOUNCY_CASTLE_PROVIDER_ID)
				.build(SHA_256_WITH_RSA, nøkkel, sertifikat);
		generator.addSignerInfoGenerator(signerInfoGenerator);

		return generator;
	}

	private byte[] asn1ToPem(byte[] asn1Message) throws IOException {
		ContentInfo asn1Content = ContentInfo.getInstance(ASN1Sequence.fromByteArray(asn1Message));
		ByteArrayOutputStream baos = new ByteArrayOutputStream(PEM_BUFFER_SIZE);

		JcaPEMWriter pemWriter = new JcaPEMWriter(new OutputStreamWriter(baos));
		pemWriter.writeObject(asn1Content);
		pemWriter.close();

		return baos.toByteArray();
	}

}
