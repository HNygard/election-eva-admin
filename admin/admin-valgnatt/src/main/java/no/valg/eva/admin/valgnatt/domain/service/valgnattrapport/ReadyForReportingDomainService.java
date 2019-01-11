package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport.FORHAAND_FORELOPIG;
import static no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport.VALGTING_FORELOPIG;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;

/**
 * Finner tellekategorier som er klare for rapportering.
 */
public class ReadyForReportingDomainService {

	private ContestReportRepository contestReportRepository;
	private VoteCountService voteCountService;
	private RapporteringsområdeDomainService rapporteringsomraadeDomainService;

	@Inject
	public ReadyForReportingDomainService(ContestReportRepository contestReportRepository, VoteCountService voteCountService,
			RapporteringsområdeDomainService rapporteringsomraadeDomainService) {
		this.contestReportRepository = contestReportRepository;
		this.voteCountService = voteCountService;
		this.rapporteringsomraadeDomainService = rapporteringsomraadeDomainService;
	}

	/**
	 * Sjekker om stemmeskjemaet kan rapporteres
	 */
	public boolean erStemmeskjemaKlarForRapportering(Valgnattrapport valgnattrapport, MvElection mvElectionContest) {

		if (!valgnattrapport.getReportType().isStemmeskjema()) {
			throw new IllegalArgumentException("Tjenesten støtter bare stemmeskjemarapporter.");
		}

		boolean erForelopigTelling = FORHAAND_FORELOPIG.equals(valgnattrapport.countQualifier())
				|| VALGTING_FORELOPIG.equals(valgnattrapport.countQualifier());

		if (valgnattrapport.getReportType().isForhaandsstemmeskjema()) {
			return erForhaandsstemmeskjemaKlarForRapportering(valgnattrapport, mvElectionContest, erForelopigTelling);
		}
		return erValgtingsstemmeskjemaKlarForRapportering(valgnattrapport, mvElectionContest, erForelopigTelling);

	}

	/**
	 * Sjekker om forhåndsstemmer er klare for rapportering. Det ligger delvis til grunn en antakelse om at forhåndsstemmer telles sentralt samlet eller på
	 * tekniske kretser. <br>
	 * For foreløpig stemmeskjema gjelder: Hvis det brukes tekniske kretser, så må hver teknisk krets ha en foreløpig telling som er godkjent. Hvis ikke det er
	 * tekniske kretser holder det at det finnes en foreløpig godkjent FO telling. <br>
	 * For endelig stemmeskjema gjelder: Hvis det brukes tekniske kretser, så må hver teknisk krets ha en endelig telling som er godkjent eller satt klar til
	 * valgoppgjør, og det må finnes en endelig telling for FS på krets 0000.
	 */
	private boolean erForhaandsstemmeskjemaKlarForRapportering(Valgnattrapport valgnattrapport, MvElection mvElectionContest, boolean forelopigTelling) {

		Optional<ContestReport> contestReportOptional = finnContestReportForValgstyret(valgnattrapport, mvElectionContest);
		if (!contestReportOptional.isPresent()) {
			return false;
		}

		ContestReport contestReport = contestReportOptional.get();
		CountingMode countingMode = voteCountService.countingMode(FO, valgnattrapport.getMunicipality(), mvElectionContest);

		if (countingMode.isTechnicalPollingDistrictCount()) {
			return erStemmeskjemaForTekniskeKretserKlarForRapportering(valgnattrapport, mvElectionContest, forelopigTelling, contestReport);
		}

		if (forelopigTelling) {
			return finnesForelopigTellingKlarTilRapporteringFor(valgnattrapport, contestReport, FO);
		}

		return finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, FO)
				&& finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, FS);
	}

	private boolean erStemmeskjemaForTekniskeKretserKlarForRapportering(Valgnattrapport valgnattrapport, MvElection mvElectionContest, boolean forelopigTelling,
			ContestReport contestReport) {
		Set<MvArea> tekniskeKretser = rapporteringsomraadeDomainService
				.kretserForRapporteringAvForhåndsstemmerOgSentralValgting(valgnattrapport.getMunicipality(), mvElectionContest).stream()
				.filter(mvArea -> mvArea.getPollingDistrict().isTechnicalPollingDistrict())
				.collect(Collectors.toSet());

		if (forelopigTelling) {
			return contestReport.finnesRapporterbareTellingerForAlle(tekniskeKretser, PRELIMINARY);
		}
		return contestReport.finnesRapporterbareTellingerForAlle(tekniskeKretser, FINAL)
				&& finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, FS);
	}

	private boolean finnesForelopigTellingKlarTilRapporteringFor(Valgnattrapport valgnattrapport, ContestReport contestReport, CountCategory category) {
		return contestReport.findVoteCountsByAreaQualifierAndCategory(valgnattrapport.getMvArea(), PRELIMINARY, category)
				.stream()
				.anyMatch(VoteCount::isApproved);
	}

	private boolean finnesEndeligTellingKlarTilRapporteringFor(Valgnattrapport valgnattrapport, ContestReport contestReport, CountCategory category) {
		return contestReport.findVoteCountsByAreaQualifierAndCategory(valgnattrapport.getMvArea(), FINAL, category)
				.stream()
				.anyMatch(voteCount -> voteCount.isApproved() || voteCount.isToSettlement());
	}

	/**
	 * Sjekker om valgtingsstemmer er klare for rapportering. Det ligger til grunn en antakelse om at særskilte stemmer (VS) og beredskapsstemmer (VB) telles
	 * sentralt samlet. VO kan telles på ulike måter. VF antas å være sentralt samlet eller sentralt fordelt på krets.<br>
	 * Dersom det er rapportering på krets (altså ikke bare på krets 0000) antas det at VO skal rapporteres på krets. Hvis det er sentralt fordelt på krets, så
	 * er det valgstyret som teller. Dersom det er lokalt fordelt på krets så teller stemmestyret den foreløpige tellingen, mens valgsstyret teller den
	 * endelige. For fremmedstemmer (VF): hvis de ikke er konfigurert som opptellingsmåte så vil counting mode vf være null (og dermed != CENTRAL). For
	 * beredskapsstemmer (VB): hvis det ikke benyttes XiM, vil det ikke være beredskapsstemmer (disse brukes jo dersom nettet går ned i valglokalet..)
	 * 
	 */
	private boolean erValgtingsstemmeskjemaKlarForRapportering(Valgnattrapport valgnattrapport, MvElection mvElectionContest, boolean forelopigTelling) {

		CountingMode countingModeVo = voteCountService.countingMode(VO, valgnattrapport.getMunicipality(), mvElectionContest);
		CountingMode countingModeVf = voteCountService.countingMode(VF, valgnattrapport.getMunicipality(), mvElectionContest);

		Optional<ContestReport> contestReportValgstyretOptional = finnContestReportForValgstyret(valgnattrapport, mvElectionContest);

		boolean erRapportForKrets0000 = valgnattrapport.getMvArea().areaPath().isMunicipalityPollingDistrict();
		if (forelopigTelling) {
			if (erRapportForKrets0000) {
				return erForelopigRapportForKrets0000Klar(valgnattrapport, countingModeVo, contestReportValgstyretOptional);
			}

			Optional<ContestReport> contestReportOptional = contestReportFor(valgnattrapport, mvElectionContest, countingModeVo,
					contestReportValgstyretOptional);
			if (!contestReportOptional.isPresent()) {
				return false;
			}
			ContestReport contestReport = contestReportOptional.get();

			return finnesForelopigTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VO);
		}

		if (!contestReportValgstyretOptional.isPresent()) {
			return false;
		}
		ContestReport contestReport = contestReportValgstyretOptional.get();

		if (erRapportForKrets0000) {
			return endeligSaerskiltOk(valgnattrapport, contestReport)
					&& endeligVsOk(valgnattrapport, contestReport)
					&& endeligFremmedKrets0000Ok(valgnattrapport, countingModeVf, contestReport)
					&& endeligVoOk(valgnattrapport, countingModeVo, contestReport);
		}

		return finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VO)
				&& (countingModeVf != CENTRAL_AND_BY_POLLING_DISTRICT || finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VF));
	}

	private boolean endeligVsOk(Valgnattrapport valgnattrapport, ContestReport contestReport) {
		return finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VS);
	}

	private boolean endeligVoOk(Valgnattrapport valgnattrapport, CountingMode countingModeVo, ContestReport contestReport) {
		return countingModeVo != CENTRAL || finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VO);
	}

	private boolean endeligFremmedKrets0000Ok(Valgnattrapport valgnattrapport, CountingMode countingModeVf, ContestReport contestReport) {
		return countingModeVf != CENTRAL || finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VF);
	}

	private boolean endeligSaerskiltOk(Valgnattrapport valgnattrapport, ContestReport contestReport) {
		return brukerIkkeXim(valgnattrapport) || finnesEndeligTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VB);
	}

	private boolean brukerIkkeXim(Valgnattrapport valgnattrapport) {
		return !valgnattrapport.getMunicipality().isElectronicMarkoffs();
	}

	/**
	 * Når det er lokalt fordelt på krets er det contest report for stemmestyret som skal brukes, ellers er det contest report for valgsstyret som gjelder.
	 */
	private Optional<ContestReport> contestReportFor(Valgnattrapport valgnattrapport, MvElection mvElectionContest, CountingMode countingModeVo,
			Optional<ContestReport> contestReportValgstyretOptional) {
		if (countingModeVo == CountingMode.BY_POLLING_DISTRICT) {
			return contestReportForStemmestyret(valgnattrapport, mvElectionContest);
		} else {
			return contestReportValgstyretOptional;
		}
	}

	private Optional<ContestReport> contestReportForStemmestyret(Valgnattrapport valgnattrapport, MvElection mvElectionContest) {
		return contestReportRepository
				.findByContestAndMvArea(mvElectionContest.getContest(), valgnattrapport.getMvArea())
				.stream()
				.filter(cr -> cr.getReportingUnit().reportingUnitTypeId() == STEMMESTYRET).findAny();
	}

	private boolean erForelopigRapportForKrets0000Klar(Valgnattrapport valgnattrapport, CountingMode countingModeVo,
			Optional<ContestReport> contestReportValgstyretOptional) {
		if (!contestReportValgstyretOptional.isPresent()) {
			return false;
		}
		ContestReport contestReport = contestReportValgstyretOptional.get();
		return countingModeVo.isPollingDistrictCount() || finnesForelopigTellingKlarTilRapporteringFor(valgnattrapport, contestReport, VO);
	}

	private Optional<ContestReport> finnContestReportForValgstyret(Valgnattrapport valgnattrapport, MvElection mvElectionContest) {
		List<ContestReport> contestReports = contestReportRepository.findByContestAndMunicipality(mvElectionContest.getContest(),
				valgnattrapport.getMunicipality());
		return contestReports.stream().filter(cr -> cr.getReportingUnit().reportingUnitTypeId() == VALGSTYRET)
				.findAny();
	}
}
