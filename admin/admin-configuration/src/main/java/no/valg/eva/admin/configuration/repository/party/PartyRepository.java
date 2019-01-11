package no.valg.eva.admin.configuration.repository.party;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PartyRepository extends BaseRepository {
	protected static final String ELECTION_EVENT_PK = "electionEventPk";

	public PartyRepository() {
		// brukes av CDI / Weld
	}

	public PartyRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Party create(UserData userData, Party party) {
		return createEntity(userData, party);
	}

	public Party findByPk(Long pk) {
		return findEntityByPk(Party.class, pk);
	}

	public Party findDetachedByPk(Long pk) {
		Party party = findEntityByPk(Party.class, pk);
		// trigg lasting før detach
		party.getPartyContestAreas().size();
		detach(Collections.singletonList(party));
		return party;
	}

	public void delete(UserData userData, Long pk) {
		deleteEntity(userData, Party.class, pk);
	}

	public Party update(UserData userData, Party party) {
		return updateEntity(userData, party);
	}

	public List<Party> findAllPartiesInEvent(final Long electionEventPk) {
		return getEm()
				.createNamedQuery("Party.findPartiesInEvent", Party.class)
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.getResultList();
	}

	public List<Party> getPartyWithoutAffiliationList(Contest contest) {
		return getEm()
				.createNamedQuery("Party.findWithNoAffiliationByContest", Party.class)
				.setParameter("contestPk", contest.getPk())
				.setParameter("areaId", contest.areaIdForLocalParties())
				.getResultList();
	}

	public Party findPartyByIdAndEvent(String partyId, Long electionEventPk) {
		try {
			return getEm()
					.createNamedQuery("Party.findById", Party.class)
					.setParameter(ELECTION_EVENT_PK, electionEventPk)
					.setParameter("id", partyId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Party findPartyByShortCodeAndEvent(Integer shortCode, Long electionEventPk) {
		try {
			return getEm()
					.createNamedQuery("Party.findByShortCodeEvent", Party.class)
					.setParameter(ELECTION_EVENT_PK, electionEventPk)
					.setParameter("shortCode", shortCode).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean partyIdExist(String partyId, long electionEventPk) {
		return getEm()
				.createNamedQuery("Party.countPartiesWithId", Long.class)
				.setParameter("id", partyId)
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.getSingleResult() > 0;
	}

	public List<Party> findAllButNotBlank(Long electionEventPk) {
		return findAllPartiesInEvent(electionEventPk)
				.stream()
				.filter(party -> !party.getId().equalsIgnoreCase(EvoteConstants.PARTY_ID_BLANK))
				.collect(Collectors.toList());
	}
	
	public List<Party> findAllForAreaPathAndElectionPath(AreaPath areaPath, ElectionPath electionPath) {
		return getEm()
				.createNamedQuery("Party.findAllForAreaPathAndElectionPath", Party.class)
				.setParameter("areaPath", areaPath.toString())
				.setParameter("electionPath", electionPath.toString())
				.getResultList();
	}
}
