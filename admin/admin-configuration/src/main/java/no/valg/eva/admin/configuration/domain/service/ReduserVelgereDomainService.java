package no.valg.eva.admin.configuration.domain.service;

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
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class ReduserVelgereDomainService {

	private static final Logger LOGGER = Logger.getLogger(ReduserVelgereDomainService.class);

	@Inject
	private VoterRepository voterRepository;
	@Inject
	private ReduserGeografiDomainService reduserGeografiDomainService;

	public ReduserVelgereDomainService() {

	}

	public ReduserVelgereDomainService(VoterRepository voterRepository, ReduserGeografiDomainService reduserGeografiDomainService) {
		this.voterRepository = voterRepository;
		this.reduserGeografiDomainService = reduserGeografiDomainService;
	}

	@Transactional(Transactional.TxType.REQUIRED)
	public void reduserVelgere(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		fjernIOmraade("fylke", COUNTY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("kommune", MUNICIPALITY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("krets", POLLING_DISTRICT, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("stemmested", POLLING_PLACE, geografiSpesifikasjon, valghendelse);
	}

	private void fjernIOmraade(String omraadetype, AreaLevelEnum omraadenivaa, GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		reduserGeografiDomainService
			.finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, omraadenivaa)
			.forEach(omraade -> sjekkOgSlettVelgereIOmraade(valghendelse, omraadetype, omraade));
	}

	private void sjekkOgSlettVelgereIOmraade(ValghendelseSti valghendelse, String omraadetype, MvArea omraade) {
		LOGGER.debug("Sletter velgere i " + omraadetype + " " + omraade.getAreaPath());
		voterRepository.deleteVoters(valghendelse.toString(), omraade.getAreaPath());
	}

	@Transactional(Transactional.TxType.REQUIRED)
	public void flyttVelgere(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		flyttIOmraade("fylke", COUNTY, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("kommune", MUNICIPALITY, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("krets", POLLING_DISTRICT, geografiSpesifikasjon, valghendelse);
		flyttIOmraade("stemmested", POLLING_PLACE, geografiSpesifikasjon, valghendelse);
	}

	private void flyttIOmraade(String omraadetype, AreaLevelEnum omraadenivaa, GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		reduserGeografiDomainService
			.finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, omraadenivaa)
			.forEach(omraadeSomSkalSlettes -> flyttVelgereIOmraade(geografiSpesifikasjon, omraadetype, omraadeSomSkalSlettes));
	}

	private void flyttVelgereIOmraade(GeografiSpesifikasjon geografiSpesifikasjon, String omraadetype, MvArea omraadeSomSkalSlettes) {
		MvArea nyKrets =  reduserGeografiDomainService.finnBeholdtOmraade(geografiSpesifikasjon, omraadeSomSkalSlettes, POLLING_DISTRICT);
		LOGGER.debug("Flytter velgere i " + omraadetype + " " + omraadeSomSkalSlettes.getAreaPath() + " til krets " + nyKrets.getAreaPath());
		voterRepository.flyttVelgere(omraadeSomSkalSlettes, nyKrets);
	}
}
