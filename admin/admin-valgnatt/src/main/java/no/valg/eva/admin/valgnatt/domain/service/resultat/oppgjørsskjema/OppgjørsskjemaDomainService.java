package no.valg.eva.admin.valgnatt.domain.service.resultat.oppgjørsskjema;

import static java.util.stream.Collectors.toList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.service.ContestReportDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.valgnatt.domain.model.resultat.ResultatType;
import no.valg.eva.admin.valgnatt.domain.model.resultat.Resultatskjema;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Valgnattstatistikk;
import no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall.Stemmetall;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk.ValgnattstatistikkDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.stemmetall.StemmetallDomainService;

/**
 * Lager oppgjørsskjema til valgnatt fra møtebøker og valgoppgjør.
 */
public class OppgjørsskjemaDomainService {

	private ContestReportDomainService contestReportDS;
	private StemmetallDomainService stemmetallDS;
	private ListestemmerDomainService listestemmerDS;
	private RapporteringsområdeDomainService rapporteringsområdeDS;
	private ValgnattstatistikkDomainService valgnattstatistikkDS;
	private VoteCountService voteCountService;

	@Inject
	public OppgjørsskjemaDomainService(ContestReportDomainService contestReportDS,
									   StemmetallDomainService stemmetallDS,
									   ListestemmerDomainService listestemmerDS,
									   RapporteringsområdeDomainService rapporteringsområdeDS,
									   ValgnattstatistikkDomainService valgnattstatistikkDS,
									   VoteCountService voteCountService) {
		this.contestReportDS = contestReportDS;
		this.stemmetallDS = stemmetallDS;
		this.listestemmerDS = listestemmerDS;
		this.rapporteringsområdeDS = rapporteringsområdeDS;
		this.valgnattstatistikkDS = valgnattstatistikkDS;
		this.voteCountService = voteCountService;
	}

	/**
	 * Lager oppgjørsskjema fra møtebok/tellingene og finner listestemmer fra valgoppgjøret. Oppgjørsskjema sendes
	 * til Valgnatt/EVA Resulat.
	 */
	public List<Resultatskjema> fraMøtebokOgValgoppgjør(MvElection valgdistrikt, MvArea rapportOmråde) {

		if (rapportOmråde.getActualAreaLevel() != COUNTY && rapportOmråde.getActualAreaLevel() != MUNICIPALITY) {
			throw new IllegalArgumentException("Område må være fylke eller kommune, var: " + rapportOmråde.getAreaLevel());
		}

		List<ContestReport> contestReports = contestReportDS.findFinalContestReportsByContest(valgdistrikt.getContest());
		List<Resultatskjema> oppgjørsskjemaList = new ArrayList<>();
		for (Municipality kommune : rapporteringsområdeDS.kommunerForRapportering(valgdistrikt.getContest())) {
			CountingMode voCountingMode = voteCountService.countingMode(VO, kommune, valgdistrikt);
			CountingMode vfCountingMode = voteCountService.countingMode(VF, kommune, valgdistrikt);
			for (MvArea stemmekrets : rapporteringsområdeDS.kretserForRapporteringAvValgtingsstemmer(kommune, valgdistrikt)) {
				List<BallotCount> tellinger = finnTellinger(contestReports, Collections.singleton(stemmekrets), voCountingMode, vfCountingMode);
				List<Stemmetall> stemmetallList = stemmetallDS.hentStemmetall(tellinger);
				Valgnattstatistikk statistikk = valgnattstatistikkDS.lagStatistikk(valgdistrikt, tellinger, stemmekrets);
				oppgjørsskjemaList.add(new Resultatskjema(ResultatType.OP, stemmekrets, valgdistrikt, stemmetallList, statistikk));
			}
			Set<MvArea> stemmekretser = rapporteringsområdeDS.kretserForRapporteringAvForhåndsstemmerOgSentralValgting(kommune, valgdistrikt);
			List<BallotCount> tellinger = finnTellinger(contestReports, stemmekretser, voCountingMode, vfCountingMode);
			Set<AffiliationVoteCount> listestemmer = listestemmerDS.finnListestemmer(valgdistrikt);
			List<Stemmetall> stemmetallList = stemmetallDS.hentStemmetall(tellinger, listestemmer);
			MvArea kommunekrets = rapporteringsområdeDS.kommunekrets(kommune);
			Valgnattstatistikk statistikk = valgnattstatistikkDS.lagStatistikk(valgdistrikt, tellinger, kommunekrets);
			oppgjørsskjemaList.add(new Resultatskjema(ResultatType.OP, kommunekrets, valgdistrikt, stemmetallList, statistikk));
		}

		return oppgjørsskjemaList;
	}

	private List<BallotCount> finnTellinger(List<ContestReport> contestReports, Set<MvArea> stemmekretser, CountingMode voCountingMode, CountingMode vfCountingMode) {
		return contestReports
				.stream()
				.map(cr -> cr.tellingerForRapportering(gyldigeTellingTyper(), stemmekretser, Collections.singleton(TO_SETTLEMENT), voCountingMode, vfCountingMode))
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private Set<CountQualifier> gyldigeTellingTyper() {
		HashSet<CountQualifier> countQualifiers = new HashSet<>();
		countQualifiers.add(FINAL);
		return countQualifiers;
	}

}
