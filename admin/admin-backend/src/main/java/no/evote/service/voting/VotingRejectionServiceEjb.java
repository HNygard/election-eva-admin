package no.evote.service.voting;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving_Prøving;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "VotingRejectionService")
@Remote(VotingRejectionService.class)
public class VotingRejectionServiceEjb implements VotingRejectionService {
	@Inject
	private VotingRejectionRepository votingRejectionRepository;

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public VotingRejection findByPk(UserData userData, Long pk) {
		return votingRejectionRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public List<VotingRejection> findByEarly(UserData userData, Voting voting) {
		return votingRejectionRepository.findByEarly(voting);
	}

}
