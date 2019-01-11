package no.valg.eva.admin.valgnatt.domain.service.resultat;

import static java.util.stream.Collectors.toSet;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;

/**
 * Finner kommuner det skal rapporteres for.
 */
public class RapporteringsområdeDomainService {

	private VoteCountService voteCountService;
	private MvAreaRepository mvAreaRepository;

	@Inject
	public RapporteringsområdeDomainService(VoteCountService voteCountService, MvAreaRepository mvAreaRepository) {
		this.voteCountService = voteCountService;
		this.mvAreaRepository = mvAreaRepository;
	}

	public Set<Municipality> kommunerForRapportering(Contest contest) {
		Set<ContestArea> contestAreaSet = contest.getContestAreaSet();
		if (contestAreaSet.size() == 1) {
			Set<Municipality> municipalities;
			MvArea reportForArea = contestAreaSet.iterator().next().getMvArea();
			if (reportForArea.getActualAreaLevel() == AreaLevelEnum.COUNTY) {
				municipalities = reportForArea.getCounty().getMunicipalities();
			} else {
				municipalities = new HashSet<>();
				municipalities.add(reportForArea.getMunicipality());
			}
			return municipalities;
		} else {
			return contestAreaSet.stream()
					.filter(contestArea -> !contestArea.isChildArea())
					.map(contestArea -> contestArea.getMvArea().getMunicipality())
					.collect(toSet());
		}
	}

	public Set<MvArea> kretserForRapporteringAvValgtingsstemmer(Municipality municipality, MvElection mvElectionContest) {
		boolean pollingDistrictCount = isPollingDistrictCount(municipality, mvElectionContest);
		if (pollingDistrictCount) {
			return asMvAreas(municipality.regularPollingDistricts(true, false));
		}
		return Collections.emptySet();
	}

	private boolean isPollingDistrictCount(Municipality municipality, MvElection mvElectionContest) {
		CountingMode countingMode = voteCountService.countingMode(VO, municipality, mvElectionContest);
		return countingMode != null && countingMode.isPollingDistrictCount();
	}

	private Set<MvArea> asMvAreas(Collection<PollingDistrict> pollingDistricts) {
		return pollingDistricts
				.stream()
				.map(pollingDistrict -> mvAreaRepository.findSingleByPath(pollingDistrict.areaPath()))
				.collect(toSet());
	}

	public Set<MvArea> kretserForRapporteringAvForhåndsstemmerOgSentralValgting(Municipality municipality, MvElection mvElectionContest) {
		Set<PollingDistrict> pollingDistricts = new HashSet<>();
		boolean technicalPollingDistrictCount = voteCountService.countingMode(FO, municipality, mvElectionContest).isTechnicalPollingDistrictCount();
		if (technicalPollingDistrictCount) {
			pollingDistricts.addAll(municipality.technicalPollingDistricts());
		}
		pollingDistricts.add(municipality.getMunicipalityPollingDistrict());
		return asMvAreas(pollingDistricts);
	}

	public MvArea kommunekrets(Municipality municipality) {
		return mvAreaRepository.findSingleByPath(municipality.areaPath().toMunicipalityPollingDistrictPath());
	}

	public MvArea kretsForRapportering(AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.POLLING_DISTRICT);
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		if (mvArea.getPollingDistrict().isTechnicalPollingDistrict()) {
			return mvAreaRepository.findSingleByPath(mvArea.getMunicipality().areaPath().toMunicipalityPollingDistrictPath());
		}
		return mvArea;
	}

	public Set<ValggeografiSti> finnOmråderForStemmegivningsstatistikk(Municipality kommune, MvElection valgdistrikt, StemmekretsSti stemmekretsSti) {
		if (erHeleKommunenOgSentraltSamlet(stemmekretsSti, kommune, valgdistrikt)) {
			return valggeografiStier(kommune.kommuneSti());
		} else if (erForeldrekrets(stemmekretsSti)) { // Må være en tellekrets pga første if-ledd
			return valggeografiStierForBarnekretser(stemmekretsSti);
		} else if (erSamlekommune(valgdistrikt)) { // Samlekommuner (bare i sametingssvalget!) har ikke samlet VO eller tellekretser
			return valggeografiStierForSamleKommuneOgTilhørendeUnder30Kommuner(valgdistrikt);
		} else { // Vanlige kretser
			return valggeografiStier(stemmekretsSti);
		}
	}

	private boolean erHeleKommunenOgSentraltSamlet(StemmekretsSti stemmekretsSti, Municipality kommune, MvElection valgdistrikt) {
		boolean sentraltSamlet = voteCountService.countingMode(VO, kommune, valgdistrikt) == CountingMode.CENTRAL;
		return stemmekretsSti.erKommunekretsen() && sentraltSamlet; // implementere denne sjekket i Sti-APIet
	}

	private Set<ValggeografiSti> valggeografiStier(ValggeografiSti sti) {
		return Collections.singleton(sti);
	}

	private Boolean erForeldrekrets(StemmekretsSti stemmekretsSti) {
		return mvAreaRepository.findSingleByPath(stemmekretsSti.areaPath()).getParentPollingDistrict();
	}

	private Set<ValggeografiSti> valggeografiStierForBarnekretser(StemmekretsSti stemmekretsSti) {
		return mvAreaRepository.findSingleByPath(stemmekretsSti.areaPath()).getPollingDistrict().getChildPollingDistricts().stream()
				.map(PollingDistrict::stemmekretsSti).collect(toSet());
	}

	private boolean erSamlekommune(MvElection valgdistrikt) {
		List<ContestArea> valgdistriktOmråder = valgdistrikt.getContest().getContestAreaList(); // sortert liste, foreldre valgdistrikt kommer først
		// skjer bare i sametingsvalg
		return valgdistriktOmråder.size() > 1 && valgdistriktOmråder.get(0).isParentArea();
	}

	private Set<ValggeografiSti> valggeografiStierForSamleKommuneOgTilhørendeUnder30Kommuner(MvElection valgdistrikt) {
		Set<ValggeografiSti> valggeografiStier = new HashSet<>();
		for (ContestArea contest : valgdistrikt.getContest().getContestAreaList()) {
			if (contest.isParentArea() || contest.isChildArea()) {
				valggeografiStier.add(contest.getMvArea().valggeografiSti());
			}
		}
		return valggeografiStier;
	}

}
