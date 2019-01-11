package no.valg.eva.admin.common.rbac.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

import java.io.Serializable;
import java.net.InetAddress;

public interface UserDataService extends Serializable {
	
	UserData createUserDataAndCheckOperator(String uid, String securityLevel, String locale, InetAddress clientAddress);

	UserData setAccessCacheOnUserData(UserData userData, OperatorRole operatorRole);

	UserMenuMetadata findUserMenuMetadata(UserData userData);

}
