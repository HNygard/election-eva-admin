package no.valg.eva.admin.backend.bakgrunnsjobb.application.service;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Konfigurasjon_Grunnlagsdata;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.felles.bakgrunnsjobb.service.BakgrunnsjobbService;

@Stateless(name = "BakgrunnsjobbService")
@Remote(BakgrunnsjobbService.class)
public class BakgrunnsjobbApplicationService implements BakgrunnsjobbService {

	@Inject
	private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;

	@Override
	@Security(accesses = { Konfigurasjon_Geografi, Aggregert_Konfigurasjon_Grunnlagsdata }, type = READ)
	public boolean erManntallsnummergenereringStartetEllerFullfort(UserData userData) {
		return bakgrunnsjobbDomainService.erManntallsnummergenereringStartetEllerFullfort(userData.electionEvent());
	}
}
