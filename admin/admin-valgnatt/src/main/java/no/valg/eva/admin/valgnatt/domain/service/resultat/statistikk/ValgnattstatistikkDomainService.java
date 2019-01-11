package no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Stemmeseddelstatistikk;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Valgnattstatistikk;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.repository.StemmegivningsstatistikkRepository;
import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ValgnattstatistikkDomainService {

	private StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository;
	private RapporteringsområdeDomainService rapporteringsområdeDomainService;

	@Inject
	public ValgnattstatistikkDomainService(StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository,
										   RapporteringsområdeDomainService rapporteringsområdeDomainService) {
		this.stemmegivningsstatistikkRepository = stemmegivningsstatistikkRepository;
		this.rapporteringsområdeDomainService = rapporteringsområdeDomainService;
	}

	/**
	 * Lager statistikk. Hvis det er en krets som ikke er kommunekretsen (0000) som sendes inn, skal det lages statistikk for denne kretsen. Hvis kommunekretsen
	 * sendes inn, skal det enten lages statistikk for denne, eller for alle kretsene i kommunen hvis kommunen teller sentralt samlet. Hvis det er en tellekrets
	 * som sendes inn, skal det lages statistikk for barna til denne.
	 */
	public Valgnattstatistikk lagStatistikk(MvElection valgdistrikt, List<BallotCount> ballotCounts, MvArea telleOmråde) {
		Stemmegivningsstatistikk sgStat = stemmegivningsstatistikk(valgdistrikt, telleOmråde);
		Stemmeseddelstatistikk ssStat = stemmeseddelstatistikk(ballotCounts);
		return new Valgnattstatistikk(sgStat, ssStat);
	}

	protected Stemmeseddelstatistikk stemmeseddelstatistikk(List<BallotCount> ballotCounts) {
		return new Stemmeseddelstatistikk(
				summerStemmesedler(ballotCounts, forhåndsstemmer(), godkjente()), // sts-forhånd-godkjente
				summerStemmesedler(ballotCounts, valgtingsstemmer(), godkjente()), // sts-valgting-godkjente
				summerStemmesedler(ballotCounts, forhåndsstemmer(), forkastede()), // sts-forhånd-forkastede
				summerStemmesedler(ballotCounts, valgtingsstemmer(), forkastede())); // sts-valgting-forkastede
	}

	private Integer summerStemmesedler(List<BallotCount> ballotCounts, Predicate<BallotCount> type, Predicate<BallotCount> status) {
		return ballotCounts
				.stream()
				.filter(type)
				.filter(status)
				.mapToInt(ballotCount -> ballotCount.getUnmodifiedBallots() + ballotCount.getModifiedBallots())
				.sum();
	}

	protected Stemmegivningsstatistikk stemmegivningsstatistikk(MvElection valgdistrikt, MvArea telleOmråde) {
		StemmekretsSti stemmekretsSti = telleOmråde.valggeografiSti().tilStemmekretsSti();
		Municipality kommune = telleOmråde.getMunicipality();
		Set<ValggeografiSti> valgGeografiStier = rapporteringsområdeDomainService.finnOmråderForStemmegivningsstatistikk(kommune, valgdistrikt, stemmekretsSti);
		return valgGeografiStier
				.stream()
				.map(sti -> statistikkForOmråde(sti, kommune.papirmanntall(), valgdistrikt.getContest()))
				.reduce(new Stemmegivningsstatistikk(0, 0, 0, 0), Stemmegivningsstatistikk::add);
	}

	private Stemmegivningsstatistikk statistikkForOmråde(ValggeografiSti valggeografiSti, boolean papirmanntall, Contest contest) {
		Stemmegivningsstatistikk stemmegivningsstatistikk = stemmegivningsstatistikkRepository.finnForOmrådeOgValg(valggeografiSti, papirmanntall);
		if (papirmanntall) { // papirmantallskommuner henter sine VO-stemmegivninger fra en annen tabell
			stemmegivningsstatistikk.addApprovedElectionDayVotings(stemmegivningsstatistikkRepository.finnVOStemmegivninger(valggeografiSti, contest.getPk()));
		}
		stemmegivningsstatistikk.addRejectedEarlyVotings(stemmegivningsstatistikkRepository.numberOfRejectedVotings(valggeografiSti, "FA"));
		stemmegivningsstatistikk.addRejectedElectionDayVotings(stemmegivningsstatistikkRepository.numberOfRejectedVotings(valggeografiSti, "VA"));
		return stemmegivningsstatistikk;
	}

	private Predicate<BallotCount> valgtingsstemmer() {
		return BallotCount::isValgting;
	}

	private Predicate<BallotCount> forhåndsstemmer() {
		return BallotCount::isForhånd;
	}

	private Predicate<BallotCount> forkastede() {
		return ballotCount -> !ballotCount.hasBallot();
	}

	private Predicate<BallotCount> godkjente() {
		return BallotCount::hasBallot;
	}
}
