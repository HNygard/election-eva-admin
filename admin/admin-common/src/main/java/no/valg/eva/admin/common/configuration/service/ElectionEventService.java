package no.valg.eva.admin.common.configuration.service;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.security.UserData;
import no.evote.service.cache.CacheInvalidate;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface ElectionEventService extends Serializable {

	/**
	 * Creates a new election event, with the possibility of copying data from an existing election event
	 * 
	 * @param electionEventTo The new election event you want to be created
	 * @param electionEventFrom The existing election event you want to copy data from
	 * @param votingHierarchy Specifies the scope of copying of voting-related data
	 * @param countingHierarchy Specifies the scope of copying of counting-related data
	 */
	ElectionEvent create(final UserData userData, final ElectionEvent electionEventTo, final boolean copyRoles, VotingHierarchy votingHierarchy,
			CountingHierarchy countingHierarchy, final ElectionEvent electionEventFrom, final Set<Locale> localeSet);

	@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 1)
	ElectionEvent update(UserData userData, ElectionEvent electionEvent, Set<Locale> localeSet);

	ElectionEvent findByPk(Long pk);

	ElectionEvent findById(UserData userData, String id);

	List<ElectionEvent> findAll(UserData userData);

	@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 1)
	void approveConfiguration(UserData userData, Long pk);

	void copyRoles(UserData userData, final ElectionEvent electionEventFrom, final ElectionEvent electionEventTo);

	@Cacheable
	List<ElectionEventStatus> findAllElectionEventStatuses(UserData userData);

	ElectionDay createElectionDay(UserData userData, ElectionDay electionDay);

	ElectionDay updateElectionDay(UserData userData, ElectionDay electionDay);

	void deleteElectionDay(UserData userData, ElectionDay electionDay);

	List<ElectionDay> findElectionDaysByElectionEvent(UserData userData, ElectionEvent electionEvent);

	ElectionDay findElectionDayByPk(UserData userData, Long electionDayPk);

	List<ElectionEventLocale> getElectionEventLocalesForEvent(UserData userData, ElectionEvent electionEvent);

	List<Locale> getLocalesForEvent(UserData userData, ElectionEvent electionEvent);

	/**
	 * Creates new election event asynchronously
	 */
	void createAsync(UserData userData, ElectionEvent electionEventTo, boolean copyRoles, VotingHierarchy votingHierarchy,
			CountingHierarchy countingHierarchy, ElectionEvent electionEventFrom, Set<Locale> localeSet);
}
