package no.valg.eva.admin.common.rbac.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.rbac.Access;

/**
 * Defines services for rbac.
 */
public interface AccessService extends Serializable {

	/**
	 * Finds accesses for roles
	 * 
	 * @param userData contains a set of roles
	 * 
	 * @return cache with set of accesses
	 */
	AccessCache findAccessCacheFor(UserData userData);

	/**
	 * Finds all accesses available
	 */
	@Cacheable
	List<Access> findAll(UserData userData);

}
