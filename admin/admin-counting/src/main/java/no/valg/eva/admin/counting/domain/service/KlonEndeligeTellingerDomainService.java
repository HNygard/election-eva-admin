package no.valg.eva.admin.counting.domain.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.util.EvoteProperties.TEST_KAN_KLONE_ENDELIGE_TELLINGER;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.VALGSTYRET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import no.evote.model.BinaryData;
import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.FylkeskommuneMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.KommuneMapper;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.KlonEndeligeTellingerResultat;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class KlonEndeligeTellingerDomainService {
	private static final Logger LOGGER = Logger.getLogger(KlonEndeligeTellingerDomainService.class);
	private static final String ADVARSEL_IKKE_TILGANG = "Et forsøk på å nå tjenesten KlonEndeligeTellingerServlet ble registrert."
			+ " Denne funksjonaliteten er skrudd av så et kall på denne tjenesten tyder på et mulig forsøk på innbrudd i systemet";

	private MvElectionRepository mvElectionRepository;
	private ReportCountCategoryRepository reportCountCategoryRepository;
	private MvAreaRepository mvAreaRepository;
	private ContestReportRepository contestReportRepository;
	private ReportingUnitRepository reportingUnitRepository;
	private CountingCodeValueRepository countingCodeValueRepository;
	private BinaryDataRepository binaryDataRepository;

	private boolean erTjenestenTilkoblet;

	public KlonEndeligeTellingerDomainService() {
		// CDI
	}

	@Inject
	public KlonEndeligeTellingerDomainService(MvElectionRepository mvElectionRepository,
											  ReportCountCategoryRepository reportCountCategoryRepository,
											  MvAreaRepository mvAreaRepository,
											  ContestReportRepository contestReportRepository,
											  ReportingUnitRepository reportingUnitRepository,
											  CountingCodeValueRepository countingCodeValueRepository,
											  BinaryDataRepository binaryDataRepository,
											  @EvaConfigProperty @Named(TEST_KAN_KLONE_ENDELIGE_TELLINGER) boolean erTjenestenTilkoblet) {
		this.mvElectionRepository = mvElectionRepository;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.contestReportRepository = contestReportRepository;
		this.reportingUnitRepository = reportingUnitRepository;
		this.countingCodeValueRepository = countingCodeValueRepository;
		this.binaryDataRepository = binaryDataRepository;
		this.erTjenestenTilkoblet = erTjenestenTilkoblet;
	}

	@Transactional(REQUIRED)
	public KlonEndeligeTellingerResultat klonEndeligeTellinger(ValghendelseSti valghendelseSti, int skipAntallLokaltFordeltPaaKrets,
															   int skipAntallSentraltFordeltPaaKrets, int skipAntallSentraltSamlet,
															   List<KommuneSti> skippedeKommuneStier) {
		sjekkOmTjenestenErTilkoblet();
		
		Map<Fylkeskommune, Map<Kommune, CountingMode>> fylkeskommunerKommunerOgCountingModes = new LinkedHashMap<>();
		Map<Fylkeskommune, Contest> fylkeskommunerOgContests = fylkeskommunerOgContests(valghendelseSti);
		List<KommuneSti> kommunerSomSkalSkippes = behandlKommunerPrFylke(fylkeskommunerKommunerOgCountingModes, fylkeskommunerOgContests,
				skipAntallLokaltFordeltPaaKrets, skipAntallSentraltFordeltPaaKrets, skipAntallSentraltSamlet, skippedeKommuneStier);
		Map<Fylkeskommune, List<ContestReport>> fylkesKommunerOgContestReportsForValgstyrer =
				fylkesKommunerOgContestReportsForValgstyrer(valghendelseSti, kommunerSomSkalSkippes, fylkeskommunerOgContests);
		Map<FylkeskommuneSti, ReportingUnit> fylkeskommuneStierOgReportingUnits = fylkeskommuneStierOgReportingUnits(valghendelseSti);
		VoteCountStatus toSettlementStatus = countingCodeValueRepository.findVoteCountStatusByCountStatus(TO_SETTLEMENT);
		
		Map<Fylkeskommune, ContestReport> fylkeskommunerOgContestReportsForFylkesvalgstyret = lagContestReportsForFylkesvalgstyrer(
				fylkesKommunerOgContestReportsForValgstyrer, fylkeskommunerOgContests, fylkeskommuneStierOgReportingUnits);
		lagVoteCountsForFylkesvalgstyrer(fylkesKommunerOgContestReportsForValgstyrer, fylkeskommunerOgContestReportsForFylkesvalgstyret, toSettlementStatus);

		for (Fylkeskommune fylkeskommune : fylkeskommunerOgContests.keySet()) {
			ContestReport contestReportForFylkesvalgstyret = fylkeskommunerOgContestReportsForFylkesvalgstyret.get(fylkeskommune);
			if (contestReportForFylkesvalgstyret == null) {
				continue;
			}
			contestReportRepository.create(contestReportForFylkesvalgstyret);
		}
		return new KlonEndeligeTellingerResultat(fylkeskommunerKommunerOgCountingModes, kommunerSomSkalSkippes);
	}

	private void sjekkOmTjenestenErTilkoblet() {
		if (!erTjenestenTilkoblet) {
			LOGGER.warn(ADVARSEL_IKKE_TILGANG);
			throw new IllegalStateException("Ikke tilgang!");
		}
	}

	private List<KommuneSti> behandlKommunerPrFylke(Map<Fylkeskommune, Map<Kommune, CountingMode>> fylkeskommunerKommunerOgCountingModes,
													Map<Fylkeskommune, Contest> fylkeskommunerOgContests,
													int skipAntallLokaltFordeltPaaKrets, int skipAntallSentraltFordeltPaaKrets, int skipAntallSentraltSamlet,
													List<KommuneSti> skippedeKommuneStier) {
		List<KommuneSti> kommunerSomSkalSkippes = new ArrayList<>();
		for (Fylkeskommune fylkeskommune : fylkeskommunerOgContests.keySet()) {
			Map<Kommune, CountingMode> kommunerOgCountingModes = new LinkedHashMap<>();
			fylkeskommunerKommunerOgCountingModes.put(fylkeskommune, kommunerOgCountingModes);
			
			List<MvArea> kommunerForFylkeskommune = mvAreaRepository.finnKommunerForFylkeskommune(fylkeskommune.sti());
			Contest contest = fylkeskommunerOgContests.get(fylkeskommune);
			behandlKommunerForEttFylke(kommunerOgCountingModes, kommunerSomSkalSkippes, contest, kommunerForFylkeskommune,
					skipAntallLokaltFordeltPaaKrets, skipAntallSentraltFordeltPaaKrets, skipAntallSentraltSamlet, skippedeKommuneStier);
		}
		return kommunerSomSkalSkippes;
	}

	private void behandlKommunerForEttFylke(Map<Kommune, CountingMode> kommunerOgCountingModes, List<KommuneSti> kommunerSomSkalSkippes,
											Contest contest, List<MvArea> kommunerForFylkeskommune,
											int skipAntallLokaltFordeltPaaKrets, int skipAntallSentraltFordeltPaaKrets, int skipAntallSentraltSamlet,
											List<KommuneSti> skippedeKommuneStier) {
		ElectionGroup electionGroup = contest.getElection().getElectionGroup();
		Antall antall = new Antall();
		for (MvArea kommuneMvArea : kommunerForFylkeskommune) {
			behandlKommune(kommunerOgCountingModes, kommunerSomSkalSkippes, skipAntallLokaltFordeltPaaKrets, skipAntallSentraltFordeltPaaKrets,
					skipAntallSentraltSamlet, skippedeKommuneStier, electionGroup, antall, kommuneMvArea);
		}
	}

	private void behandlKommune(Map<Kommune, CountingMode> kommunerOgCountingModes, List<KommuneSti> kommunerSomSkalSkippes,
								int skipAntallLokaltFordeltPaaKrets, int skipAntallSentraltFordeltPaaKrets, int skipAntallSentraltSamlet,
								List<KommuneSti> skippedeKommuneStier, ElectionGroup electionGroup, Antall antall, MvArea kommuneMvArea) {
		Kommune kommune = KommuneMapper.kommune(kommuneMvArea);
		Municipality municipality = kommuneMvArea.getMunicipality();
		ReportCountCategory reportCountCategory =
				reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, electionGroup, VO);
		if (reportCountCategory == null) {
			kommunerSomSkalSkippes.add(kommune.sti());
			return;
		}
		CountingMode countingMode = reportCountCategory.getCountingMode();
		kommunerOgCountingModes.put(kommune, countingMode);

		if (skippedeKommuneStier.contains(kommune.sti())) {
			kommunerSomSkalSkippes.add(kommune.sti());
			return;
		}

		switch (countingMode) {
			case BY_POLLING_DISTRICT:
				if (antall.antallLokaltFordeltPaaKrets < skipAntallLokaltFordeltPaaKrets) {
					kommunerSomSkalSkippes.add(kommune.sti());
					antall.antallLokaltFordeltPaaKrets++;
				}
				break;
			case CENTRAL:
				if (antall.antallSentraltSamlet < skipAntallSentraltSamlet) {
					kommunerSomSkalSkippes.add(kommune.sti());
					antall.antallSentraltSamlet++;
				}
				break;
			case CENTRAL_AND_BY_POLLING_DISTRICT:
				if (antall.antallSentraltFordeltPaaKrets < skipAntallSentraltFordeltPaaKrets) {
					kommunerSomSkalSkippes.add(kommune.sti());
					antall.antallSentraltFordeltPaaKrets++;
				}
				break;
			default:
				throw new IllegalStateException("uvented counting mode: " + countingMode);
		}
	}

	private Map<Fylkeskommune, Contest> fylkeskommunerOgContests(ValghendelseSti valghendelseSti) {
		List<MvElection> mvElections = mvElectionRepository.findByPathAndLevelAndAreaLevel(valghendelseSti.electionPath(), CONTEST, COUNTY);
		return mvElections.stream().collect(toMap(this::fylkeskommune, MvElection::getContest));
	}

	private Fylkeskommune fylkeskommune(MvElection mvElection) {
		return FylkeskommuneMapper.fylkeskommune(mvElection.contestMvArea());
	}

	private Map<Fylkeskommune, List<ContestReport>> fylkesKommunerOgContestReportsForValgstyrer(
			ValghendelseSti valghendelseSti, List<KommuneSti> kommunerSomSkalSkippes, Map<Fylkeskommune, Contest> fylkeskommunerOgContests) {
		List<ContestReport> contestReports = contestReportRepository.finnForValghendelseStiOgStyretype(valghendelseSti, VALGSTYRET);
		return contestReports.stream()
				.filter(contestReport -> inkluderKommune(contestReport, kommunerSomSkalSkippes, fylkeskommunerOgContests))
				.collect(groupingBy(this::fylkeskommune));
	}

	private boolean inkluderKommune(
			ContestReport contestReport, List<KommuneSti> kommunerSomSkalSkippes, Map<Fylkeskommune, Contest> fylkeskommunerOgContests) {
		KommuneSti kommuneSti = contestReport.getReportingUnit().getMvArea().valggeografiSti().tilKommuneSti();
		return !kommunerSomSkalSkippes.contains(kommuneSti)
				&& kommunensFylkeskommuneInkludert(fylkeskommunerOgContests, kommuneSti);
	}

	private boolean kommunensFylkeskommuneInkludert(Map<Fylkeskommune, Contest> fylkeskommunerOgContests, KommuneSti kommuneSti) {
		return fylkeskommunerOgContests.keySet().stream().map(Fylkeskommune::sti).anyMatch(sti -> sti.equals(kommuneSti.fylkeskommuneSti()));
	}

	private Fylkeskommune fylkeskommune(ContestReport contestReport) {
		MvArea kommuneMvArea = contestReport.getReportingUnit().getMvArea();
		KommuneSti kommuneSti = kommuneMvArea.valggeografiSti().tilKommuneSti();
		return new Fylkeskommune(kommuneSti.fylkeskommuneSti(), kommuneMvArea.getCountyName());
	}

	private Map<FylkeskommuneSti, ReportingUnit> fylkeskommuneStierOgReportingUnits(ValghendelseSti valghendelseSti) {
		List<ReportingUnit> reportingUnits = reportingUnitRepository.finnAlleFylkesvalgstyrerForValghendelse(valghendelseSti);
		return reportingUnits.stream().collect(toMap(this::fylkeskommuneSti, Function.identity()));
	}

	private FylkeskommuneSti fylkeskommuneSti(ReportingUnit reportingUnit) {
		return reportingUnit.getMvArea().valggeografiSti().tilFylkeskommuneSti();
	}

	private Map<Fylkeskommune, ContestReport> lagContestReportsForFylkesvalgstyrer(
			Map<Fylkeskommune, List<ContestReport>> fylkesKommunerOgContestReportsForValgstyrer,
			Map<Fylkeskommune, Contest> fylkeskommunerOgContests,
			Map<FylkeskommuneSti, ReportingUnit> fylkeskommuneStierOgReportingUnits) {
		Map<Fylkeskommune, ContestReport> fylkeskommunerOgContestReports = new HashMap<>();

		for (Fylkeskommune fylkeskommune : fylkesKommunerOgContestReportsForValgstyrer.keySet()) {
			Contest contest = fylkeskommunerOgContests.get(fylkeskommune);
			ReportingUnit reportingUnit = fylkeskommuneStierOgReportingUnits.get(fylkeskommune.sti());

			ContestReport contestReport = new ContestReport();
			contestReport.setContest(contest);
			contestReport.setReportingUnit(reportingUnit);

			fylkeskommunerOgContestReports.put(fylkeskommune, contestReport);
		}
		
		return fylkeskommunerOgContestReports;
	}

	private void lagVoteCountsForFylkesvalgstyrer(
			Map<Fylkeskommune, List<ContestReport>> fylkesKommunerOgContestReportsForValgstyrer,
			Map<Fylkeskommune, ContestReport> fylkeskommunerOgContestReportsForFylkesvalgstyret, VoteCountStatus toSettlementStatus) {
		for (Fylkeskommune fylkeskommune : fylkesKommunerOgContestReportsForValgstyrer.keySet()) {
			List<ContestReport> contestReports = fylkesKommunerOgContestReportsForValgstyrer.get(fylkeskommune);
			ContestReport contestReportForFylkesvalgstyret = fylkeskommunerOgContestReportsForFylkesvalgstyret.get(fylkeskommune);
			lagVoteCountsForFylkesvalgstyret(contestReportForFylkesvalgstyret, contestReports, toSettlementStatus);
		}
	}

	private void lagVoteCountsForFylkesvalgstyret(ContestReport contestReportForFylkesvalgstyret, List<ContestReport> contestReports,
												  VoteCountStatus toSettlementStatus) {
		for (ContestReport contestReport : contestReports) {
			List<VoteCount> godkjenteVoteCountsForValgstyret = contestReport.godkjenteEndeligeVoteCounts();
			for (VoteCount godkjentVoteCountForValgstyret : godkjenteVoteCountsForValgstyret) {
				lagVoteCountForFylkesvalgstyret(contestReportForFylkesvalgstyret, godkjentVoteCountForValgstyret, toSettlementStatus);
			}
		}
	}

	private void lagVoteCountForFylkesvalgstyret(ContestReport contestReportForFylkesvalgstyret, VoteCount godkjentVoteCountForValgstyret,
												 VoteCountStatus toSettlementStatus) {
		VoteCount voteCountTilValgoppgjoerForFylkesvalgstyret = new VoteCount();
		voteCountTilValgoppgjoerForFylkesvalgstyret.setPollingDistrict(godkjentVoteCountForValgstyret.getPollingDistrict());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setVoteCountCategory(godkjentVoteCountForValgstyret.getVoteCountCategory());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setCountQualifier(godkjentVoteCountForValgstyret.getCountQualifier());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setApprovedBallots(godkjentVoteCountForValgstyret.getApprovedBallots());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setRejectedBallots(godkjentVoteCountForValgstyret.getRejectedBallots());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setManualCount(godkjentVoteCountForValgstyret.isManualCount());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setModifiedBallotsProcessed(godkjentVoteCountForValgstyret.isModifiedBallotsProcessed());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setRejectedBallotsProcessed(godkjentVoteCountForValgstyret.isRejectedBallotsProcessed());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setVoteCountStatus(toSettlementStatus);
		voteCountTilValgoppgjoerForFylkesvalgstyret.setInfoText(godkjentVoteCountForValgstyret.getInfoText());
		voteCountTilValgoppgjoerForFylkesvalgstyret.setMvArea(godkjentVoteCountForValgstyret.getMvArea());
		klonBallotCounts(voteCountTilValgoppgjoerForFylkesvalgstyret, godkjentVoteCountForValgstyret.getBallotCountSet());
		contestReportForFylkesvalgstyret.add(voteCountTilValgoppgjoerForFylkesvalgstyret);
	}

	private void klonBallotCounts(VoteCount voteCount, Set<BallotCount> ballotCountSet) {
		for (BallotCount ballotCount : ballotCountSet) {
			klonBallotCount(voteCount, ballotCount);
		}
	}

	private void klonBallotCount(VoteCount voteCount, BallotCount ballotCount) {
		BallotCount nyBallotCount;
		if (ballotCount.getBallot() != null) {
			nyBallotCount = voteCount.addNewBallotCount(ballotCount.getBallot(), ballotCount.getUnmodifiedBallots(), ballotCount.getModifiedBallots());
		} else {
			nyBallotCount = voteCount.addNewRejectedBallotCount(ballotCount.getBallotRejection(), ballotCount.getUnmodifiedBallots());
		}
		klonCastBallots(nyBallotCount, ballotCount.getCastBallots());
	}

	private void klonCastBallots(BallotCount nyBallotCount, Set<CastBallot> castBallots) {
		for (CastBallot castBallot : castBallots) {
			klonCastBallot(nyBallotCount, castBallot);
		}
	}

	private void klonCastBallot(BallotCount nyBallotCount, CastBallot gammelCastBallot) {
		CastBallot castBallot = new CastBallot();
		castBallot.setId(gammelCastBallot.getId());
		castBallot.setBinaryData(klonBinaryData(gammelCastBallot.getBinaryData()));
		castBallot.setType(gammelCastBallot.getType());
		klonCandidateVotes(castBallot, gammelCastBallot);
		nyBallotCount.addCastBallot(castBallot);
	}

	private BinaryData klonBinaryData(BinaryData gammelBinaryData) {
		if (gammelBinaryData != null) {
			BinaryData binaryData = new BinaryData(gammelBinaryData);
			return binaryDataRepository.createBinaryData(binaryData);
		}
		return null;
	}

	private void klonCandidateVotes(CastBallot castBallot, CastBallot gammelCastBallot) {
		for (CandidateVote gammelCandidateVote : gammelCastBallot.getCandidateVotes()) {
			klonCandidateVote(castBallot, gammelCandidateVote);
		}
	}

	private void klonCandidateVote(CastBallot castBallot, CandidateVote gammelCandidateVote) {
		Candidate candidate = gammelCandidateVote.getCandidate();
		VoteCategory voteCategory = gammelCandidateVote.getVoteCategory();
		Integer renumberPosition = gammelCandidateVote.getRenumberPosition();
		castBallot.addNewCandidateVote(candidate, voteCategory, renumberPosition);
	}
	
	private static class Antall {
		int antallLokaltFordeltPaaKrets;
		int antallSentraltFordeltPaaKrets;
		int antallSentraltSamlet;
	}
}
