package no.valg.eva.admin.configuration.domain.service;

import static no.valg.eva.admin.util.TidtakingUtil.taTiden;

import java.util.List;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.manntall.papir.Rodefordeler;
import no.valg.eva.admin.configuration.domain.model.manntall.papir.SideOgLinjefordelerForRoder;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

/**
 * Tjenesteklasse ment for å separere ut logikk som har med papirmanntall å gjøre, inkludert side- og linje-beregninger.
 * Dersom papirmanntallet blir fjernet fra valgene, kan denne klassen (og en del tilstøtende kode) slettes
 */
public class PapirmanntallDomainService {
	private static final Logger LOG = Logger.getLogger(PapirmanntallDomainService.class);

	private ElectionEventRepository electionEventRepository;
	private PollingDistrictRepository pollingDistrictRepository;
	private PollingStationRepository pollingStationRepository;
	private PollingPlaceRepository pollingPlaceRepository;
	private VoterRepository voterRepository;

	@Inject
	public PapirmanntallDomainService(ElectionEventRepository electionEventRepository, 
									  PollingDistrictRepository pollingDistrictRepository,
									  PollingPlaceRepository pollingPlaceRepository, 
									  PollingStationRepository pollingStationRepository,
									  VoterRepository voterRepository) {
		this.electionEventRepository = electionEventRepository;
		this.pollingDistrictRepository = pollingDistrictRepository;
		this.pollingStationRepository = pollingStationRepository;
		this.pollingPlaceRepository = pollingPlaceRepository;
		this.voterRepository = voterRepository;
	}

	public void regenererSideOgLinjeForRoder(UserData userData, Long electionEventPk) {
		int antallGenerertSaaLangt = 0;
		List<PollingDistrict> stemmekretser = pollingDistrictRepository.findPollingDistrictsUsingPollingStation(electionEventPk);

		LOG.debug("Number of polling districts: " + stemmekretser.size());
		for (PollingDistrict stemmekrets : stemmekretser) {
			antallGenerertSaaLangt++;
			LOG.debug("Regenererer side og linje for stemmekrets:");
			LOG.debug(" - PK: " + stemmekrets.getPk());
			LOG.debug(" - ID: " + stemmekrets.getId());
			LOG.debug(" - Navn: " + stemmekrets.getName());
			LOG.debug(" - Antall generert så langt: " + antallGenerertSaaLangt);
			regenererSideOgLinjeIStemmekrets(userData, stemmekrets, electionEventPk);
		}
	}

	private void regenererSideOgLinjeIStemmekrets(UserData userData, PollingDistrict stemmekrets, Long electionEventPk) {
		List<Voter> velgereIKrets = taTiden(LOG, "- Henter velgere i krets", true,
			() -> voterRepository.getElectoralRollForPollingDistrict(stemmekrets));
		LOG.debug(" - Antall velgere i kretsen: " + velgereIKrets.size());

		List<PollingStation> roderIKrets = finnRoderIKrets(stemmekrets);
		LOG.debug(" - Antall roder: " + roderIKrets.size());

		if (!velgereIKrets.isEmpty() && !roderIKrets.isEmpty()) {
			fordelVelgereTilRoder(velgereIKrets, roderIKrets);
			oppdaterSideOgLinje(stemmekrets, electionEventPk, velgereIKrets);

			taTiden(LOG, "- Lagre velgere i krets", true, 
				() -> voterRepository.updateVoters(userData, velgereIKrets));
		}
	}

	private List<PollingStation> finnRoderIKrets(PollingDistrict pollingDistrict) {
		PollingPlace stemmested = pollingPlaceRepository.findPollingPlaceByElectionDayVoting(pollingDistrict.getPk());
		return pollingStationRepository.findByPollingPlace(stemmested.getPk());
	}

	private void fordelVelgereTilRoder(List<Voter> velgereIKrets, List<PollingStation> roderIKrets) {
		List<Pair<Voter, PollingStation>> velgereMedRode = new Rodefordeler(velgereIKrets).distribuerVelgereTil(roderIKrets);
		for (Pair<Voter, PollingStation> velgerMedRode : velgereMedRode) {
			velgerMedRode.getLeft().setPollingStation(velgerMedRode.getRight());
		}
	}

	private void oppdaterSideOgLinje(PollingDistrict stemmekrets, Long electionEventPk, List<Voter> velgereIKrets) {
		int maxAntallVelgerePerSide = electionEventRepository.findByPk(electionEventPk).getElectoralRollLinesPerPage();
		SideOgLinjefordelerForRoder assigner = new SideOgLinjefordelerForRoder(velgereIKrets, maxAntallVelgerePerSide);
		assigner.tilordneSideOgLinjeTilVelgere();
		voterRepository.updateLastLineLastPageNumber(stemmekrets, assigner.getNesteLinje(), assigner.getNesteSide());
	}

}
