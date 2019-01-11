package no.evote.service.security;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.evote.service.AccessTokenAndSignature;
import no.evote.service.LegacyUserDataService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.service.AccessServiceBean;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.InetAddress;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Importer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "LegacyUserDataService")
@Remote(LegacyUserDataService.class)
public class LegacyUserDataServiceEjb implements LegacyUserDataService {
	@Inject
	private LegacyUserDataServiceBean userDataService;
	@Inject
	private AccessServiceBean accessServiceBean;

	/**
	 * Creates and populates an UserData object based on the identifiers sent in as parameters
	 */
	@Override
	@SecurityNone
	public UserData getUserData(String operatorId, String roleId, String areaPath, String electionPath, InetAddress remoteAddr) {
		return userDataService.getUserData(operatorId, roleId, areaPath, electionPath, remoteAddr);
	}

	/**
	 * Make sure UserData is populated with AccessCache.
	 */
	@Override
	@SecurityNone
	public AccessCache getAccessCache(UserData userData) {
		return accessServiceBean.findAccessCacheFor(userData);
	}

	/**
	 * Used by web services called by scanning to verify the validity of the token with regards to the signature, user id, and expiration
	 */
	@Override
	@Security(accesses = Opptelling_Importer, type = READ)
	public boolean isFileUploadDownloadTokenValid(UserData userData, byte[] tokenZip, String userId) {
		return userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userId);
	}

	@Override
	@SecurityNone
	public AccessTokenAndSignature exportSignedAccessToken(UserData userData, Operator operator) {
		return userDataService.exportSignedAccessToken(userData, operator);
	}

}
