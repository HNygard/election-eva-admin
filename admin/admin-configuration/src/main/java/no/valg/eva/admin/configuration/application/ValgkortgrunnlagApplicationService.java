package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Generer_Valgkortgrunnlag;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.ValgkortgrunnlagService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.service.ValgkortgrunnlagDomainService;

@Stateless(name = "ValgkortgrunnlagService")
@Remote(ValgkortgrunnlagService.class)
public class ValgkortgrunnlagApplicationService implements ValgkortgrunnlagService {

	@Inject
	private ValgkortgrunnlagDomainService valgkortgrunnlagDomainService;

	@Override
	@Security(accesses = { Manntall_Generer_Valgkortgrunnlag }, type = WRITE)
	@Asynchronous
	public void genererValgkortgrunnlag(UserData userData, boolean tillatVelgereIkkeTilknyttetValgdistrikt) {
		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, tillatVelgereIkkeTilknyttetValgdistrikt);
	}

	@Override
	@Security(accesses = { Manntall_Generer_Valgkortgrunnlag }, type = READ)
	public GenererValgkortgrunnlagStatus sjekkForutsetningerForGenerering(UserData userData) {
		return valgkortgrunnlagDomainService.sjekkForutsetningerForGenerering(userData.electionEvent());
	}
}
