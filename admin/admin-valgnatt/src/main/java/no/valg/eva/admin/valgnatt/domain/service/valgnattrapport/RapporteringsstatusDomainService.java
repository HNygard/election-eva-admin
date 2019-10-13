package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import static no.evote.constants.ElectionLevelEnum.ELECTION;

/**
 * Forretningslogikk knyttet til hvilke skjema som skal brukes for å finne status på rapporteringene, om
 * fylket kan rapportere og hvor mange skjema fylket rapporterer (1).
 */
@Default
@ApplicationScoped
public class RapporteringsstatusDomainService {

	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private SettlementRepository settlementRepository;

	public RapporteringsstatusDomainService() {

	}

	public RapporteringsstatusDomainService(MvElectionRepository mvElectionRepository, SettlementRepository settlementRepository) {
		this.mvElectionRepository = mvElectionRepository;
		this.settlementRepository = settlementRepository;
	}

    public boolean brukStatusForStemmeskjema(AreaPath reportingAreaPath) {
		return reportingAreaPath.isMunicipalityLevel();
	}

	public boolean kanFylketRapportere(ElectionPath contestPath) {
		contestPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		Contest contest = mvElection.getContest();
		return contest != null && settlementRepository.erValgoppgjørKjørt(contest);
	}
	
	public boolean erValgPaaBydelsnivaa(ElectionPath valgsti, AreaPath omraadesti) {
		return omraadesti.isMunicipalityLevel() && ELECTION.equals(valgsti.getLevel());
	}

	public boolean brukStatusForOppgjorsskjema(ElectionPath electionPath, AreaPath reportingAreaPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		return (reportingAreaPath.isCountyLevel() || mvElection.getActualAreaLevel() == AreaLevelEnum.MUNICIPALITY) && kanFylketRapportere(electionPath);
	}

	public boolean brukStatusforOppgjorOgStemmeskjema(ElectionPath electionPath, AreaPath reportingAreaPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		Contest contest = mvElection.getContest();
		return reportingAreaPath.isMunicipalityLevel() && mvElection.getActualAreaLevel() == AreaLevelEnum.MUNICIPALITY && contest.isSingleArea();
	}

	public long antallRapporterbareOppgjorsskjema() {
		return 1L;
	}
}
