package no.evote.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.evote.service.security.SystemPasswordStore;
import no.valg.eva.admin.backend.common.application.buypass.RevocationListHolder;
import no.valg.eva.admin.backend.common.application.buypass.SubjectSerialNumber;
import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.crypto.CmsDecoder;
import no.valg.eva.admin.crypto.CmsEncoder;
import no.valg.eva.admin.crypto.CryptoException;
import no.valg.eva.admin.crypto.Pkcs12Decoder;
import no.valg.eva.admin.crypto.SertifikatOgNøkkel;
import no.valg.eva.admin.rbac.domain.model.Operator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static no.evote.constants.EvoteConstants.CHARACTER_SET;

public class CryptoServiceBean {

	private static final String BC = "BC";
	private static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5PADDING";
	private static final String PBEWITHMD5AND256BITAES_CBC_OPENSSL = "PBEWITHMD5AND256BITAES-CBC-OPENSSL";
	private static final Logger LOGGER = Logger.getLogger(CryptoServiceBean.class);
    private static final int KEY_ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

	private final Cache electionEventPKCS12Cache = CacheManager.create().getCache("election-event-pkcs12");

	@Inject
	private CertificateService certificateService;
	@Inject
	private SigningKeyRepository signingKeyRepository;
	@Inject
	private SystemPasswordStore systemPasswordStore;
	@Inject
	private RevocationListHolder revocationListHolder;

	/**
	 * Signs the bytesToSign with the p12 connected to the electionEvent connected to the userData parameter
	 */
	public byte[] signDataWithCurrentElectionEventCertificate(final UserData userData, final byte[] bytesToSign) {
		SertifikatOgNøkkel p12 = getElectionEventPKCS12(userData, userData.getElectionEventPk());

		try {
			CmsEncoder cmsEncoder = new CmsEncoder();
			return cmsEncoder.signer(p12.sertifikat(), p12.sertifikatkjede(), p12.nøkkel(), bytesToSign);
		} catch (CryptoException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Performs a symmetric encryption on the data using a AES 256 and OpenSSL standard encryption mode and salt
	 */
	public byte[] encryptSymmetricallyOpenSSL(final byte[] dataToEncrypt, final String password) {
		Cipher cipher;
		
		byte[] salt = new byte[8];
		
		SecretKey key;
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.nextBytes(salt);


            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
			
			key = generateSecretKeyBC(PBEWITHMD5AND256BITAES_CBC_OPENSSL, keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}

		byte[] encrytpedData;
		try {
			cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING, BC);
			synchronized (cipher) {
				cipher.init(Cipher.ENCRYPT_MODE, key);
				encrytpedData = cipher.doFinal(dataToEncrypt);
			}
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException
				| NoSuchProviderException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}

		
		byte[] encrytpedDataAndSalt = new byte[encrytpedData.length + 16];

		System.arraycopy("Salted__".getBytes(), 0, encrytpedDataAndSalt, 0, 8);
		System.arraycopy(salt, 0, encrytpedDataAndSalt, 8, 8);
		System.arraycopy(encrytpedData, 0, encrytpedDataAndSalt, 16, encrytpedData.length);
		
		return encrytpedDataAndSalt;

	}

	/**
	 * Performs a symmetric decryption on the data using a AES 256 and OpenSSL standard encryption mode and salt
	 */
	public byte[] decryptSymmetricallyOpenSSL(final byte[] dataToDecrypt, final String password) {
		if (password == null) {
			throw new EvoteSecurityException("No password supplied, unable to continue.");
		}

		Cipher cipher;
		SecretKey key;

		try {
			
			byte[] salt = new byte[8];
			// Salt is first 8 bytes of stored encrypted value
			System.arraycopy(dataToDecrypt, 8, salt, 0, 8);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
			
			key = generateSecretKeyBC(PBEWITHMD5AND256BITAES_CBC_OPENSSL, keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}
		byte[] decryptedData;
		try {
			cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING, BC);
			synchronized (cipher) {
				cipher.init(Cipher.DECRYPT_MODE, key);
				
				decryptedData = cipher.doFinal(dataToDecrypt, 16, dataToDecrypt.length - 16);
				
			}
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new EvoteException(e.getMessage(), e);
		} catch (NoSuchPaddingException | InvalidKeyException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}
		return decryptedData;
	}

	private SecretKey generateSecretKeyBC(final String alg, final PBEKeySpec keySpec) throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		SecretKey key;
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(alg, BC);
		synchronized (keyFactory) {
			key = keyFactory.generateSecret(keySpec);
		}
		return key;
	}

	/**
	 * Performs a symmetric encryption on the data using a AES 256 and OpenSSL standard encryption mode and salt Using the system password as password
	 */
	public byte[] encryptWithSystemPassword(final byte[] dataToEncrypt) {
		String decryptP12Password = getSymmetricPassword();
		return encryptSymmetricallyOpenSSL(dataToEncrypt, decryptP12Password);
	}

	public boolean verifyAdminElectionEventSignature(final UserData userData, final byte[] data, final byte[] signature, final long electionEventPk) {
		X509Certificate electionEventCertificate = getElectionEventP12Cert(userData);
		CmsDecoder cmsDecoder = new CmsDecoder();
		try {
			return cmsDecoder.verifySignedBy(data, signature, electionEventCertificate);
		} catch (CryptoException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Extracts the CA certificate in the p12 connected to the election event connected to the userData parameter
	 */
	private X509Certificate getElectionEventP12Cert(final UserData userData) {
		final Long electionEventPk = userData.getElectionEventPk();
		final String cacheKey = electionEventPk + "-cert";
		if (electionEventPKCS12Cache.isKeyInCache(cacheKey)) {
			Element result = electionEventPKCS12Cache.get(cacheKey);
			if (result != null) {
				return (X509Certificate) result.getObjectValue();
			}
		}

		SertifikatOgNøkkel p12 = getElectionEventPKCS12(userData, electionEventPk);

		X509Certificate electionEventCertificate = p12.sertifikat();

		electionEventPKCS12Cache.put(new Element(cacheKey, electionEventCertificate));

		return electionEventCertificate;
	}

	public void verifyScanningCountSignature(final byte[] data, final byte[] signatureFile, Operator signingOperator) {
		Collection<X509Certificate> caBundle = certificateService.getScanningCertificateIssuersFromBundle();

		X509Certificate signingCert;
		try {
			signingCert = verifySignatureAndGetSigningCertificate(data, signatureFile);
		} catch (CryptoException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EvoteException(ErrorCode.ERROR_CODE_0302_SIGNATURE_VALIDATION_FAILED, null);
		}

		if (StringUtils.isBlank(signingOperator.getKeySerialNumber())) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0328_OPERATOR_MISSING_SUBJECT_SERIAL_NUMBER, null);
		}

		SubjectSerialNumber subjectSerialNumber = SubjectSerialNumber.fromPrincipal(signingCert.getSubjectX500Principal());
		if (!subjectSerialNumber.matches(signingOperator.getKeySerialNumber())) {
			LOGGER.error(format(
					"Certificate serial number mismatch! Operator %s's registered certificate serial number is %s, but count file was signed with serial number %s.",
					signingOperator.getId(), signingOperator.getKeySerialNumber(), subjectSerialNumber.asString()));
			throw new EvoteException(ErrorCode.ERROR_CODE_0329_CERTIFICATE_SERIAL_NUMBER_MISMATCH, null,
					signingOperator.getKeySerialNumber(), subjectSerialNumber.asString());
		}

		for (X509Certificate caCert : caBundle) {
			try {
				try {
					PublicKey publicKey = caCert.getPublicKey();
					signingCert.verify(publicKey);

					valdidateCrl(signingCert, caCert);

					return;
				} catch (SignatureException | CRLException | InvalidAlgorithmParameterException e) {
					// Vi verifiserer mot "nærmeste" CA sertifikat, fortsett til neste.
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(e.getMessage(), e);
					}
				} catch (CertPathValidatorException e) {
					// CRL valideringsfeil!
					LOGGER.error(e.getMessage(), e);
					throw new EvoteException(ErrorCode.ERROR_CODE_0330_CERTIFICATE_REVOKED, null);
				}
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | CertificateException e) {
				LOGGER.error(e.getMessage(), e);
				throw new EvoteException(ErrorCode.ERROR_CODE_0302_SIGNATURE_VALIDATION_FAILED, null);
			}
		}

		throw new EvoteException(ErrorCode.ERROR_CODE_0302_SIGNATURE_VALIDATION_FAILED, null);
	}

	private void valdidateCrl(X509Certificate signingCertToVerify, X509Certificate caCert) throws CertificateException, CRLException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, CertPathValidatorException, NoSuchProviderException {
		CertificateRevocationList crl = revocationListHolder.getBuypassCrl(caCert.getSubjectX500Principal());
		if (crl == null) {
			LOGGER.warn("Did not find CRL for CA certificate with subject principal: " + caCert.getSubjectX500Principal()
					+ ", signing cert principal: " + signingCertToVerify.getSubjectX500Principal()
					+ ", serial number: " + signingCertToVerify.getSerialNumber());
			return;
		}
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		X509CRL x509Crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crl.getEncodedCrl()));

		PKIXParameters params = new PKIXParameters(makeTrustAnchors(caCert));
		params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(Collections.singletonList(x509Crl))));

		CertPath cp = cf.generateCertPath(Collections.singletonList(signingCertToVerify));
		CertPathValidator.getInstance("PKIX", "BC").validate(cp, params);
	}

	private Set<TrustAnchor> makeTrustAnchors(X509Certificate caCert) {
		Set<TrustAnchor> trustedCerts = new HashSet<>();
		trustedCerts.add(new TrustAnchor(caCert, null));
		return trustedCerts;
	}

	private X509Certificate verifySignatureAndGetSigningCertificate(byte[] data, byte[] signatureFile) throws CryptoException {
		return new CmsDecoder().verifySignatureAndReturnSecurityCertificate(data, signatureFile);
	}

	private SertifikatOgNøkkel getElectionEventPKCS12(final UserData userData, final Long electionEventPk) {
		final String cacheKey = electionEventPk + "-p12";
		if (electionEventPKCS12Cache.isKeyInCache(cacheKey)) {
			Element result = electionEventPKCS12Cache.get(cacheKey);
			if (result != null) {
				return (SertifikatOgNøkkel) result.getObjectValue();
			}
		}

		SigningKey electionEventSigningKey = signingKeyRepository.getSigningKeyForElectionEventSigning(userData.getElectionEventPk());

		String password = getSymmetricPassword();
		byte[] p12Password = decryptSymmetricallyOpenSSL(Base64.decodeBase64(electionEventSigningKey.getKeyEncryptedPassphrase()), password);
		
		try (InputStream fis = new ByteArrayInputStream(electionEventSigningKey.getBinaryData().getBinaryData())) {
			Pkcs12Decoder pkcs12Decoder = new Pkcs12Decoder();
			SertifikatOgNøkkel sertifikatOgNøkkel = pkcs12Decoder.lesPkcs12(fis, new String(p12Password, CHARACTER_SET));
			electionEventPKCS12Cache.put(new Element(cacheKey, sertifikatOgNøkkel));
			
			return sertifikatOgNøkkel;
		} catch (IOException | CryptoException e) {
			throw new EvoteSecurityException(e.getMessage(), e);
		}
	}

	private String getSymmetricPassword() {
		String password = systemPasswordStore.getPassword();

		if (password == null) {
			throw new EvoteSecurityException("@config.certificate_management.error.error");
		}
		return password;
	}

}
