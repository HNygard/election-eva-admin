package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import static no.valg.eva.admin.counting.domain.model.report.ReportType.GEOGRAFI_STEMMEBERETTIGEDE;
import static no.valg.eva.admin.counting.domain.model.report.ReportType.PARTIER_OG_KANDIDATER;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportAntall;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;

/**
 * Henter frem eller lager Valgnattrapport instanser som er metadata for rapporteringer.
 */
@Default
@ApplicationScoped
public class ValgnattrapportDomainService {

	@Inject
	private ValgnattrapportRepository valgnattrapportRepository;
	@Inject
	private ValgnattElectoralRollRepository valgnattElectoralRollRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private ReadyForReportingDomainService readyForReportingDomainService;
	@Inject
	private SettlementRepository settlementRepository;

	public ValgnattrapportDomainService() {

	}

	public ValgnattrapportDomainService(ValgnattrapportRepository valgnattrapportRepository, ValgnattElectoralRollRepository valgnattElectoralRollRepository,
			MvAreaRepository mvAreaRepository, ReadyForReportingDomainService readyForReportingDomainService, SettlementRepository settlementRepository) {
		this.valgnattrapportRepository = valgnattrapportRepository;
		this.valgnattElectoralRollRepository = valgnattElectoralRollRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.readyForReportingDomainService = readyForReportingDomainService;
		this.settlementRepository = settlementRepository;
	}

	public List<Valgnattrapport> grunnlagsdataRapporter(Election election) {
		List<Valgnattrapport> rapporter = new ArrayList<>();
		rapporter.add(grunnlagsdataRapport(election, GEOGRAFI_STEMMEBERETTIGEDE));
		rapporter.add(grunnlagsdataRapport(election, PARTIER_OG_KANDIDATER));
		return rapporter;
	}

	private Valgnattrapport grunnlagsdataRapport(Election election, ReportType reportType) {
		Valgnattrapport rapport = valgnattrapportRepository.byElectionAndReportType(election, reportType);
		if (rapport == null) {
			rapport = valgnattrapportRepository.create(new Valgnattrapport(election, reportType));
		}
		return rapport;
	}
	
	public long antallRapporterbareStemmeskjemaRapporter(MvElection mvElectionContest, Municipality municipality) {
		ValgnattrapportAntall valgnattrapportAntall =
				valgnattrapportRepository.countReportableByContestAndMunicipality(mvElectionContest.getContest(), municipality);
		if (valgnattrapportAntall.ingenRapporter()) {
			return beregnAntallRapporterbareStemmeskjemaRapporterOgOpprettNyeHvisDeIkkeFinnes(mvElectionContest, municipality);

		}
		return valgnattrapportAntall.getAntallRapporterbare();
	}

	private long beregnAntallRapporterbareStemmeskjemaRapporterOgOpprettNyeHvisDeIkkeFinnes(MvElection mvElectionContest, Municipality municipality) {
		List<Valgnattrapport> valgnattrapporter = stemmeskjemaRapporter(mvElectionContest, municipality);
		return valgnattrapporter.stream().filter(Valgnattrapport::isReadyForReport).count();
	}

	public boolean erAlleStemmeskjemaRapportert(MvElection mvElectionContest, Municipality municipality) {
		return valgnattrapportRepository.countReportableByContestAndMunicipality(mvElectionContest.getContest(), municipality).isAlleFerdig();
	}

	/**
	 * Henter ut rapportmetadata for stemmeskjemarapporter. Lager nye hvis de ikke finnes.
	 * @param mvElectionContest valgdistrikt
	 * @param municipality kommune
	 * @return
	 */
	public List<Valgnattrapport> stemmeskjemaRapporter(MvElection mvElectionContest, Municipality municipality) {
		mvElectionContest.electionPath().assertContestLevel();
		List<Valgnattrapport> valgnattrapportList = valgnattrapportRepository.byContestAndMunicipality(mvElectionContest.getContest(), municipality);
		if (valgnattrapportList.isEmpty()) {
			List<ReportConfiguration> reportConfigurations = valgnattElectoralRollRepository.valgnattReportConfiguration(mvElectionContest, municipality);
			valgnattrapportList.addAll(lagValgnattrapporter(mvElectionContest, municipality, reportConfigurations));
		}
		boolean valgoppgjorKjort = settlementRepository.erValgoppgjørKjørt(mvElectionContest.getContest());
		setReadyForReporting(valgnattrapportList, mvElectionContest, valgoppgjorKjort);
		return valgnattrapportList;
	}

	private void setReadyForReporting(List<Valgnattrapport> valgnattrapportList, MvElection mvElectionContest, boolean valgoppgjorKjort) {
		valgnattrapportList.forEach(vr -> {
			if (valgoppgjorKjort) {
				vr.setReadyForReport(false);
			} else {
				vr.setReadyForReport(readyForReportingDomainService.erStemmeskjemaKlarForRapportering(vr, mvElectionContest));
			}
		});
	}

	private List<Valgnattrapport> lagValgnattrapporter(MvElection mvElectionContest, Municipality municipality,
			List<ReportConfiguration> reportConfigurations) {
		List<Valgnattrapport> valgnattrapportList = new ArrayList<>();
		for (ReportConfiguration reportConfiguration : reportConfigurations) {
			MvArea mvArea = mvAreaRepository.findByPk(reportConfiguration.getMvAreaPk().longValue());
			if (reportConfiguration.isMunicipalityPollingDistrict()) {
				// hvis 0000 og ikke by municipality -> FF, FE, VE
				// hvis 0000 og by municipality -> FF, FE, VF, VE
				valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_FF));
				if (mvElectionContest.getContest().isContestOrElectionPenultimateRecount()) {
					valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_FE));
					valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_VE));
				}
				if (reportConfiguration.isByMunicipality()) {
					valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_VF));
				}
			} else {
				// hvis noe annet (bør vel ikke dukke opp hvis byMunicipality) -> VF, VE
				valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_VF));
				if (mvElectionContest.getContest().isContestOrElectionPenultimateRecount()) {
					valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_VE));
				}
			}
		}
		if (reportConfigurations.isEmpty() && municipality.areaPath().isSamiValgkretsPath()) {
			MvArea mvArea = mvAreaRepository.findSingleByPath(municipality.areaPath().toMunicipalityPollingDistrictPath());
			valgnattrapportList.add(lagValgnattrapport(mvElectionContest, municipality, mvArea, ReportType.STEMMESKJEMA_FF));
		}
		
		return valgnattrapportList;
	}

	private Valgnattrapport lagValgnattrapport(MvElection mvElectionContest, Municipality municipality, MvArea mvArea, ReportType reportType) {
		return valgnattrapportRepository
				.create(new Valgnattrapport(mvArea, municipality, mvElectionContest.getContest(), mvElectionContest.getElection(),
						reportType, ValgnattrapportStatus.NOT_SENT, null, false));
	}

	public Valgnattrapport oppgjorsskjemaRapporter(MvElection mvElection, MvArea reportForArea) {
		List<Valgnattrapport> valgnattrapportList = valgnattrapportRepository.byContestAndReportType(mvElection.getContest(), ReportType.VALGOPPGJOR);
		Valgnattrapport valgnattrapport;
		if (valgnattrapportList.isEmpty()) {
			valgnattrapport = valgnattrapportRepository
					.create(new Valgnattrapport(reportForArea, null, mvElection.getContest(), mvElection.getElection(), ReportType.VALGOPPGJOR,
							ValgnattrapportStatus.NOT_SENT, null, false));
		} else {
			valgnattrapport = valgnattrapportList.get(0); // bør bare være en
		}
		valgnattrapport.setReadyForReport(settlementRepository.erValgoppgjørKjørt(mvElection.getContest()));
		return valgnattrapport;
	}

	public void markerStemmeskjemaRapportert(MvElection mvElectionContest) {
		markerRapportert(mvElectionContest, ReportType.STEMMESKJEMA_FF);
		markerRapportert(mvElectionContest, ReportType.STEMMESKJEMA_FE);
		markerRapportert(mvElectionContest, ReportType.STEMMESKJEMA_VF);
		markerRapportert(mvElectionContest, ReportType.STEMMESKJEMA_VE);
	}

	private void markerRapportert(MvElection mvElectionContest, ReportType reportType) {
		List<Valgnattrapport> valgnattrapportList = valgnattrapportRepository.byContestAndReportType(mvElectionContest.getContest(), reportType);
		valgnattrapportList.forEach(vr -> {
			if (vr.isNotSent()) {
				vr.oppdaterTilStatusOk();
			}
		});
	}
}
