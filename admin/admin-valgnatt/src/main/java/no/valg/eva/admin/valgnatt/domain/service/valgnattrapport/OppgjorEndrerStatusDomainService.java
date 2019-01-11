package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import static no.valg.eva.admin.counting.domain.model.report.ReportType.VALGOPPGJOR;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.settlement.domain.event.OppgjorEndrerStatus;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;

/**
 * Håndter oppdatering av korresponderende oppgjørsrapport når et oppgjør blir slettet.
 */
public class OppgjorEndrerStatusDomainService {

	private MvElectionRepository mvElectionRepository;
	private ValgnattrapportRepository valgnattrapportRepository;
	private MvAreaRepository mvAreaRepository;

	@Inject
	public OppgjorEndrerStatusDomainService(MvElectionRepository mvElectionRepository,
											ValgnattrapportRepository valgnattrapportRepository, MvAreaRepository mvAreaRepository) {

		this.mvElectionRepository = mvElectionRepository;
		this.valgnattrapportRepository = valgnattrapportRepository;
		this.mvAreaRepository = mvAreaRepository;
	}

	public void oppdaterValgnattRapport(@Observes OppgjorEndrerStatus oppgjorEndrerStatus) {
		List<Valgnattrapport> rapporter = finnValgnattrapporter(oppgjorEndrerStatus);
		markerRapporterSomMaaSendesPaaNytt(rapporter);
	}

	private List<Valgnattrapport> finnValgnattrapporter(OppgjorEndrerStatus oppgjorEndrerStatus) {
		MvArea omraade = finnOmraade(oppgjorEndrerStatus);
		MvElection valghierarkielement = finnValghierarkielementFor(oppgjorEndrerStatus);
		List<MvElection> fylker = finnFylker(omraade, valghierarkielement);

		return finnValgnattrapporterForValgtNivaa(valghierarkielement, omraade, fylker);
	}

	private MvArea finnOmraade(OppgjorEndrerStatus oppgjorEndrerStatus) {
		return mvAreaRepository.findSingleByPath(oppgjorEndrerStatus.getAreaPath());
	}

	private MvElection finnValghierarkielementFor(OppgjorEndrerStatus oppgjorEndrerStatus) {
		MvElection mvElection = finnIValghierarkiet(oppgjorEndrerStatus);
		if (erPaaValggruppenivaa(mvElection)) {
			mvElection = finnFoersteValg(mvElection);
		}
		return mvElection;
	}

	private MvElection finnIValghierarkiet(OppgjorEndrerStatus oppgjorEndrerStatus) {
		return mvElectionRepository.finnEnkeltMedSti(oppgjorEndrerStatus.getContestPath().tilValghierarkiSti());
	}

	private boolean erPaaValggruppenivaa(MvElection mvElection) {
		return mvElection.getActualElectionLevel() == ElectionLevelEnum.ELECTION_GROUP;
	}

	private MvElection finnFoersteValg(MvElection mvElection) {
		return mvElectionRepository.findByPathAndChildLevel(mvElection).get(0);
	}

	private List<MvElection> finnFylker(MvArea omraade, MvElection mvElection) {
		return mvElectionRepository.findContestsForElectionAndArea(mvElection.electionPath(), omraade.areaPath());
	}

	private List<Valgnattrapport> finnValgnattrapporterForValgtNivaa(MvElection mvElection, MvArea mvArea, List<MvElection> fylker) {
		List<Valgnattrapport> rapporter;
		if (harFylker(fylker)) {
			rapporter = finnFylkeRapporter(fylker);
		} else if (erPaaLandsnivaa(mvArea)) {
			rapporter = finnLandRapporter(mvElection.getElection());
		} else {
			rapporter = finnKommuneRapporter(mvArea, mvElection);
		}
		return rapporter;
	}

	private boolean harFylker(List<MvElection> fylker) {
		return !fylker.isEmpty();
	}

	private List<Valgnattrapport> finnFylkeRapporter(List<MvElection> fylker) {
		List<Valgnattrapport> rapporter = new ArrayList<>();

		for (MvElection fylke : fylker) {
			rapporter.addAll(valgnattrapportRepository.byContestAndReportType(fylke.getContest(), VALGOPPGJOR));
		}

		return rapporter;
	}

	private boolean erPaaLandsnivaa(MvArea mvArea) {
		return mvArea.getActualAreaLevel() == AreaLevelEnum.COUNTRY;
	}

	private List<Valgnattrapport> finnLandRapporter(Election election) {
		return valgnattrapportRepository.finnFor(election, VALGOPPGJOR);
	}

	private List<Valgnattrapport> finnKommuneRapporter(MvArea mvArea, MvElection mvElection) {
		List<Valgnattrapport> rapporter = new ArrayList<>();

		for (MvElection kommune : finnKommuner(mvArea, mvElection)) {
			rapporter.addAll(valgnattrapportRepository.byContestAndReportType(kommune.getContest(), VALGOPPGJOR));
		}

		return rapporter;
	}

	private List<MvElection> finnKommuner(MvArea mvArea, MvElection mvElection) {
		List<MvArea> areas = mvAreaRepository.findByPathAndChildLevel(mvArea);
		List<MvElection> kommuner = new ArrayList<>();

		for (MvArea area : areas) {
			kommuner.addAll(mvElectionRepository.findContestsForElectionAndArea(mvElection.electionPath(), AreaPath.from(area.getPath())));
		}

		return kommuner;
	}

	private void markerRapporterSomMaaSendesPaaNytt(List<Valgnattrapport> rapporter) {
		rapporter.stream()
				.filter(Valgnattrapport::isOk)
				.forEach(Valgnattrapport::maaRapporteresPaaNytt);
	}
}
