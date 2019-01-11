package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;
import no.valg.eva.admin.configuration.domain.model.Voter;

public interface ProposerService extends Serializable {
	List<Proposer> findByBallot(UserData userData, Long ballotPk);

	Proposer createNewProposer(UserData userData, Ballot ballot);

	void createDefaultProposers(UserData userData, Ballot ballot);

	Proposer findByBallotAndOrder(UserData userData, Long ballotPk, int displayOrder);

	@Cacheable
	List<ProposerRole> findSelectiveProposerRoles(UserData userData);

	@Cacheable
	ProposerRole findProposerRoleByPk(UserData userData, Long proposerRolePk);

	Proposer create(UserData userData, Proposer toProposer);

	Proposer update(UserData userData, Proposer proposer);

	void deleteAndReorder(UserData userData, Proposer proposer, Long ballotPk);

	Proposer validate(UserData userData, Proposer proposer, Long ballotPk);

	List<Proposer> changeDisplayOrder(UserData userData, Proposer proposer, int fromPosition, int toPosition);

	Proposer convertVoterToProposer(UserData userData, Proposer proposer, Voter voter);

	Proposer setMockIdForEmptyId(UserData userData, Proposer proposer, Long ballotPk, Map<String, Proposer> existingIds);

	List<Voter> searchVoter(UserData userData, Proposer proposer, String electionEventId, Set<MvArea> mvAreas);
}
