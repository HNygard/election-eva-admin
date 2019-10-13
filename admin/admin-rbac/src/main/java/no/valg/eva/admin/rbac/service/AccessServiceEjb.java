package no.valg.eva.admin.rbac.service;

import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Administrere;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Tilganger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.service.AccessService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "AccessService")
@Remote(AccessService.class)
@Default
public class AccessServiceEjb implements AccessService {
	@Inject
	private AccessServiceBean accessService;

	@Override
	@SecurityNone
	public AccessCache findAccessCacheFor(UserData userData) {
		return accessService.findAccessCacheFor(userData);
	}

	@Override
	@Cacheable
	@Security(accesses = {Tilgang_Roller_Tilganger, Tilgang_Roller_Administrere}, type = READ)
	public List<Access> findAll(UserData userData) {
		return accessService.findAll();
	}

}
