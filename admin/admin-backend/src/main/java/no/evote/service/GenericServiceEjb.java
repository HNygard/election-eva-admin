package no.evote.service;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.GenericRepository;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionLevel;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;

import org.apache.log4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * A convenience class for getting different entities by primary key. Makes it possible to create generic functionality.
 * 
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "GenericService")


@Default
@Remote(GenericService.class)
public class GenericServiceEjb implements GenericService {
	private static final Logger LOGGER = Logger.getLogger(GenericServiceEjb.class);

	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private CandidateRepository candidateRepository;
	@Inject
	private BallotRepository ballotRepository;
	@Inject
	private BallotRejectionRepository ballotRejectionRepository;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private LocaleRepository localeRepository;
	@Inject
	private RoleRepository roleRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private PartyRepository partyRepository;
	@Inject
	private GenericRepository genericRepository;

	@Override
	@SecurityNone
	public BaseEntity findByPk(UserData userData, Class<?> clazz, Long pk) {
		if (clazz.equals(ElectionEvent.class)) {
			return electionEventRepository.findByPk(pk);
		} else if (clazz.equals(Candidate.class)) {
			return candidateRepository.findCandidateByPk(pk);
		} else if (clazz.equals(BallotRejection.class)) {
			return ballotRejectionRepository.findBallotRejectionByPk(pk);
		} else if (clazz.equals(Ballot.class)) {
			return ballotRepository.findBallotByPk(pk);
		} else if (clazz.equals(Affiliation.class)) {
			return affiliationRepository.findAffiliationByPk(pk);
		} else if (clazz.equals(Locale.class)) {
			return localeRepository.findByPk(pk);
		} else if (clazz.equals(Party.class)) {
			return partyRepository.findByPk(pk);
		} else if (clazz.equals(Role.class)) {
			return roleRepository.findByPk(pk);
		} else if (clazz.equals(ElectionLevel.class)) {
			return mvElectionRepository.findElectionLevelByPk(pk);
		} else if (clazz.equals(AreaLevel.class)) {
			return mvAreaRepository.findAreaLevelByPk(pk);
		} else if (clazz.equals(MvElection.class)) {
			return mvElectionRepository.findByPk(pk);
		} else if (clazz.equals(MvArea.class)) {
			return mvAreaRepository.findByPk(pk);
		}

		LOGGER.warn("Using generic findByPk for class: " + clazz.getName());
		return genericRepository.findByPk(clazz, pk);
	}

}
