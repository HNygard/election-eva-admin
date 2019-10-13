package no.valg.eva.admin.opptelling.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class ReduserOpptellingerDomainService {

	private static final Logger LOGGER = Logger.getLogger(ReduserOpptellingerDomainService.class);

	@Inject
	private ReduserGeografiDomainService reduserGeografiDomainService;
	@Inject
	private OpptellingDomainService opptellingDomainService;

	public ReduserOpptellingerDomainService() {

	}
	public ReduserOpptellingerDomainService(ReduserGeografiDomainService reduserGeografiDomainService, OpptellingDomainService opptellingDomainService) {
		this.reduserGeografiDomainService = reduserGeografiDomainService;
		this.opptellingDomainService = opptellingDomainService;
	}

	@Transactional(Transactional.TxType.REQUIRED)
	public void reduserOpptellinger(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		fjernIOmraade("fylke", COUNTY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("kommune", MUNICIPALITY, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("krets", POLLING_DISTRICT, geografiSpesifikasjon, valghendelse);
		fjernIOmraade("stemmested", POLLING_PLACE, geografiSpesifikasjon, valghendelse);
	}
	
	private void fjernIOmraade(String omraadetype, AreaLevelEnum omraadenivaa, GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		reduserGeografiDomainService
			.finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, omraadenivaa)
			.forEach(omraade -> sjekkOgSlettOpptellingerIOmraade(valghendelse, omraadetype, omraade));
	}

	private void sjekkOgSlettOpptellingerIOmraade(ValghendelseSti valghendelse, String omraadetype, MvArea omraade) {
		LOGGER.debug("Sletter opptellinger i " + omraadetype + " " + omraade.getAreaPath());
		ValghierarkiSti valghierarkiSti = ValghierarkiSti.fra(ElectionPath.from(valghendelse.toString()));
		opptellingDomainService.slettOpptellinger(valghierarkiSti, omraade.valggeografiSti());
	}

}
