package no.valg.eva.admin.backend.application.service;

import static no.evote.constants.EvoteConstants.CHARACTER_SET;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.model.SigningKey;
import no.evote.service.CryptoServiceBean;
import no.evote.service.security.SystemPasswordStore;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.crypto.CryptoException;
import no.valg.eva.admin.crypto.Pkcs12Decoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class SystemPasswordApplicationService {
	private static final Logger LOGGER = Logger.getLogger(SystemPasswordApplicationService.class);

	@Inject
	private SystemPasswordStore systemPasswordStore;

	@Inject
	private SigningKeyRepository signingKeyRepository;

	@Inject
	private CryptoServiceBean cryptoService;

	/**
	 * Sets the system password. If the password is already set an EvoteSecurityException is thrown
	 */
	public void setSystemPassword(final String password) {
		if (!isPasswordSet()) {
			systemPasswordStore.setPassword(password);
		} else {
			throw new EvoteSecurityException("System passphrase has already been entered");
		}
	}

	/**
	 * Returns true or false based on if the system password is set or not
	 */
	public boolean isPasswordSet() {
		return systemPasswordStore.getPassword() != null;
	}

	/**
	 * Checks if a password is correct of not. This is done by trying to decrypt an admin signing election p12 password and then opening the file
	 */
	public boolean isPasswordCorrect(final String systemPassword) {
		if (isPasswordSet()) {
			return false;
		}

		List<SigningKey> adminSigningkeys = signingKeyRepository.getAllSigningKeyForElectionEventSigning();

		if (adminSigningkeys.isEmpty()) {
			LOGGER.debug("None p12 in the database, not possible to check if system password is correct");
			return true;
		} else {
			LOGGER.debug("Admin election signing p12 in the database, checks if it is possible to decrypt password");
			try {
				
				byte[] p12Password = cryptoService.decryptSymmetricallyOpenSSL(Base64.decodeBase64(adminSigningkeys.get(0).getKeyEncryptedPassphrase()), systemPassword);

				if (p12Password == null) {
					return false;
				}

				try (InputStream fis = new ByteArrayInputStream(adminSigningkeys.get(0).getBinaryData().getBinaryData())) {
					Pkcs12Decoder pkcs12Decoder = new Pkcs12Decoder();
					return pkcs12Decoder.validerPassord(fis, new String(p12Password, CHARACTER_SET));
				}
			} catch (IOException | CryptoException e) {
				throw new EvoteException(e.getMessage(), e);
			} catch (EvoteException e) {
				LOGGER.warn(e.getMessage(), e);
				return false;
			}
		}
	}
}
