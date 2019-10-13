package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Import_Områder_Hierarki;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.io.IOException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Security;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "AreaImportService")



@Default
@Remote(AreaImportService.class)
public class AreaImportServiceEjb implements AreaImportService {
	@Inject
	private AreaImportServiceBean areaImportService;

	@Override
	@Security(accesses = Import_Områder_Hierarki, type = WRITE)
	public void importAreaHierarchy(UserData userData, byte[] data) throws IOException {
		areaImportService.importAreaHierarchy(userData, data);
	}
}
