package no.evote.service.voting;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.voting.domain.model.Voting;

public interface VotingRejectionService extends Serializable {
	List<VotingRejection> findByEarly(UserData userData, final Voting voting);

	@Cacheable
	VotingRejection findByPk(UserData userData, Long pk);
}
