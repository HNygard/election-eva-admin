package no.evote.service.security;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import no.evote.model.KeyDomain;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.SigningKeyData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public interface SigningKeyService extends Serializable {
	List<SigningKeyData> findAllSigningKeys(UserData userData);

	SigningKey findSigningKeyByPk(UserData userData, Long pk);

	KeyDomain findKeyDomainById(UserData userData, String keyDomainId);

	SigningKey create(UserData userData, SigningKey signingKey, byte[] bytes, String fileName, String password, ElectionEvent electionEvent) throws IOException;

	boolean isSigningKeySetForElectionEvent(UserData userData, ElectionEvent electionEvent);
}
