package no.valg.eva.admin.backend.application.service;

import java.util.Properties;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.valg.eva.admin.common.application.service.StatusService;
import no.valg.eva.admin.common.rbac.SecurityNone;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * DEV-NOTE: Merk at denne klassen avviker fra vanlig navnestandard. Årsaken er at @Stateless-annotasjonen 
 *           forhindrer oss i å bruke @Inject for applikasjonstjenester. I de tilfellene hvor vi ønsker å
 *           bruke applikasjonstjenester internt, så må vi derfor skille "APIet" til applikasjonstjenester 
 *           ut i en egen klasse.
 */
@Stateless(name = "StatusService")


@Default
@Remote(StatusService.class)
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class StatusApplicationServiceApi implements StatusService {

	@Inject
	private StatusApplicationService statusApplicationService;

	@Override
	@SecurityNone
	@Deprecated // Use getStatusProperties() instead
	public String getStatus() {
		return statusApplicationService.getStatus();
	}

	@Override
	@SecurityNone
	public Properties getStatusProperties() {
		return statusApplicationService.getStatusAndConfiguredVersionProperties();
	}
}
