package no.valg.eva.admin.voting.application;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.CentrallyRegisteredVotingAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.VotingAuditEvent;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.domain.service.VotingRegistrationDomainService;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer_Sent_Innkommet;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer_Sentralt;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Valgting_Registrer_Sentralt;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

@Stateless(name = "VotingRegistrationService")
@Remote(VotingRegistrationService.class)
@NoArgsConstructor //CDI
public class VotingRegistrationApplicationService implements VotingRegistrationService {

    private static final long serialVersionUID = -8020664742972491965L;

    @Inject
    private VotingRegistrationDomainService votingRegistrationDomainService;
    @Inject
    private MunicipalityRepository municipalityRepository;

    @Override
    @Security(accesses = { Stemmegiving_Forhånd_Registrer, Stemmegiving_Forhånd_Registrer_Sent_Innkommet, Stemmegiving_Forhånd_Registrer_Sentralt }, type = WRITE)
    @AuditLog(eventClass = VotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
    public Voting registerAdvanceVotingInEnvelope(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Municipality municipalityDto, Voter voter, VotingCategory votingCategory, boolean isLate, VotingPhase votingPhase) {
        no.valg.eva.admin.configuration.domain.model.Municipality municipality = municipalityRepository.findByPk(municipalityDto.getPk());
        return votingRegistrationDomainService.registerAdvanceVotingInEnvelope(userData, pollingPlace, electionGroup, municipality, voter, votingCategory, isLate, votingPhase);
    }

    @Override
    @Security(accesses = { Stemmegiving_Forhånd_Registrer_Sentralt, Stemmegiving_Valgting_Registrer_Sentralt }, type = WRITE)
    @AuditLog(eventClass = CentrallyRegisteredVotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
    public Voting registerElectionDayVotingInEnvelopeCentrally(UserData userData, ElectionGroup electionGroup, no.valg.eva.admin.configuration.domain.model.Municipality municipality, Voter voter, VotingCategory votingCategory, VotingPhase votingPhase) {
        return votingRegistrationDomainService.registerElectionDayVotingInEnvelopeCentrally(userData, electionGroup, municipality, voter, votingCategory, votingPhase);
    }
}
