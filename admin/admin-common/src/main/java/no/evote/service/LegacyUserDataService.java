package no.evote.service;

import java.io.Serializable;
import java.net.InetAddress;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.valg.eva.admin.rbac.domain.model.Operator;

public interface LegacyUserDataService extends Serializable {
	String ROOT_TAG = "token";
	String ACCESSES_TAG = "accesses";
	String REPORTING_UNIT_NAME_TAG = "name";
	String REPORTING_UNIT_ID_TAG = "id";
	String REPORTING_UNITS_TAG = "reportingUnits";
	String ELECTION_CONTEXT_ID_TAG = "electionContextId";
	String ELECTION_CONTEXT_NAME_TAG = "electionContextName";
	String AREA_CONTEXT_ID_TAG = "areaContextId";
	String AREA_CONTEXT_NAME_TAG = "areaContextName";
	String ROLE_TAG = "role";
	String ROLES_TAG = "roles";
	String EXPIRATION_TAG = "expiration";
	String CREATED_TAG = "created";
	String OPERATOR_NAME_TAG = "operatorName";
	String UID_TAG = "uid";
	String ELECTION_EVENT_ID_TAG = "electionEventId";
	String ELECTION_EVENT_NAME_TAG = "electionEventName";

	String ROLE_NAME_TAG = "roleName";
	String ROLE_ID_TAG = "roleId";
	String ACCESS_TAG = "access";
	String REPORTING_UNIT_TAG = "reportingUnit";

	UserData getUserData(String operatorId, String roleId, String areaPath, String electionPath, InetAddress remoteAddr);

	AccessCache getAccessCache(UserData userData);

	boolean isFileUploadDownloadTokenValid(UserData userData, byte[] token, String userId);

	AccessTokenAndSignature exportSignedAccessToken(UserData userData, Operator operator);
}
