package no.valg.eva.admin.rapport.application.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.rapport.ValghendelsesRapportAuditEvent;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rapport.service.RapportService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.SecurityType;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.rapport.domain.service.RapportDomainService;
import no.valg.eva.admin.rapport.repository.ElectionEventReportRepository;
import no.valg.eva.admin.rapport.repository.ReportRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Rediger;

@Stateless(name = "RapportService")
@Remote(RapportService.class)
public class RapportApplicationService implements RapportService {

    private static final long serialVersionUID = 1104835219421370444L;

    private RapportDomainService rapportDomainService;
    private ReportRepository reportRepository;
    private ElectionEventRepository electionEventRepository;
    private ElectionEventReportRepository electionEventReportRepository;

    public RapportApplicationService() {
        // CDI
    }

    @Inject
    public RapportApplicationService(RapportDomainService rapportDomainService, ElectionEventRepository electionEventRepository,
                                     ReportRepository reportRepository, ElectionEventReportRepository electionEventReportRepository) {
        this.rapportDomainService = rapportDomainService;
        this.electionEventRepository = electionEventRepository;
        this.reportRepository = reportRepository;
        this.electionEventReportRepository = electionEventReportRepository;
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = SecurityType.READ)
    public List<ValghendelsesRapport> rapporterForValghendelse(UserData userData, ElectionPath electionEventPath) {
        return rapportDomainService.rapporterForValghendelse(electionEventPath);
    }

    @Override
    @SecurityNone // Access is checked in method
    public List<ValghendelsesRapport> rapporterForBruker(UserData userData, ElectionPath electionEventPath) {
        return rapportDomainService.rapporterForBruker(userData, electionEventPath);
    }

    @Override
    @Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = SecurityType.WRITE)
    @AuditLog(eventClass = ValghendelsesRapportAuditEvent.class, eventType = AuditEventTypes.Save)
    public List<ValghendelsesRapport> lagre(UserData userData, ElectionPath electionEventPath, List<ValghendelsesRapport> rapporter) {
        electionEventPath.assertElectionEventLevel();
        ElectionEvent electionEvent = electionEventRepository.findById(electionEventPath.getElectionEventId());
        List<Report> allReports = reportRepository.findAll();

        List<ElectionEventReport> electionEventReports = electionEventReportRepository.findByElectionEventPath(electionEventPath);
        List<ElectionEventReport> delete = new ArrayList<>();
        for (ValghendelsesRapport rapport : rapporter) {
            handtereLagring(userData, electionEvent, rapport, electionEventReports, allReports, delete);
        }
        if (!delete.isEmpty()) {
            electionEventReportRepository.deleteReports(userData, delete);
        }
        return rapporter;
    }

    private void handtereLagring(UserData userData, ElectionEvent electionEvent, ValghendelsesRapport rapport, List<ElectionEventReport> electionEventReports,
                                 List<Report> allReports, List<ElectionEventReport> delete) {
        ElectionEventReport dbRapport = findElectionEventReport(electionEventReports, rapport);
        if (rapport.isSynlig()) {
            if (dbRapport == null) {
                Report parentReport = findReport(allReports, rapport);
                if (parentReport == null) {
                    throw new IllegalArgumentException("Invalid report id '" + rapport.getRapportId() + "'");
                }
                electionEventReportRepository.create(userData, new ElectionEventReport(electionEvent, parentReport));
            }
        } else {
            if (dbRapport != null) {
                delete.add(dbRapport);
            }
        }
    }

    private ElectionEventReport findElectionEventReport(List<ElectionEventReport> electionEventReports, ValghendelsesRapport rapport) {
        return electionEventReports.stream().filter(r -> r.getReport().getId().equals(rapport.getRapportId())).findFirst().orElse(null);
    }

    private Report findReport(List<Report> reports, ValghendelsesRapport rapport) {
        return reports.stream().filter(r -> r.getId().equals(rapport.getRapportId())).findFirst().orElse(null);
    }
}
