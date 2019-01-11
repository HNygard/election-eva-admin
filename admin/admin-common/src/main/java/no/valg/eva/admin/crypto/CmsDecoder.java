package no.valg.eva.admin.crypto;

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.DecoderException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class CmsDecoder {

	private static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
	private static final String BOUNCY_CASTLE_PROVIDER_ID = "BC";
    private static final String CERTIFICATE_START = "<PublicCertificate>";
    private static final String CERTIFICATE_END = "</PublicCertificate>";
    private static final String SIGNATURE_START = "<Signature>";
    private static final String SIGNATURE_END = "</Signature>";

	/**
	 * Verifiserer at en signatur er gjort av det angitte sertifikat. Det gjøres ingen ytterligere sjekk av dette sertifikatet, mot key usage-attributter eller
	 * CRL.
	 *
	 * @param data data
     * @param pemSignature signatur av data
     * @param certificate sertifikat som har signert
	 * @return {@code true} hvis signaturen er gjort av det gitte sertifikatet
	 * @throws CryptoException hvis signatur mangler eller verifisering ikke lot seg utføre
	 */
    public boolean verifySignedBy(byte[] data, byte[] pemSignature, X509Certificate certificate) throws CryptoException {
		try {
            byte[] asn1Signatur = pemToAsn1(pemSignature);

			CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(data), asn1Signatur);
			SignerInformationStore signerInformationStore = signedData.getSignerInfos();
			Iterator<SignerInformation> signerIterator = signerInformationStore.getSigners().iterator();

			if (signerIterator.hasNext()) {
				SignerInformation signer = signerIterator.next();

                return verifySignature(certificate, signer);
			}

			throw new CryptoException("Ingen hadde signert");

		} catch (IOException | CMSException | OperatorCreationException e) {
			throw new CryptoException("Klarte ikke verifisere signatur", e);
		}
	}

    private byte[] pemToAsn1(byte[] pemSignatur) throws CryptoException, IOException {
        PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemSignatur)));
        ContentInfo pemObject = (ContentInfo) pemParser.readObject();

        if (pemObject == null || !ID_PKCS7_SIGNED_DATA.equals(pemObject.getContentType().getId())) {
            throw new CryptoException("Ikke en CMS/PKCS7 signatur");
        }

        return pemObject.getEncoded();
    }

    private boolean verifySignature(X509Certificate certificate, SignerInformation signer) throws CMSException, OperatorCreationException {
        try {
            return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(BOUNCY_CASTLE_PROVIDER_ID).build(certificate));
        } catch (CMSSignerDigestMismatchException e) {
            // API-mapping fra exception til boolean returverdi.
            return false;
        }
    }

	/**
	 * Verifiserer at en signatur er gyldig, og returnerer sertifikatet som signerte.
	 * 
	 * Det sjekkes at sertifikatet brukt til signering er gyldig for digitale signaturer. Det gjøres ikke sjekk mot CRL.
	 *
	 * @param data data
     * @param signatureFile fil som inneholder sertifikat og signatur av data
	 * @return sertifikatet som har signert, gitt at signaturen er gyldig
	 * @throws CryptoException hvis ingen hadde signert, eller verifisering ikke lot seg utføre
	 */
	@NotNull
    public X509Certificate verifySignatureAndReturnSecurityCertificate(byte[] data, byte[] signatureFile) throws CryptoException {
		try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(getCertificate(signatureFile)));
            if (verifySignatureWithCertificate(certificate, data, getSignature(signatureFile))) {
                return certificate;
            } else {
                throw new CryptoException("Signatur ikke ok for subject " + certificate.getSubjectDN().getName());
            }
        } catch (GeneralSecurityException | DecoderException e) {
			throw new CryptoException("Klarte ikke verifisere signatur", e);
		}
	}

    private byte[] getCertificate(byte[] input) {
        String contents = new String(input);
        String certificate = contents.substring(contents.indexOf(CERTIFICATE_START) + CERTIFICATE_START.length(), contents.indexOf(CERTIFICATE_END));
        return java.util.Base64.getMimeDecoder().decode(certificate.getBytes());
    }

    private boolean verifySignatureWithCertificate(X509Certificate certificate, byte[] data, byte[] signature) throws GeneralSecurityException {
        PublicKey pk = certificate.getPublicKey();
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pk);
        sig.update(data);
        return sig.verify(signature);
    }

    private byte[] getSignature(byte[] input) {
        String contents = new String(input);
        String signature = contents.substring(contents.indexOf(SIGNATURE_START) + SIGNATURE_START.length(), contents.indexOf(SIGNATURE_END));
        return java.util.Base64.getMimeDecoder().decode(signature.getBytes());
    }
}
