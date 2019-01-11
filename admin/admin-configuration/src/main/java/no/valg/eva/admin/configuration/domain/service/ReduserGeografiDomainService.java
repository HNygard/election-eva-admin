package no.valg.eva.admin.configuration.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.apache.log4j.Logger;

public class ReduserGeografiDomainService {

	private static final Logger LOGGER = Logger.getLogger(ReduserGeografiDomainService.class);

	private MvAreaRepository mvAreaRepository;
	private CountyRepository countyRepository;
	private MunicipalityRepository municipalityRepository;
	private PollingDistrictRepository pollingDistrictRepository;
	private PollingPlaceRepository pollingPlaceRepository;
	private UserData userData;

	@Inject
	public ReduserGeografiDomainService(MvAreaRepository mvAreaRepository, CountyRepository countyRepository, MunicipalityRepository municipalityRepository,
										PollingDistrictRepository pollingDistrictRepository, PollingPlaceRepository pollingPlaceRepository) {
		this.mvAreaRepository = mvAreaRepository;
		this.countyRepository = countyRepository;
		this.municipalityRepository = municipalityRepository;
		this.pollingDistrictRepository = pollingDistrictRepository;
		this.pollingPlaceRepository = pollingPlaceRepository;
		this.userData = createFakeUserData();
	}

	// Det er laget en JIRA-issue, EVA-2125, som vil bidra til å fjerne dette hacket
	@SuppressWarnings("deprecation")
	private UserData createFakeUserData() {
		UserData brukerkontekst = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(new Operator());
		operatorRole.getOperator().setElectionEvent(new ElectionEvent());
		brukerkontekst.setOperatorRole(operatorRole);
		brukerkontekst.getOperatorRole().setRole(new Role());
		brukerkontekst.getOperatorRole().getRole().setUserSupport(false);
		return brukerkontekst;
	}
	
	@Transactional(Transactional.TxType.REQUIRED)
	public void reduserGeografi(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		fjernFylker(geografiSpesifikasjon, valghendelse);
		fjernKommuner(geografiSpesifikasjon, valghendelse);	
		fjernKretser(geografiSpesifikasjon, valghendelse);
		fjernStemmesteder(geografiSpesifikasjon, valghendelse);
	}

	private void fjernFylker(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, AreaLevelEnum.COUNTY)
			.forEach(this::slettFylke);
	}
	
	public List<MvArea> finnOmraaderSomSkalSlettes(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse, AreaLevelEnum omraadenivaa) {
		List<MvArea> omraader = mvAreaRepository.finnFor(valghendelse, omraadenivaa);
		return omraader.stream()
			.filter(omraade -> omraadeSkalSlettes(omraade, geografiSpesifikasjon, omraadenivaa))
			.collect(Collectors.toList());
	}

	private boolean omraadeSkalSlettes(MvArea omraade, GeografiSpesifikasjon geografiSpesifikasjon, AreaLevelEnum omraadenivaa) {
		switch (omraadenivaa) {
			case COUNTY:
				return !geografiSpesifikasjon.referererFylke(omraade.getCountyId());
			case MUNICIPALITY:
				return !geografiSpesifikasjon.referererKommune(omraade.getMunicipalityId());
			case POLLING_DISTRICT:
				return !geografiSpesifikasjon.referererKrets(omraade.getMunicipalityId(), omraade.getPollingDistrictId());
			case POLLING_PLACE:
				return !geografiSpesifikasjon.referererStemmested(omraade.getMunicipalityId(), omraade.getPollingDistrictId(), omraade.getPollingPlaceId());
			default:
				throw new IllegalArgumentException("Geografireduksjon ikke støttet på omraadenivå " + omraadenivaa); 
		}
	}

	private void slettFylke(MvArea fylke) {
		LOGGER.debug("Sletter fylke " + fylke.getAreaPath());
		countyRepository.delete(userData, fylke.getCounty().getPk());
	}

	private void fjernKommuner(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, AreaLevelEnum.MUNICIPALITY)
			.forEach(this::slettKommune);
	}

	private void slettKommune(MvArea kommune) {
		LOGGER.debug("Sletter kommune " + kommune.getAreaPath());
		municipalityRepository.delete(userData, kommune.getMunicipality().getPk());
	}

	private void fjernKretser(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, AreaLevelEnum.POLLING_DISTRICT)
			.forEach(this::slettKrets);
	}

	private void slettKrets(MvArea krets) {
		LOGGER.debug("Sletter krets " + krets.getAreaPath());
		pollingDistrictRepository.delete(userData, krets.getPollingDistrict().getPk());
	}

	private void fjernStemmesteder(GeografiSpesifikasjon geografiSpesifikasjon, ValghendelseSti valghendelse) {
		finnOmraaderSomSkalSlettes(geografiSpesifikasjon, valghendelse, AreaLevelEnum.POLLING_PLACE)
			.forEach(this::slettStemmested);
	}

	private void slettStemmested(MvArea stemmested) {
		LOGGER.debug("Sletter stemmested " + stemmested.getAreaPath());
		pollingPlaceRepository.delete(userData, stemmested.getPollingPlace().getPk());
	}
	
	public MvArea finnBeholdtOmraade(GeografiSpesifikasjon geografiSpesifikasjon, MvArea omraadeSomSkalSlettes, AreaLevelEnum omraadenivaaForResultat) {

		for (AreaLevelEnum omraadenivaa : finnOmraadenivaaerForFlytting(omraadeSomSkalSlettes, omraadenivaaForResultat)) {
			ValggeografiSti valggeografiSti = ValggeografiSti.fra(omraadeSomSkalSlettes.areaPath().toAreaLevelPath(omraadenivaa));
			MvArea forsteOmraade = mvAreaRepository.finnFor(valggeografiSti, omraadenivaaForResultat).stream()
				.filter(geografiSpesifikasjon::referererOmraade)
				.findFirst()
				.orElse(null);
			if (forsteOmraade != null) {
				return forsteOmraade;
			}
		}
		
		throw new IllegalStateException("Finner intet beholdt område å flytte til for omraade " + omraadeSomSkalSlettes.getAreaPath()
			+ ". Dette tyder på at for mye geografi er slettet.");
	}

	List<AreaLevelEnum> finnOmraadenivaaerForFlytting(MvArea omraadeSomSkalSlettes, AreaLevelEnum omraadenivaaForResultat) {
		AreaLevelEnum omraadenivaaSomDetSlettesFra = omraadeSomSkalSlettes.getActualAreaLevel();
		return Stream.of(POLLING_STATION, POLLING_PLACE, POLLING_DISTRICT, MUNICIPALITY, COUNTY, COUNTRY)
			.filter(omraadenivaaSomDetSlettesFra::lowerThan)
			.filter(omraadenivaaForResultat::lowerThan)
			.collect(Collectors.toList());
	}

}
