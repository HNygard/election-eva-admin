package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsvalgstyrer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.configuration.application.ResponsibleOfficerMapper.toResponsibleOfficer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import no.evote.security.UserData;
import no.evote.service.configuration.ResponsibleOfficerServiceBean;
import no.evote.validation.ValideringVedManuellRegistrering;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.ElectoralRollSearchAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.config.ResponsibleOfficerAuditEvent;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;
import no.valg.eva.admin.common.configuration.model.local.DisplayOrder;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.configuration.service.ReportingUnitService;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvElectionReportingUnitsRepository;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import org.apache.log4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ReportingUnitService")


@Default
@Remote(ReportingUnitService.class)
public class ReportingUnitApplicationService implements ReportingUnitService {
    private static final Logger LOGGER = Logger.getLogger(ReportingUnitApplicationService.class);

    private static final int MAX_RESULT_SIZE = 50;

    private ReportingUnitDomainService reportingUnitDomainService;
    private ResponsibleOfficerRepository responsibleOfficerRepository;
    private ResponsibleOfficerServiceBean responsibleOfficerService;
    private VoterRepository voterRepository;
    private MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository;

    @Inject
    public ReportingUnitApplicationService(ReportingUnitDomainService reportingUnitDomainService, ResponsibleOfficerRepository responsibleOfficerRepository,
                                           ResponsibleOfficerServiceBean responsibleOfficerService, VoterRepository voterRepository,
                                           MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository) {
        this.reportingUnitDomainService = reportingUnitDomainService;
        this.responsibleOfficerRepository = responsibleOfficerRepository;
        this.responsibleOfficerService = responsibleOfficerService;
        this.voterRepository = voterRepository;
        this.mvElectionReportingUnitsRepository = mvElectionReportingUnitsRepository;
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = READ)
    public List<ResponsibleOfficer> findByArea(UserData userData, AreaPath areaPath) {
        no.valg.eva.admin.configuration.domain.model.ReportingUnit db = reportingUnitDomainService.getReportingUnit(userData, areaPath);
        List<no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer> officers = responsibleOfficerRepository
                .findResponsibleOfficersForReportingUnit(db.getPk());
        List<ResponsibleOfficer> result = new ArrayList<>();
        for (no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer : officers) {
            ResponsibleOfficer officer = toResponsibleOfficer(dbOfficer);
            result.add(officer);
        }
        return result;
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = WRITE)
    @AuditLog(eventClass = ResponsibleOfficerAuditEvent.class, eventType = AuditEventTypes.Save)
    public ResponsibleOfficer save(UserData userData, ResponsibleOfficer responsibleOfficer) {
        no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer = getDBResponsibleOfficer(userData, responsibleOfficer);

        dbOfficer = responsibleOfficerService.save(userData, dbOfficer);

        return toResponsibleOfficer(dbOfficer);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = WRITE)
    public List<ResponsibleOfficer> saveResponsibleOfficerDisplayOrder(UserData userData, AreaPath areaPath, List<DisplayOrder> displayOrders) {
        for (DisplayOrder displayOrder : displayOrders) {
            no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer officer = responsibleOfficerRepository.findByPk(displayOrder.getPk());
            officer.checkVersion(displayOrder);
            officer.setDisplayOrder(displayOrder.getDisplayOrder());
            responsibleOfficerService.save(userData, officer);
        }
        return findByArea(userData, areaPath);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = READ)
    @AuditLog(eventClass = ElectoralRollSearchAuditEvent.class, eventType = AuditEventTypes.SearchElectoralRoll)
    public List<ResponsibleOfficer> search(UserData userData, AreaPath areaPath, ElectoralRollSearch electoralRollSearch) {
        List<Voter> voters = new ArrayList<>();
        if (electoralRollSearch.hasValidSsn()) {
            voters = voterRepository.findByElectionEventAreaAndId(userData.getElectionEventPk(), areaPath, electoralRollSearch.getSsn());
        } else if (electoralRollSearch.hasValidBirthDateAndOrName()) {
            Voter voter = new Voter();
            voter.setDateOfBirth(electoralRollSearch.getBirthDate());
            voter.setNameLine(electoralRollSearch.getName());
            voters = voterRepository.searchVoter(voter, areaPath.getCountyId(), areaPath.getMunicipalityId(), null,
                    MAX_RESULT_SIZE, false, userData.getElectionEventPk());
        }
        return voters.stream().map(ResponsibleOfficerMapper::toResponsibleOfficer).collect(Collectors.toList());
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = WRITE)
    @AuditLog(eventClass = ResponsibleOfficerAuditEvent.class, eventType = AuditEventTypes.Delete)
    public void delete(UserData userData, ResponsibleOfficer responsibleOfficer) {
        no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer = responsibleOfficerRepository.findByPk(responsibleOfficer.getPk());
        dbOfficer.checkVersion(responsibleOfficer);
        responsibleOfficerService.delete(userData, dbOfficer);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = READ)
    public boolean hasReportingUnitTypeConfigured(UserData userData, ReportingUnitTypeId reportingUnitTypeId) {
        return mvElectionReportingUnitsRepository.hasReportingUnitTypeConfigured(userData, reportingUnitTypeId);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = READ)
    public boolean validate(UserData userData, ResponsibleOfficer selectedResponsibleOfficer) {
        no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbResponsibleOfficer = getDBResponsibleOfficer(userData, selectedResponsibleOfficer);

        return validateResponsibleOfficerConstraints(selectedResponsibleOfficer, dbResponsibleOfficer);
    }

    private boolean validateResponsibleOfficerConstraints(ResponsibleOfficer selectedResponsibleOfficer, 
                                                          no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbResponsibleOfficer) {
        if (selectedResponsibleOfficer.isSkalValideres()) {
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer>> validationResult = validator
                    .validate(dbResponsibleOfficer, ValideringVedManuellRegistrering.class);
            validationResult.forEach(violation -> logConstraintViolation(dbResponsibleOfficer, violation));
            return validationResult.isEmpty();
        } else {
            return true;
        }
    }

    private no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer getDBResponsibleOfficer(UserData userData, ResponsibleOfficer responsibleOfficer) {
        no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer;
        if (responsibleOfficer.getPk() == null) {
            dbOfficer = new no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer();
            dbOfficer.setReportingUnit(reportingUnitDomainService.getReportingUnit(userData, responsibleOfficer.getAreaPath()));
        } else {
            dbOfficer = responsibleOfficerRepository.findByPk(responsibleOfficer.getPk());

            //Detaching to make sure we are not updating the object
            responsibleOfficerRepository.detach(dbOfficer);
            dbOfficer.checkVersion(responsibleOfficer);
        }
        dbOfficer.setResponsibility(responsibleOfficerRepository.findResponsibilityById(responsibleOfficer.getResponsibilityId()));

        ResponsibleOfficerMapper.merge(dbOfficer, responsibleOfficer);

        return dbOfficer;
    }

    private void logConstraintViolation(no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer responsibleOfficer,
                                        ConstraintViolation<no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer> constraintViolation) {
        LOGGER.info("Forsøk på å legge til responsibleOfficer med navn " + responsibleOfficer.getNameLine() + " har valideringsfeil. "
                + "Felt=" + constraintViolation.getPropertyPath()
                + ", verdi=" + constraintViolation.getInvalidValue()
                + ", valideringsregel=" + constraintViolation.getMessage());
    }
}
