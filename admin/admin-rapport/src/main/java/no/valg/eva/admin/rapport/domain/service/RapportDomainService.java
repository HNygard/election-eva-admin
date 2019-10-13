package no.valg.eva.admin.rapport.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Avkrysningsmanntall;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Bydelsutvalg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.BoroughElectionDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rapport.application.ValghendelsesRapportMapper;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.rapport.repository.ElectionEventReportRepository;
import no.valg.eva.admin.rapport.repository.ReportRepository;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

@Default
@ApplicationScoped
public class RapportDomainService {

    // Injected
    private ReportRepository reportRepository;
    private ElectionEventReportRepository electionEventReportRepository;
    private ValghendelsesRapportMapper valghendelsesRapportMapper;
    private MunicipalityRepository municipalityRepository;
    private ElectionEventRepository electionEventRepository;
    private MvElectionRepository mvElectionRepository;
    private SettlementRepository settlementRepository;
    private BoroughElectionDomainService boroughElectionDomainService;

    public RapportDomainService() {
        // CDI
    }

    @Inject
    public RapportDomainService(ReportRepository reportRepository, ElectionEventReportRepository electionEventReportRepository,
                                ValghendelsesRapportMapper valghendelsesRapportMapper, MunicipalityRepository municipalityRepository,
                                ElectionEventRepository electionEventRepository, MvElectionRepository mvElectionRepository,
                                SettlementRepository settlementRepository, BoroughElectionDomainService boroughElectionDomainService) {
        this.reportRepository = reportRepository;
        this.electionEventReportRepository = electionEventReportRepository;
        this.valghendelsesRapportMapper = valghendelsesRapportMapper;
        this.municipalityRepository = municipalityRepository;
        this.electionEventRepository = electionEventRepository;
        this.mvElectionRepository = mvElectionRepository;
        this.settlementRepository = settlementRepository;
        this.boroughElectionDomainService = boroughElectionDomainService;
    }

    public List<ValghendelsesRapport> rapporterForValghendelse(ElectionPath electionEventPath) {
        electionEventPath.assertElectionEventLevel();

        List<Report> reports = reportRepository.findAll();
        List<ElectionEventReport> electionEventReports = electionEventReportRepository.findByElectionEventPath(electionEventPath);
        List<ValghendelsesRapport> result = reports
                .stream()
                .map(report -> valghendelsesRapportMapper.fromReport(report, electionEventReports))
                .collect(Collectors.toList());
        // If no reports on election event, set all to synlig
        if (electionEventReports.isEmpty()) {
            result.forEach(rapport -> {
                rapport.setSynlig(true);
                rapport.setTilgjengelig(true);
            });
        }
        return result;
    }

    public List<ValghendelsesRapport> rapporterForBruker(UserData userData, ElectionPath electionEventPath) {
        List<ValghendelsesRapport> rapporter = rapporterForValghendelse(electionEventPath);
        List<ValghendelsesRapport> result = rapporter.stream()
                // Kategori VALGHENDELSE_ADMIN behøver ikke være synlig da de kun er tilgjengelig som blåmenypunkt
                .filter(rapport -> rapport.isSynlig() || rapport.getKategori() == ReportCategory.VALGHENDELSE_ADMIN)
                .filter(rapport -> userData.hasAccess(Accesses.fromAccess(rapport.getAccess())))
                .filter(rapport -> !isBoroughProtocolReport(rapport) || userHasAccessToBoroughProtocols(userData, rapport))
                .collect(Collectors.toList());

        result.forEach(rapport -> rapport.setTilgjengelig(!isReportDisabledForUser(userData, rapport)));

        return result;
    }

    private boolean isReportDisabledForUser(UserData userData, ValghendelsesRapport rapport) {
        boolean disabled = false;
        if (Rapport_Manntall_Avkrysningsmanntall.getAccess().equals(rapport.getAccess())) {
            disabled = isAvkryssingsMantallRapportDisabled(userData);
        }
        return disabled;
    }

    boolean userHasAccessToBoroughProtocols(UserData userData, ValghendelsesRapport rapport) {
        return isBoroughProtocolReport(rapport)
                && userHasAccessToABoroughElection(userData);
    }

    private boolean isBoroughProtocolReport(ValghendelsesRapport rapport) {
        return Rapport_Møtebøker_Bydelsutvalg.getAccess().equals(rapport.getAccess());
    }

    private boolean userHasAccessToABoroughElection(UserData userData) {
        return userData.isElectionEventAdminUser() ||
                boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(userData.getOperatorElectionPath(),
                        userData.getOperatorMvArea().areaPath());
    }

    boolean isAvkryssingsMantallRapportDisabled(UserData userData) {
        Municipality municipality = userData.getOperatorMvArea().getMunicipality();
        if (municipality == null || !municipalityRepository.findByPk(municipality.getPk()).isElectronicMarkoffs()) {
            // User needs to be on at least municipality level and XiM
            return false;
        }
        List<AreaPath> areaPaths = new ArrayList<>();
        for (int i = COUNTY.getLevel(); i <= userData.getOperatorAreaLevel().getLevel(); i++) {
            areaPaths.add(userData.getOperatorAreaPath().toAreaLevelPath(AreaLevelEnum.getLevel(i)));
        }
        ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
        List<MvElection> contests = mvElectionRepository.findContestsByElectionEventAndAreas(electionEvent, areaPaths);
        // All contests must be settled
        for (MvElection contest : contests) {
            if (!settlementRepository.erValgoppgjørKjørt(contest.getContest())) {
                return true;
            }
        }
        return false;
    }

}
