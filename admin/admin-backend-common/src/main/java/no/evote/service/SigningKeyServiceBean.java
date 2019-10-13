package no.evote.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.model.BinaryData;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.valg.eva.admin.util.ExceptionUtil;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class SigningKeyServiceBean {
	private static final Logger LOGGER = Logger.getLogger(SigningKeyServiceBean.class);

	@Inject
	private CryptoServiceBean cryptoService;
	@Inject
	private BinaryDataServiceBean binaryDataService;
	@Inject
	private BinaryDataRepository binaryDataRepository;
	@Inject
	private SigningKeyRepository signingKeyRepository;

	public SigningKeyServiceBean() {

	}

	/**
	 * Creates a signing key. If it already exist delete the binary data attached to it
	 */
	public SigningKey create(UserData userData, SigningKey signingKey, byte[] bytes, String fileName, String password, ElectionEvent electionEvent)
			throws IOException {
		try {
			SigningKey signingKeyLocal = signingKey;

			if (signingKeyLocal.getBinaryData() != null) {
				Long binaryDataPk = signingKeyLocal.getBinaryData().getPk();
				signingKeyLocal.setBinaryData(null);
				signingKeyLocal = signingKeyRepository.update(userData, signingKeyLocal);
				binaryDataRepository.deleteBinaryData(userData, binaryDataPk);
			}

			BinaryData binaryData = binaryDataService.createBinaryData(userData, bytes, fileName, electionEvent, "signing_key", "key_binary_data_pk",
					getMimeType(fileName));
			signingKeyLocal.setBinaryData(binaryData);

			if (!signingKeyLocal.getKeyDomain().getPublicKey()) {
				String encryptedPassword = cryptPassword(password);
				signingKeyLocal.setKeyEncryptedPassphrase(encryptedPassword);
			}

			return signingKeyRepository.update(userData, signingKeyLocal);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			if (e instanceof IOException) {
				throw e;
			}
			String rootMsg = ExceptionUtil.buildErrorMessage(e);
			throw new EvoteException(rootMsg, e);
		}
	}

	private String getMimeType(final String fileName) {
		if (fileName.endsWith(".p12") || fileName.endsWith(".pfx")) {
			return "application/x-pkcs12";
		} else if (fileName.endsWith(".p7b") || fileName.endsWith(".spc")) {
			return "application/x-pkcs7-certificates";
		}
		return null;
	}

	/**
	 * Encrypts the password sent in with the current system password
	 */
	private String cryptPassword(final String password) throws UnsupportedEncodingException {
		return Base64.getEncoder().encodeToString(cryptoService.encryptWithSystemPassword(password.getBytes(EvoteConstants.CHARACTER_SET)));
	}

}
