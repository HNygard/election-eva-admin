package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionDayAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionEventAuditEvent;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Brukere_Roller;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Valghendelse;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Oversettelser;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Oversikt;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Opprett;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Kopier;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDomainModel;
import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDto;
import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDtoList;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ElectionEventService")


@Default
@Remote(ElectionEventService.class)
@NoArgsConstructor //For CDI
public class ElectionEventApplicationService implements ElectionEventService {
    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private ElectionEventDomainService electionEventService;

    /**
     * Creates new election event asynchronously
     */
    @Override
    @Asynchronous
    @Security(accesses = Konfigurasjon_Valghendelse_Opprett, type = WRITE)
    @AuditLog(eventClass = ElectionEventAuditEvent.class, eventType = AuditEventTypes.Create)
    public void createAsync(UserData userData, ElectionEvent electionEventTo, boolean copyRoles, VotingHierarchy votingHierarchy,
                            CountingHierarchy countingHierarchy, ElectionEvent electionEventFrom, Set<Locale> localeSet) {
        electionEventService.create(
                userData, electionEventTo, copyRoles, votingHierarchy, countingHierarchy, electionEventFrom, localeSet);
    }

    /**
     * Creates new election event
     */
    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Opprett, type = WRITE)
    @AuditLog(eventClass = ElectionEventAuditEvent.class, eventType = AuditEventTypes.Create)
    public ElectionEvent create(UserData userData, ElectionEvent electionEventTo, boolean copyRoles, VotingHierarchy votingHierarchy,
                                CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, Set<Locale> localeSet) {
        return electionEventService.create(
                userData, electionEventTo, copyRoles, votingHierarchy, countingHierarchy, fromElectionEvent, localeSet);
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = WRITE)
    @AuditLog(eventClass = ElectionEventAuditEvent.class, eventType = AuditEventTypes.Update)
    public ElectionEvent update(final UserData userData, final ElectionEvent electionEvent, final Set<Locale> localeSet) {
        return electionEventService.update(userData, electionEvent, localeSet);
    }

    @Override
    @Security(accesses = Aggregert_Valghendelse, type = READ)
    public ElectionEvent findById(UserData userData, String id) {
        return electionEventRepository.findById(id);
    }

    @Override
    @SecurityNone
    public ElectionEvent findByPk(final Long pk) {
        return electionEventService.findByPk(pk);
    }

    @Override
    @Security(accesses = {Aggregert_Valghendelse, Aggregert_Brukere_Roller}, type = READ)
    public List<ElectionEvent> findAll(UserData userData) {
        return electionEventRepository.findAll();
    }

    @Override
    @Security(accesses = Konfigurasjon_Oversikt, type = WRITE)
    public void approveConfiguration(UserData userData, Long pk) {
        electionEventService.approveConfiguration(userData, pk);
    }

    @Override
    @Security(accesses = Tilgang_Roller_Kopier, type = WRITE)
    public void copyRoles(final UserData userData, final ElectionEvent electionEventFrom, final ElectionEvent electionEventTo) {
        electionEventRepository.copyRoles(electionEventFrom, electionEventTo);
    }

    @Override
    @Security(accesses = Aggregert_Valghendelse, type = READ)
    public List<ElectionEventStatus> findAllElectionEventStatuses(UserData userData) {
        return electionEventRepository.findAllElectionEventStatuses();
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = WRITE)
    @AuditLog(eventClass = ElectionDayAuditEvent.class, eventType = AuditEventTypes.Create)
    public ElectionDay createElectionDay(final UserData userData, final ElectionDay electionDay) {
        return toDto(electionEventRepository.createElectionDay(userData, toDomainModel(electionDay)));
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = WRITE)
    @AuditLog(eventClass = ElectionDayAuditEvent.class, eventType = AuditEventTypes.Update)
    public ElectionDay updateElectionDay(final UserData userData, final ElectionDay electionDay) {
        return toDto(electionEventRepository.updateElectionDay(userData, toDomainModel(electionDay)));
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = WRITE)
    @AuditLog(eventClass = ElectionDayAuditEvent.class, eventType = AuditEventTypes.Delete)
    public void deleteElectionDay(final UserData userData, final ElectionDay electionDay) {
        electionEventRepository.deleteElectionDay(userData, toDomainModel(electionDay));
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = READ)
    public ElectionDay findElectionDayByPk(final UserData userData, final Long electionDayPk) {
        return toDto(electionEventRepository.findElectionDayByPk(electionDayPk));
    }

    @Override
    @Security(accesses = {Konfigurasjon_Geografi, Konfigurasjon_Grunnlagsdata_Redigere, Aggregert_Valghendelse}, type = READ)
    public List<ElectionDay> findElectionDaysByElectionEvent(final UserData userData, final ElectionEvent electionEvent) {
        return toDtoList(electionEventRepository.findElectionDaysByElectionEvent(electionEvent));
    }

    @Override
    @Security(accesses = Aggregert_Valghendelse, type = READ)
    public List<ElectionEventLocale> getElectionEventLocalesForEvent(final UserData userData, final ElectionEvent electionEvent) {
        return electionEventRepository.getElectionEventLocalesForEvent(electionEvent);
    }

    @Override
    @Security(accesses = Konfigurasjon_Oversettelser, type = READ)
    public List<Locale> getLocalesForEvent(final UserData userData, final ElectionEvent electionEvent) {
        return electionEventRepository.getLocalesForEvent(electionEvent);
    }
}
