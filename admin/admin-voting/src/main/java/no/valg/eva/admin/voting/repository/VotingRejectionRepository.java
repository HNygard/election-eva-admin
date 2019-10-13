package no.valg.eva.admin.voting.repository;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Default
@ApplicationScoped

public class VotingRejectionRepository extends BaseRepository {
	public VotingRejectionRepository() {
	}

	public VotingRejectionRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VotingRejection findByPk(Long pk) {
		return super.findEntityByPk(VotingRejection.class, pk);
	}

    public VotingRejection findById(String id) {
        return super.findEntityById(VotingRejection.class, id);
    }

	public List<VotingRejection> findByEarly(Voting voting) {
		TypedQuery<VotingRejection> query = getEm().createNamedQuery("VotingRejection.findByEarly", VotingRejection.class)
				.setParameter("earlyVoting", voting.getVotingCategory().isEarlyVoting());
		setCacheHint(query);
		return query.getResultList();
	}
	
	public List<VotingRejection> findAll() {
		return super.findAllEntities(VotingRejection.class);
	}
}
