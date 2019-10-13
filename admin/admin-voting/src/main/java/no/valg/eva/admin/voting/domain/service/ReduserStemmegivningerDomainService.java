package no.valg.eva.admin.voting.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.voting.repository.VotingRepository;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class ReduserStemmegivningerDomainService {

	private static final Logger LOGGER = Logger.getLogger(ReduserStemmegivningerDomainService.class);

	@Inject
	private ReduserGeografiDomainService reduserGeografiDomainService;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;

	public ReduserStemmegivningerDomainService() {

	}

	public ReduserStemmegivningerDomainService(ReduserGeografiDomainService reduserGeografiDomainService, VotingRepository votingRepository,
											   MvElectionRepository mvElectionRepository) {
		this.reduserGeografiDomainService = reduserGeografiDomainService;
		this.votingRepository = votingRepository;
		this.mvElectionRepository = mvElectionRepository;
	}

	@Transactional(Transactional.TxType.REQUIRED)
	public void reduserStemmegivninger(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		fjernIOmraade("fylke", COUNTY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("kommune", MUNICIPALITY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("krets", POLLING_DISTRICT, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("stemmested", POLLING_PLACE, geografiSpesifikasjon, valghendelse);
	}

	private void fjernIOmraade(String omraadetype, AreaLevelEnum omraadenivaa, GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		reduserGeografiDomainService
			.finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, omraadenivaa)
			.forEach(omraade -> sjekkOgSlettStemmegivningerIOmraade(valghendelse, omraadetype, omraade));
	}

	private void sjekkOgSlettStemmegivningerIOmraade(ValghendelseSti valghendelse, String omraadetype, MvArea omraade) {
		LOGGER.debug("Sletter stemmegivninger avgitt i " + omraadetype + " " + omraade.getAreaPath());
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valghendelse.tilValghierarkiSti());
		votingRepository.deleteVotings(mvElection, omraade, 0);

		LOGGER.debug("Sletter resterende stemmegivninger avgitt av velgere i " + omraadetype + " " + omraade.getAreaPath());
		votingRepository.slettStemmegivningerFraVelgereTilhoerendeI(ValggeografiSti.fra(omraade.areaPath()));
	}

	@Transactional(Transactional.TxType.REQUIRED)
	public void flyttStemmegivninger(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		flyttIOmraade("fylke", COUNTY, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("kommune", MUNICIPALITY, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("krets", POLLING_DISTRICT, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("stemmested", POLLING_PLACE, geografiSpesifikasjon, valghendelse);
	}

	private void flyttIOmraade(String omraadetype, AreaLevelEnum omraadenivaa, GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		reduserGeografiDomainService
			.finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, omraadenivaa)
			.forEach(omraadeSomSkalSlettes -> flyttStemmegivningerIOmraade(omraadetype, omraadeSomSkalSlettes, geografiSpesifikasjon));
	}
	
	private void flyttStemmegivningerIOmraade(String omraadetype, MvArea omraadeSomSkalSlettes, GeografiSpesifikasjon geografiSpesifikasjon) {
		flyttStemmegivningerForVelgereI(omraadetype, omraadeSomSkalSlettes, geografiSpesifikasjon);
		flyttStemmegivningerAvgittI(omraadetype, omraadeSomSkalSlettes, geografiSpesifikasjon);
	}

	private void flyttStemmegivningerForVelgereI(String omraadetype, MvArea omraadeSomSkalSlettes, GeografiSpesifikasjon geografiSpesifikasjon) {
		MvArea nyStemmekrets = reduserGeografiDomainService.finnBeholdtOmraade(geografiSpesifikasjon, omraadeSomSkalSlettes, POLLING_DISTRICT);
		LOGGER.debug("Flytter stemmegivninger gitt av velgere tilhørende i " + omraadetype + " " + omraadeSomSkalSlettes.getAreaPath()
			+ " til stemmekrets " + nyStemmekrets.getAreaPath());
		votingRepository.flyttStemmegivningerForVelgereI(omraadeSomSkalSlettes, nyStemmekrets);
	}

	private void flyttStemmegivningerAvgittI(String omraadetype, MvArea omraadeSomSkalSlettes, GeografiSpesifikasjon geografiSpesifikasjon) {
		MvArea nyttStemmested = reduserGeografiDomainService.finnBeholdtOmraade(geografiSpesifikasjon, omraadeSomSkalSlettes, POLLING_PLACE);
		LOGGER.debug("Flytter stemmegivninger avgitt i " + omraadetype + " " + omraadeSomSkalSlettes.getAreaPath() + " til stemmested " + nyttStemmested.getAreaPath());
		votingRepository.flyttStemmegivningerAvgittI(omraadeSomSkalSlettes, nyttStemmested);
	}
}
