package no.valg.eva.admin.configuration.application.party;

import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Opprett_Eksisterende_Parti;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.service.PartiService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;

import lombok.NoArgsConstructor;

@Stateless(name = "PartiService")
@Remote(PartiService.class)
@NoArgsConstructor
public class PartiApplicationService implements PartiService {

	private PartyRepository partyRepository;
	private MvElectionRepository mvElectionRepository;
	private PartyMapper partyMapper;

	@Inject
	public PartiApplicationService(PartyRepository partyRepository, MvElectionRepository mvElectionRepository, PartyMapper partyMapper) {
		this.partyRepository = partyRepository;
		this.mvElectionRepository = mvElectionRepository;
		this.partyMapper = partyMapper;
	}

	@Override
	@Security(accesses = Listeforslag_Opprett_Eksisterende_Parti, type = READ)
	public List<Parti> partierUtenListeforslag(UserData userData, ElectionPath contestPath) {
		contestPath.assertContestLevel();
		return partyRepository.getPartyWithoutAffiliationList(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti()).getContest())
				.stream().map(party -> partyMapper.toParti(userData, party)).collect(Collectors.toList());
	}
}
