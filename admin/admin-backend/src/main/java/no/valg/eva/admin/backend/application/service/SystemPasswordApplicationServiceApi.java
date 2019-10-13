package no.valg.eva.admin.backend.application.service;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.service.security.SystemPasswordService;
import no.valg.eva.admin.common.rbac.SecurityNone;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * DEV-NOTE: Merk at denne klassen avviker fra vanlig navnestandard. Årsaken er at @Stateless-annotasjonen 
 *           forhindrer oss i å bruke @Inject for applikasjonstjenester. I de tilfellene hvor vi ønsker å
 *           bruke applikasjonstjenester internt, så må vi derfor skille "APIet" til applikasjonstjenester 
 *           ut i en egen klasse.
 */
@Stateless(name = "SystemPasswordService")


@Default
@Remote(SystemPasswordService.class)
public class SystemPasswordApplicationServiceApi implements SystemPasswordService {

	@Inject
	private SystemPasswordApplicationService systemPasswordApplicationService;

	/**
	 * Returns true or false based on if the system password is set or not
	 */
	@Override
	@SecurityNone
	public boolean isPasswordSet() {
		return systemPasswordApplicationService.isPasswordSet();
	}
}
