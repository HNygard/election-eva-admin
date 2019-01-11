package no.evote.service.security;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Sertifikater;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.KeyDomain;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.evote.service.SigningKeyServiceBean;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.common.configuration.model.SigningKeyData;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "SigningKeyService")
@Remote(SigningKeyService.class)
public class SigningKeyServiceEjb implements SigningKeyService {
	@Inject
	private SigningKeyServiceBean signingKeyService;
	@Inject
	private SigningKeyRepository signingKeyRepository;
	@Resource
	private SessionContext context;

	/**
	 * Returns a list of all election events with all key domains for it and a signing key if there is one
	 */
	@Override
	@Security(accesses = Konfigurasjon_Valghendelse_Sertifikater, type = READ)
	public List<SigningKeyData> findAllSigningKeys(UserData userData) {
		return signingKeyRepository.findAllSigningKeys();
	}

	/**
	 * Creates a signingkey, if i already exist delete the binary data attached to it
	 */
	@Override
	@Security(accesses = Konfigurasjon_Valghendelse_Sertifikater, type = WRITE)
	public SigningKey create(UserData userData, SigningKey signingKey, byte[] bytes, String fileName, String password, ElectionEvent electionEvent)
			throws IOException {
		try {
			return signingKeyService.create(userData, signingKey, bytes, fileName, password, electionEvent);
		} catch (Exception e) {
			context.setRollbackOnly();
			throw e;
		}
	}

	@Override
	@Security(accesses = Konfigurasjon_Valghendelse_Sertifikater, type = READ)
	public SigningKey findSigningKeyByPk(UserData userData, Long pk) {
		return signingKeyRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Valghendelse_Sertifikater, type = READ)
	public KeyDomain findKeyDomainById(final UserData userData, final String keyDomainId) {
		return signingKeyRepository.findKeyDomainById(keyDomainId);
	}

	@Override
	@SecurityNone
	public boolean isSigningKeySetForElectionEvent(final UserData userData, final ElectionEvent electionEvent) {
		return signingKeyRepository.isSigningKeySetForElectionEvent(electionEvent);
	}

}
