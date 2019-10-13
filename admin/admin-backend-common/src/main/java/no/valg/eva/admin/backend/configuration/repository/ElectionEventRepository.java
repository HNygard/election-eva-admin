package no.valg.eva.admin.backend.configuration.repository;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static no.valg.eva.admin.util.TidtakingUtil.taTiden;

@Default
@ApplicationScoped

public class ElectionEventRepository extends BaseRepository {
    private static final Logger LOG = Logger.getLogger(ElectionEventRepository.class);

    public ElectionEventRepository() {
    }

    public ElectionEventRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public ElectionEvent create(final UserData userData, final ElectionEvent electionEventTo) {
        return createEntity(userData, electionEventTo);
    }

    public void createElectionEventLocale(final UserData userData, final ElectionEventLocale electionEventLocale) {
        createEntity(userData, electionEventLocale);
    }

    public List<ElectionEventLocale> getElectionEventLocalesForEvent(final ElectionEvent electionEvent) {
        return findEntitiesByElectionEvent(ElectionEventLocale.class, electionEvent.getPk());
    }

    public void deleteElectionEventLocaleForElectionEvent(final ElectionEvent electionEvent, final Locale locale) {
        Query query = getEm().createNativeQuery(
                "DELETE FROM election_event_locale eel WHERE eel.election_event_pk = :electionEventPk AND eel.locale_pk = :localePk");
        query.setParameter("electionEventPk", electionEvent.getPk());
        query.setParameter("localePk", locale.getPk());
        query.executeUpdate();
    }

    public List<Locale> getLocalesForEvent(final ElectionEvent electionEvent) {
        List<Locale> localeList = new ArrayList<>();
        List<ElectionEventLocale> electionEventLocaleList = getElectionEventLocalesForEvent(electionEvent);
        for (ElectionEventLocale electionEventLocale : electionEventLocaleList) {
            localeList.add(electionEventLocale.getLocale());
        }
        return localeList;
    }

    public List<ElectionDay> findElectionDaysByElectionEvent(final ElectionEvent electionEvent) {
        return findEntitiesByElectionEvent(ElectionDay.class, electionEvent.getPk());
    }

    public ElectionDay findElectionDayByPk(final Long electionDayPk) {
        return findEntityByPk(ElectionDay.class, electionDayPk);
    }

    public ElectionEvent findByPk(final Long pk) {
        return findEntityByPk(ElectionEvent.class, pk);
    }

    public List<ElectionEvent> findAll() {
        return findAllEntities(ElectionEvent.class);
    }

    public ElectionEvent findById(final String id) {
        return findEntityById(ElectionEvent.class, id);
    }

    @SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
    public List<ElectionEvent> findAllActiveElectionEvents() {
        Query query = getEm().createNamedQuery("ElectionEvent.findAllActive");
        query.setParameter("adminEventId", ROOT_ELECTION_EVENT_ID);
        return query.getResultList();
    }

    public LocalDate findLatestElectionDay(final ElectionEvent electionEvent) {
        LocalDate latestDate = null;
        List<ElectionDay> electionDays = findElectionDaysByElectionEvent(electionEvent);

        for (ElectionDay electionDay : electionDays) {
            LocalDate date = electionDay.getDate();
            if (latestDate == null || (date != null && date.isAfter(latestDate))) {
                latestDate = date;
            }
        }

        return latestDate;
    }

    public ElectionEvent update(final UserData userData, final ElectionEvent electionEvent) {
        return updateEntity(userData, electionEvent);
    }

    public List<ElectionEventStatus> findAllElectionEventStatuses() {
        return findAllEntities(ElectionEventStatus.class);
    }

    public ElectionEventStatus findElectionEventStatusById(final int id) {
        return findEntityById(ElectionEventStatus.class, id);
    }

    public ElectionDay createElectionDay(final UserData userData, final ElectionDay electionDay) {
        return createEntity(userData, electionDay);
    }

    public ElectionDay updateElectionDay(final UserData userData, final ElectionDay electionDay) {
        return updateEntity(userData, electionDay);
    }

    public void deleteElectionDay(final UserData userData, final ElectionDay electionDay) {
        deleteEntity(userData, ElectionDay.class, electionDay.getPk());
    }

    public void deleteElectionEvent(final UserData userData, final Long electionEventPk) {
        ElectionEvent electionEvent = findByPk(electionEventPk);
        if (electionEvent.getId().equals(ROOT_ELECTION_EVENT_ID)) {
            throw new EvoteException("Cannot delete admin election event");
        }

        deleteEntity(userData, ElectionEvent.class, electionEventPk);
    }

    public void delete(UserData userData, Long pk) {
        super.deleteEntity(userData, ElectionEvent.class, pk);
    }

    public void copyRoles(ElectionEvent electionEventFrom, ElectionEvent electionEventTo) {
        copyRoles(electionEventFrom, electionEventTo, null);
    }

    public void copyRoles(ElectionEvent electionEventFrom, ElectionEvent electionEventTo, String roleId) {
        taTiden(LOG, "Kopierer Roller/Roles", () -> {
            String queryString = "/* NO LOAD BALANCE */SELECT copy_roles(?, ?, ?)";
            Query query = getEm().createNativeQuery(queryString);
            query.setParameter(1, electionEventFrom.getPk().intValue());
            query.setParameter(2, electionEventTo.getPk().intValue());
            query.setParameter(3, StringUtils.isEmpty(roleId) ? "" : roleId);
            return (int) query.getSingleResult();
        });
    }

    public void copyAreas(final ElectionEvent electionEventFrom, final ElectionEvent electionEventTo) {
        taTiden(LOG, "Kopierer Områder/Areas", () -> {
            String queryString = "/* NO LOAD BALANCE */SELECT copy_areas(?, ?)";
            return eksekverKopiering(queryString, electionEventFrom, electionEventTo);
        });
    }

    public void copyElections(ElectionEvent electionEventFrom, ElectionEvent electionEventTo) {
        taTiden(LOG, "Kopierer Områder/Areas", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_elections(?, ?)";
            return eksekverKopiering(queryString, electionEventFrom, electionEventTo);
        });
    }

    public void copyElectoralRoll(final ElectionEvent electionEventFrom, final ElectionEvent electionEventTo) {
        taTiden(LOG, "Kopierer Velgere/Voters", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_voter(?,?)";
            return eksekverKopiering(queryString, electionEventFrom, electionEventTo);
        });
    }

    public void copyContestReports(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer ContestReports", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_contest_reports(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyVoteCounts(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer VoteCounts", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_vote_counts(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyBallotCounts(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer BallotCounts", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_ballot_counts(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyModifiedBallots(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer ModifiedBallots", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_cast_votes(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyCandidateVotes(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer CandidateVotes", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_candidate_votes(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyVotings(final ElectionGroup electionGroupFrom, final ElectionGroup electionGroupTo, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer Styremedlemmer/ResponsibleOfficers", () -> {
            String queryString = "/* NO LOAD BALANCE */SELECT copy_votings(?, ?, ?)";
            Query query = getEm().createNativeQuery(queryString);
            query.setParameter(1, electionGroupFrom.getPk().intValue());
            query.setParameter(2, electionGroupTo.getPk().intValue());
            query.setParameter(3, toElectionEvent.getPk().intValue());
            return (int) query.getSingleResult();
        });
    }

    public void copyManualVoting(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer ManualVotings", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_manual_votings(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyReportingUnits(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer Styrer/ReportingUnits", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_reporting_units(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });

        copyResponsibleOfficer(fromElectionEvent, toElectionEvent);

    }

    private void copyResponsibleOfficer(final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer Styremedlemmer/ResponsibleOfficers", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_responsible_officers(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    public void copyPartyNumberConfiguration(ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        taTiden(LOG, "Kopierer Partinummere/PartyNumbers", () -> {
            String queryString = "/* NO LOAD BALANCE */select copy_party_number(?,?)";
            return eksekverKopiering(queryString, fromElectionEvent, toElectionEvent);
        });
    }

    private Integer eksekverKopiering(String sporring, ElectionEvent fra, ElectionEvent til) {
        Query query = getEm().createNativeQuery(sporring);
        query.setParameter(1, fra.getPk().intValue());
        query.setParameter(2, til.getPk().intValue());
        return (int) query.getSingleResult();
    }
}
