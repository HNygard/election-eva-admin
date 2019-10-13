package no.valg.eva.admin.valgnatt.domain.service.resultat.stemmeskjema;

import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.valgnatt.domain.model.resultat.ResultatType;
import no.valg.eva.admin.valgnatt.domain.model.resultat.Resultatskjema;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Valgnattstatistikk;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk.ValgnattstatistikkDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.stemmetall.StemmetallDomainService;

@Default
@ApplicationScoped
public class StemmeskjemaDomainService {

	@Inject
	private ContestReportRepository contestReportRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private StemmetallDomainService stemmetallDS;
	@Inject
	private RapporteringsområdeDomainService rapporteringsområdeDS;
	@Inject
	private ValgnattstatistikkDomainService valgnattstatistikkDS;
	@Inject
	private VoteCountService voteCountService;

	public StemmeskjemaDomainService() {

	}

	public StemmeskjemaDomainService(ContestReportRepository contestReportRepository,
									 MvAreaRepository mvAreaRepository,
									 StemmetallDomainService stemmetallDS,
									 RapporteringsområdeDomainService rapporteringsområdeDS,
									 ValgnattstatistikkDomainService valgnattstatistikkDS,
									 VoteCountService voteCountService) {
		this.contestReportRepository = contestReportRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.stemmetallDS = stemmetallDS;
		this.rapporteringsområdeDS = rapporteringsområdeDS;
		this.valgnattstatistikkDS = valgnattstatistikkDS;
		this.voteCountService = voteCountService;
	}

	/**
	 * Lager et Resultatskjema som skal sendes til Valgnatt/EVA Resultat.
	 */
	public Resultatskjema fraMøtebok(MvElection valgdistrikt, MvArea rapportOmråde) {
		List<ContestReport> contestReports = contestReportRepository.byContestInArea(valgdistrikt.getContest(), rapportOmråde.areaPath().toMunicipalityPath());
		MvArea telleOmråde = finnTelleOmråde(rapportOmråde); // telleOmråde vil alltid være på stemmekretsnivå
		CountingMode voCountingMode = voteCountService.countingMode(VO, rapportOmråde.getMunicipality(), valgdistrikt);
		CountingMode vfCountingMode = voteCountService.countingMode(VF, rapportOmråde.getMunicipality(), valgdistrikt);
		List<BallotCount> tellinger = finnTellinger(contestReports, finnStemmekretser(telleOmråde, valgdistrikt), voCountingMode, vfCountingMode);
		Valgnattstatistikk statistikk = valgnattstatistikkDS.lagStatistikk(valgdistrikt, tellinger, telleOmråde);
		return new Resultatskjema(ResultatType.TE, telleOmråde, valgdistrikt, stemmetallDS.hentStemmetall(tellinger), statistikk);
	}

	// Dersom rapportOmråde er en hel kommune, så returner 0000-kretsen i kommunen
	private MvArea finnTelleOmråde(MvArea rapportOmråde) {
		if (rapportOmråde.isMunicipalityLevel()) {
			return mvAreaRepository.findSingleByPath(AreaPath.from(rapportOmråde.getAreaPath()).toMunicipalityPollingDistrictPath());
		}
		return rapportOmråde;
	}

	private List<BallotCount> finnTellinger(List<ContestReport> contestReports, Set<MvArea> stemmekretser, CountingMode voCountingMode, CountingMode vfCountingMode) {
		return contestReports
				.stream()
				.map(cr -> cr.tellingerForRapportering(gyldigeTellingTyper(), stemmekretser, gyldigeStatus(), voCountingMode, vfCountingMode))
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private Set<CountQualifier> gyldigeTellingTyper() {
		HashSet<CountQualifier> countQualifiers = new HashSet<>();
		countQualifiers.add(PRELIMINARY);
		countQualifiers.add(FINAL);
		return countQualifiers;
	}

	private Set<CountStatus> gyldigeStatus() {
		Set<CountStatus> countStatuses = new HashSet<>();
		countStatuses.add(APPROVED);
		countStatuses.add(TO_SETTLEMENT);
		return countStatuses;
	}

	private Set<MvArea> finnStemmekretser(MvArea telleOmråde, MvElection mvElectionContest) {
		if (telleOmråde.areaPath().isMunicipalityPollingDistrict()) {
			return rapporteringsområdeDS.kretserForRapporteringAvForhåndsstemmerOgSentralValgting(telleOmråde.getMunicipality(), mvElectionContest);
		} else {
			return Collections.singleton(telleOmråde);
		}
	}
}
