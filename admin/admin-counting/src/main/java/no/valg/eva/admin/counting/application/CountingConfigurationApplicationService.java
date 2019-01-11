package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.CountingConfigurationService;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.service.CountingConfigurationDomainService;

@Stateless(name = "CountingConfigurationService")
@Remote(CountingConfigurationService.class)
public class CountingConfigurationApplicationService implements CountingConfigurationService {
	@Inject
	private CountingConfigurationDomainService countingConfigurationDomainService;

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public CountingConfiguration getCountingConfiguration(UserData userData, CountContext countContext, AreaPath areaPath) {
		return countingConfigurationDomainService.getCountingConfiguration(countContext, areaPath);
	}

}
